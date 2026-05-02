package com.example.schooltexthelper.service;

import com.example.schooltexthelper.entity.Document;
import com.example.schooltexthelper.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ParseService parseService;

    // ✅ 从配置文件读取上传路径（更专业）
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    public String upload(MultipartFile file) {

        try {
            // ❗ 0️⃣ 空文件校验
            if (file.isEmpty()) {
                return "上传失败：文件为空";
            }

            // 1️⃣ 获取绝对路径
            String basePath = System.getProperty("user.dir") + "/" + uploadDir + "/";

            // 2️⃣ 创建目录
            File dir = new File(basePath);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    return "上传失败：无法创建目录";
                }
            }

            // 3️⃣ 获取原始文件名
            String originalName = file.getOriginalFilename();

            // ❗ 防空
            if (originalName == null || originalName.isBlank()) {
                return "上传失败：文件名异常";
            }

            // 4️⃣ 生成安全文件名（UUID 更安全）
            String safeName = UUID.randomUUID() + "_" + originalName.replaceAll("\\s+", "_");

            // 5️⃣ 保存文件
            String fullPath = basePath + safeName;
            File dest = new File(fullPath);
            file.transferTo(dest);

            System.out.println("文件保存路径：" + dest.getAbsolutePath());

            // 6️⃣ 存数据库（只存文件名）
            Document doc = new Document();
            doc.setTitle(originalName);
            doc.setFilePath(safeName);

            documentRepository.save(doc);

            // 7️⃣ 异步解析（⚠️ 传 ID 更稳）
            parseService.parseAsync(doc);

            return "上传成功";

        } catch (Exception e) {
            e.printStackTrace();
            return "上传失败：" + e.getMessage();
        }
    }
}