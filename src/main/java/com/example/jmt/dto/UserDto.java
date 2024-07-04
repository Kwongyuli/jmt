package com.example.jmt.dto;

import java.io.Serializable;

import com.example.jmt.entity.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDto implements Serializable {
    private long id;
    
    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Pattern(regexp = "^[a-z0-9]{4,12}$", message = "아이디는 영문 소문자와 숫자 4~12자리여야 합니다.")
    private String userId;

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W)(?=\\S+$).{8,20}", message = "비밀번호는 영문 대,소문자와 숫자, 특수기호가 적어도 1개 이상씩 포함된 8자 ~ 20자의 비밀번호여야 합니다.")
    private String pw;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(min = 3, max = 8, message = "이름은 3~12자여야 합니다.")
    private String name;

    @Pattern(regexp = "^(?:\\w+\\.?)*\\w+@(?:\\w+\\.)+\\w+$", message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    private String email;

    public User toEntity() {
        User user = new User();
        user.setUserId(this.userId);
        user.setPw(this.pw);
        user.setName(this.name);
        user.setEmail(this.email);
        return user;
    }
}
