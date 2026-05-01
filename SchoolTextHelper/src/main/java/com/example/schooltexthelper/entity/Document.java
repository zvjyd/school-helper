package com.example.schooltexthelper.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "documents")
@Data
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 文档标题（文件名）
    private String title;

    // 文件存储路径
    private String filePath;

    // 发布单位（后期解析填充）
    private String publishUnit;

    // 发布时间（后期解析填充）
    private String publishDate;
}