# APK 배포 폴더

이 폴더에는 배포용 안드로이드 APK 파일을 올립니다.

## 파일 이름 규칙

랜딩 페이지(`/`)와 다운로드 컨트롤러(`/download/apk`)는 다음 파일을 참조합니다.

```
src/main/resources/static/downloads/MyImageLogApp.apk
```

새 버전을 배포할 때는 **같은 파일명으로 덮어쓰기** 하세요.
랜딩 페이지나 컨트롤러 코드를 수정할 필요가 없습니다.

## 다운로드 경로

- 직접: `https://<도메인>/downloads/MyImageLogApp.apk`
- 권장(컨트롤러 경유): `https://<도메인>/download/apk`
  - 정확한 MIME(`application/vnd.android.package-archive`)과 강제 다운로드 헤더를 적용합니다.

## 배포 절차 (현재 저장소 기준)

1. 이 폴더에 `MyImageLogApp.apk`를 추가/교체
2. `git add` → `commit` → `push origin master`
3. `git tag vX.Y.Z` → `git push origin vX.Y.Z`
   - `.github/workflows/deploy-on-tag.yml`이 자동으로 Hetzner에 배포합니다.
