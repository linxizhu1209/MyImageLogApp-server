import os
import json
import urllib.request
import urllib.error


class GeminiClient:
    """
    최소 의존성(추가 패키지 없이)으로 Gemini REST API 호출.
    환경변수:
      - GEMINI_API_KEY: 필수
      - GEMINI_MODEL: 선택 (비워두면 계정에서 사용 가능한 모델을 자동 선택)
    """

    def __init__(self, api_key: str | None = None, model: str | None = None):
        self.api_key = api_key or os.getenv("GEMINI_API_KEY", "")
        self.model = (model if model is not None else os.getenv("GEMINI_MODEL", "")).strip()
        if not self.api_key:
            raise RuntimeError("GEMINI_API_KEY is not set")
        self._cached_models: list[str] | None = None

    def _list_models(self) -> list[str]:
        if self._cached_models is not None:
            return self._cached_models

        url = f"https://generativelanguage.googleapis.com/v1beta/models?key={self.api_key}"
        req = urllib.request.Request(url, method="GET")
        with urllib.request.urlopen(req, timeout=30) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        models = data.get("models") or []
        out: list[str] = []
        for m in models:
            if not isinstance(m, dict):
                continue
            # baseModelId가 generation 호출에 넣는 값 (예: gemini-2.0-flash)
            base = (m.get("baseModelId") or "").strip()
            methods = m.get("supportedGenerationMethods") or []
            if base and any(x == "generateContent" for x in methods):
                out.append(base)
        self._cached_models = sorted(set(out))
        return self._cached_models

    def _pick_default_model(self, models: list[str]) -> str:
        # 비용/속도 우선: flash-lite/flash 계열 선호. (계정에 없는 건 자동으로 제외됨)
        preferred = [
            "gemini-2.5-flash-lite",
            "gemini-2.5-flash",
            "gemini-2.0-flash-lite",
            "gemini-2.0-flash",
            "gemini-3.1-flash-lite-preview",
            "gemini-3-flash-preview",
        ]
        for p in preferred:
            if p in models:
                return p
        # 그래도 없으면 flash가 포함된 아무 모델
        for m in models:
            if "flash" in m:
                return m
        # 최후: 목록 첫 번째
        if models:
            return models[0]
        raise RuntimeError("No Gemini models available for generateContent on this API key.")

    def _resolve_model(self) -> str:
        if self.model:
            return self.model
        models = self._list_models()
        return self._pick_default_model(models)

    def _generate(self, contents: list[dict], max_output_tokens: int) -> str:
        payload = {
            "contents": contents,
            "generationConfig": {
                "maxOutputTokens": max_output_tokens,
                "temperature": 0.4,
            },
        }
        # 1) env로 지정된 모델이 있으면 그걸 먼저 시도
        # 2) 실패(특히 404/403)하면 models.list 기반으로 다음 후보들을 자동으로 재시도
        tried: list[str] = []
        candidates: list[str] = []
        if self.model:
            candidates.append(self.model)

        try:
            models = self._list_models()
        except Exception:
            models = []

        # preferred 우선순위대로 붙이되, 목록에 없더라도 일단 시도해볼 수 있게 구성
        preferred = [
            "gemini-2.5-flash-lite",
            "gemini-2.5-flash",
            "gemini-3.1-flash-lite-preview",
            "gemini-3-flash-preview",
            "gemini-2.0-flash-lite",
            "gemini-2.0-flash",
        ]
        for p in preferred:
            if p not in candidates:
                candidates.append(p)
        for m in models:
            if m not in candidates:
                candidates.append(m)

        last_error: Exception | None = None
        for model in candidates:
            tried.append(model)
            url = (
                f"https://generativelanguage.googleapis.com/v1beta/models/"
                f"{model}:generateContent?key={self.api_key}"
            )
            req = urllib.request.Request(
                url,
                data=json.dumps(payload).encode("utf-8"),
                headers={"Content-Type": "application/json"},
                method="POST",
            )
            try:
                with urllib.request.urlopen(req, timeout=60) as resp:
                    data = json.loads(resp.read().decode("utf-8"))
                last_error = None
                break
            except urllib.error.HTTPError as e:
                body = e.read().decode("utf-8", errors="replace")
                # 모델 미지원/권한 문제(404/403 등)는 다음 모델로 계속 시도
                last_error = RuntimeError(f"Gemini HTTPError {e.code} (model={model}): {body}")
                continue
            except Exception as e:
                last_error = RuntimeError(f"Gemini request failed (model={model}): {e}")
                continue

        if last_error is not None:
            raise last_error

        candidates = data.get("candidates") or []
        if not candidates:
            return ""
        parts = (candidates[0].get("content") or {}).get("parts") or []
        texts = [p.get("text", "") for p in parts if isinstance(p, dict)]
        return "".join(texts).strip()

    def generate_text(self, prompt: str, max_output_tokens: int = 512) -> str:
        return self._generate(
            contents=[{"role": "user", "parts": [{"text": prompt}]}],
            max_output_tokens=max_output_tokens,
        )

    def generate_vision(self, prompt: str, images_base64: list[str], max_output_tokens: int = 512) -> str:
        if not images_base64:
            return "분석할 이미지가 없습니다."
        parts: list[dict] = [{"text": prompt}]
        for b64 in images_base64:
            parts.append({"inline_data": {"mime_type": "image/jpeg", "data": b64}})
        return self._generate(
            contents=[{"role": "user", "parts": parts}],
            max_output_tokens=max_output_tokens,
        )

