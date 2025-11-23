package com.faijan.neuralDoc.api;

import com.faijan.neuralDoc.ingestion.IngestionService;
import com.faijan.neuralDoc.model.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ingest")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    // 1. UPLOAD (POST)
    @PostMapping
    public ResponseEntity<Map<String, Object>> ingestDocument(@RequestParam("file") MultipartFile file) {
        long startTime = System.currentTimeMillis();
        try {
            Document savedDoc = ingestionService.processAndSave(file);
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("document_id", savedDoc.getId());
            response.put("filename", savedDoc.getFilename());
            response.put("chunks_saved", savedDoc.getChunks().size());
            response.put("processing_time_ms", duration);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. RETRIEVE (GET) - This is what was missing/not loaded!
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDocument(@PathVariable Long id) {
        return ingestionService.getDocumentById(id)
                .map(doc -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("filename", doc.getFilename());
                    // This retrieves the massive JSON that took 87 seconds to generate
                    response.put("study_material", doc.getStudyMaterial()); 
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. TRIGGER DEEP DIVE (Called via JavaScript)
    @PostMapping("/deep-dive/{id}")
    public ResponseEntity<Map<String, Object>> generateDeepDive(@PathVariable Long id) {
        try {
            Document doc = ingestionService.getDocumentById(id).orElseThrow();
            
            // A. Cache Check: If we already generated it, just return it!
            if (doc.getDeepDiveMaterial() != null) {
                return ResponseEntity.ok(Map.of("data", doc.getDeepDiveMaterial()));
            }

            // B. Prepare Text (Reuse the first chunk logic for speed)
            // Note: In a real app, use prepareTextForAI here too, but first chunk is fine for questions.
            String text = doc.getChunks().isEmpty() ? "" : doc.getChunks().get(0).getTextContent();
            
            // C. Generate & Save
            String deepDiveJson = ingestionService.generateDeepDiveAndSave(id, text);
            
            return ResponseEntity.ok(Map.of("data", deepDiveJson));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}