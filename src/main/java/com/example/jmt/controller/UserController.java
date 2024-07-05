package com.example.jmt.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.jmt.dto.UserDto;
import com.example.jmt.model.User;
import com.example.jmt.repository.UserRepository;
import com.example.jmt.service.UserService;
import com.example.jmt.util.Mailer;
import com.example.jmt.util.RandomCodeGenerator;
import com.example.jmt.util.SMTPAuthenticator;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    HttpSession session;

    @Autowired
    UserService userService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    private Mailer mailer;

    private static final String EMAIL_SUBJECT = "비밀번호 재설정 인증 코드";

    @GetMapping("/jmt/signin")
    public String signin() {
        return "signin";
    }

    @PostMapping("/jmt/signin")
    public String signinPost(@ModelAttribute User user) {
        User dbUser = userRepository.findByUserId(user.getUserId());

        if (dbUser != null && passwordEncoder.matches(user.getPw(), dbUser.getPw())) {
            session.setAttribute("user_info", dbUser);
            return "redirect:/jmt/mypageEdit";
        } else {
            return "redirect:/jmt/signin";
        }
    }

    @GetMapping("/jmt/signout")
    public String signout() {
        session.invalidate();
        return "redirect:/jmt/signin";
    }

    @GetMapping("/jmt/signup")
    public String signup(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/jmt/signup")
    public String signupPost(@ModelAttribute @Valid UserDto user, BindingResult bindingResult, Model model) {
        bindingResult.getFieldErrors().forEach(error -> {
            System.out.println(error.getField());
            System.out.println(error.getDefaultMessage());
        });

        if (bindingResult.hasErrors()) {
            model.addAttribute("user", user);
            model.addAllAttributes(bindingResult.getModel());
            return "signup";
        }

        if (userRepository.findByUserId(user.getUserId()) != null) {
            model.addAttribute("error", "이미 사용 중인 아이디입니다.");
            return "signup";
        }

        if (userRepository.findByName(user.getName()) != null) {
            model.addAttribute("error", "이미 사용 중인 이름입니다.");
            return "signup";
        }

        userService.userJoin(user);
        return "redirect:/jmt/signin";
    }

    @GetMapping("/jmt/mypageEdit")
    public String mypage(Model model) {
        User user = (User) session.getAttribute("user_info");
        if (user == null) {
            return "redirect:/jmt/signin";
        }
        model.addAttribute("user", user);
        return "mypageEdit";
    }

    @PostMapping("/jmt/mypageEdit")
    public String updateUser(@ModelAttribute User user) {
        User dbUser = (User) session.getAttribute("user_info");

        if (dbUser != null) {
            dbUser.setName(user.getName());

            if (!user.getPw().isEmpty()) {
                dbUser.setPw(passwordEncoder.encode(user.getPw()));
            }

            userRepository.save(dbUser);

            session.setAttribute("user_info", dbUser);
        }

        return "redirect:/jmt/mypageEdit";
    }

    @PostMapping("/jmt/deleteAccount")
    public String deleteAccount() {
        User user = (User) session.getAttribute("user_info");
        if (user != null) {
            userRepository.delete(user);
            session.invalidate();
            return "redirect:/jmt/signin";
        } else {
            return "redirect:/jmt/signin";
        }
    }

    @PostMapping("/jmt/checkCurrentPassword")
    @ResponseBody
    public Map<String, Boolean> checkCurrentPassword(@RequestBody Map<String, String> requestBody) {
        Map<String, Boolean> response = new HashMap<>();

        User user = (User) session.getAttribute("user_info");
        if (user != null) {
            String currentPassword = requestBody.get("currentPassword");
            boolean correct = passwordEncoder.matches(currentPassword, user.getPw());
            response.put("correct", correct);
        } else {
            response.put("correct", false);
        }

        return response;
    }

    @PostMapping("/jmt/checkUserId")
    @ResponseBody
    public Map<String, Boolean> checkUserId(@RequestBody Map<String, String> requestBody) {
        Map<String, Boolean> response = new HashMap<>();
        String userId = requestBody.get("userId");
        boolean exists = userRepository.findByUserId(userId) != null;
        response.put("exists", exists);
        return response;
    }

    @PostMapping("/jmt/checkName")
    @ResponseBody
    public Map<String, Boolean> checkNickname(@RequestBody Map<String, String> requestBody) {
        Map<String, Boolean> response = new HashMap<>();
        String name = requestBody.get("name");
        boolean exists = userRepository.findByName(name) != null;
        response.put("exists", exists);
        return response;
    }

    @GetMapping("/jmt/findUserId")
    public String findId() {
        return "findUserId";
    }

    @PostMapping("/jmt/findUserId")
    @ResponseBody
    public Map<String, Object> findUserId(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        String name = requestBody.get("name");
        String email = requestBody.get("email");
        Optional<User> user = userRepository.findByNameAndEmail(name, email);

        if (user.isPresent()) {
            response.put("exists", true);
            response.put("userId", user.get().getUserId());
        } else {
            response.put("exists", false);
        }

        return response;
    }

    @GetMapping("/jmt/resetPassword")
    public String showResetPasswordForm() {
        return "resetPasswordForm";
    }

    @PostMapping("/jmt/resetPassword")
    @ResponseBody
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        String userId = requestBody.get("userId");
    
        User user = userRepository.findByUserId(userId);
    
        if (user != null) {
            String verificationCode = RandomCodeGenerator.generateCode();
    
            session.setAttribute("verificationCode", verificationCode);
            session.setAttribute("resetEmail", user.getEmail());
    
            String emailContent = "비밀번호를 재설정하려면 다음 인증 코드를 입력하세요: " + verificationCode;
            mailer.sendMail(user.getEmail(), EMAIL_SUBJECT, emailContent, new SMTPAuthenticator());
    
            response.put("success", true);
            response.put("userId", userId);
        } else {
            response.put("success", false);
            response.put("message", "사용자 아이디가 존재하지 않습니다.");
        }
    
        return response;
    }

    @GetMapping("/jmt/verifyCode")
    public String showVerificationCodeForm() {
        return "verificationCodeForm";
    }

    @PostMapping("/jmt/verifyCode")
    public String verifyCode(@RequestParam Map<String, String> param, Model model) {
        String enteredCode = param.get("code");
        String savedCode = (String) session.getAttribute("verificationCode"); // 비밀번호 재설정 인증코드
        String resetEmail = (String) session.getAttribute("resetEmail"); // 저거 보낸 사용자 이메일 주소

        if (savedCode != null && savedCode.equals(enteredCode)) {
            model.addAttribute("email", resetEmail);

            return "saveNewPassword";
        } else {
            model.addAttribute("error", "인증 코드가 올바르지 않습니다.");
            return "verificationCodeForm";
        }
    }

    @PostMapping("/jmt/saveNewPassword")
    public String saveNewPassword(@RequestParam String newPassword, Model model) {
        String email = (String) session.getAttribute("resetEmail");
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPw(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            session.invalidate();
            return "redirect:/jmt/signin"; 
        } else {
            String errorMessage = "해당 이메일로 등록된 사용자가 없습니다. 다시 시도해 주세요.";
            model.addAttribute("error", errorMessage);
            return "resetPasswordForm"; 
        }
    }

    @GetMapping("/jmt/mylist")
    public String mylist() {
        return "mylist";
    }

}
