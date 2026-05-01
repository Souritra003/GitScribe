package com.docpilot.docpilot_backend.controller;

import com.docpilot.docpilot_backend.dto.RepoRequest;
import com.docpilot.docpilot_backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docpilot")
public class DocController {

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private AnalyzerService analyzerService;

    @Autowired
    private TreeStructureService treeService;

    @Autowired
    private SummaryService summaryService;

    @Autowired
    private AIService aiService;

    @Autowired
    private DocGeneratorService docService;

    @PostMapping(value = "/analyze", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> analyzeRepo(@RequestBody RepoRequest request) {

        // 1️⃣ Extract owner & repo
        String[] repo = gitHubService.extractRepoDetails(request.getRepoUrl());

        // 2️⃣ Fetch all files
        List<Map<String, Object>> files =
                gitHubService.fetchAllFiles(repo[0], repo[1], "");

        // 3️⃣ Analyze
        Map<String, Object> analysis = analyzerService.analyze(files);

        // 4️⃣ Build Tree 🌳
        Map<String, Object> tree = treeService.buildTree(files);

        // 5️⃣ Build Summary ✨
        String summary = summaryService.buildSummary(repo[1], analysis, tree);

        // 6️⃣ Call AI 🤖
        String aiDoc = aiService.generateDocumentation(summary);

        // 7️⃣ Final Doc (AI + fallback)
        String finalDoc = docService.generateDoc(repo[1], analysis, tree, aiDoc);

        return ResponseEntity.ok(finalDoc);
    }
}