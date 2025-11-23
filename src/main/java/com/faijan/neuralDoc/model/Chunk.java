package com.faijan.neuralDoc.model;

import jakarta.persistence.*;

@Entity
public class Chunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 2000) // Allow up to 2000 chars per chunk
    private String textContent;

    private int chunkIndex;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    // Constructors
    public Chunk() {}
    public Chunk(String textContent, int chunkIndex, Document document) {
        this.textContent = textContent;
        this.chunkIndex = chunkIndex;
        this.document = document;
    }

    // Getters
    public String getTextContent() { return textContent; }
}