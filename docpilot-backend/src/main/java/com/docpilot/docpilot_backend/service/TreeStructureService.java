package com.docpilot.docpilot_backend.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TreeStructureService {

    public Map<String, Object> buildTree(List<Map<String, Object>> files) {

        Map<String, Object> root = new HashMap<>();

        for (Map<String, Object> file : files) {

            String path = (String) file.get("path");
            if (path == null) continue;

            String[] parts = path.split("/");
            Map<String, Object> current = root;

            for (int i = 0; i < parts.length; i++) {

                String part = parts[i];

                if (i == parts.length - 1) {
                    // file
                    current.putIfAbsent("files", new ArrayList<String>());
                    ((List<String>) current.get("files")).add(part);
                } else {
                    // directory
                    current.putIfAbsent(part, new HashMap<String, Object>());
                    current = (Map<String, Object>) current.get(part);
                }
            }
        }

        return root;
    }
}