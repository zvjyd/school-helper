package com.example.schooltexthelper.service;

import com.example.schooltexthelper.entity.Chunk;
import com.example.schooltexthelper.repository.ChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ChunkRepository chunkRepository;

    public List<Chunk> search(String query) {

        List<Chunk> all = chunkRepository.findAll();

        return all.stream()
                .filter(c -> c.getContent().contains(query))
                .limit(5)
                .toList();
    }
}
