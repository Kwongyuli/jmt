package com.example.jmt.desert.model;


import com.example.jmt.model.FileInfo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Desert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public String title;
    public String content;

    private LocalDateTime createdAt = LocalDateTime.now();

    private double lat; // 위도
    private double lng; // 경도

    private int viewCount; // 조회수

    //orphanRemoval = true, cascade = CascadeType.REMOVE -> 연관관계로 외부키 있으면 삭제를 도와준다.
        //                          (부모 엔티티가 삭제될 때, 연관된 자식 엔티티도 삭제)
    // toString 으로 stackOverFlow 발생해서 해당내역을 board 위에 어노테이션으로 exclude 시켜준다,
    @OneToMany(mappedBy = "desert",orphanRemoval = true, cascade = CascadeType.REMOVE)
    List<FileInfo> fileInfos = new ArrayList<>();

    @OneToMany(mappedBy = "desert", cascade = CascadeType.ALL)
    private List<VoteDesert> voteDeserts;

    @OneToMany(mappedBy = "desert", cascade = CascadeType.REMOVE)
    private List<CommentDesert> commentDeserts;

    @Builder
    public Desert(String title, String content, LocalDateTime createdAt, double lat, double lng, int viewCount) {
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.lat = lat;
        this.lng = lng;
        this.viewCount = viewCount;
    }
}
