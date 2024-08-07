package com.example.jmt.meal.controller;

import com.example.jmt.meal.model.CommentMeal;
import com.example.jmt.meal.model.Meal;
import com.example.jmt.model.User;
import com.example.jmt.repository.FileInfoRepository;
import com.example.jmt.meal.repository.MealRepository;
import com.example.jmt.meal.request.MealCreate;
import com.example.jmt.meal.request.MealUpdate;
import com.example.jmt.meal.response.MealResponse;
import com.example.jmt.meal.service.CommentMealService;
import com.example.jmt.service.FileInfoService;
import com.example.jmt.meal.service.MealService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
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
                           @RequestParam(value = "sort", required = false, defaultValue = "createdAt") String sort) {

        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Order.desc(sort)));
        List<MealResponse> meals = mealService.getList(pageable, search, sort);


        long totalElements = mealService.getTotalCount(search); // 전체 글의 수
        int totalPages = (int) Math.ceil((double) totalElements / 10); // 전체 페이지 수

        int startPage = Math.max(1, (page - 1) / 10 * 10 + 1);
        int endPage = Math.min(startPage + 9, totalPages);

        model.addAttribute("meals", meals);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPage", totalPages);
        model.addAttribute("currentPage", page);  // currentPage 변수 설정

        model.addAttribute("search", search);  // 검색
        model.addAttribute("sort", sort);

        return "meal/mealList";
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

        return "meal/mealDetail";
    }

    @GetMapping("/write")
    public String createMealForm(Model model) {
        model.addAttribute("mealCreate", new MealCreate());
        return "meal/mealForm";
    }

    // 글 작성
    @PostMapping("/write")
    public String createMeal(@Valid @ModelAttribute MealCreate mealCreate, @RequestParam("files") MultipartFile[] files)
            throws IOException {

        User user = getCurrentUser();

        Meal savedMeal = mealService.write(mealCreate, files,user);
        return "redirect:/meals/" + savedMeal.getId();
    }

    // 댓글 쓰기 메서드
    @PostMapping("/{id}/comment")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addComment(@PathVariable Long id, @RequestParam("comment") String comment) {
        Map<String, String> response = new HashMap<>();
        try {
            User user = getCurrentUser();
            CommentMeal commentMeal = commentMealService.addComment(id, comment, user);
            response.put("message", "댓글이 추가되었습니다.");
            response.put("username", user.getName());
            response.put("commentId", String.valueOf(commentMeal.getId())); // commentId 추가
            response.put("createdAt", commentMeal.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            response.put("message", "로그인해주세요.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (IllegalArgumentException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // 댓글 삭제
    @PostMapping("/{id}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long id, @PathVariable Long commentId, HttpSession session) {
        User user = getCurrentUser();

        commentMealService.deleteComment(commentId, user);
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
            return "meal/mealEditForm";
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
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public String handleIllegalArgumentException(IllegalArgumentException ex) {
        return "<script>alert('" + ex.getMessage() + "'); window.location.href = document.referrer;</script>";
    }
}
