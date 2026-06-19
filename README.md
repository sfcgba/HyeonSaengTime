# HyeonSaengTime

## 이번 작업: 정산 히어로 표시 정리

## 변경 내용

- 정산 모달 상단의 현생 시간 표기를 `00h 00m` 형태로 두 자리 고정했습니다.
- 히어로 시간 뒤에 깔리던 연한 원형 배경을 제거했습니다.

## 방식과 이유

- 정산 데이터 계산과 저장 구조는 바꾸지 않고 `ResultScreen`의 표시 방식만 수정했습니다.
- 시간/분 자릿수를 고정해 0분, 1분, 두 자리 분 모두 같은 폭으로 읽히게 했습니다.

## 수정 문서

- `README.md`: 이번 정산 히어로 표시 수정의 변경 방식과 이유만 남기도록 갱신했습니다.

## 테스트

```bash
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' sh gradlew test
```
