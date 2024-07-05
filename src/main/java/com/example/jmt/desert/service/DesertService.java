package com.example.jmt.desert.service;

import com.example.jmt.desert.model.Desert;
import com.example.jmt.desert.model.VoteDesert;
import com.example.jmt.desert.repository.DesertRepository;
import com.example.jmt.desert.repository.VoteDesertRepository;
import com.example.jmt.desert.request.DesertCreate;
import com.example.jmt.desert.request.DesertUpdate;
import com.example.jmt.desert.response.DesertResponse;
import com.example.jmt.model.FileInfo;
import com.example.jmt.model.User;
import com.example.jmt.repository.FileInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DesertService {

    private final DesertRepository desertRepository;
    private final VoteDesertRepository voteDesertRepository;
    private final FileInfoRepository fileInfoRepository;

    // 글 저장 메서드
    public Desert write(DesertCreate desertCreate, MultipartFile[] files) throws IOException {
        Desert desert = Desert.builder()
                .title(desertCreate.getTitle())
                .content(desertCreate.getContent())
                .lat(desertCreate.getLat())
                .lng(desertCreate.getLng())
                .createdAt(LocalDateTime.now())
                .viewCount(0)
                .user(desertCreate.getUser())
                .build();

        Desert savedDesert = desertRepository.save(desert);

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                saveFile(file, savedDesert);
            }
        }

        return savedDesert;
    }

    // 추천 메서드
    public String upvote(Long desertId, User user) {
        Optional<VoteDesert> existingVote = Optional.ofNullable(voteDesertRepository.findByDesertIdAndUserId(desertId, user.getId()));

        if (existingVote.isPresent()) {
            if (existingVote.get().isUpvote()) {
                voteDesertRepository.delete(existingVote.get());
                return "추천을 취소했습니다.";
            } else {
                existingVote.get().setUpvote(true);
                voteDesertRepository.save(existingVote.get());
                return "비추천을 추천으로 변경했습니다.";
            }
        } else {
            Desert desert = desertRepository.findById(desertId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
            VoteDesert vote = VoteDesert.builder()
                    .desert(desert)
                    .user(user)
                    .upvote(true)
                    .build();
            voteDesertRepository.save(vote);
            return "추천했습니다.";
        }
    }

    // 비추천 메서드
    public String downvote(Long desertId, User user) {
        Optional<VoteDesert> existingVote = Optional.ofNullable(voteDesertRepository.findByDesertIdAndUserId(desertId, user.getId()));

        if (existingVote.isPresent()) {
            if (!existingVote.get().isUpvote()) {
                voteDesertRepository.delete(existingVote.get());
                return "비추천을 취소했습니다.";
            } else {
                existingVote.get().setUpvote(false);
                voteDesertRepository.save(existingVote.get());
                return "추천을 비추천으로 변경했습니다.";
            }
        } else {
            Desert desert = desertRepository.findById(desertId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
            VoteDesert voteDesert = VoteDesert.builder()
                    .desert(desert)
                    .user(user)
                    .upvote(false)
                    .build();
            voteDesertRepository.save(voteDesert);
            return "비추천했습니다.";
        }
    }

    public long getUpvotes(Long desertId) {
        return voteDesertRepository.countByDesertIdAndUpvote(desertId, true);
    }

    public long getDownvotes(Long desertId) {
        return voteDesertRepository.countByDesertIdAndUpvote(desertId, false);
    }

    // 단건 조회 -> 글 상세페이지
    public DesertResponse get(Long id) {
        Desert desert = desertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        // 조회수 증가 로직 추가
        desert.setViewCount(desert.getViewCount() + 1);
        desertRepository.save(desert);

        return DesertResponse.builder()
                .id(desert.getId())
                .title(desert.getTitle())
                .content(desert.getContent())
                .lat(desert.getLat())
                .lng(desert.getLng())
                .createdAt(desert.getCreatedAt())
                .fileInfos(desert.getFileInfos())
                .comments(desert.getCommentDeserts()) // 댓글
                .viewCount(desert.getViewCount()) // 조회수
                .build();
    }

    // 전체 게시글 조회
    public List<DesertResponse> getList(Pageable pageable, String search, String sort) {
        Pageable sortedPageable = pageable;

        if ("viewCount".equals(sort)) {
            sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                    Sort.by(Sort.Order.desc("viewCount")));
        }

        Page<Desert> deserts;
        if (search == null || search.isEmpty()) {
            deserts = desertRepository.findAll(sortedPageable);
        } else {
            deserts = desertRepository.findByTitleContainingOrContentContaining(search, search, sortedPageable);
        }

        List<DesertResponse> desertResponses = deserts.stream()
                .map(desert -> {
                    long upvotes = getUpvotes(desert.getId());
                    long downvotes = getDownvotes(desert.getId());
                    return DesertResponse.builder()
                            .id(desert.getId())
                            .title(desert.getTitle())
                            .content(desert.getContent())
                            .lat(desert.getLat())
                            .lng(desert.getLng())
                            .createdAt(desert.getCreatedAt())
                            .viewCount(desert.getViewCount())
                            .upvotes(upvotes)
                            .downvotes(downvotes)
                            .fileInfos(desert.getFileInfos())
                            .comments(desert.getCommentDeserts())
                            .commentCount(desert.getCommentDeserts().size()) // 댓글 개수 설정
                            .build();
                })
                .collect(Collectors.toList());

        if ("upvotes".equals(sort)) {
            desertResponses.sort(Comparator.comparingLong(DesertResponse::getUpvotes).reversed());
        }

        int start = Math.min((int) sortedPageable.getOffset(), desertResponses.size());
        int end = Math.min((start + sortedPageable.getPageSize()), desertResponses.size());

        return desertResponses.subList(start, end);
    }

    // 글 수정
    public DesertResponse update(Long id, DesertUpdate desertUpdate, MultipartFile[] files) throws IOException {
        Desert desert = desertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        desert.setTitle(desertUpdate.getTitle());
        desert.setContent(desertUpdate.getContent());
        desert.setLat(desertUpdate.getLat());
        desert.setLng(desertUpdate.getLng());
        desert.setCreatedAt(LocalDateTime.now());

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                saveFile(file, desert);
            }
        }

        Desert updatedDesert = desertRepository.save(desert);

        return DesertResponse.builder()
                .id(updatedDesert.getId())
                .title(updatedDesert.getTitle())
                .content(updatedDesert.getContent())
                .lat(updatedDesert.getLat())
                .lng(updatedDesert.getLng())
                .createdAt(updatedDesert.getCreatedAt())
                .build();
    }

    // DesertUpdate 객체 생성 메서드
    public DesertUpdate getDesertUpdate(Long id,User user) {

        Desert desert = desertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        return DesertUpdate.builder()
                .id(desert.getId()) // desertUpdate 객체에 id 값 설정
                .userId(desert.getUser().getId())
                .title(desert.getTitle())
                .content(desert.getContent())
                .createdAt(desert.getCreatedAt())
                .lat(desert.getLat())
                .lng(desert.getLng())
                .fileInfos(desert.getFileInfos())
                .build();
    }

    // 글 삭제
    public void delete(Long id) {
        Desert desert = desertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        desertRepository.delete(desert);
    }

    // 페이지네이션 메서드 + 검색
    public Page<Desert> getDeserts(Pageable pageable, String search) {
        if (search == null || search.isEmpty()) {
            return desertRepository.findAll(pageable);
        } else {
            return desertRepository.findByTitleContainingOrContentContaining(search, search, pageable);
        }
    }

    // 파일 저장 메서드
    private void saveFile(MultipartFile file, Desert desert) throws IOException {
        String filename = file.getOriginalFilename();
        try {
            file.transferTo(new File("/Users/kimyoungjun/Desktop/Coding/Busan_BackLecture/fileUPloadFolder/" + filename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileInfo fileInfo = new FileInfo();
        fileInfo.setDesert(desert);
        fileInfo.setOriginalName(filename);
        fileInfo.setSaveName(filename);
        fileInfoRepository.save(fileInfo);
    }

    public long getTotalCount(String search) {
        if (search == null || search.isEmpty()) {
            return desertRepository.count();
        } else {
            return desertRepository.countByTitleContainingOrContentContaining(search, search);
        }
    }

    public Desert getDesertById(Long id) {
        return desertRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
    }

    // 사용자 Desert 글 목록
    public List<DesertResponse> getMyDeserts(User user) {
        List<Desert> desertList = desertRepository.findByUser(user);
        return desertList.stream()
                .map(desert -> DesertResponse.builder()
                        .id(desert.getId())
                        .title(desert.getTitle())
                        .content(desert.getContent())
                        .createdAt(desert.getCreatedAt())
                        .viewCount(desert.getViewCount())
                        .upvotes(getUpvotes(desert.getId()))
                        .downvotes(getDownvotes(desert.getId()))
                        .fileInfos(desert.getFileInfos())
                        .comments(desert.getCommentDeserts())
                        .build())
                .collect(Collectors.toList());
    }
}
