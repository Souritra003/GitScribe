package com.docpilot.docpilot_backend.dto;

import lombok.Getter;

@Getter
public class DocResponse {
    private String documentation;

    public DocResponse(String documentation) {
        this.documentation = documentation;
    }

}
