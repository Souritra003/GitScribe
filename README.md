# 🚀 GitScribe

> Turn your GitHub repositories into clean, structured documentation — automatically.

---

## 📌 Overview

**GitScribe** is a Spring Boot–based application that integrates with GitHub to fetch repository data, analyze code structure, and generate human-readable documentation. It helps developers eliminate manual documentation effort by automatically producing project summaries, tech stack insights, and structured documentation using intelligent processing and AI.

---

## 🎯 Problem Statement

Developers often skip writing proper documentation due to:

* Time constraints ⏳
* Lack of consistency 📉
* Complex codebases 🤯

👉 This leads to poor project understanding and onboarding difficulties.

---

## 💡 Solution

GitScribe solves this by:

* Fetching repository data from GitHub
* Analyzing project structure and files
* Generating structured documentation automatically
* Enhancing output using AI-based summarization

---

## ⚙️ Features

* 🔗 GitHub Repository Integration
* 📂 Automatic Code & File Structure Analysis
* 🧠 AI-powered Documentation Generation
* 📄 README / Project Summary Generation
* 📊 Tech Stack Detection
* ⚡ REST API-based Architecture

---

## 🛠 Tech Stack

### Backend

* Java 21
* Spring Boot
* Spring Web
* Spring Data JPA

### Integration

* GitHub REST API

### AI (Optional)

* Gemini API / Claude API

### Tools

* Maven
* Lombok

---

## 🏗️ System Architecture

```
Client → Controller → GitHub Service → Analyzer → AI Service → Doc Generator → Response
```

---

## 🔄 Workflow

1. User provides a GitHub repository URL
2. Application extracts repository details
3. Fetches files and metadata from GitHub API
4. Analyzes project structure and technologies
5. Sends summarized data to AI (optional)
6. Generates structured documentation

---

## 📡 API Endpoint

### Analyze Repository

```
POST /api/docpilot/analyze
```

#### Request Body

```json
{
  "repoUrl": "https://github.com/user/repository"
}
```

#### Response

```text
Project Documentation

Tech Stack: Java, Spring Boot  
Modules: Controller, Service, Repository  
Summary: This project is a backend system for managing...
```

---

## 📂 Project Structure

```
com.souritra.docpilot
 ├── controller
 ├── service
 │     ├── github
 │     ├── analyzer
 │     ├── ai
 │     ├── generator
 ├── dto
 ├── model
 └── util
```

---

## 🚀 Getting Started

### Prerequisites

* Java 21
* Maven
* Git

---

### Installation

```bash
git clone https://github.com/your-username/gitscribe.git
cd gitscribe
mvn clean install
```

---

### Run the Application

```bash
mvn spring-boot:run
```

---

## 🔐 Configuration

### Disable Database (if not used)

```properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```

---

### GitHub API (Optional Token)

```properties
github.token=your_token_here
```

---

### AI API (Optional)

```properties
ai.api.key=your_api_key
```

---

## 🌟 Future Enhancements

* 📐 UML Diagram Generation
* 📄 PDF Export
* 🔄 Multi-repo analysis
* 🔐 User Authentication
* 📊 Advanced analytics dashboard

---

## 💬 Interview Explanation

> GitScribe is a Spring Boot application that integrates with GitHub APIs to analyze repositories and generate structured documentation automatically. It uses code analysis and AI-based summarization to improve documentation quality and reduce manual effort.

---

## 🤝 Contributing

Contributions are welcome!
Feel free to fork the repo and submit pull requests.

---

## 📜 License

This project is licensed under the MIT License.

---

## 👨‍💻 Author

**Souritra Nandy**

---

## ⭐ Show Your Support

If you like this project, give it a ⭐ on GitHub!

---
