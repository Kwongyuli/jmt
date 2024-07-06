package com.example.jmt.pub.controller;

import com.example.jmt.model.User;
import com.example.jmt.pub.model.CommentPub;
import com.example.jmt.pub.model.Pub;
import com.example.jmt.pub.repository.PubRepository;
import com.example.jmt.pub.request.PubCreate;
import com.example.jmt.pub.request.PubUpdate;
import com.example.jmt.pub.response.PubResponse;
import com.example.jmt.pub.service.CommentPubService;
import com.example.jmt.pub.service.PubService;
import com.example.jmt.repository.FileInfoRepository;
import com.example.jmt.service.FileInfoService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/pubs")
public class PubController {

    private final PubService pubService;
    private final FileInfoRepository fileInfoRepository;
    private final CommentPubService commentPubService;
    private final PubRepository pubRepository;
    private final FileInfoService fileInfoService;
    private final HttpSession session;


    // 게시글 목록
    @GetMapping("/list")
    public String getPubs(Model model,
                          @RequestParam(value = "page", defaultValue = "1") int page,
                          @RequestParam(value = "search", required = false) String search,
                          @RequestParam(value = "sort", required = false, defaultValue = "id") String sort) {


        Pageable pageable = PageRequest.of(page - 1, 10);
        List<PubResponse> pubs = pubService.getList(pageable, search, sort);

        long totalElements = pubService.getTotalCount(search); // 전체 글의 수
        int totalPages = (int) Math.ceil((double) totalElements / 10); // 전체 페이지 수

        int startPage = Math.max(1, (page - 1) / 10 * 10 + 1);
        int endPage = Math.min(startPage + 9, totalPages);

        model.addAttribute("pubs", pubs);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("currentPage", page);  // currentPage 변수 설정

        model.addAttribute("search", search);  // 검색
        model.addAttribute("sort", sort);

        return "/pub/pubList";
    }


    // 글 상세페이지
    @GetMapping("/{id}")
    public String getPub(@PathVariable Long id, Model model,  RedirectAttributes redirectAttributes) {
        PubResponse pub = pubService.get(id);

        if (pub == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "해당 글을 찾을 수 없습니다.");
            return "redirect:/pubs/list"; // 목록 페이지로 리디렉션
        }

        List<CommentPub> comments = commentPubService.getCommentsByPub(id);

        model.addAttribute("pub", pub);
        model.addAttribute("upvotes", pubService.getUpvotes(id));
        model.addAttribute("downvotes", pubService.getDownvotes(id));
        model.addAttribute("comments", comments);

        return "pub/pubDetail";
    }

    @GetMapping("/write")
    public String createPubForm(Model model) {
        model.addAttribute("pubCreate", new PubCreate());
        return "pub/pubForm";
    }

    // 글 작성
    @PostMapping("/write")
    public String createPub(@Valid @ModelAttribute PubCreate pubCreate,
                            @RequestParam("files") MultipartFile[] files) throws IOException {

        User user = getCurrentUser();

        Pub savedPub = pubService.write(pubCreate, files, user);
        return "redirect:/pubs/" + savedPub.getId();
    }


    // 댓글 쓰기 메서드
    @PostMapping("/{id}/comment")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addComment(@PathVariable Long id, @RequestParam("comment") String comment) {
        Map<String, String> response = new HashMap<>();
        try {
            User user = getCurrentUser();
            CommentPub commentPub = commentPubService.addComment(id, comment, user);
            response.put("message", "댓글이 추가되었습니다.");
            response.put("username", user.getName());
            response.put("commentId", String.valueOf(commentPub.getId())); // commentId 추가
            response.put("createdAt", commentPub.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));

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

        commentPubService.deleteComment(commentId, user);
        return "redirect:/pubs/" + id;
    }

    @GetMapping("/{id}/edit")
    public String editPubForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            PubUpdate pubUpdate = pubService.getPubUpdate(id, user);

            if (!user.getId().equals(pubUpdate.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "해당 글 작성자가 아닙니다.");
                return "redirect:/pubs/" + id;
            }

            model.addAttribute("pubUpdate", pubUpdate);
            return "pub/pubEditForm";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인해주세요.");
            return "redirect:/jmt/signin";
        }
    }

    // 글 수정
    @PostMapping("/{id}")
    public String updatePub(@PathVariable Long id, @ModelAttribute PubUpdate pubUpdate, @RequestParam("files") MultipartFile[] files) throws IOException {
        User user = getCurrentUser();
        pubService.update(id, pubUpdate, files, user);
        return "redirect:/pubs/" + id;
    }

    // 글 삭제
//    @PostMapping("/{id}/delete")
//    public String deletePub(@PathVariable Long id) {
//
//        User user = getCurrentUser();
//
//        pubService.delete(id, user);
//        return "redirect:/pubs/list";
//    }

    @PostMapping("/{id}/delete")
    public String deletePub(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = getCurrentUser();
            Pub pub = pubService.getPubById(id);

            if (!user.getId().equals(pub.getUser().getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "해당 글 작성자가 아닙니다.");
                return "redirect:/pubs/" + id;
            }

            pubService.delete(id, user);
            redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
            return "redirect:/pubs/list";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인해주세요.");
            return "redirect:/jmt/signin";
        }
    }

    @PostMapping("/{id}/upvote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> upvotePub(@PathVariable Long id) {

        User user = getCurrentUser();
        String message = pubService.upvote(id, user);
        long upvotes = pubService.getUpvotes(id);
        long downvotes = pubService.getDownvotes(id);
        Map<String, Object> response = new HashMap<>();
        response.put("upvotes", upvotes);
        response.put("downvotes", downvotes);
        response.put("message", message);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/downvote")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> downvotePub(@PathVariable Long id) {
        User user = getCurrentUser();
        String message = pubService.downvote(id, user);
        long upvotes = pubService.getUpvotes(id);
        long downvotes = pubService.getDownvotes(id);
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

    @GetMapping("/myPubList")
    public String getMyPubs(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("user_info");

        if (loggedInUser == null) {
            return "redirect:/jmt/signin";
        }

        List<PubResponse> myPubs = pubService.getMyPubs(loggedInUser);

        model.addAttribute("pubs", myPubs);
        return "/pub/myPubList";
    }

    private User getCurrentUser() {
        User user = (User) session.getAttribute("user_info");
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return user;
    }
}
