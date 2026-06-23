package com.pill.platform.domain.supplement.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.pill.platform.config.OpenApiProperties;
import com.pill.platform.domain.supplement.dto.SupplementSearchResult;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class FoodSafetyApiClient {

  private final RestClient restClient;
  private final OpenApiProperties properties;

  public FoodSafetyApiClient(
      @Qualifier("foodSafetyRestClient") RestClient restClient, OpenApiProperties properties) {
    this.restClient = restClient;
    this.properties = properties;
  }

  public List<SupplementSearchResult> search(String keyword, int pageNo, int numOfRows) {
    try {
      int startIdx = (pageNo - 1) * numOfRows + 1;
      int endIdx = pageNo * numOfRows;

      JsonNode root =
          restClient
              .get()
              .uri(
                  "/{key}/C003/json/{start}/{end}/PRDLST_NM={keyword}",
                  properties.getSecretKey(),
                  startIdx,
                  endIdx,
                  keyword)
              .retrieve()
              .body(JsonNode.class);

      return parseItems(root);
    } catch (Exception e) {
      log.error("식품안전처 API 호출 실패: {}", e.getMessage());
      throw new IllegalStateException("영양제 검색 중 오류가 발생했습니다.");
    }
  }

  private List<SupplementSearchResult> parseItems(JsonNode root) {
    List<SupplementSearchResult> results = new ArrayList<>();
    if (root == null) return results;

    JsonNode rows = root.path("C003").path("row");
    if (rows.isArray()) {
      for (JsonNode item : rows) {
        results.add(toResult(item));
      }
    }

    return results;
  }

  private SupplementSearchResult toResult(JsonNode item) {
    return new SupplementSearchResult(
        text(item, "PRDLST_REPORT_NO"),
        text(item, "PRDLST_NM"),
        text(item, "BSSH_NM"),
        text(item, "SHAP"),
        text(item, "PRIMARY_FNCLTY"),
        text(item, "IFTKN_ATNT_MATR_CN"),
        text(item, "RAWMTRL_NM"));
  }

  private String text(JsonNode node, String field) {
    JsonNode value = node.path(field);
    return value.isMissingNode() || value.isNull() ? null : value.asText();
  }
}
