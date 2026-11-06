# AGENTS.md: PRD로 Android 프로젝트 만들기

이 레포는 템플릿과 생성 스크립트만 담고 있습니다. 사용자가 "AGENTS.md대로 PRD로 프로젝트 만들어줘"라고 하면 아래 절차를 순서대로 따릅니다. 다른 맥락은 필요 없습니다.

목표는 어떤 LLM 도구로 실행해도 **똑같은 프로젝트**가 나오는 것입니다. 그래서 파일을 만들고 바꾸는 일은 전부 스크립트가 하고, 당신은 PRD 값을 확인하고 스크립트를 실행하고 결과를 전달합니다.

## 건드리면 안 되는 것

생성하는 동안 다음은 읽기만 하고 수정하지 않습니다.

- `template/`
- `variants/`
- `scripts/`

생성된 프로젝트(`outputDir`)도 수정하지 않습니다(5단계 참고). 당신이 쓸 수 있는 파일은 사용자가 고른 PRD 파일(`packageName` 등 값 입력)뿐입니다.

## 절차

### 1. PRD 읽기

PRD 경로는 사용자가 알려준 파일이고, 없으면 `PRD.md`입니다. 파일이 없으면 `PRD.template.md`를 `PRD.md`로 복사할지 사용자에게 묻고, 값 채우는 것을 도와줍니다.

PRD에서는 첫 번째 ```` ```properties ```` 블록만 읽습니다(`key = value`, `#`은 주석). 키는 `appName`, `packageName`, `minSdk`(기본 26), `baseUrl`, `navigation`(`nav2`/`nav3`, 기본 `nav3`), `ci`, `outputDir`입니다. `PRD.template.md`에 키 설명이 있습니다.

- 필수 값이 비었거나 모호하면 추측하지 말고 사용자에게 물어봅니다.
- 값 검증은 스크립트가 합니다. 직접 판단해서 고치지 말고 스크립트의 오류 메시지를 그대로 전달합니다.
- `appName`은 필수입니다. `baseUrl`은 `https://`로 시작하고 `/`로 끝나야 합니다.
- `ci`는 `build-debug`, `build-release`, `test`, `ui-diff`를 쉼표로 적습니다. 비우면 `.github/`가 생기지 않습니다.
- `outputDir`을 생략하면 PRD 파일이 있는 디렉터리 기준 `../<패키지명 마지막 세그먼트>`에 생성합니다.

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
3. 사용자가 후보 중 하나를 고르거나 직접 입력할 때까지 **기다립니다.** 답이 오기 전에는 3단계로 넘어가지 않습니다.
4. 정해진 값을 PRD 파일의 `packageName = ` 뒤에 써 넣습니다. 그다음 3단계로 진행합니다.

사용자가 직접 입력한 값이 규칙에 어긋나 보이면 알려주고 다시 받습니다.

### 3. 생성

```bash
python3 scripts/generate.py --prd <PRD 경로>
```

- 출력 디렉터리가 이미 있고 비어 있지 않으면 실패합니다. 사용자가 덮어써도 된다고 명시했을 때만 `--overwrite`를 붙입니다.
- 성공하면 `generated: <경로>`를 출력하고, 그 경로에서 `git init`과 첫 커밋까지 만듭니다.

### 4. 검증

```bash
python3 scripts/verify.py <outputDir>
```

`<outputDir>`는 3단계가 출력한 `generated:` 경로입니다. 생성물의 모듈 목록과 `gradle/libs.versions.toml`이 템플릿과 같은지 비교한 뒤 `./gradlew verifyStarter --continue`를 실행합니다. 시간이 오래 걸리니 끝날 때까지 기다립니다. 성공하면 `verify: ok`가 나옵니다.

### 5. 실패하면 생성물을 고치지 말고 멈춘다

**3단계나 4단계가 실패하면 생성된 프로젝트를 수정하지 말고, 멈춰서 사용자에게 보고합니다.** 재시도로 덮어쓰거나, 코드를 패치해서 통과시키거나, 설정을 바꿔 우회하는 일을 하지 않습니다.

보고에 넣을 것은 다음과 같습니다.

- 종료 코드
- 로그의 마지막 몇 줄
- 원인 추정

이유는 이렇습니다. 손대지 않은 생성물에서 검증이 실패했다면 템플릿 버그입니다. 수정은 이 레포의 `template/`(또는 `variants/`, `scripts/`)에서 해야 하고, 그 일은 템플릿 관리자가 합니다. LLM이 생성물을 제각각 고치면 도구마다 결과가 달라져서 이 레포가 존재하는 이유가 없어집니다.

**유일한 예외는 로컬 환경 문제입니다.** JDK 21이 없거나, Android SDK 경로를 못 찾는 경우(`ANDROID_HOME` 환경 변수, 생성물의 `local.properties`의 `sdk.dir`)입니다. 이 경우에는 어떻게 설치하고 설정하면 되는지 안내만 하고, 사용자가 환경을 고친 뒤 4단계를 다시 실행하게 합니다. 이때도 생성물의 소스는 고치지 않습니다.

### 6. 요약

성공하면 사용자에게 다음을 알려줍니다.

- 생성된 프로젝트의 경로
- 선택된 옵션: `navigation`(nav2/nav3)과 `ci`(생성된 workflow 목록, 없으면 없음)
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
| `generate.py` | 2 | PRD 검증 실패 또는 입출력 오류 (stderr에 `error: ...`) | 메시지를 그대로 전달하고 어느 PRD 값을 고칠지 묻습니다. 값이 정해지면 PRD를 고쳐 3단계를 다시 실행합니다. 출력 디렉터리가 비어 있지 않다는 오류면 다른 `outputDir`을 쓸지, `--overwrite`로 덮어써도 되는지 묻습니다. |
| `generate.py` | 3 | 치환 후 `com.example.starter`/`Starter`가 남음 | 템플릿 버그입니다. 남은 `경로:줄` 목록을 전달하고 멈춥니다. 수정은 이 레포의 `template/`, `variants/`, `scripts/`에서 합니다. |
| `verify.py` | 0 | 구조 비교와 `verifyStarter` 통과 (`verify: ok`) | 6단계 요약으로 진행합니다. |
| `verify.py` | 4 | 생성물 구조가 템플릿과 다름 (모듈 목록이나 `libs.versions.toml`) 또는 프로젝트 디렉터리 없음 | 결정성이 깨진 것입니다. 오류 메시지를 전달하고 멈춥니다. 디렉터리가 없다는 오류면 경로를 확인합니다. |
| `verify.py` | 5 | Gradle 실패 또는 Gradle을 실행하지 못함 | 로그 마지막 80줄과 원인 추정을 전달합니다. JDK 21 또는 Android SDK 경로 문제면 환경 설정을 안내하고, 그 외에는 템플릿 버그로 보고 멈춥니다. |

종료 코드 2의 대표 메시지는 다음과 같습니다.

- `error: packageName is required: ask the user to choose one (see AGENTS.md)`: 2단계를 건너뛰었다는 뜻입니다. 사용자에게 후보를 제안하고 고르게 합니다.
- `error: packageName must not start with com.example`
- `error: packageName segment '<seg>' is a reserved word`
- `error: packageName '<name>' needs at least 2 segments (e.g. com.club.app)`
- `error: appName is required`
- `error: baseUrl must start with https:// and end with '/' (Retrofit requirement)`
