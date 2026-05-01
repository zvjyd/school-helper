package com.example.schooltexthelper.controller;

import com.example.schooltexthelper.dto.AskRequest;
import com.example.schooltexthelper.dto.AskResponse;
import com.example.schooltexthelper.service.QAService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/qa")
@RequiredArgsConstructor
public class QAController {

    private final QAService qaService;

    @PostMapping("/ask")
    public AskResponse ask(@RequestBody AskRequest req) {
        return qaService.ask(req.getQuestion());
    }
}