package com.example.jmt.service;

import com.example.jmt.model.FileInfo;
import com.example.jmt.model.Meal;
import com.example.jmt.model.Vote;
import com.example.jmt.repository.FileInfoRepository;
import com.example.jmt.repository.MealRepository;
import com.example.jmt.repository.VoteRepository;
import com.example.jmt.request.MealCreate;
import com.example.jmt.request.MealUpdate;
import com.example.jmt.response.MealResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MealService {

    private final MealRepository mealRepository;

    // 추천/비추천기능
    private final VoteRepository voteRepository;

    // 이미지파일 저장 레포지토리
    private final FileInfoRepository fileInfoRepository;

//    글 저장 메서드 _ 엔티티로 넘기기
    public Meal write(MealCreate mealCreate,MultipartFile[] files) throws IOException {

        Meal meal = Meal.builder()
                .title(mealCreate.getTitle())
                .content(mealCreate.getContent())
                .lat(mealCreate.getLat())
                .lng(mealCreate.getLng())
                .createdAt(mealCreate.getCreatedAt())
                .updatedAt(LocalDateTime.now()) // 현재 시간으로 updatedAt 설정
                .build();

        Meal savedMeal = mealRepository.save(meal);

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                saveFile(file, savedMeal);
            }
        }

        return savedMeal;
    }

    // 추천/비추천 메서드
    public void upvote(Long mealId) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        Vote vote = Vote.builder()
                .meal(meal)
                .upvote(true)
                .build();
        voteRepository.save(vote);
    }

    public void downvote(Long mealId) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
        Vote vote = Vote.builder()
                .meal(meal)
                .upvote(false)
                .build();
        voteRepository.save(vote);
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

        return MealResponse.builder()
                .id(meal.getId())
                .title(meal.getTitle())
                .content(meal.getContent())
                .lat(meal.getLat())
                .lng(meal.getLng())
                .createdAt(meal.getCreatedAt())
                .updatedAt(meal.getUpdatedAt())
                .fileInfos(meal.getFileInfos())
                .comments(meal.getCommentMeals())
                .build();
    }

    // 전체 게시글 조회
    public List<MealResponse> getList() {
        return mealRepository.findAll().stream()
                .map(MealResponse::new)
                .collect(Collectors.toList());
    }

    // 글 수정
    public MealResponse update(Long id, MealUpdate mealUpdate, MultipartFile[] files) throws IOException {
        Meal meal = mealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        meal.setTitle(mealUpdate.getTitle());
        meal.setContent(mealUpdate.getContent());
        meal.setLat(mealUpdate.getLat());
        meal.setLng(mealUpdate.getLng());
        meal.setCreatedAt(mealUpdate.getCreatedAt());
        meal.setUpdatedAt(LocalDateTime.now()); // 현재 시간으로 updatedAt 설정

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
                .updatedAt(meal.getUpdatedAt())
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
    private void saveFile(MultipartFile file, Meal meal) throws IOException {
        String filename = file.getOriginalFilename();
        try {
            file.transferTo(new File("c:/files/" + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileInfo fileInfo = new FileInfo();
        fileInfo.setMeal(meal);
        fileInfo.setOriginalName(filename);
        fileInfo.setSaveName(filename);
        fileInfoRepository.save(fileInfo);
    }
}
