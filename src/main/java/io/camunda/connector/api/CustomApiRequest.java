package io.camunda.connector.api;

import com.fasterxml.jackson.databind.JsonNode;

public class CustomApiRequest {

  private String method;
  private String url;
  private JsonNode payload;
  private String headers;
  // FEEL-driven URL can also be a JsonNode on the FEEL side if needed
  private JsonNode urlNode;

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getUrl() {
    // Prefer explicit string URL, otherwise fall back to FEEL-provided urlNode
    if (url != null && !url.isEmpty()) return url;
    if (urlNode != null && !urlNode.isNull()) return urlNode.asText();
    return null;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public JsonNode getPayload() {
    return payload;
  }

  public void setPayload(JsonNode payload) {
    this.payload = payload;
  }

  public String getHeaders() {
    return headers;
  }

  public void setHeaders(String headers) {
    this.headers = headers;
  }

  public JsonNode getUrlNode() {
    return urlNode;
  }

  public void setUrlNode(JsonNode urlNode) {
    this.urlNode = urlNode;
  }
}
