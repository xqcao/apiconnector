# CRUD API Connector for Camunda 8.8

## Overview

High-performance custom REST API connector for Camunda 8.8 with support for CRUD operations (GET, POST, PUT, PATCH, DELETE, HEAD). The connector uses Java 21 HttpClient for optimal performance and supports JSON payloads and headers.

## Performance Features

- **HTTP/2 Support**: Java 21 HttpClient with HTTP_2 protocol
- **Connection Pooling**: Built-in HTTP/2 connection pooling
- **High-Performance**: Modern Java 21 HttpClient with non-blocking I/O
- **Efficient JSON Handling**: Direct JSON string return, no unnecessary parsing

## Project Structure

```
custom-api-camunda88/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/io/camunda/connector/api/
│   │   │   ├── CrudConnectorFunction.java
│   │   │   └── CustomApiRequest.java
│   │   └── resources/
│   │       ├── crud-api-connector.json
│   │       └── META-INF/services/
│   │           └── io.camunda.connector.api.outbound.OutboundConnectorFunction
│   └── test/
│       └── java/io/camunda/connector/api/
│           └── CrudConnectorFunctionTest.java
└── target/
    ├── custom-api-camunda88-1.0.0.jar              (Main JAR)
    └── custom-api-camunda88-1.0.0-shaded.jar   (Fat JAR with dependencies)
```

## Building the Connector

```bash
cd custom-api-camunda88
mvn clean package
```

## Artifacts Generated

- `target/custom-api-camunda88-1.0.0.jar` - Main JAR (for Camunda Connectors Runtime)
- `target/custom-api-camunda88-1.0.0-shaded.jar` - Fat JAR with all dependencies shaded

## Adding to Camunda Modeler

1. Copy the template JSON to your Camunda Modeler's element templates directory:
   ```bash
   cp src/main/resources/crud-api-connector.json "D:\camunda-modeler-5.41.0-win-x64\resources\element-templates\crud-api-connector.json"
   ```
2. Or let the Maven ant run plugin copy it during install phase

## Adding to Camunda Connectors Runtime

### Manual Copy
```bash
# Copy to Camunda 8.8 connectors directory
cp target/custom-api-camunda88-1.0.0-shaded.jar D:\try2026demo\camunda-distributions\docker-compose\versions\camunda-8.8\myconnectors\custom-api-camunda88.jar
```

### Docker Example
```bash
docker run --rm --name=crud-connector \
  -v "$(pwd)/target/custom-api-camunda88-1.0.0-shaded.jar":/opt/app/connectors/custom-api-camunda88.jar \
  -e CAMUNDA_CLIENT_BROKER_GATEWAY-ADDRESS=host.docker.internal:26500 \
  -e CAMUNDA_CLIENT_SECURITY_PLAINTEXT=true \
  camunda/connectors-bundle:8.8.0
```

## Usage in BPMN

1. Drag a Service Task onto your process in Camunda Modeler
2. Select the "CRUD API Connector" template
3. Configure the HTTP Method, URL, Payload, and Headers (all support FEEL expressions)
4. Map the result variable to store the API response

### FEEL Expression Support

All input fields support FEEL expressions, allowing dynamic configuration based on process variables, business data, and conditions. This enables powerful patterns like:

- Dynamic API endpoints based on environment variables
- Conditional request payloads based on process state
- Authentication headers using process variables
- Retry logic based on response codes

## FEEL Support

All connector properties support FEEL expressions for dynamic value evaluation:

| Property | Description | Required | FEEL Support |
|----------|-------------|----------|-------------|
| HTTP Method | GET, POST, PUT, PATCH, DELETE, HEAD | Yes | ✅ Optional |
| API URL | REST endpoint URL | Yes | ✅ Optional |
| Request Payload | JSON body for POST/PUT/PATCH/DELETE | No | ✅ Optional |
| HTTP Headers | Custom headers as JSON | No | ✅ Optional |
| Retries | Number of retries on failure | No | ✅ Optional |

### FEEL Examples

**Dynamic URL from process variable:**
```
= "https://" + apiDomain + "/api/v1/users"
```

**Dynamic headers with authentication:**
```
= {"Authorization": "Bearer " + accessToken, "X-Tenant": tenantId}
```

**Conditional payload:**
```
= if condition then {"status": "active"} else {"status": "inactive"}
```

**Dynamic HTTP method:**
```
= if operationType = "create" then "POST" else if operationType = "update" then "PUT" else "GET"
```

## Template JSON

The element template is located at:
- `src/main/resources/crud-api-connector.json`

Template ID: `io.camunda.crud-api:1`

## Connector Type

- **Type**: `io.camunda:crud-api:1`
- **Input Variables**: method, url, headers, payload
- **Output**: JSON string (API response)

## Java Version

This connector requires **Java 21** to use the modern `java.net.http.HttpClient` with HTTP/2 support.

test payload:
{
  "userDetails":{
    "one":{
      "class":"input task class","email":"xxx@gmail.com","id":"4"
    }
  }
  
}

get test:
"https://jsonplaceholder.typicode.com/users/"+userDetails.one.id
 
 or:
 "https://jsonplaceholder.typicode.com/users/"+string(3)


post:
"https://jsonplaceholder.typicode.com/users/"
payload:
{
     "id":userDetails.one.id,
  "class":"post payload test",
  "elmail":userDetails.one.email
}