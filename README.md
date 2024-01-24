# Desolve Services
A mono repo holding all of Desolve's internal microservices. Although this was never used or completed, it is a project that allowed me to explore technologies and methodologies I now use daily.

**Tech Stack:**
- KTor serving all of the external web content
- gRPC on HTTP for interservice communication
- Protobuf for model building and serialization
- Mongo for user profile and build metadata storage
- Jakarta mail for email delivery
- Redis for build -> storage server mappings
- Consul for service discovery

### Development Environment
Desolve services depend on a containerized environment to run properly.

#### Code Generation
KSP must handle extension generation before some code (like the one shown below) is resolvable:

```kotlin
override fun modules() =
    listOf(
        DesolveManagerModule.module // <-- generated `module` extension
    )
```

**To properly generate the sources:**
- `./gradlew kspKotlin`: Generate koin-annotations sources through ksp.
- `./gradlew protocol-stub:build`: Generate protobuf classes to serve to gRPC.

**Dependent Services:**
- Redis server (port `6379`)
- MongoDB server (port `27017`)
- Consul server (port `8500`)
  - `docker run -d --name=dev-consul -p 8500:8500 consul`: Run a dev Consul instance. *(exposed to 0.0.0.0:8500)*

#### Building Services
To build/start, you must have all services built (`./gradlew clean build`). You may then run the following commands within the module's directory:
- `docker build -t <module>:latest .`: Build the Dockerfile
- `docker run --name <module> --volume <module> -p <modulePort>:8080 -it <module>:latest`: Run the Docker container

#### Firewall Configuration
This guide expects the user to be using Ubuntu (with UFW).
- Ensure [ufw-docker](https://github.com/chaifeng/ufw-docker) is configured.
- Run the following commands:
  - `ufw allow from 172.17.0.0/16`: Allow in/out from docker subnets.

Microservices need to communicate with each other.

**NOTE:** Artifact servers may require a volume to be specified for persistent artifact storage.
