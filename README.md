# RunTale_BE
<img width="1164" alt="스크린샷 2024-08-06 오후 10 58 29" src="https://github.com/user-attachments/assets/aeec1a99-81f7-46aa-ac67-25699851dbc3">
🏃‍♂️이야기와 함께하는 즐거운 러닝의 시작 런테일 백엔드 레포지토리🏃‍♂

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [프로젝트 설명](#프로젝트-설명)
  1. [Backend Tech](#1-backend-tech)
  2. [Database ERD](#2-database-erd)
  3. [Architecture](#3-architecture)
- [기능 설명](#기능-설명)
  1. [회원 가입 및 로그인](#1-회원-가입-및-로그인)
  2. [홈 화면](#2-홈-화면)
  3. [시나리오 기능](#3-시나리오-기능)
  4. [러닝 기능](#4-러닝-기능)
  5. [러닝 목표 달성](#5-러닝-기록)
  6. [이달의 기록](#6-이달의-기록)
  7. [티어 시스템](#7-티어-시스템)
- [Backend Developers](#Backend-Developers)

## 프로젝트 개요

| 항목       | 내용                                          |
|------------|---------------------------------------------|
| 프로젝트 소개 | 이야기와 함께하는 즐거운 러닝의 시작 [Runtale]  |
| 개발 인원    | 5명 (PM/디자인 1명 + 프론트엔드 2명 + 백엔드 2명) |
| 개발 기간    | 2024. 07. 05 ~ 2024. 08. 06               |

## 프로젝트 설명

### 1. Backend Tech

| 기술             | 사용                             |
|------------------|--------------------------------|
| Language         | ![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white) |
| Framework        | ![Spring Boot](https://img.shields.io/badge/Spring_Boot-%236DB33F.svg?style=for-the-badge&logo=spring-boot&logoColor=white) ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white) |
| Database         | ![MySQL](https://img.shields.io/badge/MySQL-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white) |
| Deploy           | ![AWS EC2](https://img.shields.io/badge/Amazon%20EC2-%23FF9900.svg?style=for-the-badge&logo=amazon-ec2&logoColor=white) ![AWS RDS](https://img.shields.io/badge/Amazon%20RDS-527FFF?style=for-the-badge&logo=amazon-rds&logoColor=white) ![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white) |
| API              | ![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white) ![Swagger](https://img.shields.io/badge/Swagger-%23Clojure.svg?style=for-the-badge&logo=swagger&logoColor=white) |
| Cooperative Tool | ![Git](https://img.shields.io/badge/Git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white) ![GitHub](https://img.shields.io/badge/GitHub-%23121011.svg?style=for-the-badge&logo=github&logoColor=white) |
| IDE              | ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ_IDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white) |

### 2. Database ERD

![runtale_erd_1](https://github.com/user-attachments/assets/c61bdcef-aa9a-40a1-9df3-836c179d4a28)
### 3. Architecture

![_runtale 아키텍처](https://github.com/user-attachments/assets/a01bb52e-eff5-4438-9803-d0befb23c7bf)


## 기능 설명

### 1. 회원 가입 및 로그인

<img src="https://i.imgur.com/plIqBMI.png" alt loading="lazy" height="400px" />


- **회원 가입**: 아이디, 비밀번호, 닉네임으로 가입
  - 아이디 및 비밀번호 유효성 검사
  - 중복 아이디 검사
- **로그인**: 아이디와 비밀번호 입력
  - 세션 기반 로그인
  - 로그인을 하지 않으면 다른 페이지로 이동 불가
 
### 2. 홈 화면

<div style="display: flex;">
  <img src="https://i.imgur.com/pRznFXR.png" alt="티어, 랭킹, 기록" loading="lazy" height="500px" />
  <img src="https://i.imgur.com/B4fjEPZ.png" alt="튜토리얼" loading="lazy" height="500px" />
</div>

- 본인의 티어, 랭킹, 대략적인 기록 확인
- 튜토리얼을 통해 서비스 기능 파악 가능

### 3. 시나리오 기능
<div style="display: flex;">
<img src="https://i.imgur.com/zzYcD4L.png" alt loading="lazy" height="400px"/>
<img src="https://i.imgur.com/ot9iMPa.png" alt loading="lazy" height="400px"/>
<img src="https://i.imgur.com/j3QbuXj.png" alt loading="lazy" height="400px" />
</div>
- 시나리오를 선택하여 러닝을 시작
- 진행 상황에 따라 적절한 사운드 제공

### 4. 러닝 기능

<div style="display: flex;">
  <img src="https://i.imgur.com/rEo5kqK.png" alt loading="lazy" height="400px" />
  <img src="https://i.imgur.com/pfnSzbq.png" alt loading="lazy" height="400px" />
</div>

- 러닝 화면, 시나리오 화면 전환 가능
- 1km 당 달성 시 사운드 제공
- 지도에서 현재 위치와 경로 확인
- 실시간 페이스와 달린 거리 등 통계 확인

### 5. 러닝 기록

<div style="display: flex;">
  <img src="https://i.imgur.com/BAI8qUv.png" alt loading="lazy" height="400px" />
  <img src="https://i.imgur.com/O1sShHa.png" alt loading="lazy" height="400px" />
</div>

- 러닝이 끝난 후 평균 페이스, 총 달린 거리, 소요 시간 등 상세 정보 확인
- 시작하기 전에 설정한 목표 페이스 달성 여부 확인

### 6. 이달의 기록

<img src="https://i.imgur.com/YCdtBJd.png" alt loading="lazy" height="400px" />

- 한 달 간 달린 거리, 러닝 횟수, 목표 페이스 달성 횟수 등의 기록을 숫자와 그래프로 확인

### 7. 티어 시스템

<div style="display: flex;">
  <img src="https://i.imgur.com/gxYst1V.png" alt loading="lazy" height="400px" />
  <img src="https://i.imgur.com/rWVMANV.png" alt loading="lazy" height="400px" />
</div>

- 한 달마다 초기화되는 티어 시스템 도입 (0단계부터 5단계까지)
- 티어는 한 달 동안 달린 횟수, 한 달 동안 달린 거리, 한 달 평균 페이스에 가중치를 적용하여 계산

## [Backend Developers]

<table>
    <tr align="center">
        <td><B>최규원</B></td>
        <td><B>주병주</B></td>
    </tr>
    <tr align="center">
        <td>
            <img src="https://github.com/Kyuwon-Choi.png?size=100" width="100">
            <br>
            <a href="https://github.com/Kyuwon-Choi"><I>Kyuwon-Choi</I></a>
        </td>
        <td>
            <img src="https://github.com/GotoBill.png?size=100" width="100">
            <br>
            <a href="https://github.com/GotoBill"><I>GotoBill</I></a>
        </td>
    </tr>
</table>

