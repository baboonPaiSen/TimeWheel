package com.riven.common.utils;

import com.alibaba.fastjson.JSON;
import com.riven.common.dto.SubscriptionMailDTO;
import com.riven.common.entity.SubscriptionItem;
import com.riven.common.entity.SubscriptionReportItem;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HtmlTemplateProcessor {

    private final TemplateEngine templateEngine;

    public HtmlTemplateProcessor() {
        // 创建模板解析器
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");  // 模板文件存放的目录
        resolver.setSuffix(".html");       // 模板文件的后缀
        resolver.setTemplateMode("HTML"); // 设置模板模式为HTML5
        resolver.setCharacterEncoding("UTF-8"); // 设置字符编码

        // 创建模板引擎
        templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(resolver);
    }

    /**
     * 根据给定的模板名称和数据模型生成HTML内容
     * @param templateName 模板名称（不包括路径和扩展名）
     * @param model 数据模型
     * @return 填充后的HTML字符
     */
    public String processTemplate(String templateName, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);

        StringWriter writer = new StringWriter();
        templateEngine.process(templateName, context, writer);

        return writer.toString();
    }


    /**
     * 创建并填充订阅列表的数据模型
     */
    private Map<String, Object> createSubscriptionListModel() {
        List<SubscriptionItem> subscriptionList = new ArrayList<>();

        // 添加一些示例数据到列表中
        SubscriptionItem subscription1 = new SubscriptionItem();
        subscription1.setType("类型1");
        subscription1.setName("名称1");

        List<SubscriptionReportItem> reportsForSubscription1 = new ArrayList<>();
        reportsForSubscription1.add(new SubscriptionReportItem(
                "标题1", "2024-12-01", "来源1", "作者", "类别1", "摘要1", 1L));
        reportsForSubscription1.add(new SubscriptionReportItem(
                "标题2", "2024-11-30", "来源2", "作者", "类别2", "摘要2", 2L));

        subscription1.setReports(reportsForSubscription1);
        subscriptionList.add(subscription1);

        // 可以继续添加更多订阅...

        SubscriptionItem subscription2 = new SubscriptionItem();
        subscription2.setType("类型2");
        subscription2.setName("名称2");

        List<SubscriptionReportItem> reportsForSubscription2 = new ArrayList<>();
        reportsForSubscription2.add(new SubscriptionReportItem(
                "标题3", "2024-11-25", "来源3", "作者", "类别3", "摘要3", 3L));
        reportsForSubscription2.add(new SubscriptionReportItem(
                "标题4", "2024-11-20", "来源4", "作者", "类别4", "摘要4", 4L));

        subscription2.setReports(reportsForSubscription2);
        subscriptionList.add(subscription2);

        // 将列表添加到模型中
        SubscriptionMailDTO subscriptionMailDTO = new SubscriptionMailDTO();
        subscriptionMailDTO.setEmailTitle("您的个性化标题");
        subscriptionMailDTO.setTitle("整体标题");
        subscriptionMailDTO.setSubTitle("整体子标题");
        subscriptionMailDTO.setSubscriptionList(subscriptionList);
        subscriptionMailDTO.setEmailExplanation("注：系统自动发送，请勿回复。为追求更佳的阅读效果，本邮件列表为您精选至多10篇资讯详情，更多请登录Wind金融终端查看。如需退订，请进入财经资讯（RPP）模块订阅进行设置");
        Map<String,Object> model = JSON.parseObject(JSON.toJSONString(subscriptionMailDTO), Map.class);
        return model;
    }

    /**
     * 根据给定的模板名称和数据模型生成HTML内容
     * @param templateName 模板名称（不包括路径和扩展名）
     * @return 填充后的HTML字符
     */
    public String generateHtml(String templateName) {
        // 获取填充了数据的模型
        Map<String, Object> model = createSubscriptionListModel();

        // 使用 HtmlTemplateProcessor 处理模板并生成HTML
        return processTemplate(templateName, model);
    }


     /* 将生成的HTML内容保存到指定文件路径
            * @param htmlContent 要保存的HTML内容
     * @param filePath 文件路径（例如："D:/output.html"）
            */
    public void saveHtmlToFile(String htmlContent, String filePath) {
        try {
            // 确保文件路径存在
            Files.createDirectories(Paths.get(filePath).getParent());
            // 将HTML内容写入文件
            Files.write(Paths.get(filePath), htmlContent.getBytes("UTF-8"));
            System.out.println("HTML content has been successfully written to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to write HTML content to file: " + filePath);
        }
    }

    public static void main(String[] args) {
        HtmlTemplateProcessor processor = new HtmlTemplateProcessor();
        String htmlContent = processor.generateHtml("welcome");

        // 定义要保存的文件路径
        String outputPath = "D:/generated_output.html";

        // 保存生成的HTML到文件
        processor.saveHtmlToFile(htmlContent, outputPath);
    }

}
