package com.pill.platform.domain.supplement.controller;

import com.pill.platform.domain.supplement.dto.SupplementResponse;
import com.pill.platform.domain.supplement.dto.SupplementSearchResult;
import com.pill.platform.domain.supplement.service.SupplementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/supplements")
@RequiredArgsConstructor
public class SupplementController {

  private final SupplementService supplementService;

  /** 식품안전처 API로 영양제 검색 */
  @GetMapping("/search")
  public ResponseEntity<List<SupplementSearchResult>> search(
      @RequestParam String keyword,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int size) {
    return ResponseEntity.ok(supplementService.search(keyword, page, size));
  }

  /** 검색 결과를 DB에 저장 */
  @PostMapping
  public ResponseEntity<SupplementResponse> save(@RequestBody SupplementSearchResult request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(supplementService.save(request));
  }

  /** DB에서 영양제 단건 조회 */
  @GetMapping("/{id}")
  public ResponseEntity<SupplementResponse> getById(@PathVariable Long id) {
    return ResponseEntity.ok(supplementService.getById(id));
  }
}
