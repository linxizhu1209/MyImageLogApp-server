import streamlit as st
import requests
from datetime import datetime

# ======== 설정 ============
API_BASE = "http://localhost:8080" # spring boot 서버 주소

def get_user_id_from_query():
    """URL 쿼리 파라미터에서 userID 추출"""
    params = st.query_params
    return params.get("userId")

def fetch_week_data(user_id: int) -> dict:
    """서버(spring) API에서 이번 주 이미지/기록 조회 """
    r = requests.get(
        f"{API_BASE}/api/images/week",
        params={"userId": user_id},
        timeout=10
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

# ======== LLM 공통 호출 (모델 변경 시 여기만 수정) ========
LLM_MODEL = "llama3.2"

def call_llm(prompt: str, model: str = LLM_MODEL) -> str:
    """LLM 호출 (Ollama). 이후 OpenAI 등으로 교체 시 이 함수만 수정."""
    try:
        import ollama
        r = ollama.chat(
            model=model,
            messages=[{"role": "user", "content": prompt}]
        )
        return r["message"]["content"]
    except Exception as e:
        return f"분석 실패: {e}\n\n(Ollama 실행 여부, 모델 설치 확인: ollama pull {model})"

def analyze_with_ollama(prompt: str) -> str:
    """하위 호환용. call_llm 사용 권장."""
    return call_llm(prompt)


def main():
    st.set_page_config(page_title="이번 주 요약", layout="wide")
    st.title("📅 이번 주 요약")

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

    # 이번 주 데이터 조회
    try:
        with st.spinner("이번 주 데이터 불러오는 중..."):
            data = fetch_week_data(user_id)
    except requests.RequestException as e:
        st.error(f"서버 연결 실패: {e}\n\nSpring Boot 서버가 실행중인지 확인해주세요.")
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