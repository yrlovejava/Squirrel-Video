package com.squirrel;

import com.squirrel.service.FileStorageService;
import com.squirrel.service.VideoUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
public class VideoTest {

    @Resource
    private FileStorageService fileStorageService;

    /**
     * 初始化默认头像库
     */
    @Test
    public void initImage() throws IOException {
        String filePath = "C:\\Users\\14780\\Desktop\\image";
        File file = new File(filePath);
        String prefix = "image/";
        File[] files = file.listFiles();
        for (File f : files) {
            String objectName = prefix + f.getName();
            Path path = Paths.get(f.getPath());
            byte[] bytes = Files.readAllBytes(path);
            System.out.println(fileStorageService.upload(bytes, objectName));
        }
    }
}
