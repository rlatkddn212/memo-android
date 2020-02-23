# memo-android
memo android

### 소개
단순 memo app 입니다.

### 언어, 환경
안드로이드 스튜디오, 안드로이드 sdk 29, 코틀린 언어로 작성했습니다. 

### 외부 라이브러리
이미지 라이브러리 사용(picasso)  
https://square.github.io/picasso/

### 테이블 구성
SQLite에 제목, 내용, 이미지 경로가 저장됩니다.
2개의 테이블로 구성했습니다. 
memo 테이블, memoimage 테이블입니다.

#### memo 테이블
|ID|COLUMN_NAME_TITLE|COLUMN_NAME_CONTENTS|COLUMN_NAME_THUMBNAIL|
  

#### memoimage 테이블
|ID|COLUMN_NAME_MEMO_ID|COLUMN_NAME_IMAGE_URL|
  
memo테이블에 ID(KEY)이 memoimage테이블에 COLUMN_NAME_MEMO_ID와 연결됩니다.
