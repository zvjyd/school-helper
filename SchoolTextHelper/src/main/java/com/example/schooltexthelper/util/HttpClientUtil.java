package com.example.schooltexthelper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.util.Map;

public class HttpClientUtil {

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String post(String url, Map<String, Object> body, String apiKey) {

        try {
            // 1️⃣ 转 JSON
            String json = mapper.writeValueAsString(body);

            // ⭐ 打印请求（方便调试）
            System.out.println("请求URL: " + url);
            System.out.println("请求Body: " + json);

            // 2️⃣ MediaType
            MediaType mediaType = MediaType.get("application/json; charset=utf-8");

            // 3️⃣ RequestBody
            RequestBody requestBody = RequestBody.create(json, mediaType);

            // 4️⃣ 请求
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // 5️⃣ 执行
            Response response = client.newCall(request).execute();

            String result = response.body() != null ? response.body().string() : null;

            // ⭐ 打印返回（核心！！）
            System.out.println("DeepSeek返回：" + result);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
