    package com.example.jmt.response;

    import com.example.jmt.model.CommentMeal;
    import com.example.jmt.model.FileInfo;
    import com.example.jmt.model.Meal;
    import lombok.Builder;
    import lombok.Getter;

    import java.time.LocalDateTime;
    import java.util.List;

    @Getter
    public class MealResponse {

        private Long id;

        public String title;
        public String content;

        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime updatedAt;

        private double lat; // 위도
        private double lng; // 경도

        private List<FileInfo> fileInfos; // fileInfos 필드

        private List<CommentMeal> comments; // 댓글

        public MealResponse(Meal meal) {
            this.id = meal.getId();
            this.title = meal.getTitle();
            this.content = meal.getContent();
            this.lat = meal.getLat();
            this.lng = meal.getLng();
            this.createdAt = meal.getCreatedAt();
            this.updatedAt = meal.getUpdatedAt();
            this.fileInfos = meal.getFileInfos();  // fileInfos 필드
            this.comments = meal.getCommentMeals();
        }

        @Builder
        public MealResponse(Long id, String title, String content, List<FileInfo> fileInfos
                , LocalDateTime createdAt, LocalDateTime updatedAt, double lat, double lng
                ,List<CommentMeal> comments) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.fileInfos = fileInfos;  // fileInfos 필드
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
            this.lat = lat;
            this.lng = lng;
            this.comments = comments;

        }
    }
