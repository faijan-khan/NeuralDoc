package com.faijan.neuralDoc.api;

import com.faijan.neuralDoc.model.Chunk;
import com.faijan.neuralDoc.repository.ChunkRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final ChunkRepository chunkRepository;

    public SearchController(ChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    @GetMapping
    public List<String> search(@RequestParam("query") String query) {
        System.out.println("Searching for: " + query);
        
        // 1. Fetch chunks that match the keyword
        List<Chunk> results = chunkRepository.findByTextContentContainingIgnoreCase(query);
        
        // 2. Return just the text (so it looks like a chatbot answer)
        return results.stream()
                .map(Chunk::getTextContent)
                .collect(Collectors.toList());
    }
}