package com.example.schooltexthelper.service;

import com.example.schooltexthelper.repository.ChunkRepository;
import com.example.schooltexthelper.dto.AskResponse;
import com.example.schooltexthelper.entity.Chunk;
import com.example.schooltexthelper.util.HttpClientUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QAService {

    private final ChunkRepository chunkRepository;

    // 🔥 换成你的 DeepSeek Key
    private static final String API_KEY = "sk-5d8b371dc5ea4634a1c78cd05bcc66f5";

    public AskResponse ask(String question) {

        // 1️⃣ 检索
        List<Chunk> chunks = chunkRepository.findTop5ByOrderByIdDesc();

        List<String> contextList = chunks.stream()
                .map(Chunk::getContent)
                .toList();

        String context = String.join("\n", contextList);

        // 2️⃣ Prompt（很关键）
        String prompt = """
你是一个校园政策问答助手，请严格根据提供的资料回答。

【资料】
""" + context + """

【问题】
""" + question + """

要求：
1. 必须基于资料回答
2. 不允许编造
3. 如果资料不足，说：无法从文档中找到答案
4. 回答要结构清晰
""";

        // 3️⃣ 调用 DeepSeek
        String answer = callDeepSeek(prompt);

        return new AskResponse(answer, contextList);
    }

    private String callDeepSeek(String prompt) {

        try {
            Map<String, Object> body = new HashMap<>();

            body.put("model", "deepseek-chat");

            body.put("messages", List.of(
                    Map.of("role", "user", "content", prompt)
            ));

            String response = HttpClientUtil.post(
                    "https://api.deepseek.com/v1/chat/completions",
                    body,
                    API_KEY
            );

            System.out.println("DeepSeek返回：" + response);

            if (response == null) {
                return "AI调用失败";
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // ⭐ 1️⃣ 先处理错误（关键！）
            if (root.has("error")) {
                String msg = root.get("error").get("message").asText();
                return "AI错误：" + msg;
            }

            // ⭐ 2️⃣ 再取正常结果
            JsonNode choices = root.get("choices");

            if (choices == null || choices.size() == 0) {
                return "AI返回异常";
            }

            return choices
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

        } catch (Exception e) {
            e.printStackTrace();
            return "AI调用异常";
        }
    }
}