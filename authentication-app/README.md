# Authentication app

This project is a small service inside an asynchronous system that provides the authentication of a set of stations and drivers. 
The service is built using [Ktor](https://ktor.io/), a Kotlin framework for building asynchronous servers and clients and uses RabbitMQ to communicate with other services and HTTP request to send data on callbackURls.
## Features

Here's a list of features included in this project:

| Name                                                                             | Description                                                                        |
|----------------------------------------------------------------------------------|------------------------------------------------------------------------------------ |
| [Routing](https://start.ktor.io/p/routing)                                       | Provides a structured routing DSL                                                  |
| [Static Content](https://start.ktor.io/p/static-content)                         | Serves static files from defined locations                                         |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)               | Provides automatic content conversion according to Content-Type and Accept headers |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization)           | Handles JSON serialization using kotlinx.serialization library                     |
| [ktor-server-rabbitmq](https://github.com/DamirDenis-Tudor/ktor-server-rabbitmq) | Handles JSON serialization using kotlinx.serialization library                     |
 | [koin-ktor](https://start.ktor.io/p/koin)                                        | Dependency injection using Koin library                     |
## Building & Running

### Using Dockerfile

To build and run the project using Docker, follow these steps:

1. Build the Docker image:

```bash

./gradlew buildImage
```

2. Run the Docker container:

```bash

./gradlew runDocker
```

### Commands
To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
| -------------------------------|---------------------------------------------------------------------- |
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8081
```

