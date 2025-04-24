# Bank IVR State Machine

A demonstration of a bank IVR (Interactive Voice Response) system using Spring State Machine. This implementation showcases how to model and implement an authentication flow for a banking IVR system.

## Features

- State machine design for managing IVR call flow
- Authentication via:
  - SSN (Social Security Number)
  - Debit card number + PIN
- Menu-based navigation through banking services
- REST API for integration with IVP clients
- Log4j logging integration

## Project Structure

- `model`: State, event, and data definitions
- `config`: Spring and State Machine configuration
- `service`: Authentication and banking services
- `action`: State machine action handlers
- `api`: REST API controllers and models

## REST API Endpoints

The system provides a RESTful JSON-based API that allows the IVP client to interact with the IVR system:

- **POST /api/ivr/session**: Initializes a new IVR session
  - Returns a session ID and the initial state

- **POST /api/ivr/process**: Processes user input and advances the state machine
  - Required fields: sessionId, userInput, inputType

- **DELETE /api/ivr/session/{sessionId}**: Ends an IVR session

### API Flow

1. IVP client initiates a new session via POST to `/api/ivr/session`
2. Server responds with the initial state and a prompt for the user (authentication method)
3. IVP collects user input and sends it to `/api/ivr/process`
4. Server processes the input, transitions the state machine, and returns the next prompt
5. Steps 3-4 repeat until authentication succeeds or fails
6. If authentication succeeds, the user can navigate through the menus
7. Session ends when the user completes their tasks or on authentication failure

## Getting Started

### Prerequisites

- Java 1.8 or higher
- Maven

### Building the Project

```bash
mvn clean package
```

### Running the Application

```bash
java -jar target/ivr-state-machine-1.0-SNAPSHOT.jar
```

The REST API will be available at `http://localhost:8080/api/ivr/`

## Demo Credentials

For demonstration purposes, the following credentials are pre-configured:

- SSN Authentication:
  - SSN: `123-45-6789` (Customer ID: `CUST001`)
  - SSN: `987-65-4321` (Customer ID: `CUST002`)

- Card Authentication:
  - Card: `4111111111111111`, PIN: `1234` (Customer ID: `CUST001`)
  - Card: `5555555555554444`, PIN: `5678` (Customer ID: `CUST002`)

## State Machine Diagram

The IVR system follows this state transition flow:

```
WELCOME → AUTHENTICATION_METHOD → [SSN_PROMPT | CARD_NUMBER_PROMPT → PIN_PROMPT] 
→ VALIDATING → [AUTHENTICATED | ERROR] → MAIN_MENU → [Account Services] → END_CALL
```

## Example API Requests

### Initialize Session

```json
POST /api/ivr/session
Response:
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "currentState": "AUTHENTICATION_METHOD",
  "nextAction": "COLLECT_AUTH_METHOD",
  "promptMessage": "Please select your authentication method: 1 for SSN, 2 for Debit Card"
}
```

### Process User Input (SSN Path)

```json
POST /api/ivr/process
Request:
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "userInput": "1",
  "inputType": "AUTH_METHOD"
}

Response:
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "currentState": "SSN_PROMPT",
  "nextAction": "COLLECT_SSN",
  "promptMessage": "Please enter your Social Security Number"
}
```

```json
POST /api/ivr/process
Request:
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "userInput": "123-45-6789",
  "inputType": "SSN"
}

Response:
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "currentState": "AUTHENTICATED",
  "nextAction": "PROCEED_TO_MENU",
  "promptMessage": "You have been successfully authenticated",
  "authenticated": true
}
```

## Extending the Project

To extend this project for a real-world application:

1. Replace the hardcoded authentication with database or API calls
2. Add proper error handling and retry mechanisms
3. Implement actual banking functionality
4. Add security features and auditing
5. Connect to a real voice interface system 