# android-project-starter-with-llm

PRD 한 장을 채우면 LLM 도구(Claude Code, Cursor, Codex, Gemini CLI 등)가 Android 프로젝트를 만들어 주는 동아리용 스타터입니다. 파일 생성과 치환은 스크립트(`scripts/generate.py`)가 하고 LLM은 그 스크립트를 실행하고 결과를 검증합니다. 그래서 어떤 도구로 만들어도 같은 프로젝트가 나옵니다.

## 필요한 환경

- JDK 21
- Android SDK (platform 37 설치). `ANDROID_HOME` 또는 생성된 프로젝트의 `local.properties`에 `sdk.dir`이 설정돼 있어야 합니다.
- Python 3.10 이상
- git

## 사용법

1. `PRD.template.md`를 `PRD.md`로 복사해서 값을 채웁니다. `packageName`은 비워 둬도 됩니다. LLM이 앱 이름으로 후보를 제안하면 고르면 됩니다.

   ```bash
   cp PRD.template.md PRD.md
   ```

   `PRD.md`는 `.gitignore`에 들어 있어서 실수로 커밋되지 않습니다.
2. 쓰는 LLM 도구에 이렇게 요청합니다.

   > AGENTS.md대로 PRD.md로 프로젝트 만들어줘

   Claude Code에서는 `/android-init`을 쓰면 됩니다. 다른 PRD 파일은 `/android-init 경로`로 지정합니다.
3. 결과를 확인합니다. LLM이 생성 경로, 선택한 옵션, 다음에 할 일을 알려 줍니다. Android Studio에서 생성된 프로젝트를 열고 그 안의 `AGENTS.md`를 읽어 보세요. 생성물에는 첫 커밋이 이미 있습니다. GitHub 연결은 직접 합니다(`git remote add origin <URL>`, `git push -u origin main`).

검증이 실패하면 LLM은 생성물을 고치지 않고 멈춰서 보고합니다. 템플릿 버그일 가능성이 높으니 이 레포에 이슈로 알려 주세요.

## 고정 스택

생성되는 프로젝트는 항상 아래 구성입니다.

- Kotlin, Jetpack Compose, Material 3
- Hilt (DI)
- Retrofit + OkHttp + kotlinx.serialization (네트워크)
- Room (로컬 DB), DataStore (설정)
- 멀티 모듈: `app`, `core:*`, `data`, `domain`, `feature:sample`, `konsist`
- 테스트와 린트: Konsist, Roborazzi, detekt, ktlint
- 샘플 화면 `feature:sample`: API 호출, Room 캐시, DataStore 설정을 한 화면에서 보여줍니다.

정확한 버전은 `template/gradle/libs.versions.toml`에 있습니다.

## 선택 옵션

PRD의 `navigation`과 `ci`로 고릅니다.

| 키 | 값 | 설명 |
| --- | --- | --- |
| `navigation` | `nav2` | Navigation Compose |
| `navigation` | `nav3` (기본) | Navigation 3 |
| `ci` | `build-debug` | debug 빌드 workflow |
| `ci` | `build-release` | release 빌드 workflow |
| `ci` | `test` | 테스트 workflow |
| `ci` | `ui-diff` | UI 스크린샷 diff workflow |

`ci`는 쉼표로 여러 개를 고르거나 비울 수 있습니다. 비우면 `.github/`가 만들어지지 않습니다. 비공개 저장소에서는 GitHub Actions 실행 시간이 유료로 과금될 수 있으니 필요한 것만 켜세요.

## 템플릿 관리자용

레포 구조는 다음과 같습니다.

- `template/`: 실제로 빌드되는 Android 프로젝트 (패키지 `com.example.starter`, 이름 `Starter` 기준)
- `variants/`: `template/` 위에 덮어쓰는 파일들 (`navigation/nav2`, `navigation/nav3`, `ci/*`)
- `scripts/`: `generate.py`(복사, 치환, variant 적용), `verify.py`(검증), `tests/`, `fixtures/`

개발 루프는 이렇습니다. 템플릿을 고친 뒤 개발용 PRD로 생성하고 검증합니다.

```bash
python3 scripts/generate.py --prd scripts/fixtures/dev-nav3.prd.md --overwrite
python3 scripts/verify.py build/dev/nav3
```

스크립트 단위 테스트는 다음과 같이 실행합니다.

```bash
python3 -m unittest discover -s scripts/tests
```

`AGENTS.md`는 LLM이 따라야 할 전체 절차이고 `CLAUDE.md`는 이를 참조합니다. 절차를 바꿀 때는 `AGENTS.md`만 고치세요.

## 라이선스

Apache-2.0. 자세한 내용은 [LICENSE](LICENSE)를 참고하세요.
