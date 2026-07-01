package com.pill.platform.domain.analysis.service;

import com.pill.platform.domain.analysis.client.AiServerClient;
import com.pill.platform.domain.analysis.client.AiServerClient.AiAnalyzeRequest;
import com.pill.platform.domain.analysis.client.AiServerClient.AiAnalyzeRequest.IngredientInfo;
import com.pill.platform.domain.analysis.client.AiServerClient.AiAnalyzeRequest.SupplementInfo;
import com.pill.platform.domain.analysis.dto.AnalysisRequest;
import com.pill.platform.domain.analysis.dto.AnalysisResponse;
import com.pill.platform.domain.dosage.entity.UserSupplement;
import com.pill.platform.domain.dosage.repository.UserSupplementRepository;
import com.pill.platform.domain.supplement.repository.SupplementIngredientRepository;
import com.pill.platform.domain.user.entity.User;
import com.pill.platform.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {

  private final UserRepository userRepository;
  private final UserSupplementRepository userSupplementRepository;
  private final SupplementIngredientRepository supplementIngredientRepository;
  private final AiServerClient aiServerClient;

  public AnalysisResponse analyze(String email, AnalysisRequest request) {
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    List<UserSupplement> activeSupplements =
        userSupplementRepository.findByUserIdAndIsActiveTrue(user.getId());

    if (activeSupplements.size() < 2) {
      throw new IllegalArgumentException("상호작용 분석을 위해 2개 이상의 영양제를 등록해야 합니다.");
    }

    List<SupplementInfo> supplements =
        activeSupplements.stream()
            .map(
                us -> {
                  List<IngredientInfo> ingredients =
                      supplementIngredientRepository
                          .findBySupplementId(us.getSupplement().getId())
                          .stream()
                          .map(
                              si ->
                                  new IngredientInfo(
                                      si.getIngredient().getName(), si.getAmount(), si.getUnit()))
                          .toList();
                  return new SupplementInfo(
                      us.getSupplement().getProductName(),
                      ingredients,
                      us.getSupplement().getRawMaterial(),
                      us.getSupplement().getPrimaryFunction());
                })
            .toList();

    String ageGroup = request != null ? request.ageGroup() : null;
    String gender = request != null ? request.gender() : null;

    return aiServerClient.analyze(new AiAnalyzeRequest(supplements, ageGroup, gender));
  }
}
