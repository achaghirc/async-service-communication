# Asynchronous Service Communication

This is a simple project to demostrate how to communicate between two services using asynchronous communication. The project is composed by two services: `Api service` and `Authentication service`. 
The `API Serveice` is responsible to receive an HTTP request, respond immediately with an ACK and send a message to `Authentication service`. 
The `Authentication service` is responsible to authenticate the payload received from API service and send a response to a callback URL specified in the payload.

## How to run the project

To run the project you need to have `docker` and `docker-compose` installed in your machine.

1. Clone the project
2. Run `docker-compose up` in the root folder of the project
3. The services will be available in the following ports:
    - API Service: `http://localhost:8080`
    - Authentication Service: `http://localhost:8081`
    - RabbitMQ Management (Optional, not needed to interact with the console): `http://localhost:15672` (user: `root`, password: `root`)

## How to interact with the services

The API service has only one endpoint `POST /api/v1/start-session` that receives a JSON payload with the following structure and starts the workflow:

```json
{
    "stationId": "550e8400-e29b-41d4-a716-446655440000",
    "driverId": "ValidDriver-._123456",
    "callbackUrl": "http://localhost:8080/callback-test"
}
```

The `Authentication service` will receive the payload, authenticate the driverId and send a response to the callback URL specified in the payload. The response will have the following structure:

```json
 {
    "station_id": "123e4567-e89b-12d3-a456-426614174000",
    "driver_token": "validDriverToken123",
    "status": "allowed"
}
```

## How to stop the project

To stop the project you can use the `docker-compose down` command in the root folder of the project.

## Diagram of the project

The following diagram shows the architecture of the project:

![Architecture Diagram](./media/Asynchronous%20communication.png)

## Sequence diagram

The following diagram shows the sequence of the project:

![Flow Diagram](./media/Sequence%20Diagram.png)

## How to test the project

To test the project you can use the `Postman` collection available in the `postman` folder. The collection has one request for each scenario we want to test and you can import it in your `Postman` application.

I also provide the `curl` commands to test the project:

#### 1. (Allowed Request) Start a session with an allowed driver-stationId pair

```bash
curl --location 'http://localhost:8080/api/v1/start-session' \
--header 'Content-Type: application/json' \
--data '{
    "stationId": "550e8400-e29b-41d4-a716-446655440000",
    "driverId": "ValidDriver-._123456",
    "callbackUrl": "http://localhost:8080/api/v1/callback-test"
}'
```
Expected result:

```json
{
  "status": "accepted",
  "message": "Request is being processed asynchronously. The result will be sent to the provided callback URL."
}
```
To the callback URL specified in the payload, the following response will be sent:

```json
{
  "station_id": "550e8400-e29b-41d4-a716-446655440000",
  "driver_token": "ValidDriver-._123456",
  "status": "allowed"
}
```

#### 2. (Not Allowed Request) Start a session with a not allowed driver-stationId pair

```bash
curl --location 'http://localhost:8080/api/v1/start-session' \
--header 'Content-Type: application/json' \
--data '{
    "stationId": "550e8400-e29b-41d4-a716-446655440000",
    "driverId": "ValidDriver123456-._~",
    "callbackUrl": "http://localhost:8080/api/v1/callback-test"
}'
```

To the callback URL specified in the payload, the following response will be sent:

```json
{
  "station_id": "123e4567-e89b-12d3-a456-426614174000",
  "driver_token": "ValidDriver123456-._~",
  "status": "not_allowed"
}
```

#### 3. (Invalid request) Start a session with an invalid driverId value

```bash
curl --location 'http://localhost:8080/api/v1/start-session' \
--header 'Content-Type: application/json' \
--data '{
    "stationId": "550e8400-e29b-41d4-a716-446655440101",
    "driverId": "ValidDriver123456-._~",
    "callbackUrl": "http://localhost:8080/api/v1/callback-test"
}'
```
Expected result:

```json
{
  "status": "accepted",
  "message": "Request is being processed asynchronously. The result will be sent to the provided callback URL."
}
```
To the callback URL specified in the payload, the following response will be sent:

```json
{
  "station_id": "550e8400-e29b-41d4-a716-446655440101",
  "driver_token": "ValidDriver123456-._~",
  "status": "invalid"
}
```

#### 4. (Bad URL) Start a session with a callback URL malformed

```bash
curl --location 'http://localhost:8080/api/v1/start-session' \
--header 'Content-Type: application/json' \
--data '{
    "stationId": "550e8400-e29b-41d4-a716-446655440000",
    "driverId": "ValidDriver-._123456",
    "callbackUrl": "http://localhost:8080/api/v1/callback-test-malformed"
}'
```
Expected result:

```json
{
  "error": "Invalid parameters values on body request please check."
}
```


#### 5. (Invalid stationID) Start a session invalid stationID UUID value

```bash
curl --location 'http://localhost:8080/api/v1/start-session' \
--header 'Content-Type: application/json' \
--data '{
    "stationId": "550e8400-e29b-41d4-a716-446655440000--",
    "driverId": "ValidDriver123456-._~",
    "callbackUrl": "http://localhost:8080/api/v1/callback-test"
}'
```
Expected result:
```json
{
  "error": "Invalid parameters values on body request please check."
}
```

#### 6. (Invalid DriverId Token) Start a session invalid driverId token value

```bash
curl --location 'http://localhost:8080/api/v1/start-session' \
--header 'Content-Type: application/json' \
--data '{
    "stationId": "550e8400-e29b-41d4-a716-446655440000",
    "driverId": "ValidDriver123456-._~BADTOKEN&",
    "callbackUrl": "http://localhost:8080/api/v1/callback-test"
}'
```

Expected result:
```json
{
  "error": "Invalid parameters values on body request please check."
}
```

#### 7. (Timeout) Start a stationID-driverId pair that will timeout

```bash
curl --location 'http://localhost:8080/api/v1/start-session' \
--header 'Content-Type: application/json' \
--data '{
    "stationId": "550e8400-e29b-41d4-a716-446655440001",
    "driverId": "UnknownDriver-._123456",
    "callbackUrl": "http://localhost:8080/api/v1/callback-test"
}'
```

Expected result:
```json
{
   "status": "accepted",
   "message": "Request is being processed asynchronously. The result will be sent to the provided callback URL."
}
```

To the callback URL specified in the payload, the following response will be sent:

```json
{
  "station_id": "550e8400-e29b-41d4-a716-446655440001",
  "driver_token": "UnknownDriver-._123456",
  "status": "unknown"
}
```

# Author Information

This project was created by [Amine Chaghir Chikhaoui](
https://www.linkedin.com/in/amine-chaghir-chikhaoui/).
and the source code is available on [GitHub](
https://github.com/achaghirc/async-service-communication.git
).