FROM openjdk:17
COPY ./artifacts.jar /
ENV DESOLVE_DOCKER=true
ENTRYPOINT ["java", "-jar", "artifacts.jar"]
