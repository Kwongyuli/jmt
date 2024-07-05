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
import com.example.jmt.request.MealUpdate;
import com.example.jmt.service.FileInfoService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/deserts")
public class DesertController {

    private final DesertService desertService;
    private final FileInfoRepository fileInfoRepository;
    private final CommentDesertService commentDesertService;
    private final DesertRepository desertRepository;
    private final FileInfoService fileInfoService;

    // 게시글 목록
    @GetMapping("/list")
    public String getDeserts(Model model,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", required = false, defaultValue = "id") String sort) {

        Pageable pageable = PageRequest.of(page - 1, 10);
        List<DesertResponse> deserts = desertService.getList(pageable, search, sort);

        long totalElements = desertService.getTotalCount(search); // 전체 글의 수
        int totalPages = (int) Math.ceil((double) totalElements / 10); // 전체 페이지 수

        int startPage = Math.max(1, (page - 1) / 10 * 10 + 1);
        int endPage = Math.min(startPage + 9, totalPages);

        // int startPage = (page - 1) / 10 * 10 + 1;
        // int endPage = startPage + 9;
        //// int total = (int) desertRepository.count();
        // int total = (int) desertService.getTotalCount(search); // 전체 페이지 수
        //
        // if (endPage > total) {
        // endPage = total;
        // }

        model.addAttribute("deserts", deserts);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", page); // currentPage 변수 설정

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
    public String addComment(@PathVariable Long id, @RequestParam("comment") String comment, HttpSession session) {
        User user = (User) session.getAttribute("user_info");

        if (user == null) {
            return "redirect:/jmt/signin";
        }

        commentDesertService.addComment(id, comment);
        return "redirect:/deserts/" + id;
    }

    // 댓글 삭제
    @PostMapping("/{id}/comment/{commentId}/delete")
    public String deleteComment(@PathVariable Long id, @PathVariable Long commentId, HttpSession session) {
        User user = (User) session.getAttribute("user_info");
        CommentDesert comment = commentDesertService.getCommentById(commentId);

        if (user == null) {
            return "redirect:/jmt/signin";
        }

        if (user.equals(comment.getUser())) {
            commentDesertService.deleteComment(commentId);
        }

        commentDesertService.deleteComment(commentId);
        return "redirect:/deserts/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editDesertForm(@PathVariable Long id, Model model, HttpSession session) {
        User author = (User) session.getAttribute("user_info");
        Desert desert = desertService.getDesertById(id);

        if (author != null && author.equals(desert.getUser())) {
            DesertUpdate desertUpdate = desertService.getDesertUpdate(id);
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

    // // 추천/비추천 컨트롤러
    // @PostMapping("/{id}/upvote")
    // public String upvoteDesert(@PathVariable Long id) {
    // desertService.upvote(id);
    // return "redirect:/deserts/" + id;
    // }
    //
    // @PostMapping("/{id}/downvote")
    // public String downvoteDesert(@PathVariable Long id) {
    // desertService.downvote(id);
    // return "redirect:/deserts/" + id;
    // }

    @PostMapping("/{id}/upvote")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> upvoteDesert(@PathVariable Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user_info");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        desertService.upvote(id);
        long upvotes = desertService.getUpvotes(id);
        long downvotes = desertService.getDownvotes(id);
        Map<String, Long> response = new HashMap<>();
        response.put("upvotes", upvotes);
        response.put("downvotes", downvotes);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/downvote")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> downvoteDesert(@PathVariable Long id, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user_info");
        if (loggedInUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        desertService.downvote(id);
        long upvotes = desertService.getUpvotes(id);
        long downvotes = desertService.getDownvotes(id);
        Map<String, Long> response = new HashMap<>();
        response.put("upvotes", upvotes);
        response.put("downvotes", downvotes);
        return ResponseEntity.ok(response);
    }

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

}