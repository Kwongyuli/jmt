package com.example.jmt.controller;

import com.example.jmt.model.CommentMeal;
import com.example.jmt.model.Meal;
import com.example.jmt.model.User;
import com.example.jmt.repository.FileInfoRepository;
import com.example.jmt.repository.MealRepository;
import com.example.jmt.request.MealCreate;
import com.example.jmt.request.MealUpdate;
import com.example.jmt.response.MealResponse;
import com.example.jmt.service.CommentMealService;
import com.example.jmt.service.FileInfoService;
import com.example.jmt.service.MealService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/meals")
public class MealController {

    private final MealService mealService;
    private final FileInfoRepository fileInfoRepository;
    private final CommentMealService commentMealService;
    private final MealRepository mealRepository;
    private final FileInfoService fileInfoService;
    private final HttpSession session;

    // 게시글 목록
    @GetMapping("/list")
    public String getMeals(Model model,
                           @RequestParam(value = "page", defaultValue = "1") int page,
                           @RequestParam(value = "search", required = false) String search,
                           @RequestParam(value = "sort", required = false, defaultValue = "id") String sort) {

        Pageable pageable = PageRequest.of(page - 1, 10);
        List<MealResponse> meals = mealService.getList(pageable, search, sort);


        long totalElements = mealService.getTotalCount(search); // 전체 글의 수
        int totalPages = (int) Math.ceil((double) totalElements / 10); // 전체 페이지 수

        int startPage = Math.max(1, (page - 1) / 10 * 10 + 1);
        int endPage = Math.min(startPage + 9, totalPages);

        model.addAttribute("meals", meals);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", page); // currentPage 변수 설정
        
        model.addAttribute("search", search);  // 검색
        model.addAttribute("sort", sort);

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
    public String createMeal(@Valid @ModelAttribute MealCreate mealCreate, @RequestParam("files") MultipartFile[] files,
            HttpSession session)
            throws IOException {
        User author = (User) session.getAttribute("user_info");

        if (author == null) {
            return "redirect:/jmt/signin";
        }

        mealCreate.setUser(author);

        Meal savedMeal = mealService.write(mealCreate, files);
        return "redirect:/meals/" + savedMeal.getId();
    }

    // 댓글 쓰기 메서드
    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id, @RequestParam("comment") String comment, HttpSession session) {

        User user = (User) session.getAttribute("user_info");

        if (user == null) {
            return "redirect:/jmt/signin";
        }

        commentMealService.addComment(id, comment);
        return "redirect:/meals/" + id;
    }

    // 댓글 삭제
    @PostMapping("/{id}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long id, @PathVariable Long commentId, HttpSession session) {
        User user = (User) session.getAttribute("user_info");
        CommentMeal comment = commentMealService.getCommentById(commentId);

        if (user == null) {
            return "redirect:/jmt/signin";
        }

        if (user.equals(comment.getUser())) {
            commentMealService.deleteComment(commentId);
        }

        commentMealService.deleteComment(commentId);
        return "redirect:/meals/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editMealForm(@PathVariable Long id, Model model, HttpSession session) {
        User author = (User) session.getAttribute("user_info");
        Meal meal = mealService.getMealById(id);

        if (author != null && author.equals(meal.getUser())) {
            MealUpdate mealUpdate = mealService.getMealUpdate(id);
            mealUpdate.setId(id);
            model.addAttribute("mealUpdate", mealUpdate);
            return "mealEditForm";
        } else {
            return "redirect:/jmt/signin";
        }
    }

    // 글 수정
    @PostMapping("/{id}")
    public String updateMeal(@PathVariable Long id, @ModelAttribute MealUpdate mealUpdate,
            @RequestParam("files") MultipartFile[] files, HttpSession session) throws IOException {
        
        User user = (User) session.getAttribute("user_info");
        Meal meal = mealService.getMealById(id);

        if (user != null && user.equals(meal.getUser())) {
            mealService.update(id, mealUpdate, files);
            return "redirect:/meals/" + id;
        } else {
            return "redirect:/jmt/signin";
        }
    }

    // 글 삭제
    @PostMapping("/{id}/delete")
    public String deleteMeal(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user_info");
        Meal meal = mealService.getMealById(id);

        if (user != null && user.equals(meal.getUser())) {
            mealService.delete(id);
            return "redirect:/meals/list";
        } else {
            return "redirect:/jmt/signin";
        }
    }

    // // 추천/비추천 컨트롤러
    // @PostMapping("/{id}/upvote")
    // public String upvoteMeal(@PathVariable Long id) {
    // mealService.upvote(id);
    // return "redirect:/meals/" + id;
    // }
    //
    // @PostMapping("/{id}/downvote")
    // public String downvoteMeal(@PathVariable Long id) {
    // mealService.downvote(id);
    // return "redirect:/meals/" + id;
    // }

    @PostMapping("/{id}/upvote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> upvoteMeal(@PathVariable Long id) {

        User user = getCurrentUser();

        String message = mealService.upvote(id, user);
        long upvotes = mealService.getUpvotes(id);
        long downvotes = mealService.getDownvotes(id);
        Map<String, Object> response = new HashMap<>();
        response.put("upvotes", upvotes);
        response.put("downvotes", downvotes);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/downvote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> downvoteMeal(@PathVariable Long id) {

        User user = getCurrentUser();

        String message = mealService.downvote(id, user);
        long upvotes = mealService.getUpvotes(id);
        long downvotes = mealService.getDownvotes(id);
        Map<String, Object> response = new HashMap<>();
        response.put("upvotes", upvotes);
        response.put("downvotes", downvotes);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deleteFile")
    @ResponseBody
    public ResponseEntity<?> deleteFile(@RequestParam Integer fileId) {
        fileInfoService.deleteFile(fileId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/myMealList")
    public String getMyDeserts(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user_info");
        if (loggedInUser == null) {
            return "redirect:/jmt/signin";
        }

        List<MealResponse> myMeals = mealService.getMyMeals(loggedInUser);

        model.addAttribute("meals", myMeals);
        return "myMealList";
    }
    private User getCurrentUser() {
        User user = (User) session.getAttribute("user_info");
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return user;
    }
}
