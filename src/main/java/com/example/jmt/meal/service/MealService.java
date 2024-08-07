package com.example.jmt.meal.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.jmt.model.FileInfo;
import com.example.jmt.meal.model.Meal;
import com.example.jmt.model.User;
import com.example.jmt.meal.model.Vote;
import com.example.jmt.repository.FileInfoRepository;
import com.example.jmt.meal.repository.MealRepository;
import com.example.jmt.meal.repository.VoteRepository;
import com.example.jmt.meal.request.MealCreate;
import com.example.jmt.meal.request.MealUpdate;
import com.example.jmt.meal.response.MealResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;

    // 추천/비추천기능
    private final VoteRepository voteRepository;

    // 이미지파일 저장 레포지토리
    private final FileInfoRepository fileInfoRepository;

    @Autowired
    private AmazonS3 s3Client;  // S3 클라이언트 빈을 주입받아야 함


    // 글 저장 메서드 _ 엔티티로 넘기기
    public Meal write(MealCreate mealCreate, MultipartFile[] files,User user) throws IOException {

        Meal meal = Meal.builder()
                .title(mealCreate.getTitle())
                .content(mealCreate.getContent())
                .lat(mealCreate.getLat())
                .lng(mealCreate.getLng())
                .createdAt(mealCreate.getCreatedAt())
                .user(mealCreate.getUser())
                .viewCount(0) // 처음 글 작성시 조회수 0 으로 초기화
                .user(user)
                .build();

        Meal savedMeal = mealRepository.save(meal);

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                saveFile(file, savedMeal);
            }
        }

        return savedMeal;
    }

    // 추천 메서드
    public String upvote(Long mealId, User user) {
        Optional<Vote> existingVote = Optional.ofNullable(voteRepository.findByMealIdAndUserId(mealId, user.getId()));

        if (existingVote.isPresent()) {
            if (existingVote.get().isUpvote()) {
                voteRepository.delete(existingVote.get());
                return "추천을 취소했습니다.";
            } else {
                existingVote.get().setUpvote(true);
                voteRepository.save(existingVote.get());
                return "비추천을 추천으로 변경했습니다.";
            }
        } else {
            Meal meal = mealRepository.findById(mealId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
            Vote vote = Vote.builder()
                    .meal(meal)
                    .user(user)
                    .upvote(true)
                    .build();
            voteRepository.save(vote);
            return "추천했습니다.";
        }
    }

    // 비추천 메서드
    public String downvote(Long mealId, User user) {
        Optional<Vote> existingVote = Optional.ofNullable(voteRepository.findByMealIdAndUserId(mealId, user.getId()));

        if (existingVote.isPresent()) {
            if (!existingVote.get().isUpvote()) {
                voteRepository.delete(existingVote.get());
                return "비추천을 취소했습니다.";
            } else {
                existingVote.get().setUpvote(false);
                voteRepository.save(existingVote.get());
                return "추천을 비추천으로 변경했습니다.";
            }
        } else {
            Meal meal = mealRepository.findById(mealId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
            Vote vote = Vote.builder()
                    .meal(meal)
                    .user(user)
                    .upvote(false)
                    .build();
            voteRepository.save(vote);
            return "비추천했습니다.";
        }
    }

    public long getUpvotes(Long mealId) {
        return voteRepository.countByMealIdAndUpvote(mealId, true);
    }

    public long getDownvotes(Long mealId) {
        return voteRepository.countByMealIdAndUpvote(mealId, false);
    }

    // 단건 조회
    public MealResponse get(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        // 조회수 증가 로직 추가
        meal.setViewCount(meal.getViewCount() + 1);
        mealRepository.save(meal);

        return MealResponse.builder()
                .id(meal.getId())
                .title(meal.getTitle())
                .content(meal.getContent())
                .lat(meal.getLat())
                .lng(meal.getLng())
                .createdAt(meal.getCreatedAt())
                .fileInfos(meal.getFileInfos())
                .comments(meal.getCommentMeals())
                .viewCount(meal.getViewCount())
                .username(meal.getUser().getName())
                .build();
    }

    // 전체 게시글 조회
    public List<MealResponse> getList(Pageable pageable, String search, String sort) {
        Pageable sortedPageable = pageable;

        if ("viewCount".equals(sort)) {
            sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("viewCount")));
        }else {
            sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("createdAt")));
        }

        Page<Meal> meals;
        if (search == null || search.isEmpty()) {
            meals = mealRepository.findAll(sortedPageable);
        } else {
            meals = mealRepository.findByTitleContainingOrContentContaining(search, search, sortedPageable);
        }

        List<MealResponse> mealResponses = meals.stream()
                .map(meal -> {
                    long upvotes = getUpvotes(meal.getId());
                    long downvotes = getDownvotes(meal.getId());
                    return MealResponse.builder()
                            .id(meal.getId())
                            .title(meal.getTitle())
                            .content(meal.getContent())
                            .lat(meal.getLat())
                            .lng(meal.getLng())
                            .createdAt(meal.getCreatedAt())
                            .viewCount(meal.getViewCount())
                            .upvotes(upvotes)
                            .downvotes(downvotes)
                            .fileInfos(meal.getFileInfos())
                            .comments(meal.getCommentMeals())
                            .commentCount(meal.getCommentMeals().size()) // 댓글 개수 설정
                            .username(meal.getUser().getName())
                            .build();
                })
                .collect(Collectors.toList());

        if ("upvotes".equals(sort)) {
            mealResponses.sort(Comparator.comparingLong(MealResponse::getUpvotes).reversed());
        }

        return mealResponses;

    }

    // 글 수정
    public MealResponse update(Long id, MealUpdate mealUpdate, MultipartFile[] files) throws IOException {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        meal.setTitle(mealUpdate.getTitle());
        meal.setContent(mealUpdate.getContent());
        meal.setLat(mealUpdate.getLat());
        meal.setLng(mealUpdate.getLng());
        meal.setCreatedAt(LocalDateTime.now());

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                saveFile(file, meal);
            }
        }

        Meal updatedMeal = mealRepository.save(meal);

        return MealResponse.builder()

                .id(updatedMeal.getId())
                .title(updatedMeal.getTitle())
                .content(updatedMeal.getContent())
                .lat(updatedMeal.getLat())
                .lng(updatedMeal.getLng())
                .createdAt(updatedMeal.getCreatedAt())
                .build();
    }

    // MealUpdate 객체 생성 메서드
    public MealUpdate getMealUpdate(Long id) {
        MealResponse meal = get(id);
        return MealUpdate.builder()
                .id(meal.getId()) // mealUpdate 객체에 id 값 설정
                .title(meal.getTitle())
                .content(meal.getContent())
                .createdAt(meal.getCreatedAt())
                .lat(meal.getLat())
                .lng(meal.getLng())
                .fileInfos(meal.getFileInfos())
                .build();
    }

    // 글 삭제
    public void delete(Long id) {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        mealRepository.delete(meal);
    }

    // 페이지네이션 메서드 + 검색
    public Page<Meal> getMeals(Pageable pageable, String search) {
        if (search == null || search.isEmpty()) {
            return mealRepository.findAll(pageable);
        } else {
            return mealRepository.findByTitleContainingOrContentContaining(search, search, pageable);
        }
    }

    // 파일 저장 메서드
//   private void saveFile(MultipartFile file, Meal meal) throws IOException {
//       String filename = file.getOriginalFilename();
//       try {
////        File file = new File("/Users/kimyoungjun/Desktop/Coding/Busan_BackLecture/fileUPloadFolder/",saveName);
//           file.transferTo(new File("c://files/" + filename));
//       } catch (IOException e) {
//           e.printStackTrace();
//       }
//
//       FileInfo fileInfo = new FileInfo();
//       fileInfo.setMeal(meal);
//       fileInfo.setOriginalName(filename);
//       fileInfo.setSaveName(filename);
//       fileInfoRepository.save(fileInfo);
//   }

     // AWS 용 업로드
     private void saveFile(MultipartFile file, Meal meal) throws IOException {
         String bucketName = "jmt-files";
         String keyName = "uploads/" + file.getOriginalFilename();  // S3에 저장될 파일 이름

         ObjectMetadata metadata = new ObjectMetadata();
         metadata.setContentType(file.getContentType());
         metadata.setContentLength(file.getSize());
         InputStream inputStream = file.getInputStream();

         // S3 버킷에 파일 업로드
         s3Client.putObject(new PutObjectRequest(bucketName, keyName, inputStream, metadata));

         FileInfo fileInfo = new FileInfo();
         fileInfo.setMeal(meal);
         fileInfo.setOriginalName(file.getOriginalFilename());
         fileInfo.setSaveName(keyName);
         fileInfoRepository.save(fileInfo);
     }

    public Meal getMealById(Long id) {
        return mealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
    }

    public List<MealResponse> getMyMeals(User user) {
        List<Meal> mealList = mealRepository.findByUser(user);
        return mealList.stream()
                .map(meal -> MealResponse.builder()
                        .id(meal.getId())
                        .title(meal.getTitle())
                        .content(meal.getContent())
                        .createdAt(meal.getCreatedAt())
                        .viewCount(meal.getViewCount())
                        .upvotes(getUpvotes(meal.getId()))
                        .downvotes(getDownvotes(meal.getId()))
                        .fileInfos(meal.getFileInfos())
                        .comments(meal.getCommentMeals())
                        .build())
                .collect(Collectors.toList());
    }

    public long getTotalCount(String search) {
        if (search == null || search.isEmpty()) {
            return mealRepository.count(); // 검색어 없으면 전체 게시글 수 반환
        } else {
            return mealRepository.countByTitleContainingOrContentContaining(search, search); // 검색 결과 게시글 수 반환
        }
    }
}
