package com.riven.common.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TextTemplateService {

    public String process(String templateName, Map<String, Object> variables) {
        // 这里应该实现模板处理逻辑
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h1>邮件发送失败通知</h1>");
        sb.append("<p>以下是错误详情:</p>");
        sb.append("<ul>");
        if (variables.containsKey("result")) {
            for (Object item : (Iterable<?>) variables.get("result")) {
                sb.append("<li>").append(item.toString()).append("</li>");
            }
        }
        sb.append("</ul>");
        sb.append("</body></html>");
        return sb.toString();
    }
}