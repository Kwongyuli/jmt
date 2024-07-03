package com.example.jmt.controller;

import com.example.jmt.model.CommentMeal;
import com.example.jmt.model.Meal;
import com.example.jmt.repository.FileInfoRepository;
import com.example.jmt.repository.MealRepository;
import com.example.jmt.request.MealCreate;
import com.example.jmt.request.MealUpdate;
import com.example.jmt.response.MealResponse;
import com.example.jmt.service.CommentMealService;
import com.example.jmt.service.FileInfoService;
import com.example.jmt.service.MealService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/meals")
public class MealController {

    private final MealService mealService;
    private final FileInfoRepository fileInfoRepository;
    private final CommentMealService commentMealService;
    private final MealRepository mealRepository;
    private final FileInfoService fileInfoService;

    // 게시글 목록
    @GetMapping("/list")
    public String getMeals(Model model,
                           @RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "search", required = false) String search) {

        Sort sort = Sort.by(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page - 1, 10, sort);

        Page<Meal> mealPage = mealService.getMeals(pageable, search);
        List<Meal> meals = mealPage.getContent();

        int startPage = (page - 1) / 10 * 10 + 1;
        int endPage = startPage + 9;
        int total = mealPage.getTotalPages();
        if (endPage > total) {
            endPage = total;
        }

        model.addAttribute("meals", meals);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", page);  // currentPage 변수 설정

        model.addAttribute("search", search);  // 검색

        return "mealList";
    }


    // 글 상세페이지
    @GetMapping("/{id}")
    public String getMeal(@PathVariable Long id, Model model) {
        MealResponse meal = mealService.get(id);

        List<CommentMeal> comments = commentMealService.getCommentsByMeal(id);

        model.addAttribute("meal", meal);
        model.addAttribute("upvotes", mealService.getUpvotes(id));
        model.addAttribute("downvotes", mealService.getDownvotes(id));
        model.addAttribute("comments", comments);

        return "mealDetail";
    }

    @GetMapping("/write")
    public String createMealForm(Model model) {
        model.addAttribute("mealCreate", new MealCreate());
        return "mealForm";
    }

    // 글 작성
    @PostMapping("/write")
    public String createMeal(@Valid @ModelAttribute MealCreate mealCreate, @RequestParam("files") MultipartFile[] files) throws IOException {
        Meal savedMeal = mealService.write(mealCreate, files);
        return "redirect:/meals/" + savedMeal.getId();
    }


    //댓글 쓰기 메서드
    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id, @RequestParam("comment") String comment) {
        commentMealService.addComment(id, comment);
        return "redirect:/meals/" + id;
    }

    // 댓글 삭제
    @PostMapping("/{id}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
        commentMealService.deleteComment(commentId);
        return "redirect:/meals/" + id;
    }


    @GetMapping("/{id}/edit")
    public String editMealForm(@PathVariable Long id, Model model) {
        MealUpdate mealUpdate = mealService.getMealUpdate(id);
        mealUpdate.setId(id); // mealUpdate 객체에 id 값 설정
        model.addAttribute("mealUpdate", mealUpdate);
        return "mealEditForm";
    }

    // 글 수정
    @PostMapping("/{id}")
    public String updateMeal(@PathVariable Long id, @ModelAttribute MealUpdate mealUpdate, @RequestParam("files") MultipartFile[] files) throws IOException {
        mealService.update(id, mealUpdate, files);
        return "redirect:/meals/" + id;
    }

    // 글 삭제
    @PostMapping("/{id}/delete")
    public String deleteMeal(@PathVariable Long id) {
        mealService.delete(id);
        return "redirect:/meals/list";
    }

    // 추천/비추천 컨트롤러
    @PostMapping("/{id}/upvote")
    public String upvoteMeal(@PathVariable Long id) {
        mealService.upvote(id);
        return "redirect:/meals/" + id;
    }

    @PostMapping("/{id}/downvote")
    public String downvoteMeal(@PathVariable Long id) {
        mealService.downvote(id);
        return "redirect:/meals/" + id;
    }

    @PostMapping("/deleteFile")
    @ResponseBody
    public ResponseEntity<?> deleteFile(@RequestParam Integer fileId) {
        fileInfoService.deleteFile(fileId);
        return ResponseEntity.ok().build();
    }

}
