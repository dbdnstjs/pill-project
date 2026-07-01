package com.pill.platform.domain.analysis.client;

import com.pill.platform.domain.analysis.dto.AnalysisResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiServerClient {

  private final RestClient restClient;

  public AiServerClient(@Qualifier("aiServerRestClient") RestClient restClient) {
    this.restClient = restClient;
  }

  public AnalysisResponse analyze(AiAnalyzeRequest request) {
    return restClient
        .post()
        .uri("/analyze/interactions")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(AnalysisResponse.class);
  }

  public record AiAnalyzeRequest(List<SupplementInfo> supplements, String ageGroup, String gender) {

    public record SupplementInfo(
        String name,
        List<IngredientInfo> ingredients,
        String rawMaterial,
        String primaryFunction) {}

    public record IngredientInfo(String name, Double amount, String unit) {}
  }
}
