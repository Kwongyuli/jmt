package com.example.jmt.service;

import com.example.jmt.repository.FileInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileInfoService {
    private final FileInfoRepository fileInfoRepository;

    public void deleteFile(Integer fileId) {
        fileInfoRepository.deleteById(fileId);
    }
}