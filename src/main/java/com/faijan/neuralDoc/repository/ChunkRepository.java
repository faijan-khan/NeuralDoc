package com.faijan.neuralDoc.repository;

import com.faijan.neuralDoc.model.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    // 1. The "Keyword Search" (Fast & Simple)
    // SQL equivalent: SELECT * FROM chunk WHERE LOWER(text_content) LIKE %query%
    List<Chunk> findByTextContentContainingIgnoreCase(String query);

    // 2. (Future MLOps Upgrade)
    // Later we will replace this with Vector Similarity Search!
}