package com.example.schooltexthelper.controller;

import com.example.schooltexthelper.entity.Chunk;
import com.example.schooltexthelper.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public List<Chunk> search(@RequestParam String query) {
        return searchService.search(query);
    }
}
