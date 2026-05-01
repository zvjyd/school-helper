package com.example.schooltexthelper.service;

import com.example.schooltexthelper.entity.Chunk;
import com.example.schooltexthelper.entity.Document;
import com.example.schooltexthelper.repository.ChunkRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParseService {

    private final ChunkRepository chunkRepository;

    /**
     * 异步入口（由 DocumentService 调用）
     */
    @Async
    public void parseAsync(Document doc) {
        parse(doc);
    }

    /**
     * 主解析流程
     */
    public void parse(Document doc) {

        try {
// 1️⃣ 拼接完整路径（关键修复）
            String fullPath = System.getProperty("user.dir")
                    + "/uploads/"
                    + doc.getFilePath();

            File file = new File(fullPath);

// 2️⃣ 判断文件是否存在
            if (!file.exists()) {
                System.out.println("文件不存在：" + fullPath);
                return;
            }

            System.out.println("开始解析：" + fullPath);

            String fileName = file.getName().toLowerCase();
            String text;

            // ===== 1️⃣ 判断文件类型 =====
            if (fileName.endsWith(".pdf")) {
                text = parsePDF(file);
            } else if (fileName.endsWith(".docx")) {
                text = parseWord(file);
            } else {
                System.out.println("不支持的文件类型：" + fileName);
                return;
            }

            if (text == null || text.trim().isEmpty()) {
                System.out.println("解析失败：文本为空");
                return;
            }

            // ===== 2️⃣ 文本清洗（很重要）=====
            text = cleanText(text);

            // ===== 3️⃣ 切块（核心）=====
            List<String> chunks = splitText(text, 500);

            // ===== 4️⃣ 入库 =====
            int index = 0;

            for (String c : chunks) {

                // 过滤太短的无效块
                if (c.trim().length() < 20) continue;

                Chunk chunk = new Chunk();
                chunk.setDocId(doc.getId());
                chunk.setContent(c);
                chunk.setPosition(index++);
                chunk.setSection("默认段");

                chunkRepository.save(chunk);
            }

            System.out.println("解析完成：" + doc.getTitle() + "，共 " + index + " 个 chunk");

        } catch (Exception e) {
            System.out.println("解析失败：" + doc.getTitle());
            e.printStackTrace();
        }
    }

    // ===============================
    // 📄 PDF解析（PDFBox）
    // ===============================
    private String parsePDF(File file) throws Exception {

        try (PDDocument document = PDDocument.load(file)) {

            PDFTextStripper stripper = new PDFTextStripper();

            // 可选：限制页数（避免特别大文件炸）
            // stripper.setStartPage(1);
            // stripper.setEndPage(10);

            return stripper.getText(document);
        }
    }

    // ===============================
    // 📄 Word解析（POI）
    // ===============================
    private String parseWord(File file) throws Exception {

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(fis)) {

            StringBuilder sb = new StringBuilder();

            doc.getParagraphs().forEach(p -> {
                sb.append(p.getText()).append("\n");
            });

            return sb.toString();
        }
    }

    // ===============================
    // 🧹 文本清洗（非常关键）
    // ===============================
    private String cleanText(String text) {

        // 去掉多余空格
        text = text.replaceAll("\\s+", " ");

        // 去掉奇怪符号（可选）
        text = text.replaceAll("[\\u0000-\\u001F]", "");

        return text.trim();
    }

    // ===============================
    // ✂️ 切块（核心）
    // ===============================
    private List<String> splitText(String text, int chunkSize) {

        List<String> chunks = new ArrayList<>();

        int length = text.length();

        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(i + chunkSize, length);
            chunks.add(text.substring(i, end));
        }

        return chunks;
    }
}