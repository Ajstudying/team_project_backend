# FROM 기반으로 할 이미지
# eclipse temurin 프로젝트의 JRE 17버전
FROM eclipse-temurin:17-jre
# 컨테이너를 연결할 폴더(디렉터리)
# tmp(temporary) 임시 디렉터리
VOLUME /tmp
# 환경변수
ARG JAR_FILE=build/libs/*.jar
# 호스트 운영체제 실제 경로에서 파일을 VOLUME 경로에 복사
COPY ${JAR_FILE} app.jar
# 컽네이너가 구동될 때 실행하는 명령어
# command) java -jar aaa.jar 띄어쓰기
# main(args: Array<String>)
# -> 위에 띄어쓰기 한게 여기에 고대로 배열 매개변수로 들어가게 됨.
# 문자열 배열로
ENTRYPOINT ["java", "-jar", "/app.jar"]