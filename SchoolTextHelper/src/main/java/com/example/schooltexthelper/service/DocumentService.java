package com.example.schooltexthelper.service;

import com.example.schooltexthelper.entity.Document;
import com.example.schooltexthelper.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final ParseService parseService;

    public String upload(MultipartFile file) {

        try {
            // 1️⃣ 获取项目根目录（关键！）
            String basePath = System.getProperty("user.dir") + "/uploads/";

            // 2️⃣ 创建上传目录
            File dir = new File(basePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 3️⃣ 生成安全文件名（避免空格/重复）
            String fileName = System.currentTimeMillis() + "_" +
                    file.getOriginalFilename().replaceAll(" ", "_");

            // 4️⃣ 保存文件
            String filePath = basePath + fileName;
            File dest = new File(filePath);
            file.transferTo(dest);

            System.out.println("文件保存路径：" + dest.getAbsolutePath());

            // 5️⃣ 存数据库（建议存相对路径或文件名）
            Document doc = new Document();
            doc.setTitle(file.getOriginalFilename());
            doc.setFilePath(fileName);  // ⚠️ 只存文件名更好

            documentRepository.save(doc);

            // 6️⃣ 异步解析
            parseService.parseAsync(doc);

            return "上传成功";

        } catch (Exception e) {
            e.printStackTrace();
            return "上传失败：" + e.getMessage();
        }
    }
}