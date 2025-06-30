package com.vslearn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserLoginDTO {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 1, max = 50, message = "Tên đăng nhập phải từ 1-50 ký tự")
    String username;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 1, max = 100, message = "Mật khẩu không hợp lệ")
    String password;
}
