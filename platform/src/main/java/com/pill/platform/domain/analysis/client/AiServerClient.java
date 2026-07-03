package com.pill.platform.domain.analysis.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pill.platform.domain.analysis.dto.AnalysisResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class AiServerClient {

  private final RestClient restClient;
  private final String apiKey;
  private final ObjectMapper objectMapper;

  public AiServerClient(
      @Qualifier("geminiRestClient") RestClient restClient,
      @Value("${gemini.api.key}") String apiKey,
      ObjectMapper objectMapper) {
    this.restClient = restClient;
    this.apiKey = apiKey;
    this.objectMapper = objectMapper;
  }

  public AnalysisResponse analyze(AiAnalyzeRequest analyzeRequest) {
    String prompt = buildPrompt(analyzeRequest);

    GeminiApiRequest request = new GeminiApiRequest(
        List.of(new GeminiApiRequest.Content(List.of(new GeminiApiRequest.Part(prompt)))),
        new GeminiApiRequest.GenerationConfig("application/json")
    );

    GeminiApiResponse response = restClient
        .post()
        .uri("/gemini-2.5-flash:generateContent?key={key}", apiKey)
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(GeminiApiResponse.class);

    if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
      throw new RuntimeException("Gemini 응답이 비어 있습니다.");
    }

    String text = response.candidates().get(0).content().parts().get(0).text();
    text = stripMarkdown(text);

    try {
      return objectMapper.readValue(text, AnalysisResponse.class);
    } catch (JsonProcessingException e) {
      log.error("Gemini 응답 파싱 실패: {}", text);
      throw new RuntimeException("AI 응답 파싱에 실패했습니다.", e);
    }
  }

  private String stripMarkdown(String text) {
    text = text.strip();
    if (text.startsWith("```")) {
      int firstNewline = text.indexOf('\n');
      int lastFence = text.lastIndexOf("```");
      if (firstNewline > 0 && lastFence > firstNewline) {
        text = text.substring(firstNewline + 1, lastFence).strip();
      }
    }
    return text;
  }

  private String buildPrompt(AiAnalyzeRequest request) {
    StringBuilder sb = new StringBuilder();
    for (AiAnalyzeRequest.SupplementInfo s : request.supplements()) {
      String ingredients;
      if (s.ingredients() != null && !s.ingredients().isEmpty()) {
        ingredients = s.ingredients().stream()
            .map(i -> i.amount() != null ? i.name() + " " + i.amount() + i.unit() : i.name())
            .collect(Collectors.joining(", "));
      } else if (s.rawMaterial() != null) {
        ingredients = s.rawMaterial();
      } else {
        ingredients = "성분 정보 없음";
      }
      sb.append("- ").append(s.name()).append(": ").append(ingredients);
      if (s.primaryFunction() != null) {
        sb.append(" (기능: ").append(s.primaryFunction()).append(")");
      }
      sb.append("\n");
    }

    List<String> userParts = new ArrayList<>();
    if (request.ageGroup() != null) userParts.add("연령대: " + request.ageGroup());
    if (request.gender() != null) userParts.add("성별: " + request.gender());
    String userInfo = userParts.isEmpty() ? "정보 없음" : String.join(", ", userParts);

    return "당신은 영양제 상호작용 전문가입니다. 다음 영양제들의 상호작용을 분석해주세요.\n\n"
        + "복용 중인 영양제:\n" + sb
        + "사용자 정보: " + userInfo + "\n\n"
        + "다음 JSON 형식으로만 응답해주세요:\n"
        + "{\n"
        + "  \"interactions\": [\n"
        + "    {\n"
        + "      \"ingredient1\": \"영양소1\",\n"
        + "      \"ingredient2\": \"영양소2\",\n"
        + "      \"status\": \"SYNERGY | CAUTION | AVOID\",\n"
        + "      \"description\": \"상호작용 설명 (한국어, 1-2문장)\"\n"
        + "    }\n"
        + "  ],\n"
        + "  \"summary\": \"전체 복용 패턴에 대한 종합 평가 (한국어, 2-3문장)\",\n"
        + "  \"overallRisk\": \"LOW | MEDIUM | HIGH\"\n"
        + "}\n\n"
        + "status 기준:\n"
        + "- SYNERGY: 함께 복용하면 효과가 증가하는 조합\n"
        + "- CAUTION: 주의가 필요하지만 복용 가능한 조합\n"
        + "- AVOID: 함께 복용을 피해야 하는 조합\n\n"
        + "상호작용이 없으면 interactions는 빈 배열로 반환하세요.\n"
        + "반드시 유효한 JSON만 반환하고 다른 텍스트는 포함하지 마세요.";
  }

  public record AiAnalyzeRequest(List<SupplementInfo> supplements, String ageGroup, String gender) {

    public record SupplementInfo(
        String name,
        List<IngredientInfo> ingredients,
        String rawMaterial,
        String primaryFunction) {}

    public record IngredientInfo(String name, Double amount, String unit) {}
  }

  record GeminiApiRequest(List<Content> contents, GenerationConfig generationConfig) {

    record Content(List<Part> parts) {}

    record Part(String text) {}

    record GenerationConfig(String responseMimeType) {}
  }

  record GeminiApiResponse(List<Candidate> candidates) {

    record Candidate(Content content) {}

    record Content(List<Part> parts) {}

    record Part(String text) {}
  }
}
