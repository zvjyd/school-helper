package com.example.schooltexthelper.service;

import com.example.schooltexthelper.dto.AskResponse;
import com.example.schooltexthelper.entity.Chunk;
import com.example.schooltexthelper.util.HttpClientUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class QAService {

    // ✅ 使用搜索服务（按问题检索）
    private final SearchService searchService;

    @Value("${deepseek.api-key}")
    private String apiKey;

    public AskResponse ask(String question) {

        // ❗ 0️⃣ 基础校验
        if (question == null || question.isBlank()) {
            return new AskResponse("问题不能为空", List.of());
        }

        // 1️⃣ 检索相关 chunk
        List<Chunk> chunks = searchService.search(question);

        if (chunks.isEmpty()) {
            return new AskResponse("未找到相关内容，请换个问题试试", List.of());
        }

        // 2️⃣ 构建上下文
        String context = chunks.stream()
                .map(Chunk::getContent)
                .reduce("", (a, b) -> a + "\n" + b);

        // ❗ 防止 prompt 过长（很关键）
        if (context.length() > 4000) {
            context = context.substring(0, 4000);
        }

        // 3️⃣ Prompt
        String prompt = """
你是一个校园政策问答助手，请严格根据提供的资料回答。

【资料】
%s

【问题】
%s

要求：
1. 只能使用【资料】回答
2. 不允许编造
3. 如果资料不足，说：无法从文档中找到答案
4. 回答要结构清晰
""".formatted(context, question);

        // 4️⃣ 调 AI
        String answer = callDeepSeek(prompt);

        // 5️⃣ 构建结构化 sources（🔥 给前端用）
        List<AskResponse.Source> sources = chunks.stream()
                .map(c -> new AskResponse.Source(
                        c.getContent(),
                        c.getDocId(),
                        c.getPosition()
                ))
                .toList();

        return new AskResponse(answer, sources);
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
                    apiKey
            );

            System.out.println("DeepSeek返回：" + response);

            if (response == null) {
                return "AI调用失败";
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // ⭐ 1️⃣ 处理错误
            if (root.has("error")) {
                return "AI错误：" + root.get("error").get("message").asText();
            }

            // ⭐ 2️⃣ 解析返回
            JsonNode choices = root.get("choices");

            if (choices == null || choices.isEmpty()) {
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