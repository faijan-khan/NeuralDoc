package com.faijan.neuralDoc.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;
    private LocalDateTime uploadedAt;

    // A document has many chunks
    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    private List<Chunk> chunks;

    // New Field: Stores the AI-generated JSON
    @Lob 
    @Column(length = 100000) 
    private String studyMaterial;

    // --- CONSTRUCTORS ---
    public Document() {}
    
    public Document(String filename) {
        this.filename = filename;
        this.uploadedAt = LocalDateTime.now();
    }

    // --- GETTERS & SETTERS (Crucial Part) ---
    
    public Long getId() { return id; }
    
    public String getFilename() { return filename; }
    
    public List<Chunk> getChunks() { return chunks; }
    public void setChunks(List<Chunk> chunks) { this.chunks = chunks; }

    // THIS IS THE ONE YOU WERE MISSING
    public String getStudyMaterial() { return studyMaterial; }
    public void setStudyMaterial(String studyMaterial) { this.studyMaterial = studyMaterial; }
    // ... existing fields (id, filename, chunks, studyMaterial) ...

    // NEW FIELD: Stores the Professor's Deep Dive JSON
    @Lob
    @Column(length = 100000)
    private String deepDiveMaterial; 

    // --- Add Getter & Setter ---
    public String getDeepDiveMaterial() { return deepDiveMaterial; }
    public void setDeepDiveMaterial(String deepDiveMaterial) { this.deepDiveMaterial = deepDiveMaterial; }
}