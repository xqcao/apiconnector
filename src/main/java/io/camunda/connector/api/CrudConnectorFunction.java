package io.camunda.connector.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@OutboundConnector(
  name = "CRUD_CONNECTOR",
  type = "io.camunda:crud-api:1",
  inputVariables = {"method", "url", "headers", "payload"}
)
public class CrudConnectorFunction implements OutboundConnectorFunction {

  private static final Logger logger = Logger.getLogger(CrudConnectorFunction.class.getName());
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final HttpClient httpClient = HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .connectTimeout(Duration.ofSeconds(30))
      .build();

  public static String resolveFinalUrl(CustomApiRequest request) {
    String url = request.getUrl();
    if (url == null || url.isEmpty()) {
      JsonNode urlNode = request.getUrlNode();
      if (urlNode != null && !urlNode.isNull()) {
        url = urlNode.asText();
      }
    }
    return url;
  }

  @Override
  public Object execute(OutboundConnectorContext context) throws Exception {
    CustomApiRequest request = context.bindVariables(CustomApiRequest.class);

    String method = request.getMethod() != null ? request.getMethod().toUpperCase() : "GET";
    String url = request.getUrl();
    // Support FEEL-driven URL by also allowing a URL node (Json) if provided
    if (url == null || url.isEmpty()) {
      JsonNode urlNode = request.getUrlNode();
      if (urlNode != null && !urlNode.isNull()) {
        url = urlNode.asText();
      }
    }
    if (url == null || url.isEmpty()) {
      throw new ConnectorException("API_ERROR", "URL is required and cannot be empty");
    }

    // Payload as JSON string
    String payloadStr = "{}";
    if (request.getPayload() != null && !request.getPayload().isNull()) {
      JsonNode payloadNode = request.getPayload();
      if (!payloadNode.isEmpty()) {
        payloadStr = objectMapper.writeValueAsString(payloadNode);
      }
    }

    // Parse headers JSON string to Map<String, String>
    Map<String, String> headersMap = new HashMap<>();
    if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
      try {
        JsonNode headersNode = objectMapper.readTree(request.getHeaders());
        headersNode.fields().forEachRemaining(entry -> {
          String key = entry.getKey();
          JsonNode valueNode = entry.getValue();
          String value = valueNode.isTextual() ? valueNode.asText() : valueNode.toString();
          headersMap.put(key, value);
        });
        logger.info("Parsed headers: " + headersMap);
      } catch (Exception e) {
        throw new ConnectorException("Invalid headers JSON: " + e.getMessage());
      }
    }

    logger.info("CRUD request: method=" + method + ", url=" + url);
    logger.info("Payload (string): " + payloadStr);
    logger.info("Headers (map): " + headersMap);

    HttpRequest.Builder rb = HttpRequest.newBuilder().uri(URI.create(url));

    HttpRequest.BodyPublisher body = null;
    if (!"GET".equals(method) && !"HEAD".equals(method) && !"DELETE".equals(method)) {
      body = HttpRequest.BodyPublishers.ofString(payloadStr);
    }

    if (!headersMap.isEmpty()) {
      for (Map.Entry<String, String> e : headersMap.entrySet()) {
        rb.header(e.getKey(), e.getValue());
      }
    }

    rb.header("Content-Type", "application/json");

    switch (method) {
      case "GET":
        rb.GET();
        break;
      case "POST":
        rb.POST(body != null ? body : HttpRequest.BodyPublishers.ofString(""));
        break;
      case "PUT":
        rb.PUT(body != null ? body : HttpRequest.BodyPublishers.ofString(""));
        break;
      case "PATCH":
        rb.method("PATCH", body != null ? body : HttpRequest.BodyPublishers.ofString(""));
        break;
      case "DELETE":
        rb.DELETE();
        break;
      case "HEAD":
        rb.method("HEAD", HttpRequest.BodyPublishers.noBody());
        break;
      default:
        throw new ConnectorException("Unsupported HTTP method: " + method);
    }

    HttpRequest httpRequest = rb.build();

    HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    int statusCode = response.statusCode();

    logger.info("Response status: " + statusCode);
    logger.info("Response body: " + response.body());

    if (statusCode >= 400) {
      throw new ConnectorException("API_ERROR", "Status: " + statusCode + " Body: " + response.body());
    }

    return response.body();
  }
}
