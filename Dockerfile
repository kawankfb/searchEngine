FROM openjdk:14-oracle

MAINTAINER Kawan FarhadiBaneh <kawan.kfb@gmail.com>

COPY target/my-javalin-1.0.0-SNAPSHOT-shaded.jar /usr/app/
WORKDIR /usr/app
EXPOSE 7000
ENTRYPOINT ["java", "-jar", "my-javalin-1.0.0-SNAPSHOT-shaded.jar"]