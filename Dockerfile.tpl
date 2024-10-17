FROM eclipse-temurin:11-jre
COPY target/TgBot-jar-with-dependencies.jar /usr/app/TgBot-jar-with-dependencies.jar
WORKDIR /usr/app
VOLUME /Comic
VOLUME /Novel
VOLUME /config
ENV CONFIG="/config"
ENV TZ="Asia/Shanghai"
EXPOSE 13891
CMD ["java", "-jar", "TgBot-jar-with-dependencies.jar"]
