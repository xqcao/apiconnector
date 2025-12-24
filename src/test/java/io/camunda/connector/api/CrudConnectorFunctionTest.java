package io.camunda.connector.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CrudConnectorFunctionTest {

  @Test
  public void testUrlResolutionFromFeelOnly() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    CustomApiRequest request = new CustomApiRequest();
    request.setMethod("GET");
    request.setUrl("");
    request.setUrlNode(objectMapper.readTree("\"https://jsonplaceholder.typicode.com/users/1\""));
    request.setPayload(objectMapper.createObjectNode());
    request.setHeaders("{}");

    String resolved = CrudConnectorFunction.resolveFinalUrl(request);
    assertEquals("https://jsonplaceholder.typicode.com/users/1", resolved);
  }
}
