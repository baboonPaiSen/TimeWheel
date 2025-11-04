package com.riven.common.utils;

import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileUtils {

    public static void copyResourceToFile(String resourcePath, String targetDirectory) throws IOException {
        // 确保目标目录存在
        Path targetDirPath = Paths.get(targetDirectory);
        if (!Files.exists(targetDirPath)) {
            Files.createDirectories(targetDirPath);
        }

        // 获取资源文件
        File resourceFile = ResourceUtils.getFile("classpath:" + resourcePath);
        
        // 构造目标文件路径
        Path targetFilePath = Paths.get(targetDirectory, new File(resourcePath).getName());
        
        // 复制文件
        Files.copy(resourceFile.toPath(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);
    }
}