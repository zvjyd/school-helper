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

        // 1️⃣ 分词（简单版）
        String[] keywords = query.split("");

        return all.stream()
                // 2️⃣ 计算命中分数
                .map(chunk -> {
                    int score = 0;

                    for (String k : keywords) {
                        if (chunk.getContent().contains(k)) {
                            score++;
                        }
                    }

                    return new ScoredChunk(chunk, score);
                })

                // 3️⃣ 过滤低质量
                .filter(sc -> sc.score > 0)

                // 4️⃣ 按分数排序
                .sorted((a, b) -> b.score - a.score)

                // 5️⃣ 取前5
                .limit(5)

                // 6️⃣ 取原Chunk
                .map(sc -> sc.chunk)

                .toList();
    }

    static class ScoredChunk {
        Chunk chunk;
        int score;

        public ScoredChunk(Chunk chunk, int score) {
            this.chunk = chunk;
            this.score = score;
        }
    }
}
