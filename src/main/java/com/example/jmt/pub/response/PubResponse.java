    package com.example.jmt.pub.response;

    import com.example.jmt.pub.model.CommentPub;
    import com.example.jmt.pub.model.Pub;
    import com.example.jmt.model.FileInfo;
    import lombok.Builder;
    import lombok.Getter;

    import java.time.LocalDateTime;
    import java.util.List;

    @Getter
    public class PubResponse {

        private Long id;

        public String title;
        public String content;

        private LocalDateTime createdAt = LocalDateTime.now();

        private double lat; // 위도
        private double lng; // 경도

        private List<FileInfo> fileInfos; // fileInfos 필드

        private List<CommentPub> comments; // 댓글

        private int viewCount; // 조회수

        private long upvotes;
        private long downvotes;
        private int commentCount; // 댓글 개수 필드 추가

        private String username;


        public PubResponse(Pub pub) {
            this.id = pub.getId();
            this.title = pub.getTitle();
            this.content = pub.getContent();
            this.lat = pub.getLat();
            this.lng = pub.getLng();
            this.createdAt = pub.getCreatedAt();
            this.fileInfos = pub.getFileInfos();  // fileInfos 필드
            this.comments = pub.getCommentPubs();
            this.viewCount = pub.getViewCount();

        }

        @Builder
        public PubResponse(Long id, String title, String content, List<FileInfo> fileInfos
                , LocalDateTime createdAt, double lat, double lng
                , List<CommentPub> comments, int viewCount, long upvotes, long downvotes,int commentCount, String username) {
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
