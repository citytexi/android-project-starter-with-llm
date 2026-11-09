# PRD

이 파일은 고치지 말고 `PRD.md`로 복사해 값을 채운 뒤 `python3 scripts/generate.py --prd <파일>`로 프로젝트를 생성합니다.
아래 첫 번째 `properties` 블록만 읽습니다. 형식은 `key = value`이고 `#`으로 시작하는 줄은 주석입니다. 줄 끝 `#` 주석(`minSdk = 26 # 기본값`)은 지원하지 않습니다. `#`부터 끝까지 값으로 읽습니다. 아래에 없는 키, 같은 키를 두 번 쓴 줄, `=`가 없는 줄은 줄 번호와 함께 오류가 납니다.

## 키 설명

- `appName`: 앱 표시 이름. 한글, 공백, 특수문자(`%` 포함)를 써도 됩니다(`strings.xml`의 `app_name`).
- `packageName`: 앱 패키지명(예: `io.github.club.moim`). 소문자로 시작하는 소문자·숫자 세그먼트가 2개 이상이어야 하고(`_`는 lint가 거부하므로 쓸 수 없음), 예약어와 `com.example.*`는 쓸 수 없습니다. 첫 세그먼트로 `java`, `javax`, `kotlin`, `android`, `androidx`를 쓸 수 없고, `classPrefix`를 비워 두면 마지막 세그먼트는 `starter`로 시작할 수 없습니다(`starter`, `starterkit` 등). `packageName`을 비워 두면 LLM이 앱 이름으로 후보를 제안하고 당신이 고릅니다.
- `classPrefix`: 선택. 클래스 이름 접두사(`StarterTheme` → `<접두사>Theme`). 비우면 패키지명 마지막 세그먼트로 만듭니다(`moim` → `Moim`). 쓴다면 대문자로 시작하는 영문·숫자(`^[A-Z][A-Za-z0-9]*$`)이고 `Starter`로 시작할 수 없습니다. 바뀐 이름이 같은 파일이 import하는 라이브러리 이름과 겹치면(예: 마지막 세그먼트 `coroutine` → `CoroutineDispatcher`) 생성이 거부되니 이때 다른 접두사(예: `Moim`)를 적습니다.
- `minSdk`: 최소 SDK. 23 이상 36 이하의 정수(숫자만). 기본값 26.
- `baseUrl`: API 기본 주소. 호스트가 있는 `https://` 주소이고 `/`로 끝나야 합니다(Retrofit 요구사항). 포트를 쓰면 올바른 숫자여야 합니다. ASCII 문자만 쓰고, 공백, 따옴표(`"`, `'`), 역슬래시(`\`), `@`, `?`, `#`은 쓸 수 없습니다.
- `navigation`: `nav2`(Navigation Compose) 또는 `nav3`(Navigation 3). 기본값 `nav3`.
- `ci`: 생성할 GitHub Actions를 쉼표로 구분해 적습니다. `build-debug`, `build-release`, `test`, `ui-diff` 중에서 고릅니다. 비우면 `.github/`를 만들지 않습니다. `build-release`는 수동 실행 전용이고, `test`는 `testStarter`를 실행합니다.
- `outputDir`: 생성 위치. 이 파일이 있는 디렉터리 기준 상대 경로이거나 절대 경로입니다. `~`는 홈 디렉터리로 바꿉니다. 생략하면 `../<패키지명 마지막 세그먼트>`입니다. 스타터 레포 안(`build/` 아래 제외)은 쓸 수 없습니다. 비어 있는 디렉터리나 깨끗한 git 저장소의 루트(예: README/LICENSE만 있는 clone)를 쓸 수 있습니다.

## 설정

```properties
appName = My App
packageName =
# classPrefix = MyApp
minSdk = 26
baseUrl = https://api.example.com/
navigation = nav3
ci =
# outputDir = ../myapp
```
