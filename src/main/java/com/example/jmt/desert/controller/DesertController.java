package com.example.jmt.desert.controller;

import com.example.jmt.desert.model.CommentDesert;
import com.example.jmt.desert.model.Desert;
import com.example.jmt.desert.repository.DesertRepository;
import com.example.jmt.desert.request.DesertCreate;
import com.example.jmt.desert.request.DesertUpdate;
import com.example.jmt.desert.response.DesertResponse;
import com.example.jmt.desert.service.CommentDesertService;
import com.example.jmt.desert.service.DesertService;
import com.example.jmt.model.User;
import com.example.jmt.repository.FileInfoRepository;
import com.example.jmt.service.FileInfoService;
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
@RequestMapping("/deserts")
public class DesertController {

    private final DesertService desertService;
    private final FileInfoRepository fileInfoRepository;
    private final CommentDesertService commentDesertService;
    private final DesertRepository desertRepository;
    private final FileInfoService fileInfoService;
    private final HttpSession session;

    // 게시글 목록
    @GetMapping("/list")
    public String getDeserts(Model model,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", required = false, defaultValue = "createdAt") String sort) {

        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(Sort.Order.desc(sort)));
        List<DesertResponse> deserts = desertService.getList(pageable, search, sort);

        long totalElements = desertService.getTotalCount(search); // 전체 글의 수
        int totalPages = (int) Math.ceil((double) totalElements / 10); // 전체 페이지 수

        int startPage = Math.max(1, (page - 1) / 10 * 10 + 1);
        int endPage = Math.min(startPage + 9, totalPages);

        model.addAttribute("deserts", deserts);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", page); // currentPage 변수 설정
        model.addAttribute("totalPages", totalPages); // totalPages 변수 설정

        model.addAttribute("search", search); // 검색
        model.addAttribute("sort", sort);

        return "/desert/desertList";
    }

    // 글 상세페이지
    @GetMapping("/{id}")
    public String getDesert(@PathVariable Long id, Model model) {
        DesertResponse desert = desertService.get(id);

        List<CommentDesert> comments = commentDesertService.getCommentsByDesert(id);

        model.addAttribute("desert", desert);
        model.addAttribute("upvotes", desertService.getUpvotes(id));
        model.addAttribute("downvotes", desertService.getDownvotes(id));
        model.addAttribute("comments", comments);

        return "desert/desertDetail";
    }

    @GetMapping("/write")
    public String createDesertForm(Model model) {
        model.addAttribute("desertCreate", new DesertCreate());
        return "desert/desertForm";
    }

    // 글 작성
    @PostMapping("/write")
    public String createDesert(@Valid @ModelAttribute DesertCreate desertCreate,
            @RequestParam("files") MultipartFile[] files, HttpSession session) throws IOException {
        User author = (User) session.getAttribute("user_info");

        if (author == null) {
            return "redirect:/jmt/signin";
        }

        desertCreate.setUser(author);

        Desert savedDesert = desertService.write(desertCreate, files);
        return "redirect:/deserts/" + savedDesert.getId();
    }

    // 댓글 쓰기 메서드
    @PostMapping("/{id}/comment")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addComment(@PathVariable Long id, @RequestParam("comment") String comment) {
        Map<String, String> response = new HashMap<>();
        try {
            User user = getCurrentUser();
            CommentDesert commentDesert = commentDesertService.addComment(id, comment, user);
            response.put("message", "댓글이 추가되었습니다.");
            response.put("username", user.getName());
            response.put("commentId", String.valueOf(commentDesert.getId())); // commentId 추가
            response.put("createdAt", commentDesert.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));

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
    public String deleteComment(@PathVariable Long id, @PathVariable Long commentId) {
        User user = getCurrentUser();

        commentDesertService.deleteComment(commentId, user);
        return "redirect:/deserts/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editDesertForm(@PathVariable Long id, Model model, HttpSession session) {
        User user = getCurrentUser();
        User author = (User) session.getAttribute("user_info");
        Desert desert = desertService.getDesertById(id);

        if (author != null && author.equals(desert.getUser())) {
            DesertUpdate desertUpdate = desertService.getDesertUpdate(id,user);
            desertUpdate.setId(id);
            model.addAttribute("desertUpdate", desertUpdate);
            return "desert/desertEditForm";
        } else {
            return "redirect:/jmt/signin";
        }
    }

    // 글 수정
    @PostMapping("/{id}")
    public String updateDesert(@PathVariable Long id, @ModelAttribute DesertUpdate desertUpdate,
            @RequestParam("files") MultipartFile[] files, HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user_info");
        Desert desert = desertService.getDesertById(id);

        if (user != null && user.equals(desert.getUser())) {
            desertService.update(id, desertUpdate, files);
            return "redirect:/deserts/" + id;
        } else {
            return "redirect:/jmt/signin";
        }
    }

    // 글 삭제
    @PostMapping("/{id}/delete")
    public String deleteDesert(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user_info");
        Desert desert = desertService.getDesertById(id);

        if (user != null && user.equals(desert.getUser())) {
            desertService.delete(id);
            return "redirect:/deserts/list";
        } else {
            return "redirect:/jmt/signin";
        }
    }

    @PostMapping("/{id}/upvote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> upvoteDesert(@PathVariable Long id) {

        User user = getCurrentUser();
        String message = desertService.upvote(id, user);
        long upvotes = desertService.getUpvotes(id);
        long downvotes = desertService.getDownvotes(id);
        Map<String, Object> response = new HashMap<>();
        response.put("upvotes", upvotes);
        response.put("downvotes", downvotes);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/downvote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> downvoteDesert(@PathVariable Long id) {

        User user = getCurrentUser();

        String message = desertService.downvote(id, user);
        long upvotes = desertService.getUpvotes(id);
        long downvotes = desertService.getDownvotes(id);
        Map<String, Object> response = new HashMap<>();
        response.put("upvotes", upvotes);
        response.put("downvotes", downvotes);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    // 파일 삭제
    @PostMapping("/deleteFile")
    @ResponseBody
    public ResponseEntity<?> deleteFile(@RequestParam Integer fileId) {
        fileInfoService.deleteFile(fileId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/myDesertList")
    public String getMyDeserts(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user_info");

        if (loggedInUser == null) {
            return "redirect:/jmt/signin";
        }

        List<DesertResponse> myDeserts = desertService.getMyDeserts(loggedInUser);

        model.addAttribute("deserts", myDeserts);
        return "/desert/myDesertList";
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