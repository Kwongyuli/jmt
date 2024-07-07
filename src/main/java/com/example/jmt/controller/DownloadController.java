package com.example.jmt.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.example.jmt.model.FileInfo;
import com.example.jmt.repository.FileInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.Optional;

@Controller
public class DownloadController {

    @Autowired
    FileInfoRepository fileInfoRepository;
    @Autowired
    private AmazonS3 s3client;

//   @GetMapping("/download")
//   public ResponseEntity<Resource> download(@RequestParam("fileId") int fileId) throws Exception {
//
//       Optional<FileInfo> opt = fileInfoRepository.findById(fileId);
//       FileInfo fileInfo = opt.get();
//       String saveName = fileInfo.getSaveName();
//
////        File file = new File("/Users/kimyoungjun/Desktop/Coding/Busan_BackLecture/fileUPloadFolder/",saveName);
//       File file = new File("c://files/"+saveName);
//
//       InputStreamResource resource =
//               new InputStreamResource(new FileInputStream(file));
//       return ResponseEntity.ok()
//               .header("content-disposition",
//                       "filename=" + URLEncoder.encode(file.getName(), "utf-8"))
//               .contentLength(file.length())
//               .contentType(MediaType.parseMediaType("application/octet-stream"))
//               .body(resource);
//   }

     // AWS
     @GetMapping("/download")
     public ResponseEntity<Resource> download(@RequestParam("fileId") int fileId) throws Exception {
         FileInfo fileInfo = fileInfoRepository.findById(fileId).orElseThrow(() -> new IllegalStateException("File not found"));
         String bucketName = "jmt-files";
         String key = fileInfo.getSaveName();

         S3Object s3Object = s3client.getObject(new GetObjectRequest(bucketName, key));
         InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());

         return ResponseEntity.ok()
                 .header("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(fileInfo.getOriginalName(), "UTF-8") + "\"")
                 .contentLength(s3Object.getObjectMetadata().getContentLength())
                 .contentType(MediaType.parseMediaType(s3Object.getObjectMetadata().getContentType()))
                 .body(resource);
     }
}