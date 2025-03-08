# GitHub Repositories API

## Project Description

A REST API application that allows retrieving public GitHub repositories of a user that are not forks. For each repository, information about its name, owner, and a list of branches with the latest commits are returned.

## Technologies

- **Spring Boot** – Framework for the backend application
- **Spring WebFlux** – Reactive approach for request processing
- **WebClient** – HTTP client for communication with the GitHub API
- **Mutiny** – Reactive programming library for handling asynchronous operations
- **JUnit 5** – Testing framework
- **WireMock** – Mocking responses from the GitHub API

## Requirements

- Java 21+
- Maven

## Running the Application

To run the application locally, use the following command:

```
mvn spring-boot:run
```

The application will be available at: `http://localhost:8080`

## Endpoints

### Get User Repositories

**Endpoint:**
```
GET /repos?userId={userId}
```

**Example Response:**
```json
[
  {
    "repositoryName": "example-repo",
    "ownerLogin": "example-user",
    "branches": [
      {
        "name": "main",
        "lastCommitSha": "abc123"
      }
    ]
  }
]
```

**Error Handling:**
If the user does not exist, the API returns a 404 code with the response:
```json
{
  "status": 404,
  "message": "User does not exist"
}
```

## Tests

To run the integration tests, use:

```
mvn test
```

The tests check the correctness of the returned data and error handling.

## License

Project for recruitment purposes, no specified license.

