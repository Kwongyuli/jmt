    package com.example.jmt.desert.response;

    import com.example.jmt.desert.model.CommentDesert;
    import com.example.jmt.desert.model.Desert;
    import com.example.jmt.model.FileInfo;
    import lombok.Builder;
    import lombok.Getter;

    import java.time.LocalDateTime;
    import java.util.List;

    @Getter
    public class DesertResponse {

        private Long id;

        public String title;
        public String content;

        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt;

        private double lat; // 위도
        private double lng; // 경도

        private List<FileInfo> fileInfos; // fileInfos 필드

        private List<CommentDesert> comments; // 댓글

        private int viewCount; // 조회수

        private long upvotes;
        private long downvotes;
        private int commentCount; // 댓글 개수 필드 추가

        private String username;


        public DesertResponse(Desert desert) {
            this.id = desert.getId();
            this.title = desert.getTitle();
            this.content = desert.getContent();
            this.lat = desert.getLat();
            this.lng = desert.getLng();
            this.createdAt = desert.getCreatedAt();
            this.fileInfos = desert.getFileInfos();  // fileInfos 필드
            this.comments = desert.getCommentDeserts();
            this.viewCount = desert.getViewCount();
        }

        @Builder
        public DesertResponse(Long id, String title, String content, List<FileInfo> fileInfos
                , LocalDateTime createdAt, double lat, double lng
                , List<CommentDesert> comments,  int viewCount, long upvotes, long downvotes,int commentCount, String username
                              ) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.fileInfos = fileInfos;  // fileInfos 필드
            this.createdAt = createdAt;
            this.lat = lat;
            this.lng = lng;
            this.comments = comments;
            this.viewCount=viewCount;
            this.upvotes = upvotes;
            this.downvotes = downvotes;
            this.commentCount = commentCount;
            this.username = username;
        }
    }
