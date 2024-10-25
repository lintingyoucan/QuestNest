package com.lt.questnest.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public interface FileService {
    Map<String, String> uploadFile(MultipartFile file, String fileType);

    String processContentAndMedia(String content, List<MultipartFile> images, List<MultipartFile> videos);
}
