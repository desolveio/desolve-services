FROM openjdk:17
COPY ./workers.jar /
ENV DESOLVE_DOCKER=true
ENTRYPOINT ["java", "-jar", "workers.jar"]
