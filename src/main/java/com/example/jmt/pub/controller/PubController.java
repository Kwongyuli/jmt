package com.example.jmt.pub.controller;

import com.example.jmt.entity.User;
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

//        int startPage = (page - 1) / 10 * 10 + 1;
//        int endPage = startPage + 9;
////        int total = (int) pubRepository.count();
//        int total = (int) pubService.getTotalCount(search); // 전체 페이지 수
//
//        if (endPage > total) {
//            endPage = total;
//        }

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
    public String getPub(@PathVariable Long id, Model model) {
        PubResponse pub = pubService.get(id);

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


    //댓글 쓰기 메서드
    @PostMapping("/{id}/comment")
    @ResponseBody
    public ResponseEntity<Map<String, String>> addComment(@PathVariable Long id, @RequestParam("comment") String comment) {
        Map<String, String> response = new HashMap<>();
        try {
            User user = getCurrentUser();
            commentPubService.addComment(id, comment, user);
            response.put("message", "댓글이 추가되었습니다.");
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
    public String editPubForm(@PathVariable Long id, Model model) {

        User user = getCurrentUser();

        PubUpdate pubUpdate = pubService.getPubUpdate(id, user);
        pubUpdate.setId(id); // pubUpdate 객체에 id 값 설정
        model.addAttribute("pubUpdate", pubUpdate);
        return "pub/pubEditForm";
    }

    // 글 수정
    @PostMapping("/{id}")
    public String updatePub(@PathVariable Long id, @ModelAttribute PubUpdate pubUpdate, @RequestParam("files") MultipartFile[] files) throws IOException {

        User user = getCurrentUser();

        pubService.update(id, pubUpdate, files, user);
        return "redirect:/pubs/" + id;
    }

    // 글 삭제
    @PostMapping("/{id}/delete")
    public String deletePub(@PathVariable Long id) {

        User user = getCurrentUser();

        pubService.delete(id, user);
        return "redirect:/pubs/list";
    }

    //    // 추천/비추천 컨트롤러
//    @PostMapping("/{id}/upvote")
//    public String upvotePub(@PathVariable Long id) {
//        pubService.upvote(id);
//        return "redirect:/pubs/" + id;
//    }
//
//    @PostMapping("/{id}/downvote")
//    public String downvotePub(@PathVariable Long id) {
//        pubService.downvote(id);
//        return "redirect:/pubs/" + id;
//    }
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

    private User getCurrentUser() {
        User user = (User) session.getAttribute("user_info");
        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        return user;
    }
}
