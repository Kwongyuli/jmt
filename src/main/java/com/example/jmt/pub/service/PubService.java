package com.example.jmt.pub.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.jmt.meal.model.Meal;
import com.example.jmt.model.FileInfo;
import com.example.jmt.model.User;
import com.example.jmt.pub.model.Pub;
import com.example.jmt.pub.model.VotePub;
import com.example.jmt.pub.repository.PubRepository;
import com.example.jmt.pub.repository.VotePubRepository;
import com.example.jmt.pub.request.PubCreate;
import com.example.jmt.pub.request.PubUpdate;
import com.example.jmt.pub.response.PubResponse;
import com.example.jmt.repository.FileInfoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class PubService {

    private final PubRepository pubRepository;

    // 추천/비추천기능
    private final VotePubRepository votePubRepository;

    // 이미지파일 저장 레포지토리
    private final FileInfoRepository fileInfoRepository;

    @Autowired
    private AmazonS3 s3Client;  // S3 클라이언트 빈을 주입받아야 함


    //  글 저장 메서드 _ 엔티티로 넘기기
    public Pub write(PubCreate pubCreate, MultipartFile[] files, User user) throws IOException {

        Pub pub = Pub.builder()
                .title(pubCreate.getTitle())
                .content(pubCreate.getContent())
                .lat(pubCreate.getLat())
                .lng(pubCreate.getLng())
                .createdAt(pubCreate.getCreatedAt())
                .viewCount(0) // 처음 글 작성시 조회수 0 으로 초기화
                .user(user)
                .build();

        Pub savedPub = pubRepository.save(pub);

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                saveFile(file, savedPub);
            }
        }

        return savedPub;
    }

    // 추천 메서드
    public String upvote(Long pubId, User user) {
        Optional<VotePub> existingVote = Optional.ofNullable(votePubRepository.findByPubIdAndUserId(pubId, user.getId()));

        if (existingVote.isPresent()) {
            if (existingVote.get().isUpvote()) {
                votePubRepository.delete(existingVote.get());
                return "추천을 취소했습니다.";
            } else {
                existingVote.get().setUpvote(true);
                votePubRepository.save(existingVote.get());
                return "비추천을 추천으로 변경했습니다.";
            }
        } else {
            Pub pub = pubRepository.findById(pubId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
            VotePub vote = VotePub.builder()
                    .pub(pub)
                    .user(user)
                    .upvote(true)
                    .build();
            votePubRepository.save(vote);
            return "추천했습니다.";
        }
    }

    // 비추천 메서드
    public String downvote(Long pubId, User user) {
        Optional<VotePub> existingVote = Optional.ofNullable(votePubRepository.findByPubIdAndUserId(pubId, user.getId()));

        if (existingVote.isPresent()) {
            if (!existingVote.get().isUpvote()) {
                votePubRepository.delete(existingVote.get());
                return "비추천을 취소했습니다.";
            } else {
                existingVote.get().setUpvote(false);
                votePubRepository.save(existingVote.get());
                return "추천을 비추천으로 변경했습니다.";
            }
        } else {
            Pub pub = pubRepository.findById(pubId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));
            VotePub votePub = VotePub.builder()
                    .pub(pub)
                    .user(user)
                    .upvote(false)
                    .build();
            votePubRepository.save(votePub);
            return "비추천했습니다.";
        }
    }

    public long getUpvotes(Long pubId) {
        return votePubRepository.countByPubIdAndUpvote(pubId, true);
    }

    public long getDownvotes(Long pubId) {
        return votePubRepository.countByPubIdAndUpvote(pubId, false);
    }

    // 단건 조회 -> 글 상세페이지
    public PubResponse get(Long id) {
        Pub pub = pubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        // 조회수 증가 로직 추가
        pub.setViewCount(pub.getViewCount() + 1);
        pubRepository.save(pub);

        return PubResponse.builder()
                .id(pub.getId())
                .title(pub.getTitle())
                .content(pub.getContent())
                .lat(pub.getLat())
                .lng(pub.getLng())
                .createdAt(pub.getCreatedAt())
                .fileInfos(pub.getFileInfos())
                .comments(pub.getCommentPubs()) // 댓글
                .viewCount(pub.getViewCount()) // 조회수
                .username(pub.getUser().getName())
                .build();
    }

    // 전체 게시글 조회
    public List<PubResponse> getList(Pageable pageable, String search, String sort) {

        Pageable sortedPageable = pageable;

        if ("viewCount".equals(sort)) {
            sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("viewCount")));
        } else {
            sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Order.desc("createdAt")));
        }

        Page<Pub> pubs;
        if (search == null || search.isEmpty()) {
            pubs = pubRepository.findAll(sortedPageable);
        } else {
            pubs = pubRepository.findByTitleContainingOrContentContaining(search, search, sortedPageable);
        }

        List<PubResponse> pubResponses = pubs.stream()
                .map(pub -> {
                    long upvotes = getUpvotes(pub.getId());
                    long downvotes = getDownvotes(pub.getId());
                    return PubResponse.builder()
                            .id(pub.getId())
                            .title(pub.getTitle())
                            .content(pub.getContent())
                            .lat(pub.getLat())
                            .lng(pub.getLng())
                            .createdAt(pub.getCreatedAt())
                            .viewCount(pub.getViewCount())
                            .upvotes(upvotes)
                            .downvotes(downvotes)
                            .fileInfos(pub.getFileInfos())
                            .comments(pub.getCommentPubs())
                            .commentCount(pub.getCommentPubs().size())
                            .username(pub.getUser().getName())
                            .build();
                })
                .collect(Collectors.toList());

        if ("upvotes".equals(sort)) {
            pubResponses.sort(Comparator.comparingLong(PubResponse::getUpvotes).reversed());
        }

        return pubResponses;
    }

    // 글 수정
    public PubResponse update(Long id, PubUpdate pubUpdate, MultipartFile[] files,User user) throws IOException {
        Pub pub = pubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        if (!pub.getUser().equals(user)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }
        pub.setTitle(pubUpdate.getTitle());
        pub.setContent(pubUpdate.getContent());
        pub.setLat(pubUpdate.getLat());
        pub.setLng(pubUpdate.getLng());
        pub.setCreatedAt(LocalDateTime.now());
//        pub.setUser(user);

        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                saveFile(file, pub);
            }
        }


        Pub updatedPub = pubRepository.save(pub);

        return PubResponse.builder()
                .id(updatedPub.getId())
                .title(updatedPub.getTitle())
                .content(updatedPub.getContent())
                .lat(updatedPub.getLat())
                .lng(updatedPub.getLng())
                .createdAt(updatedPub.getCreatedAt())
                .build();
    }

    // PubUpdate 객체 생성 메서드
    public PubUpdate getPubUpdate(Long id, User user) {

        Pub pub = pubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

//        if (!pub.getUser().equals(user)) {
//            throw new IllegalArgumentException("수정 권한이 없습니다.");
//        }

        return PubUpdate.builder()
                .id(pub.getId()) // pubUpdate 객체에 id 값 설정
                .userId(pub.getUser().getId())
                .title(pub.getTitle())
                .content(pub.getContent())
                .createdAt(pub.getCreatedAt())
                .lat(pub.getLat())
                .lng(pub.getLng())
                .fileInfos(pub.getFileInfos())
                .build();
    }

    // 글 삭제
    public void delete(Long id, User user) {
        Pub pub = pubRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 글입니다."));

        if (!pub.getUser().equals(user)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        pubRepository.delete(pub);
    }

    // 페이지네이션 메서드 + 검색
    public Page<Pub> getPubs(Pageable pageable, String search) {
        if (search == null || search.isEmpty()) {
            return pubRepository.findAll(pageable);
        } else {
            return pubRepository.findByTitleContainingOrContentContaining(search, search, pageable);
        }
    }

    // 파일 저장 메서드
   private void saveFile(MultipartFile file, Pub pub) throws IOException {
       String filename = file.getOriginalFilename();
       try {
//        File file = new File("/Users/kimyoungjun/Desktop/Coding/Busan_BackLecture/fileUPloadFolder/",saveName);
           file.transferTo(new File("/Users/kimyoungjun/Desktop/Coding/Busan_BackLecture/fileUPloadFolder/" + filename));
       } catch (IOException e) {
           e.printStackTrace();
       }

       FileInfo fileInfo = new FileInfo();
       fileInfo.setPub(pub);
       fileInfo.setOriginalName(filename);
       fileInfo.setSaveName(filename);
       fileInfoRepository.save(fileInfo);
   }

    // // AWS 파일업로드 메서드
    // // AWS 용 업로드
    // private void saveFile(MultipartFile file, Pub pub) throws IOException {
    //     String bucketName = "jmt-files";
    //     String keyName = "uploads/" + file.getOriginalFilename();  // S3에 저장될 파일 이름

    //     ObjectMetadata metadata = new ObjectMetadata();
    //     metadata.setContentType(file.getContentType());
    //     metadata.setContentLength(file.getSize());
    //     InputStream inputStream = file.getInputStream();

    //     // S3 버킷에 파일 업로드
    //     s3Client.putObject(new PutObjectRequest(bucketName, keyName, inputStream, metadata));

    //     FileInfo fileInfo = new FileInfo();
    //     fileInfo.setPub(pub);
    //     fileInfo.setOriginalName(file.getOriginalFilename());
    //     fileInfo.setSaveName(keyName);
    //     fileInfoRepository.save(fileInfo);
    // }



    public long getTotalCount(String search) {
        if (search == null || search.isEmpty()) {
            return pubRepository.count(); // 검색어 없으면 전체 게시글 수 반환
        } else {
            return pubRepository.countByTitleContainingOrContentContaining(search, search); // 검색 결과 게시글 수 반환
        }
    }

    // 사용자 Pub 글 목록
    public List<PubResponse> getMyPubs(User user) {
        List<Pub> pubList = pubRepository.findByUser(user);
        return pubList.stream()
                .map(pub -> PubResponse.builder()
                        .id(pub.getId())
                        .title(pub.getTitle())
                        .content(pub.getContent())
                        .createdAt(pub.getCreatedAt())
                        .viewCount(pub.getViewCount())
                        .upvotes(getUpvotes(pub.getId()))
                        .downvotes(getDownvotes(pub.getId()))
                        .fileInfos(pub.getFileInfos())
                        .comments(pub.getCommentPubs())
                        .build())
                .collect(Collectors.toList());
    }
    public Pub getPubById(Long id) {
        return pubRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 글을 찾을 수 없습니다."));
    }
}
