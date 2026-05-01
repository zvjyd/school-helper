package com.example.schooltexthelper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "chunks")
@Data
public class Chunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 属于哪个文档
    private Long docId;

    // 文本内容（很长，所以用TEXT）
    @Column(columnDefinition = "TEXT")
    private String content;

    // 所在章节（后期可以优化）
    private String section;

    // 在文档中的顺序
    private Integer position;
}