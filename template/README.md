# 프로젝트 개요

Kotlin, Jetpack Compose, Hilt, Room 기반의 멀티 모듈 Android 프로젝트다. 모듈 구조와 개발 규칙은 [AGENTS.md](AGENTS.md)에 있다. LLM 도구로 작업한다면 먼저 읽게 한다.

## 요구 사항

- JDK 17 이상
- Android SDK (compileSdk 37). Android Studio를 쓰면 대부분 자동으로 맞춰진다.

## 빌드와 실행

```bash
./gradlew assembleDebug      # debug APK 빌드
./gradlew installDebug       # 연결된 기기나 에뮬레이터에 설치
```

Windows에서는 `gradlew.bat`을 쓴다. Android Studio에서는 프로젝트를 열고 `app` 구성을 실행한다.

서버 주소는 `gradle.properties`의 `starter.baseUrl`에서 바꾼다.

## 검증

```bash
./gradlew verifyStarter
```

모든 모듈의 빌드, 단위 테스트, 아키텍처 규칙(Konsist), detekt, ktlint, 스크린샷 렌더링을 한 번에 실행한다. PR을 올리기 전에 통과시킨다.

## release 서명

`release` 빌드는 아래 값이 모두 있을 때만 키스토어로 서명한다. 하나라도 없으면 debug 키로 서명하고 경고를 출력한다. 그 빌드는 배포하지 않는다. 값은 루트 `local.properties` 또는 환경변수로 준다.

| 이름 | 의미 |
|---|---|
| `STARTER_KEYSTORE_PATH` | 키스토어 파일 경로 |
| `STARTER_KEYSTORE_PASSWORD` | 키스토어 비밀번호 |
| `STARTER_KEY_ALIAS` | 키 alias |
| `STARTER_KEY_PASSWORD` | 키 비밀번호 |

키스토어 파일과 `local.properties`는 커밋하지 않는다.
