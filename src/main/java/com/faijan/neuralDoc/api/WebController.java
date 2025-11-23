package com.faijan.neuralDoc.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.faijan.neuralDoc.ingestion.IngestionService;
import com.faijan.neuralDoc.model.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Controller // Note: NOT @RestController. This returns HTML.
public class WebController {

    private final IngestionService ingestionService;
    private final ObjectMapper objectMapper; // To parse the JSON string

    public WebController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
        this.objectMapper = new ObjectMapper();
    }

    // 1. Show the Home Page (Upload Box)
    @GetMapping("/")
    public String home() {
        return "dashboard"; // Renders dashboard.html
    }

    // 2. Handle Upload & Show Results (The "Zapp")
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // A. Run the Pipeline (takes ~0.6s with Groq)
            Document doc = ingestionService.processAndSave(file);
            
            // B. Parse the JSON Study Material into a Java Map
            // The DB has a String "{\"summary\":...}", we need a real Map object for HTML
            String jsonString = doc.getStudyMaterial();

            // 🔍 DEBUG PRINT: Show exactly what is in the DB
            System.out.println("DEBUG: JSON from DB is: " + jsonString);
            
            Map<String, Object> studyData = Map.of(); // Default empty
            
            if (jsonString != null && !jsonString.isEmpty()) {
                // Convert JSON String -> Java Map
                studyData = objectMapper.readValue(jsonString, Map.class);
            }

            // C. Pass data to the HTML view
            model.addAttribute("filename", doc.getFilename());
            model.addAttribute("study", studyData); // Accessible as ${study.summary} etc.
            model.addAttribute("docId", doc.getId());
            
            return "dashboard"; // Refresh page with data

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error processing file: " + e.getMessage());
            return "dashboard";
        }
    }
}