# AGENTS.md: PRD로 Android 프로젝트 만들기

이 레포는 템플릿과 생성 스크립트만 담고 있습니다. 사용자가 "AGENTS.md대로 PRD로 프로젝트 만들어줘"라고 하면 아래 절차를 순서대로 따릅니다. 읽어야 할 파일은 이 문서와 PRD 키 설명이 있는 `PRD.template.md`입니다.

목표는 어떤 LLM 도구로 실행해도 **똑같은 프로젝트**가 나오는 것입니다. 그래서 파일을 만들고 바꾸는 일은 전부 스크립트가 하고, 당신은 PRD 값을 확인하고 스크립트를 실행하고 결과를 전달합니다.

## 건드리면 안 되는 것

생성하는 동안 다음은 읽기만 하고 수정하지 않습니다.

- `template/`
- `variants/`
- `scripts/`

생성된 프로젝트(`outputDir`)도 수정하지 않습니다(5단계 참고). 당신이 쓸 수 있는 파일은 사용자가 고른 PRD 파일(`packageName` 등 값 입력)뿐입니다. `PRD.template.md`는 읽기만 하고 고치지 않습니다(값은 `PRD.md`로 복사해 채웁니다).

## 실행 환경

- 모든 명령은 이 레포의 루트에서 실행합니다.
- macOS/Linux에서는 `python3`, Windows에서는 `python` 또는 `py -3`을 씁니다. 아래 예시는 `python3`로 적었으니 환경에 맞게 바꿔 읽습니다. Windows도 지원하며 `verify.py`는 `gradlew.bat`을 씁니다.
- 3단계 전에 `python3 --version`으로 3.10 이상인지 확인합니다. 낮거나 없으면 설치를 안내하고 멈춥니다.

## 절차

### 1. PRD 읽기

PRD 경로는 사용자가 알려준 파일이고, 없으면 `PRD.md`입니다. 파일이 없으면 `PRD.template.md`를 `PRD.md`로 복사할지 사용자에게 묻고, 값 채우는 것을 도와줍니다.

PRD에서는 첫 번째 ```` ```properties ```` 블록만 읽습니다(`key = value`, `#`으로 시작하는 줄은 주석). 줄 끝 `#` 주석은 지원하지 않습니다(값의 일부로 읽음). 키는 `appName`, `packageName`, `classPrefix`(선택), `minSdk`(23~36, 기본 26), `baseUrl`, `navigation`(`nav2`/`nav3`, 기본 `nav3`), `ci`, `outputDir`입니다. 이 밖의 키, 같은 키 두 번, `=`가 없는 줄은 스크립트가 줄 번호와 함께 거부합니다. `PRD.template.md`에 키 설명이 있습니다.

- 필수 값이 비었거나 모호하면 추측하지 말고 사용자에게 물어봅니다.
- 값 검증은 스크립트가 합니다. 직접 판단해서 고치지 말고 스크립트의 오류 메시지를 그대로 전달합니다.
- `appName`은 필수입니다. `minSdk`는 23 이상 36 이하입니다. `baseUrl`은 호스트가 있는 `https://` 주소이고 `/`로 끝나야 하며 ASCII 문자만 쓰고 공백, 따옴표(`"`, `'`), 역슬래시(`\`), `@`, `?`, `#`을 넣을 수 없습니다. 포트를 쓰면 올바른 숫자여야 합니다.
- `ci`는 `build-debug`, `build-release`, `test`, `ui-diff`를 쉼표로 적습니다. 비우면 `.github/`가 생기지 않습니다.
- `outputDir`을 생략하면 PRD 파일이 있는 디렉터리 기준 `../<패키지명 마지막 세그먼트>`에 생성합니다. `~`는 홈 디렉터리로 바뀝니다.
- `classPrefix`는 보통 비워 둡니다. 비우면 패키지명 마지막 세그먼트로 만듭니다(`club_moim` → `ClubMoim`). 스스로 채우지 않고, 3단계가 접두사 충돌(`class prefix '<Prefix>' clashes ...`)을 보고했을 때만 사용자에게 값을 물어 채웁니다.

### 2. `packageName`이 비어 있으면 사용자에게 고르게 한다

`packageName`이 비어 있으면 **혼자 정하지 않습니다.** 다음 순서로 진행합니다.

1. `appName`을 바탕으로 후보를 2~3개 제안합니다. 한글 앱 이름은 영문으로 로마자 표기하거나 번역해서 씁니다. 예시는 다음과 같습니다.
   - `io.github.<github-id>.<appname>`
   - `com.<동아리명>.<appname>`
   - `kr.club.<appname>`
2. 후보는 모두 아래 규칙을 지켜야 합니다. 제안하기 전에 직접 확인합니다.
   - 세그먼트는 `^[a-z][a-z0-9_]*$` (소문자로 시작, 소문자·숫자·`_`만)
   - 세그먼트가 2개 이상
   - Kotlin/Java 예약어(`in`, `is`, `fun`, `new`, `class`, `object` 등)를 세그먼트로 쓰지 않음
   - `com.example.*`로 시작하지 않음 (Play 스토어가 거부함)
   - 첫 세그먼트가 `java`, `javax`, `kotlin`, `android`, `androidx`가 아님 (플랫폼 패키지와 겹침)
   - 마지막 세그먼트가 `starter`로 시작하지 않음 (`starter`, `starterkit`, `starter_app` 등). 클래스 접두사가 `Starter...`가 되어 템플릿 토큰으로 오인됩니다. 사용자가 그런 이름을 원하면 `classPrefix`를 함께 정해야 합니다.
   - 후보를 제안할 때 `classPrefix`는 함께 제안하지 않습니다. 접두사 충돌은 3단계에서 스크립트가 알려주고, 그때만 정합니다.
3. 사용자가 후보 중 하나를 고르거나 직접 입력할 때까지 **기다립니다.** 답이 오기 전에는 3단계로 넘어가지 않습니다.
4. 정해진 값을 PRD 파일의 `packageName = ` 뒤에 써 넣습니다. 그다음 3단계로 진행합니다.

사용자가 직접 입력한 값이 규칙에 어긋나 보이면 알려주고 다시 받습니다.

### 3. 생성

```bash
python3 scripts/generate.py --prd <PRD 경로>
```

- 출력 디렉터리가 이미 있고 비어 있지 않으면 실패합니다(종료 코드 2).
- 출력 디렉터리는 이 레포 안이면 안 됩니다. 예외는 `build/<디렉터리>` 아래뿐입니다(템플릿 관리자의 개발용). 대소문자만 다른 경로나 심볼릭 링크도 같은 디렉터리로 보고 거부합니다.
- 생성물 루트에는 `.starter-generated` 표식 파일이 생깁니다. `--overwrite`는 이 표식이 있는 디렉터리만 지웁니다. 표식이 없는 비어 있지 않은 디렉터리는 `--overwrite`를 붙여도 `error: refusing to delete a directory not created by generate.py: <절대 경로>`로 거부합니다(종료 코드 2). 이때는 다른 `outputDir`을 쓰자고 제안합니다. 표식 파일을 직접 만들어 우회하지 않습니다.
- **`--overwrite`는 출력 디렉터리 전체를 `rmtree`로 삭제합니다(이전에 `generate.py`가 만든 디렉터리일 때만).** 기본 `outputDir`은 레포 밖의 형제 디렉터리(`../<마지막 세그먼트>`)라서 사용자의 기존 프로젝트가 있을 수 있습니다. 붙이기 전에 반드시 다음을 합니다.
  1. 출력 디렉터리의 **절대 경로**를 사용자에게 보여줍니다.
  2. 그 안의 모든 것이 삭제된다고 말합니다.
  3. 그 질문에 대한 명시적인 yes를 받습니다.

  `--overwrite`를 스스로 판단해서 붙이지 않고, 실패 후 재시도할 때도 붙이지 않습니다. 사용자가 거절하면 PRD의 `outputDir`을 바꾸자고 제안합니다.
- 성공하면 `generated: <경로>`를 출력합니다. git이 설정돼 있으면 그 경로에서 `git init`과 첫 커밋까지 만듭니다. git이 없거나 `user.name`/`user.email`이 없으면 `warning: git init/commit failed ...`를 stderr에 내고 프로젝트는 그대로 생성한 채 종료 코드 0으로 끝납니다. 이것은 로컬 환경 문제이니 git 설정 방법만 안내합니다(`git config --global user.name "<이름>"`, `git config --global user.email "<이메일>"`, 이어서 경고가 안내한 `git init -b main && git add -A && git commit ...`).

### 4. 검증

```bash
python3 scripts/verify.py <outputDir>
```

`<outputDir>`는 3단계가 출력한 `generated:` 경로입니다. 생성물의 모듈 목록과 `gradle/libs.versions.toml`이 템플릿과 같은지 비교한 뒤 `./gradlew verifyStarter --continue`를 실행합니다. 시간이 오래 걸리니 끝날 때까지 기다립니다. 성공하면 `verify: ok`가 나옵니다.

### 5. 실패하면 생성물을 고치지 말고 멈춘다

**종료 코드 3, 4, 5(그리고 아래 표에 없는 코드나 traceback. 잡히지 않은 예외는 1)는 생성된 프로젝트를 수정하지 말고 멈춰서 사용자에게 보고합니다.** 종료 코드 2는 예외입니다(`error: template changed: ...`만 빼고). 사용자 입력(PRD 값, 이미 있는 출력 디렉터리)을 바로잡는 문제이므로, 사용자의 답에 따라 **PRD 파일만** 고치고 3단계를 다시 실행할 수 있습니다. `usage:`가 나오는 종료 코드 2는 명령을 잘못 쓴 것이니 명령만 고쳐 다시 실행합니다. 재시도로 덮어쓰거나, 코드를 패치해서 통과시키거나, 설정을 바꿔 우회하는 일을 하지 않습니다.

보고에 넣을 것은 다음과 같습니다.

- 종료 코드
- 로그의 마지막 몇 줄
- 원인 추정

이유는 이렇습니다. 손대지 않은 생성물에서 검증이 실패했다면 템플릿 버그입니다. 수정은 이 레포의 `template/`(또는 `variants/`, `scripts/`)에서 해야 하고, 그 일은 템플릿 관리자가 합니다. LLM이 생성물을 제각각 고치면 도구마다 결과가 달라져서 이 레포가 존재하는 이유가 없어집니다.

**유일한 예외는 로컬 환경 문제입니다.** JDK 21이 없거나, Android SDK 경로를 못 찾는 경우입니다. 이 경우에는 어떻게 설치하고 설정하면 되는지 안내만 하고, 사용자가 환경을 고친 뒤 4단계를 다시 실행하게 합니다. SDK 경로는 사용자가 `ANDROID_HOME` 환경 변수를 설정하거나 생성물 루트에 `local.properties`(`sdk.dir=<SDK 경로>`)를 직접 만들어 알려 줍니다. 당신이 그 파일을 대신 만들거나 생성물의 소스를 고치지 않습니다.

### 6. 요약

성공하면 사용자에게 다음을 알려줍니다.

- 생성된 프로젝트의 경로
- 선택된 옵션: `navigation`(nav2/nav3)과 `ci`(생성된 workflow 목록, 없으면 없음), `classPrefix`(PRD에 적었거나 패키지명에서 만들어진 값)
- 생성된 모듈 목록(`settings.gradle.kts`의 `include(...)` 기준: `app`, `core:*`, `data`, `domain`, `feature:sample`, `konsist`)
- 다음에 할 일
  1. Android Studio에서 생성된 프로젝트를 엽니다.
  2. 생성된 프로젝트의 `AGENTS.md`를 읽습니다(이후 작업 지침).
  3. 루트 `gradle.properties`의 `starter.baseUrl`을 실제 API 주소로 바꾸고, `@GET("sample/items")` 샘플 경로를 실제 경로로 바꿉니다.
- 원격 저장소 연결은 사용자가 직접 합니다. 생성물에는 이미 첫 커밋이 있습니다. 예시는 다음과 같습니다.

  ```bash
  cd <outputDir>
  git remote add origin <저장소 URL>
  git push -u origin main
  ```

  **당신이 대신 push하거나 원격 저장소를 만들지 않습니다.** 명령만 알려줍니다.
- CI를 켰다면, 비공개 저장소에서는 GitHub Actions 실행 시간이 과금될 수 있다고 알려줍니다.

## 종료 코드

| 스크립트 | 코드 | 의미 | 사용자에게 할 말 |
| --- | --- | --- | --- |
| `generate.py` | 0 | 성공 | 4단계로 진행합니다. |
| `generate.py` | 2 | PRD 검증 실패 또는 입출력 오류 (stderr에 `error: ...`) | 메시지를 그대로 전달하고 어느 PRD 값을 고칠지 묻습니다. 사용자의 답으로 PRD만 고쳐 3단계를 다시 실행합니다. 출력 디렉터리가 비어 있지 않다는 오류면 다른 `outputDir`을 쓸지 묻고, `--overwrite`는 3단계의 확인 절차를 거친 경우에만 씁니다. 권한이나 디스크 오류면 원인을 안내합니다. |
| `generate.py` | 3 | 치환 후 `com.example.starter`/`Starter`가 남음 | 템플릿 버그입니다. 남은 `경로:줄` 목록을 전달하고 멈춥니다. 중간까지 만들어진 출력 디렉터리가 디스크에 남아 있다고 알려줍니다(지우거나 고치지 않습니다). 수정은 이 레포의 `template/`, `variants/`, `scripts/`에서 합니다. |
| `verify.py` | 0 | 구조 비교와 `verifyStarter` 통과 (`verify: ok`) | 6단계 요약으로 진행합니다. |
| `verify.py` | 4 | 생성물 구조가 템플릿과 다름 (모듈 목록, `libs.versions.toml`, `gradlew`/`gradlew.bat` 없음) 또는 프로젝트 디렉터리 없음 | 결정성이 깨진 것입니다. 오류 메시지를 전달하고 멈춥니다. 디렉터리가 없다는 오류면 경로를 확인합니다. |
| `verify.py` | 5 | Gradle 실패 또는 Gradle을 실행하지 못함 | 로그 마지막 80줄과 원인 추정을 전달합니다. JDK 21 또는 Android SDK 경로 문제면 환경 설정을 안내하고, 그 외에는 템플릿 버그로 보고 멈춥니다. |
| 둘 다 | 2 (stderr에 `usage:`) | argparse 명령행 오류(`--prd` 빠짐 등). PRD 문제가 아니라 명령을 잘못 쓴 것 | 명령을 고쳐 다시 실행합니다. PRD는 건드리지 않습니다. |
| 둘 다 | 1, 그 외 코드, traceback | 예상 밖 실패(잡히지 않은 예외는 1) | 멈추고 출력 전문과 원인 추정을 보고합니다. |

종료 코드 2의 대표 메시지는 다음과 같습니다.

- `error: packageName is required: ask the user to choose one (see AGENTS.md)`: 2단계를 건너뛰었다는 뜻입니다. 사용자에게 후보를 제안하고 고르게 합니다.
- `error: packageName must not start with com.example`
- `error: packageName segment '<seg>' is a reserved word`
- `error: packageName '<name>' needs at least 2 segments (e.g. com.club.app)`
- `error: appName is required`
- `error: packageName must not start with '<seg>' (reserved: android, androidx, java, javax, kotlin)`
- `error: packageName last segment '<seg>' gives class prefix '<Prefix>', which must not start with 'Starter' (template token): change packageName or set classPrefix`
- `error: minSdk must be between 23 and 36 (got <n>)`
- `error: baseUrl must be an https:// URL with a host (and a valid port, if any) that ends with '/' and has only ASCII characters, no spaces, quotes, backslashes, '@', '?' or '#' (Retrofit requirement)`
- `error: PRD line <n>: unknown key '<key>' (allowed: appName, packageName, classPrefix, minSdk, baseUrl, navigation, ci, outputDir)`: 오타일 가능성이 큽니다(`appname` 등). 어느 키를 뜻했는지 사용자에게 묻습니다.
- `error: PRD line <n>: duplicate key '<key>' (first set on line <m>)`
- `error: PRD line <n>: expected 'key = value' or a '#' comment: '<줄>'`
- `error: minSdk must be an integer: '<값>'`
- `error: packageName segment '<seg>' is invalid: use lowercase letters, digits and '_', starting with a letter`
- `error: classPrefix '<값>' is invalid: use PascalCase ASCII letters and digits, starting with an uppercase letter (e.g. Moim)`
- `error: classPrefix '<값>' must not start with 'Starter' (template token)`
- `error: class prefix '<Prefix>' clashes with imported names; set classPrefix in the PRD to another PascalCase name (e.g. the app name):` 뒤에 `<파일>: import <경로> clashes with <이름> (from Starter<X>)` 목록: 패키지명 마지막 세그먼트로 만든 접두사가 라이브러리 이름과 겹친 것입니다(예: `coroutine` → `CoroutineDispatcher`). 출력 디렉터리는 건드리지 않은 상태입니다. `appName`을 바탕으로 PascalCase 접두사 후보(예: `Moim`)를 제안하고, 사용자가 고른 값을 PRD의 `classPrefix = ` 뒤에 써 넣은 뒤 3단계를 다시 실행합니다.
- `error: template changed: ...`: 사용자 입력 문제가 아니라 템플릿 버그입니다(`strings.xml`의 `app_name`이나 `gradle.properties`의 `starter.minSdk=`/`starter.baseUrl=` 줄이 정확히 하나가 아님). 종료 코드는 2지만 PRD를 고치지 말고 종료 코드 3처럼 멈춰서 보고합니다.
- `error: output directory is not empty: <경로> (use --overwrite)`
- `error: refusing to delete a directory not created by generate.py: <경로>`
- `error: refusing to write inside the starter repo (only build/<dir> is allowed): <경로>`
