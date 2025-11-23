# 🧠 NeuralDoc - AI-Powered Adaptive Study Assistant

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Groq](https://img.shields.io/badge/AI-Groq_LPU-f55036?style=for-the-badge)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)

> **"The instant Study Guide"**  A high-performance RAG engine designed for high-speed ingestion and deep concept analysis.

<img src="https://github.com/faijan-khan/NeuralDoc/blob/main/NeuralDoc.gif" width="100%" alt="NeuralDoc Demo">

## 📖 Overview
NeuralDoc is an AI-powered document analysis engine built to solve the **"Last Minute Exam Prep"** problem. Unlike standard RAG applications that take 10-20 seconds to process a PDF, NeuralDoc utilizes a **Hybrid Architecture** to ingest, chunk, and analyze documents in **under 1 second**.

It features a **Progressive Loading UI** that delivers an instant Executive Summary (80/20 Rule) while asynchronously generating a "Professor's Deep Dive" (Bloom's Taxonomy Questions & Grading Checklists) in the background.

<img src="https://github.com/faijan-khan/NeuralDoc/blob/main/image.png" width="100%" alt="NeuralDoc Demo">

## 🚀 Key Engineering Features

### ⚡ Ultra-Low Latency Ingestion ("The Zapp")
* **Pipeline:** Integrated **Apache Tika** for stream-based text extraction with a custom **Sliding Window Chunking** algorithm.
* **Inference:** Leverages **Groq LPU (Language Processing Units)** running `Llama-3.1-8b` to achieve inference speeds of **~500 tokens/second**, making the analysis feel instantaneous.

### 🌊 Progressive Two-Phase Loading
* **Phase 1 (Sync):** Delivers the Executive Summary and Key Concepts immediately (<600ms).
* **Phase 2 (Async):** A background thread prompts the LLM to generate complex "Analyze/Apply" level questions and Model Answers, seamlessly injecting them into the UI when ready.

### 🧠 Smart Context Construction
* Overcomes LLM context window limits by implementing a **Bookend Strategy**:
    * If PDF < 25k chars: Ingests full context.
    * If PDF > 25k chars: Algorithmic concatenation of `Introduction` + `Key Middle Chunks` + `Conclusion` to ensure "Results" and "Definitions" are both captured.

### 🎓 "Professor Mode" Prompt Engineering
* Moved beyond simple summarization. The system is prompted to act as a strict university grader, outputting **Marking Checklists** (keywords required for full marks) alongside comprehensive Model Answers.

## 🛠️ Tech Stack

| Component | Technology | Purpose |
| :--- | :--- | :--- |
| **Backend** | Java 17, Spring Boot 3 | REST API, Async Processing, Business Logic |
| **AI Engine** | Groq Cloud API | Llama 3.1 Inference (LPU Accelerated) |
| **ETL** | Apache Tika | Unstructured Data Extraction (PDF/DOCX) |
| **Database** | H2 (File Mode) | Persistent storage for Documents & Chunks |
| **Frontend** | Thymeleaf, Bootstrap 5 | Server-side rendering with dynamic JS injection |
| **DevOps** | Docker | Containerization for cloud deployment |

## 🔧 How to Run Locally

### Prerequisites
* Java 17+
* [Optional] Docker Desktop
* A free API Key from [Groq Console](https://console.groq.com)

### Option 1: Run with Maven (Easiest)
1.  **Clone the repository**
    ```bash
    git clone [https://github.com/yourusername/neuraldoc.git](https://github.com/yourusername/neuraldoc.git)
    cd neuraldoc
    ```
2.  **Set your API Key**
    * *Mac/Linux:* `export GROQ_API_KEY=gsk_your_actual_key_here`
    * *Windows (CMD):* `set GROQ_API_KEY=gsk_your_actual_key_here`
3.  **Run the App**
    ```bash
    ./mvnw spring-boot:run
    ```
4.  **Open Browser**
    Go to `http://localhost:8080`

### Option 2: Run with Docker
```bash
# Build the image
docker build -t neuraldoc .

# Run container (Passing API Key)
docker run -p 8080:8080 -e GROQ_API_KEY=gsk_your_actual_key_here neuraldoc
