## 📌 Summary (필수)
<!-- 이 PR에서 무엇을 왜 변경했는지 간략히 설명해주세요 -->


---

## 🧩 변경 범위 (필수)
<!-- 해당하는 항목에 모두 체크해주세요 -->
- [ ] `template/` (모든 생성물에 들어가는 코드)
- [ ] `variants/navigation/` (nav2 / nav3)
- [ ] `variants/ci/` (생성물의 GitHub Actions)
- [ ] `scripts/` (generate.py / verify.py / starter_prd.py)
- [ ] 문서 (`AGENTS.md`, `PRD.template.md`, `README.md` 등)
- [ ] 이 레포의 CI (`.github/`)

---

## ✅ Checklist (필수)
- [ ] 셀프 코드 리뷰 완료
- [ ] `python3 -m unittest discover -s scripts/tests` 통과
- [ ] 템플릿·변형을 바꿨다면 생성 후 `python3 scripts/verify.py <outputDir>` 통과 (`verify: ok`)
- [ ] nav2 / nav3 양쪽에 영향이 있는지 확인
- [ ] PRD 키·종료 코드·오류 메시지를 바꿨다면 `AGENTS.md`와 `PRD.template.md`에 반영
- [ ] 머지 대상 브랜치 확인 (`develop` / `main` / 기타)

---

## 🔗 Related Issue (선택)
Closes #이슈번호

---

## 💬 Notes for Reviewer (선택)
<!-- 리뷰어가 특히 집중해서 봐야 할 부분이나 참고사항 -->


---

## 🧪 How to Test (선택)
<!-- 리뷰어가 재현할 수 있도록 PRD 값과 명령을 적어주세요 -->
1. `python3 scripts/generate.py --prd <PRD 경로>`
2. `python3 scripts/verify.py <outputDir>`
3.
