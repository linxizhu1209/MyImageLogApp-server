import os
import json
import urllib.request
import urllib.error


class GeminiClient:
    """
    최소 의존성(추가 패키지 없이)으로 Gemini REST API 호출.
    환경변수:
      - GEMINI_API_KEY: 필수
      - GEMINI_MODEL: 선택 (기본: gemini-2.0-flash)
    """

    def __init__(self, api_key: str | None = None, model: str | None = None):
        self.api_key = api_key or os.getenv("GEMINI_API_KEY", "")
        self.model = model or os.getenv("GEMINI_MODEL", "gemini-2.0-flash")
        if not self.api_key:
            raise RuntimeError("GEMINI_API_KEY is not set")

    def generate_text(self, prompt: str, max_output_tokens: int = 512) -> str:
        url = (
            f"https://generativelanguage.googleapis.com/v1beta/models/"
            f"{self.model}:generateContent?key={self.api_key}"
        )

        payload = {
            "contents": [{"role": "user", "parts": [{"text": prompt}]}],
            "generationConfig": {
                "maxOutputTokens": max_output_tokens,
                "temperature": 0.4,
            },
        }

        req = urllib.request.Request(
            url,
            data=json.dumps(payload).encode("utf-8"),
            headers={"Content-Type": "application/json"},
            method="POST",
        )

        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                data = json.loads(resp.read().decode("utf-8"))
        except urllib.error.HTTPError as e:
            body = e.read().decode("utf-8", errors="replace")
            raise RuntimeError(f"Gemini HTTPError {e.code}: {body}") from e
        except Exception as e:
            raise RuntimeError(f"Gemini request failed: {e}") from e

        # v1beta 응답: candidates[0].content.parts[*].text
        candidates = data.get("candidates") or []
        if not candidates:
            return ""
        parts = (candidates[0].get("content") or {}).get("parts") or []
        texts = [p.get("text", "") for p in parts if isinstance(p, dict)]
        return "".join(texts).strip()

