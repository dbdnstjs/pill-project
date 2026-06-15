package com.pill.platform.domain.supplement.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.pill.platform.config.OpenApiProperties;
import com.pill.platform.domain.supplement.dto.SupplementSearchResult;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class FoodSafetyApiClient {

  @Qualifier("foodSafetyRestClient")
  private final RestClient restClient;

  private final OpenApiProperties properties;

  public List<SupplementSearchResult> search(String keyword, int pageNo, int numOfRows) {
    try {
      JsonNode root =
          restClient
              .get()
              .uri(
                  uriBuilder ->
                      uriBuilder
                          .path("/getHtFoodPrmsInfoList")
                          .queryParam("serviceKey", properties.getSecretKey())
                          .queryParam("pageNo", pageNo)
                          .queryParam("numOfRows", numOfRows)
                          .queryParam("PRDT_NM", keyword)
                          .queryParam("_type", "json")
                          .build())
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

    // response.body.items.item 또는 body.items 구조 모두 처리
    JsonNode body = root.path("response").path("body");
    if (body.isMissingNode()) body = root.path("body");

    JsonNode items = body.path("items").path("item");
    if (items.isMissingNode()) items = body.path("items");

    if (items.isArray()) {
      for (JsonNode item : items) {
        results.add(toResult(item));
      }
    } else if (!items.isMissingNode() && items.isObject()) {
      results.add(toResult(items));
    }

    return results;
  }

  private SupplementSearchResult toResult(JsonNode item) {
    return new SupplementSearchResult(
        text(item, "REPORT_NO"),
        text(item, "PRDT_NM"),
        text(item, "BSSH_NM"),
        text(item, "PRDLST_MAKG_MTHD_NM"),
        text(item, "PRIMARY_FNCLTY"),
        text(item, "IFTKN_ATNT_MATR_CN"),
        text(item, "RAWMTRL_NM"));
  }

  private String text(JsonNode node, String field) {
    JsonNode value = node.path(field);
    return value.isMissingNode() || value.isNull() ? null : value.asText();
  }
}
