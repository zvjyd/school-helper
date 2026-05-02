package com.example.schooltexthelper.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AskResponse {

    private String answer;

    // 👉 改成结构化
    private List<Source> sources;

    @Data
    @AllArgsConstructor
    public static class Source {
        private String content;
        private Long docId;
        private Integer position;
    }
}