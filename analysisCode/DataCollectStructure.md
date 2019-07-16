# 데이터 수집 구조 및 파일 설명

![데이터 수집 구조](./ReadMeImage/multiSensing-Structure.png)

> 스마트폰의 경우,  `SMARTPHONE SENSING` 버튼을 누르면 사용자의 위치 정보 및 움직임 정보를 수집하며, 수집 상황을 서버로 전송합니다. `FILE TRANSFER`를 누르면 서버의 데이터 베이스에 수집 결과를 저장합니다.

![서버 구조 및 역할](./ReadMeImage/Server-Structure.png)

> `Nginx`, [nodeJs](https://github.com/YouJuwon/biometric-data-acquisition-framework/tree/master/analysisCode/nodeJs/app) 의 주요 역할은 위와 같으며, 이 외에도 DB에서 파싱하여 RDB로 삽입하는 `suggsetBot_DB_processing-master(Spring)`, 생체 데이터를 통해 통계 분석하는 `r-suggestBot(R)`가 있습니다. (추후에 수정)
