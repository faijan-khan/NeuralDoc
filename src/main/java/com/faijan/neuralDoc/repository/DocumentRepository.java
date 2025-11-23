package com.faijan.neuralDoc.repository;

import com.faijan.neuralDoc.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    // Basic CRUD operations are built-in!
}