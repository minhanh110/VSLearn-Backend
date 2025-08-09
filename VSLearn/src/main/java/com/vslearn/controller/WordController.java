package com.vslearn.controller;

import com.vslearn.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
} 