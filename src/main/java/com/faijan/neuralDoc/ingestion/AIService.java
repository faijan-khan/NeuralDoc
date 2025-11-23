package com.faijan.neuralDoc.ingestion;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.List;

@Service
public class AIService {

    private final RestClient restClient;
    // NO HARDCODED KEY HERE!
    private final String GROQ_API_KEY = System.getenv("GROQ_API_KEY");

    public AIService() {
        // Safety check: If the key is missing, crash with a helpful error.
        if (GROQ_API_KEY == null || GROQ_API_KEY.isEmpty()) {
            throw new RuntimeException("🚨 Missing Environment Variable: GROQ_API_KEY");
        }
        
        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + GROQ_API_KEY)
                .build();
    }

    public String generateStudyMaterial(String textChunk) {
        String prompt = """
            Analyze this text and output ONLY valid JSON (no markdown).
            Structure:
            {
                "summary": "One sentence ELI5 summary",
                "key_concepts": ["concept 1", "concept 2"],
                "quiz_question": "A short conceptual question?",
                "quiz_answer": "The answer"
            }
            TEXT:
            """ + textChunk;

        Map<String, Object> requestBody = Map.of(
                // 🛑 CHANGED THIS LINE: Old model retired. New model is faster.
                "model", "llama-3.1-8b-instant", 
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "response_format", Map.of("type", "json_object")
        );
    
    try {
        // 1. Send Request
        Map response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

        // 2. Extract content
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String content = (String) message.get("content");

        // --- 🧹 THE CLEANUP FIX ---
        // Llama 3 loves adding "```json" at the start. We must remove it.
        if (content.contains("```json")) {
            content = content.replace("```json", "").replace("```", "");
        } else if (content.contains("```")) {
            content = content.replace("```", "");
        }
        // Trim any extra whitespace
        return content.trim(); 
        // ---------------------------

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"summary\": \"Error.\", \"key_concepts\": [], \"quiz_question\": \"N/A\", \"quiz_answer\": \"N/A\"}";
        }
    }
    // 🧠 THE NEW "PROFESSOR MODE" METHOD
    public String generateDeepDive(String textChunk) {
        String prompt = """
            You are a strict university professor creating a 'Hard Mode' exam prep guide.
            Analyze the text and output valid JSON.
            
            For each Bloom's Taxonomy level, generate a question, a MARKING CHECKLIST (keywords to include), and a DETAILED MODEL ANSWER.
            CRITICAL: The 'model_answer' must be a complete, comprehensive paragraph (3-5 sentences) that fully explains the concept, not just a quick summary.
            
            Structure:
            {
                "blooms_taxonomy": [
                    {
                        "level": "Remember", 
                        "question": "Definitional question?", 
                        "model_answer": "A detailed, multi-sentence paragraph explaining the definition, context, and importance.",
                        "checklist": ["Keyword A", "Concept B"]
                    },
                    {
                        "level": "Apply", 
                        "question": "Scenario question?", 
                        "model_answer": "A detailed walkthrough of how to apply the concept to this scenario.",
                        "checklist": ["Step 1", "Result 2"]
                    },
                    {
                        "level": "Analyze", 
                        "question": "Comparison question?", 
                        "model_answer": "A detailed comparison highlighting differences, pros/cons, and use cases.",
                        "checklist": ["Contrast A", "Contrast B"]
                    }
                ]
            }
            TEXT TO ANALYZE:
            """ + textChunk;

        Map<String, Object> requestBody = Map.of(
                "model", "llama-3.1-8b-instant",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "response_format", Map.of("type", "json_object")
        );

        try {
            Map response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) message.get("content");
            
            // Clean Markdown if present
            if (content.contains("```json")) content = content.replace("```json", "").replace("```", "");
            else if (content.contains("```")) content = content.replace("```", "");
            
            return content.trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"Deep Dive Failed\"}";
        }
    }
}