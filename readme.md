# 스프링부트 게시판

* 2022-02-16기준 제작중
   * https://ec2-13-124-106-98.ap-northeast-2.compute.amazonaws.com:8443/
* 다양한 기능을 부여하는 게시판 제작하기

## 사용중 기능
* mapstruct를 사용한 Entity-DTO간 매핑
* spring security의 OAuth2를 사용하여 소셜 로그인 기능 부여
    * 2022-02-16기준 구글, 카카오, 네이버 로그인 기능 부여 완료
      * ec2 서버에서 구글, 카카오, 네이버 로그인 가능. 
* spring security기능 사용하여 권한과 로그인 여부에 따른 기능 차등 부여
* https적용 -> 8443포트 
* AWS를 통한 서버 배포 기능 
---
## ISSUE
* bindingResult.haserror를 사용하여 글자수를 check하는 기능이 aws상에서 적용되지 않는 이슈 있음.
* 좋아요 기능의 경우 쿼리 호출이 너무 많음.
    * 타인의 좋아요를 취소하지 못하도록 하기 위해 쿼리 호출을 여러 번 진행하였는데, 더 좋은 방법이 생각나면 변경 예정
* 댓글 기능 구현 완료. 좋아요와 마찬가지로 최적화 필요
  * 백엔드 기능이 끝난 후 UI변경 예정.
---
## 예정
* OAuth2 + JWT구현 예정
* 일반 로그인 기능 구현 예정
* 사진 등록 및 삭제 구현 예정
* 2022-02-12기준 일부 데이터 RestApi구현. 이후 전체 적용 예정
