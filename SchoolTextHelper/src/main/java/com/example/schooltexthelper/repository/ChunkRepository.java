package com.example.schooltexthelper.repository;

import com.example.schooltexthelper.entity.Chunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChunkRepository extends JpaRepository<Chunk, Long> {

    // 🔥 关键方法
    List<Chunk> findTop5ByOrderByIdDesc();

}
