package com.example.jmt.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.jmt.model.Answer;
import com.example.jmt.model.QFileInfo;
import com.example.jmt.model.Question;
import com.example.jmt.repository.AnswerRepository;
import com.example.jmt.repository.QFileInfoRepository;
import com.example.jmt.repository.QuestionRepository;

@Controller
public class QnAController {
    @Autowired QuestionRepository questionRepository;
    @Autowired QFileInfoRepository qFileInfoRepository;
    @Autowired AnswerRepository answerRepository;

    // 답변 수정
    @GetMapping("/answer/edit/{id}")
    public String answerEdit(Model model, @PathVariable("id") long id){
        Optional<Answer> data = answerRepository.findById(id);
        Answer answer = data.get();
        model.addAttribute("answer", answer);
        return "answer/edit";
    }

    // 답변에 대한 추천 및 비추천 기능
//    // 추천 및 비추천 기능
//    @GetMapping("/question/vote/{id}")
//    public String questionVote(@PathVariable("id") long id,
//                               @RequestParam("vote") boolean upvote) {
//        Optional<Question> questionOpt = questionRepository.findById(id);
//        if (questionOpt.isPresent()) {
//            Question question = questionOpt.get();
//
//            Vote vote = new Vote();
//            vote.setQuestion(question);
//            vote.setUpvote(upvote);
//            voteRepository.save(vote);
//        }
//        return "redirect:/question/" + id;
//    }
    // 답변 수정하고 질문 상세페이지로 가기위해 qId받아오기
    @PostMapping("/answer/edit/{id}")
    public String answerEdit(@PathVariable("id") long id, @RequestParam("qId") long qId, @RequestParam("text") String text){
        Optional<Answer> data = answerRepository.findById(id);
        Answer answer = data.get();
        answer.setText(text);
        answer.setCreatedAt(LocalDateTime.now());
        answerRepository.save(answer);
        return "redirect:/question/" + qId;
    }

    // 질문 수정
    @GetMapping("/question/edit/{id}")
    public String questionEdit(Model model, @PathVariable("id") long id){
        Optional<Question> data = questionRepository.findById(id);
        Question question = data.get();
        model.addAttribute("question", question);
        return "question/edit";
    }

    @PostMapping("/question/edit/{id}")
    public String questionEdit(@ModelAttribute Question question, @PathVariable("id") long id){
        question.setId(id);
        questionRepository.save(question);
        return "redirect:/question/" + id;
    }

    // 질문 상세
    @GetMapping("/question/{id}")
    public String questionDetail(Model model, @PathVariable("id") long id){
        Optional<Question> data = questionRepository.findById(id);
        Question question = data.get();
        model.addAttribute("question", question);
        return "/question/detail";
    }

    // 답변 작성 및 저장
    @PostMapping("/question/{id}")
    public String questionDetail(@PathVariable("id") long id, @RequestParam String text){
        Question question = new Question();
        Answer answer = new Answer();

        question.setId(id);
        answer.setQuestion(question);
        answer.setText(text);
        answer.setCreatedAt(LocalDateTime.now());
        answerRepository.save(answer);
        return "redirect:/question/" + id;
    }

    // 질문 작성 
    @GetMapping("/question/form")
    public String questionForm(){
        return "/question/form";
    }

    @PostMapping("/question/form")
    public String questionForm(@ModelAttribute Question question,
                               @RequestParam("file") MultipartFile qFile){

        String fileName = qFile.getOriginalFilename();

        try {
			// * 파일이 저장 될 위치 각자 지정 해주기 QDownload Controller에서도 
			qFile.transferTo(new File("/Users/leo/Desktop/study/files/" + fileName));
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

        Question saveQuestion = questionRepository.save(question);

        QFileInfo fileInfo = new QFileInfo();
        fileInfo.setQuestion(saveQuestion);
        fileInfo.setOriginalName(fileName);
        fileInfo.setSaveName(fileName);
        qFileInfoRepository.save(fileInfo);

        // 글 작성 후 상세 page로 가게 하기
        Long id = question.getId();

        return "redirect:/question/" + id;
    }

    // 질문 삭제 (파일도 삭제 해줘야해서)
    @GetMapping("/question/delete/{id}")
	public String questionDelete(@PathVariable("id") long id) {
        Question question = new Question();
		question.setId(id);
        
		List<QFileInfo> list = qFileInfoRepository.findByQuestion(question);		
		for(QFileInfo qFileInfo : list){
            qFileInfoRepository.delete(qFileInfo);
		}
        
		questionRepository.deleteById(id);
        
		return "redirect:/question/list";
	}
    
    // 답변 삭제
    @GetMapping("/answer/delete/{id}/{qId}")
    public String answerDelete(@PathVariable("id") long id, @PathVariable("qId") long qId) {
        answerRepository.deleteById(id);
        return "redirect:/question/" + qId;
    }

    // QnA 리스트
    @GetMapping("/question/list")
    public String questionList(Model model, @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "search", required = false) String search){

        Sort sort = Sort.by(Order.desc("id")); 
        Pageable pageable = PageRequest.of(page - 1, 10, sort);

        Page<Question> questionPage = null;
        List<Question> question = null;

        if(search == null){
			questionPage = questionRepository.findAll(pageable);
		} else{
			questionPage = questionRepository.findByTitleContainingOrContentContaining(search, search, pageable);
		}

		question = questionPage.getContent();

        int startPage = (page - 1) / 10 * 10 + 1;
        int endPage = startPage + 9;
		int totalPage = questionPage.getTotalPages();
		if(endPage > totalPage){
			endPage = totalPage;
		}
        
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("question", question);
        model.addAttribute("currentPage", page);  // currentPage 변수 설정

        model.addAttribute("search", search);  // 검색

        return "/question/list";
    }
}
