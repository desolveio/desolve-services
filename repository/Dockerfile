FROM openjdk:17
COPY ./repository.jar /
ENV DESOLVE_DOCKER=true
ENTRYPOINT ["java", "-jar", "repository.jar"]
