package com.example.jmt.desert.request;

import com.example.jmt.model.FileInfo;
import com.example.jmt.model.User;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DesertUpdate {

    private Long id; // id 필드 추가
    private Long userId; // 작성자 ID 필드 추가

    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt = LocalDateTime.now();
    private double lat; // 위도
    private double lng; // 경도
    private List<FileInfo> fileInfos; // fileInfos 필드 추가

    private User user;


    @Builder
    public DesertUpdate(Long id,  Long userId,String title, String content, LocalDateTime createdAt,
                        LocalDateTime updatedAt, double lat, double lng, List<FileInfo> fileInfos, User user) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lat = lat;
        this.lng = lng;
        this.fileInfos = fileInfos;
        this.user = user;
    }
}