package com.lt.questnest.service.Impl;

import com.lt.questnest.controller.UserController;
import com.lt.questnest.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final String UPLOAD_DIR = "E:/code/zhangJava/files/";

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * 通用的文件上传方法
     * @param file 上传的文件
     * @param fileType 文件类型 (image, video)
     * @return 包含上传结果的Map
     */
    public Map<String, String> uploadFile(MultipartFile file, String fileType) {

        logger.info("进入fileService的uploadFile方法");

        Map<String, String> result = new HashMap<>();

        // 判断文件是否为空
        if (file == null || file.isEmpty()) {
            result.put("status", "error");
            result.put("msg", "文件为空");
            return result;
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();// 获取图片名称
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));// 获取图片后缀
        String newFilename = UUID.randomUUID() + fileExtension;// 使用UUID+图片后缀形成新的文件名

        // 根据文件类型，决定上传路径
        String fileSubDir = fileType.equals("image") ? "images/" : "videos/";
        File destFile = new File(UPLOAD_DIR + fileSubDir + newFilename);

        try {
            // 确保目录存在
            File uploadDir = new File(UPLOAD_DIR + fileSubDir);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();  // 创建目录
            }

            // 保存文件
            file.transferTo(destFile);
        } catch (IOException e) {
            result.put("status", "error");
            result.put("msg", "文件保存失败：" + e.getMessage());
            return result;
        }

        // 构造文件的URL路径
        String fileUrl = "/" + fileSubDir + newFilename;
        result.put("status", "success");
        result.put("fileUrl", fileUrl);

        return result;
    }

    /**
     * 处理上传的图片、视频，并替换content中的占位符：放在utils还是fileService里面
     */
    public String processContentAndMedia(String content, List<MultipartFile> images, List<MultipartFile> videos) {

        // 处理图片文件
        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                MultipartFile image = images.get(i);
                Map<String, String> imageResult = uploadFile(image, "image");
                if (imageResult.containsValue("success")) {
                    String imageUrl = imageResult.get("fileUrl");
                    // 替换 {image1}, {image2} 占位符为图片 URL
                    content = content.replace("{image" + (i + 1) + "}", "![图片](" + imageUrl + ")");
                }
            }
        }

        // 处理视频文件
        if (videos != null && !videos.isEmpty()) {
            for (int i = 0; i < videos.size(); i++) {
                MultipartFile video = videos.get(i);
                Map<String, String> videoResult = uploadFile(video, "video");
                if (videoResult.containsValue("success")) {
                    String videoUrl = videoResult.get("fileUrl");
                    // 替换 {video1}, {video2} 占位符为视频 URL
                    content = content.replace("{video" + (i + 1) + "}", "[视频](" + videoUrl + ")");
                }
            }
        }

        return content;  // 返回处理过的内容
    }

}
