# PRD

이 파일을 복사해 값을 채운 뒤 `python3 scripts/generate.py --prd <파일>`로 프로젝트를 생성합니다.
아래 첫 번째 `properties` 블록만 읽습니다. 형식은 `key = value`이고 `#`으로 시작하는 줄은 주석입니다.

## 키 설명

- `appName`: 앱 표시 이름. 한글, 공백, 특수문자를 써도 됩니다(`strings.xml`의 `app_name`).
- `packageName`: 앱 패키지명(예: `io.github.club.moim`). 소문자로 시작하는 소문자·숫자·`_` 세그먼트가 2개 이상이어야 하고, 예약어와 `com.example.*`는 쓸 수 없습니다. 비워 두면 LLM이 앱 이름으로 후보를 제안하고 당신이 고릅니다.
- `minSdk`: 최소 SDK. 기본값 26.
- `baseUrl`: API 기본 주소. `https://`로 시작하고 `/`로 끝나야 합니다(Retrofit 요구사항).
- `navigation`: `nav2`(Navigation Compose) 또는 `nav3`(Navigation 3). 기본값 `nav3`.
- `ci`: 생성할 GitHub Actions를 쉼표로 구분해 적습니다. `build-debug`, `build-release`, `test`, `ui-diff` 중에서 고릅니다. 비우면 `.github/`를 만들지 않습니다.
- `outputDir`: 생성 위치. 이 파일이 있는 디렉터리 기준 상대 경로입니다. 생략하면 `../<패키지명 마지막 세그먼트>`입니다.

## 설정

```properties
appName = My App
packageName =
minSdk = 26
baseUrl = https://api.example.com/
navigation = nav3
ci =
# outputDir = ../myapp
```
