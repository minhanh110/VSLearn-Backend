package com.vslearn.controller;

import com.vslearn.entities.Word;
import com.vslearn.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/words")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class WordController {

    private final WordRepository wordRepository;

    @Autowired
    public WordController(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    // Lấy danh sách từ gây nhiễu ngẫu nhiên (text-only)
    @GetMapping("/random")
    public ResponseEntity<List<String>> getRandomWords(@RequestParam(defaultValue = "50") int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        List<String> words = wordRepository.findRandomDistractorWords(safeLimit);
        return ResponseEntity.ok(words);
    }

    // Tìm kiếm từ (đơn giản) theo chuỗi, trả về text-only
    @GetMapping("/search")
    public ResponseEntity<List<String>> searchWords(
            @RequestParam(name = "q", required = false) String query,
            @RequestParam(defaultValue = "50") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        // Hiện tại repo chưa có query search riêng cho Word -> dùng allActive rồi lọc đơn giản
        List<String> all = wordRepository.findAllActive().stream()
                .map(w -> w.getWord())
                .collect(Collectors.toList());
        if (query != null && !query.trim().isEmpty()) {
            String q = query.toLowerCase();
            all = all.stream().filter(s -> s != null && s.toLowerCase().contains(q)).collect(Collectors.toList());
        }
        if (all.size() > safeLimit) {
            all = all.subList(0, safeLimit);
        }
        return ResponseEntity.ok(all);
    }

    // Tạo word mới
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createWord(@RequestBody Map<String, String> request) {
        try {
            String wordText = request.get("word");
            if (wordText == null || wordText.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Từ không được để trống"
                ));
            }

            // Kiểm tra từ đã tồn tại chưa
            List<Word> existingWords = wordRepository.findAllActive().stream()
                    .filter(w -> w.getWord().equalsIgnoreCase(wordText.trim()))
                    .collect(Collectors.toList());
            
            if (!existingWords.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Từ đã tồn tại",
                    "data", Map.of(
                        "id", existingWords.get(0).getId(),
                        "word", existingWords.get(0).getWord()
                    )
                ));
            }

            // Lấy current user ID
            Long currentUserId = getCurrentUserId();

            // Tạo word mới
            Word newWord = Word.builder()
                    .word(wordText.trim())
                    .createdAt(Instant.now())
                    .createdBy(currentUserId)
                    .build();

            Word savedWord = wordRepository.save(newWord);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo từ thành công",
                "data", Map.of(
                    "id", savedWord.getId(),
                    "word", savedWord.getWord()
                )
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Có lỗi xảy ra: " + e.getMessage()
            ));
        }
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            org.springframework.security.core.userdetails.UserDetails userDetails = 
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            // Giả sử username là user ID, bạn có thể điều chỉnh logic này
            return Long.parseLong(userDetails.getUsername());
        }
        return 1L; // Default user ID nếu không lấy được
    }
} 