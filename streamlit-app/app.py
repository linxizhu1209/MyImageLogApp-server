import streamlit as st
import requests
import base64

# ======== 설정 ============
import os

API_BASE = os.getenv("API_BASE", "http://localhost:8080")  # spring boot 서버 주소
MAX_IMAGES_FOR_ANALYSIS = 5  # 메모리 절약: 최대 5장 (늘리면 OOM 위험) -- 메모리 오류 방지 위함

def get_token_from_query() -> str | None:
    params = st.query_params
    return params.get("token")

def verify_jwt_and_get_user_id() -> int | None:
    """
    Streamlit 공개 페이지 접근 시 JWT를 Spring에 검증 요청.
    성공 시 userId 반환, 실패 시 None.
    """
    token = get_token_from_query()
    if not token:
        return None
    try:
        r = requests.get(
            f"{API_BASE}/api/me",
            headers={"Authorization": f"Bearer {token}"},
            timeout=10,
        )
        if r.status_code != 200:
            return None
        data = r.json()
        if data.get("authenticated") is True and data.get("userId"):
            return int(data["userId"])
        return None
    except Exception:
        return None

def get_user_id_from_query():
    """URL 쿼리 파라미터에서 userID 추출"""
    params = st.query_params
    return params.get("userId")

def fetch_week_data(user_id: int, token: str) -> dict:
    """서버(spring) API에서 이번 주 이미지/기록 조회 """
    r = requests.get(
        f"{API_BASE}/api/images/week",
        params={"userId": user_id},
        headers={"Authorization": f"Bearer {token}"},
        timeout=10,
    )
    r.raise_for_status()
    return r.json()

def build_week_text(data: dict) -> str:
    """LLM에 넘길 이번 주 기록 텍스트 구성"""
    lines = []
    for day in data.get("days", []):
        date_str = day.get("date", "")
        day_name = day.get("day", "")
        for img in day.get("images", []):
            title = img.get("title") or "(제목 없음)"
            content = img.get("content") or ""
            lines.append(f"[{date_str} {day_name}]\n제목: {title}\n내용: {content}")
    return "\n\n---\n\n".join(lines) if lines else ""


def collect_week_image_urls(data: dict, max_count: int = MAX_IMAGES_FOR_ANALYSIS) -> list[str]:
    """이번 주 이미지 URL 목록 수집 (날짜순, max_count까지)"""
    urls = []
    for day in sorted(data.get("days", []), key=lambda d: d.get("date", "")):
        for img in day.get("images", []):
            url = img.get("url")
            if url:
                urls.append(url)
            if len(urls) >= max_count:
                return urls
    return urls


def download_image_as_base64(url: str) -> str | None:
    """이미지 URL을 다운로드하여 base64 문자열로 반환"""
    try:
        r = requests.get(url, timeout=15)
        r.raise_for_status()
        return base64.b64encode(r.content).decode("utf-8")
    except Exception:
        return None


def call_llm_vision(prompt: str, images_base64: list[str]) -> str:
    """이미지를 포함한 비전 LLM 호출 (Gemini). 이번 주 이미지 집계용."""
    try:
        from llm_gemini import GeminiClient
        client = GeminiClient()
        return client.generate_vision(prompt, images_base64, max_output_tokens=700)
    except Exception as e:
        return f"이미지 분석 실패: {e}"


# ======== LLM 공통 호출 (Gemini 전용) ========
def call_llm(prompt: str) -> str:
    """LLM 호출 (Gemini 전용)."""
    try:
        from llm_gemini import GeminiClient
        client = GeminiClient()
        return client.generate_text(prompt, max_output_tokens=700)
    except Exception as e:
        return f"분석 실패: {e}"

def main():
    st.set_page_config(page_title="이번 주 요약", layout="wide")
    st.title("📅 이번 주 요약")

    user_id_from_token = verify_jwt_and_get_user_id()
    if user_id_from_token is None:
        st.error("로그인이 필요합니다. 앱에서 로그인 후 Streamlit을 열어주세요. (URL에 token 필요)")
        st.stop()

    # userId 쿼리 파라미터 확인
    user_id_raw = get_user_id_from_query()
    if user_id_raw is None:
        st.warning("URL에 userId가 없습니다. 로그인한 앱의 화면에서 '이번주 요약하기' 버튼을 눌러 접속해주세요!")
        return

    try:
        user_id = int(user_id_raw)
    except ValueError:
        st.error("userId의 형식이 잘못되었습니다!")
        return

    if user_id != user_id_from_token:
        st.error("토큰의 사용자와 요청한 userId가 일치하지 않습니다.")
        st.stop()

    token = get_token_from_query()
    if not token:
        st.error("로그인이 필요합니다. 앱에서 로그인 후 Streamlit을 열어주세요. (URL에 token 필요)")
        st.stop()

    # 이번 주 데이터 조회
    try:
        with st.spinner("이번 주 데이터 불러오는 중..."):
            data = fetch_week_data(user_id, token)
    except requests.RequestException as e:
        st.error(f"서버 연결 실패: {e}\n\n(토큰 누락/만료 또는 서버 오류일 수 있어요)")
        return

    week_start = data.get("weekStart", "")
    week_end = data.get("weekEnd", "")
    st.caption(f"📆 기간: {week_start} - {week_end}")

    week_text = build_week_text(data)
    if not week_text:
        st.info("이번 주 기록이 없습니다.")
        return


    # ============== 통계 =========
    st.subheader("📊 통계")
    total = sum(len(d.get("images", [])) for d in data.get("days", []))
    col1, col2, col3 = st.columns(3)
    with col1:
        st.metric("이번 주 기록 수", total)
    with col2:
        day_count = len(data.get("days", []))
        st.metric("기록이 있는 날", f"{day_count}일")
    with col3:
        avg = total / day_count if day_count > 0 else 0
        st.metric("일 평균 기록", f"{avg:.1f}건")

    st.write("**날짜별 기록 수**")
    for day in data.get("days", []):
        date_str = day.get("date", "")
        day_name = day.get("day", "")
        count = len(day.get("images", []))
        st.write(f"- {date_str} ({day_name}): {count}건")

    # ============= 감정/기분 트렌드 ==============
    st.subheader("📈 감정/기분 트렌드")
    st.caption("이번 주 기록을 바탕으로 긍정·중립·부정 비율과 한 줄 트렌드 해석을 합니다.")
    emotion_prompt = f"""다음은 한 사용자의 이번 주(월~일) 이미지 로그 제목과 내용입니다.
이 내용만 보고 감정/기분을 분석해 주세요.

**반드시 아래 형식으로만 답하세요 (한국어):**

1) **비율**: 이번 주 전체 감정을 긍정 / 중립 / 부정 비율로 추정해 주세요. (예: 긍정 50%, 중립 30%, 부정 20%)

2) **한 줄 트렌드**: 이번 주 기분이 주 중에 어떻게 흐른지 한 문장으로 요약해 주세요. (예: "주 초반에 다소 지쳤다가 주 말에 안정적인 편이었다.")

---
{week_text}
---"""

    if st.button("📈 감정 트렌드 분석", key="btn_emotion"):
        with st.spinner("감정 트렌드 분석 중..."):
            emotion_result = call_llm(emotion_prompt)
        st.success("감정 트렌드 분석 결과")
        st.markdown(emotion_result)

    # ============= 이미지 기반 이번 주 분위기 (집계) ==============
    st.subheader("📷 이미지 기반 이번 주 분위기")
    st.caption("이번 주 업로드된 사진(표정·분위기)을 보고 전체 집계 분석을 합니다. (비전 모델: llava, 메모리 절약용)")

    image_urls = collect_week_image_urls(data)
    if image_urls:
        st.write(f"분석 대상: {len(image_urls)}장 (최대 {MAX_IMAGES_FOR_ANALYSIS}장)")
        if st.button("📷 이미지 분위기 집계 분석", key="btn_image"):
            with st.spinner("이미지 다운로드 및 분석 중... (비전 모델이라 다소 걸립니다)"):
                images_b64 = []
                for url in image_urls:
                    b64 = download_image_as_base64(url)
                    if b64:
                        images_b64.append(b64)
                if not images_b64:
                    st.error("이미지를 불러올 수 없습니다. Spring 서버가 실행 중인지, 이미지 URL이 접근 가능한지 확인하세요.")
                else:
                    vision_prompt = """아래 이미지들은 한 사용자가 이번 주(월~일)에 찍은 사진들입니다.
이미지들 전체를 보고 **이번 주 집계** 형태로 분석해 주세요.

**반드시 아래 형식으로만 답하세요 (한국어):**

1) **표정/분위기 비율**: 전체 사진을 보고 추정한 비율을 작성해 주세요. (예: 밝은 표정/미소 40%, 무표정/중립 35%, 피곤/어두운 분위기 25%)

2) **이번 주 분위기 트렌드 한 줄**: 이번 주 사진들이 전체적으로 어떤 느낌인지 한 문장으로 요약해 주세요."""
                    result = call_llm_vision(vision_prompt, images_b64)
                    st.success("이미지 기반 분위기 집계 결과")
                    st.markdown(result)
    else:
        st.info("이번 주 업로드된 이미지가 없어 분석할 수 없습니다.")

    # ============= LLM 요약 & 감정 분석 ==============
    st.subheader("🤖 LLM 요약 & 감정 분석")

    prompt = f"""다음은 한 사용자의 이번주(월요일 ~ 일요일) 이미지 로그의 제목과 내용입니다. 
이걸 바탕으로: 
1) 2~3문장으로 한 주 요약을 해 주세요.
2) 전체적인 감정/기분을 간단히 분석해 주세요.

---
{week_text}
---

위 내용을 바탕으로 요약과 감정 분석을 한국어로 작성해 주세요."""

    if st.button("🔍 분석하기"):
        with st.spinner("LLM 분석 중..."):
            result = call_llm(prompt)
        st.write(result)

    # 원본 데이터 접기
    with st.expander("📋 원본 기록 보기"):
        st.text(week_text)


if __name__ == "__main__":
    main()