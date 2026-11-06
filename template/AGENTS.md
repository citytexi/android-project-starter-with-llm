# AGENTS.md

LLM 도구(Claude Code, Cursor, Codex)와 팀원이 이 프로젝트에서 지킬 규칙이다. 코드를 바꾸기 전에 읽는다.

- 패키지 루트: `com.example.starter`, 클래스 접두사: `Starter`
- 앱 표시 이름: `app/src/main/res/values/strings.xml`의 `app_name`
- 스택: Kotlin, Compose, Hilt, Room, Retrofit, DataStore

## 1. 모듈 구성 (14개)

| 모듈 | 종류 | 역할 |
|---|---|---|
| `:app` | Android | `MainActivity`, Hilt Application, 앱 전체 nav 조립(`StarterNavHost`) |
| `:feature:sample` | Android | 예시 화면. Route, Screen, ViewModel, UiState. 새 feature의 복사 원본 |
| `:domain` | JVM | 모델, `*Repository` 인터페이스, `*UseCase`. Android 의존 금지 |
| `:data` | Android | `*RepositoryImpl`, Hilt 바인딩(`DataModule`) |
| `:core:designsystem` | Android | 테마, 공용 컴포넌트 |
| `:core:ui` | Android | feature 공용 UI 유틸 |
| `:core:navigation` | Android | route/key 정의. nav 라이브러리를 `api`로 노출 |
| `:core:network` | Android | Retrofit, OkHttp, `SampleApi`, `BuildConfig.BASE_URL` |
| `:core:database` | Android | Room |
| `:core:datastore` | Android | DataStore 설정, `SecureStore` |
| `:core:common` | JVM | 공용 타입(`AppResult` 등) |
| `:core:testing:jvm` | JVM | JVM 테스트 도구 |
| `:core:testing:android` | Android | 계측/Robolectric 테스트 도구 |
| `:konsist` | JVM | 아키텍처 규칙 테스트(소스 경로만 읽음) |

JVM 모듈은 `domain`, `core:common`, `core:testing:jvm`, `konsist`이고 나머지는 Android다.

## 2. 의존 규칙

Gradle `build-logic`의 `ModuleRules`가 모든 모듈의 프로젝트 의존을 검사하고 어기면 빌드가 실패한다. `implementation`/`api`뿐 아니라 직접 만든 구성과 `extendsFrom`으로 이어진 구성, `afterEvaluate`에서 추가한 의존도 검사한다. test 구성(`test`, `androidTest`, `testFixtures` 등 이름에 `test`/`Test` 단어가 있는 구성)은 검사하지 않는다.

| 모듈 | 의존해도 되는 모듈 |
|---|---|
| `feature:*` | `domain`, `core:designsystem`, `core:ui`, `core:navigation`, `core:common` |
| `domain` | `core:common` |
| `data` | `domain`, `core:network`, `core:database`, `core:datastore`, `core:common` |
| `core:designsystem`, `core:ui`, `core:navigation` | 이 셋과 `core:common`만 |
| 그 밖의 `core:*` | 다른 `core:*`만. `core:testing:*`은 test/androidTest 구성에서만 쓰고, 자신은 아무 `core:*`에나 의존할 수 있음 |
| `app` | 전부 |
| `konsist` | 없음 |

`feature:*` → `core:common`은 설계 문서의 목록에는 없지만 허용한다. `domain`이 `core:common`을 `api`로 노출하고, ViewModel이 dispatcher qualifier를 주입받을 수 있어야 하기 때문이다.

Konsist(`konsist/src/test`)가 import도 검사한다.

- `feature`는 `com.example.starter.data`, `core.network`, `core.database`, `core.datastore` 패키지를 import하지 않는다(와일드카드 포함).
- `domain`은 `android`, `androidx`를 import하지 않는다.

## 3. 네이밍 규칙 (Konsist)

| 대상 | 위치 |
|---|---|
| `*ViewModel` | `feature` 또는 `app`(`MainViewModel`) |
| `*Repository` 인터페이스 | `domain` |
| `*RepositoryImpl` | `data`. `domain`의 Repository를 구현해야 함 |
| `*UseCase` | `domain`. `operator fun invoke`를 가져야 함 |

nav 파일 이름은 `StarterRoutes`/`StarterNavKeys`처럼 접두사를 쓴다. feature의 화면 composable은 `*Route`, `*Screen`이다.

## 4. 새 feature 추가

1. `feature/sample`을 `feature/<name>`으로 복사한다. `sample`을 `<name>`으로 바꾼다(디렉터리, 패키지 `com.example.starter.feature.<name>`, `namespace`, 클래스 이름).
   - `build.gradle.kts`는 플러그인 `starter.android.feature`(library + compose + hilt)와 `starter.android.screenshot`을 쓰고 `namespace = "com.example.starter.feature.<name>"`로 둔다.
   - 복사한 `SampleViewModel`, `SampleViewModelTest`, `SampleScreenScreenshotTest`는 sample UseCase를 주입받는다. 1단계에서 sample을 `<name>`으로 바꾸면 존재하지 않는 UseCase를 참조하게 되므로, 새 feature의 UseCase로 다시 쓰거나 지운다.
2. `settings.gradle.kts`에 `include(":feature:<name>")`을 추가한다. `app/build.gradle.kts`에도 `implementation(project(":feature:<name>"))`을 추가한다.
3. 데이터가 필요하면 아래 순서로 만든다.
   - `domain`: 모델, `<Name>Repository` 인터페이스, `<Action><Name>UseCase`(`operator fun invoke`)
   - `data`: `<Name>RepositoryImpl`, `DataModule`에 `@Binds` 추가
   - `feature`: `@HiltViewModel` ViewModel은 UseCase만 주입받는다. Repository를 직접 주입하지 않는다.
4. nav 등록. **먼저 프로젝트가 쓰는 variant를 확인한다.** `core/navigation/build.gradle.kts`에 `navigation-compose`(`androidx.navigation.compose`)가 있으면 nav2, `navigation3`가 있으면 nav3다.
   - 시작 화면은 nav2는 `StarterNavHost`의 `startDestination = ...`, nav3는 `rememberNavBackStack(...)`에서 정한다.

   **nav2**
   - `core/navigation/.../StarterRoutes.kt`에 `@Serializable data object <Name>NavRoute`를 추가한다.
   - `feature/<name>/.../navigation/<Name>Navigation.kt`에 `fun NavGraphBuilder.<name>Screen() { composable<<Name>NavRoute> { <Name>Route() } }`를 만든다.
   - `app/.../navigation/StarterNavHost.kt`의 `NavHost { }` 블록에서 `<name>Screen()`을 호출한다.

   **nav3**
   - `core/navigation/.../StarterNavKeys.kt`에 `@Serializable data object <Name>Key : NavKey`를 추가한다.
   - `feature/<name>/.../navigation/<Name>Navigation.kt`에 `fun EntryProviderScope<NavKey>.<name>Entry() { entry<<Name>Key> { <Name>Route() } }`를 만든다.
   - `app/.../navigation/StarterNavHost.kt`의 `entryProvider { }` 블록에서 `<name>Entry()`를 호출한다.

5. 테스트를 쓴다(ViewModel 단위 테스트, 화면 스크린샷 테스트는 `SampleViewModelTest`, `SampleScreenScreenshotTest` 참고). 작업 중에는 `./gradlew testStarter`, 마무리는 `./gradlew verifyStarter`로 한다.

## 5. 검증

변경을 끝내기 전에 반드시 실행한다.

```bash
./gradlew verifyStarter
```

task는 두 개다. 모듈에 없는 task는 건너뛴다.

- `testStarter`: 빠른 검사. CI의 `test` workflow가 이것을 돌린다.
  - Android: `testDebugUnitTest`, `assembleDebugAndroidTest`(컴파일만)
  - JVM: `test` (`:konsist` 포함, 아키텍처 규칙)
  - `build-logic`: `:convention:test` (`ModuleRulesTest`)
  - 전체: `detekt`, `ktlintCheck`
- `verifyStarter`: 완료 게이트. `testStarter` 전부에 더해 Android 모듈의 `assembleDebug`, `assembleRelease`, `recordRoborazziDebug`를 실행한다.

작업 중에는 `./gradlew testStarter`로 자주 확인하고, 끝내기 전에는 `verifyStarter`를 돌린다.

포맷 오류는 `./gradlew ktlintFormat`으로 고친다. 기준 스크린샷은 커밋하지 않는다.

## 6. 금지 사항

- `feature`에서 `data`, `core:network`, `core:database`, `core:datastore`를 의존하거나 import하지 않는다. UI가 저장소 구현에 묶이면 교체와 테스트가 어려워진다.
- `domain`에 Android/androidx 타입을 쓰지 않는다. 순수 JVM이어야 단위 테스트가 빠르다.
- `ModuleRules`나 Konsist 테스트를 약화하거나 삭제하지 않는다. 규칙이 막으면 구조를 고친다.
- `@Suppress`, detekt baseline, ktlint 비활성화로 검사를 우회하지 않는다. 이유 없이 린트가 통과한 것처럼 보이게 된다.
- ViewModel에서 Repository 구현이나 `Context`를 직접 쓰지 않는다. UseCase를 거친다.
- 비밀값(토큰, 키스토어 비밀번호, API 키)을 코드, `gradle.properties`, 커밋에 넣지 않는다. 키스토어 값은 `local.properties` 또는 환경변수로만 준다.
- 토큰 같은 민감 값을 평문 DataStore나 SharedPreferences에 저장하지 않는다. `SecureStore`를 쓴다.
- 기준 스크린샷과 `local.properties`, 키스토어 파일을 커밋하지 않는다.
- `verifyStarter`를 건너뛰고 완료를 선언하지 않는다. 일부 모듈만 돌려서는 모듈 간 규칙 위반을 놓친다.

## 7. SecureStore

`core:datastore`의 `SecureStore`는 값을 AndroidKeyStore 키로 암호화해 저장하는 key-value 저장소다.

```kotlin
interface SecureStore {
    suspend fun get(key: String): String?   // 없거나 복호화 실패면 null
    suspend fun put(key: String, value: String)
    suspend fun remove(key: String)
}
```

- `DataStoreModule`이 Hilt `SingletonComponent`에 바인딩한다. 생성자 주입으로 받는다.
- `feature`는 `core:datastore`를 쓸 수 없다. `data`의 `RepositoryImpl`에서 주입받고, `domain`의 Repository 인터페이스로 감싸 노출한다.
- 키가 사라졌거나(재설치, 백업 복원) 값이 손상되면 값을 지우고 `null`을 돌려준다. 호출부는 `null`을 "로그인 필요" 같은 재입력 상태로 처리한다.
- 모든 호출은 `suspend`다. 메인 스레드 블로킹을 하지 않는다.

## 8. release 서명

`release`는 아래 4개 값이 모두 있으면 해당 키스토어로 서명한다. 하나도 없으면 debug 키로 서명하고, `assembleRelease`/`bundleRelease`를 실행할 때 경고를 출력한다(그 빌드는 업로드하지 않는다). 일부만 있으면 빠진 키 이름을 알려 주며 빌드가 실패한다. 값은 루트 `local.properties`와 환경변수에서 읽고, 둘 다 있으면 `local.properties`가 우선한다.

| 이름 | 의미 |
|---|---|
| `STARTER_KEYSTORE_PATH` | 키스토어 파일 경로(프로젝트 루트 기준) |
| `STARTER_KEYSTORE_PASSWORD` | 키스토어 비밀번호 |
| `STARTER_KEY_ALIAS` | 키 alias |
| `STARTER_KEY_PASSWORD` | 키 비밀번호 |

## 9. sample API 경로와 base URL 바꾸기

샘플 데이터는 `sample/items` 경로에서 가져온다.

- base URL: 루트 `gradle.properties`의 `starter.baseUrl`. `core:network`가 `BuildConfig.BASE_URL`로 읽는다. 값은 `/`로 끝나야 한다. 처음에는 PRD에서 넣은 값이 들어 있다. 실제 서버 주소가 아니면 바꾼다.
- 경로: `core/network/.../SampleApi.kt`의 `@GET("sample/items")`.
- 응답 모양이 바뀌면 DTO, Room entity, `SampleRepositoryImpl`의 매핑, `SampleItem`을 순서대로 맞추고 관련 테스트를 고친다.
