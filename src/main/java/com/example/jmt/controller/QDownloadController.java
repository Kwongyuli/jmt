package com.example.jmt.controller;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.jmt.model.QFileInfo;
import com.example.jmt.repository.QFileInfoRepository;


@Controller
public class QDownloadController {
    @Autowired QFileInfoRepository qFileInfoRepository;

    @GetMapping("/down")
    public ResponseEntity<Resource> download(@RequestParam("fileId") int fileId) throws Exception {
        Optional<QFileInfo> opt = qFileInfoRepository.findById(fileId);
        QFileInfo fileInfo = opt.get(); 
        String saveName = fileInfo.getSaveName(); //진짜 파일

        // 파일 경로 맞춰서 수정
        File file = new File("c://files/" + saveName );
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .header("content-disposition",
                        "filename=" + URLEncoder.encode(file.getName(), "utf-8"))
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }
}
