FROM eclipse-temurin:11-jre
COPY target/Tg-Bot-jar-with-dependencies.jar /usr/app/Tg-Bot-jar-with-dependencies.jar
WORKDIR /usr/app
VOLUME /Comic
VOLUME /Novel
VOLUME /config
ENV CONFIG="/config"
ENV TZ="Asia/Shanghai"
EXPOSE 13891
CMD ["java", "-jar", "Tg-Bot-jar-with-dependencies.jar"]
