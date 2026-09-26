package com.docpilot.docpilot_backend.controller;

import com.docpilot.docpilot_backend.dto.AnalyzeRequest;
import com.docpilot.docpilot_backend.dto.DocResponse;
import com.docpilot.docpilot_backend.model.RepositoryAnalysis;
import com.docpilot.docpilot_backend.model.RepositorySnapshot;
import com.docpilot.docpilot_backend.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/docpilot")
public class DocController {

    private final GitHubOAuthService oauthService;
    private final GitHubService gitHubService;
    private final AnalyzerService analyzerService;
    private final TreeStructureService treeStructureService;
    private final SummaryService summaryService;
    private final AIService aiService;
    private final DocGeneratorService docGeneratorService;

    public DocController(
            GitHubOAuthService oauthService,
            GitHubService gitHubService,
            AnalyzerService analyzerService,
            TreeStructureService treeStructureService,
            SummaryService summaryService,
            AIService aiService,
            DocGeneratorService docGeneratorService) {
        this.oauthService = oauthService;
        this.gitHubService = gitHubService;
        this.analyzerService = analyzerService;
        this.treeStructureService = treeStructureService;
        this.summaryService = summaryService;
        this.aiService = aiService;
        this.docGeneratorService = docGeneratorService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<DocResponse> analyze(
            @Valid @RequestBody AnalyzeRequest request,
            HttpSession session) {

        String token = oauthService.getAccessToken(session);

        if (token == null) {
            return ResponseEntity.status(401).build();
        }

        RepositorySnapshot snapshot =
                gitHubService.fetchRepository(
                        request.getOwner(),
                        request.getRepo(),
                        token
                );

        RepositoryAnalysis analysis =
                analyzerService.analyze(snapshot);

        String tree =
                treeStructureService.buildTree(
                        snapshot.getAllPaths()
                );

        String prompt =
                summaryService.buildPrompt(
                        snapshot,
                        analysis,
                        tree
                );

        String aiOutput =
                aiService.generateDocumentation(prompt);

        String documentation =
                docGeneratorService.generateDocumentation(
                        snapshot,
                        analysis,
                        tree,
                        aiOutput
                );

        return ResponseEntity.ok(
                new DocResponse(
                        snapshot.getFullName(),
                        documentation
                )
        );
    }
}
