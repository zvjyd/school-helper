package com.example.schooltexthelper.controller;
import java.util.Map;
import java.util.HashMap;
import com.example.schooltexthelper.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/doc")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public Map<String, Object> upload(@RequestParam("file") MultipartFile file) {
        String result = documentService.upload(file);

        Map<String, Object> res = new HashMap<>();
        res.put("message", result);
        return res;
    }
}