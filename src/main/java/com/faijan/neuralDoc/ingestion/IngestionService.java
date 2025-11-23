package com.faijan.neuralDoc.ingestion;

import com.faijan.neuralDoc.model.Chunk;
import com.faijan.neuralDoc.model.Document;
import com.faijan.neuralDoc.repository.DocumentRepository;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngestionService {

    private final DocumentRepository documentRepository;
    private final AIService aiService;

    public IngestionService(DocumentRepository documentRepository, AIService aiService) {
        this.documentRepository = documentRepository;
        this.aiService = aiService;
    }

    // Helper for Controller
    public java.util.Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public String extractText(MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            AutoDetectParser parser = new AutoDetectParser();
            parser.parse(stream, handler, new Metadata());
            return handler.toString();
        } catch (Exception e) {
            throw new RuntimeException("Ingestion Failed", e);
        }
    }

    public List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) return chunks;
        String cleanText = text.replaceAll("\\s+", " ").trim();
        int chunkSize = 1000;
        int overlap = 100;

        if (cleanText.length() <= chunkSize) {
            chunks.add(cleanText);
            return chunks;
        }

        int start = 0;
        while (start < cleanText.length()) {
            int end = Math.min(cleanText.length(), start + chunkSize);
            if (end < cleanText.length()) {
                int lastSpace = cleanText.lastIndexOf(' ', end);
                if (lastSpace > start) end = lastSpace;
            }
            chunks.add(cleanText.substring(start, end).trim());
            int nextStep = end - overlap;
            start = Math.max(nextStep, start + 1);
        }
        return chunks;
    }


    // Helper method to prepare the text for AI
    private String prepareTextForAI(String fullText) {
        int MAX_AI_CHARS = 25000; // ~6000 tokens (Safe for Llama 3)
        
        if (fullText.length() <= MAX_AI_CHARS) {
            return fullText; // Send everything!
        }
        
        // If too big, use the "Bookend Strategy" (Intro + Middle + Outro)
        System.out.println("⚠️ Text too big (" + fullText.length() + " chars). Trimming for AI...");
        
        String intro = fullText.substring(0, 8000); // First ~3 pages
        String outro = fullText.substring(fullText.length() - 8000); // Last ~3 pages
        
        // Grab a middle chunk too
        int midIndex = fullText.length() / 2;
        String middle = fullText.substring(midIndex, Math.min(midIndex + 5000, fullText.length()));

        return intro + "\n\n... [SKIPPED SECTION] ...\n\n" + middle + "\n\n... [SKIPPED SECTION] ...\n\n" + outro;
    }

    // --- UPDATED PROCESS METHOD ---
    public Document processAndSave(MultipartFile file) {
        String fullText = extractText(file);
        List<String> textChunks = chunkText(fullText);
        
        Document doc = new Document(file.getOriginalFilename());
        
        // 1. GENERATE AI NOTES (Now using SMART CONTEXT)
        String aiStudyNotes = null;
        if (!fullText.isEmpty()) {
            System.out.println("🤖 Preparing full text for Groq...");
            try {
                // STEP A: Prepare the "Smart String"
                String optimizedText = prepareTextForAI(fullText);
                
                // STEP B: Send it
                aiStudyNotes = aiService.generateStudyMaterial(optimizedText);
                
                System.out.println("✅ AI Response Received");
            } catch (Exception e) {
                System.err.println("⚠️ AI Generation Failed: " + e.getMessage());
            }
        }
        
        doc.setStudyMaterial(aiStudyNotes); 
        
        List<Chunk> chunkEntities = new ArrayList<>();
        for (int i = 0; i < textChunks.size(); i++) {
            chunkEntities.add(new Chunk(textChunks.get(i), i, doc));
        }
        
        doc.setChunks(chunkEntities);
        
        return documentRepository.save(doc);
    }
    // Helper to generate Deep Dive and save it to DB
    public String generateDeepDiveAndSave(Long docId, String text) {
        Document doc = documentRepository.findById(docId).orElseThrow();
        
        // Call the new AI method
        String json = aiService.generateDeepDive(text);
        
        // Save to DB
        doc.setDeepDiveMaterial(json);
        documentRepository.save(doc);
        
        return json;
    }
}