import os
import json
import urllib.request
import urllib.error


class GeminiClient:
    """
    최소 의존성(추가 패키지 없이)으로 Gemini REST API 호출.
    환경변수:
      - GEMINI_API_KEY: 필수
      - GEMINI_MODEL: 선택 (기본: gemini-1.5-flash)
      - GEMINI_MODEL_FALLBACK: 선택 (기본: gemini-1.5-flash-8b)
    """

    def __init__(self, api_key: str | None = None, model: str | None = None):
        self.api_key = api_key or os.getenv("GEMINI_API_KEY", "")
        self.model = model or os.getenv("GEMINI_MODEL", "gemini-1.5-flash")
        self.fallback_model = os.getenv("GEMINI_MODEL_FALLBACK", "gemini-1.5-flash-8b")
        if not self.api_key:
            raise RuntimeError("GEMINI_API_KEY is not set")

    def generate_text(self, prompt: str, max_output_tokens: int = 512) -> str:
        payload = {
            "contents": [{"role": "user", "parts": [{"text": prompt}]}],
            "generationConfig": {
                "maxOutputTokens": max_output_tokens,
                "temperature": 0.4,
            },
        }

        last_error: Exception | None = None
        for m in [self.model, self.fallback_model]:
            url = (
                f"https://generativelanguage.googleapis.com/v1beta/models/"
                f"{m}:generateContent?key={self.api_key}"
            )
            req = urllib.request.Request(
                url,
                data=json.dumps(payload).encode("utf-8"),
                headers={"Content-Type": "application/json"},
                method="POST",
            )

            try:
                with urllib.request.urlopen(req, timeout=30) as resp:
                    data = json.loads(resp.read().decode("utf-8"))
                last_error = None
                break
            except urllib.error.HTTPError as e:
                body = e.read().decode("utf-8", errors="replace")
                # 모델 미지원/권한 오류가 나면 fallback 모델로 한 번 더 시도
                last_error = RuntimeError(f"Gemini HTTPError {e.code} (model={m}): {body}")
                continue
            except Exception as e:
                last_error = RuntimeError(f"Gemini request failed (model={m}): {e}")
                continue

        if last_error is not None:
            raise last_error

        # v1beta 응답: candidates[0].content.parts[*].text
        candidates = data.get("candidates") or []
        if not candidates:
            return ""
        parts = (candidates[0].get("content") or {}).get("parts") or []
        texts = [p.get("text", "") for p in parts if isinstance(p, dict)]
        return "".join(texts).strip()

