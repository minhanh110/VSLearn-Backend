package com.vslearn.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserDataDTO {
  @NotBlank(message = "Tên đăng nhập không được để trống")
  @Size(min = 3, max = 20, message = "Tên đăng nhập phải từ 3-20 ký tự")
  private String username;

  @NotBlank(message = "Email không được để trống")
  @Size(max = 50, message = "Email không được vượt quá 50 ký tự")
  @Email(message = "Định dạng email không hợp lệ")
  private String email;

  @NotBlank(message = "Mật khẩu không được để trống")
  @Size(min = 6, max = 40, message = "Mật khẩu phải từ 6-40 ký tự")
  private String password;

  @NotBlank(message = "Họ không được để trống")
  @Size(max = 50, message = "Họ không được vượt quá 50 ký tự")
  private String firstName;

  @NotBlank(message = "Tên không được để trống")
  @Size(max = 50, message = "Tên không được vượt quá 50 ký tự")
  private String lastName;

  @Size(max = 15, message = "Số điện thoại không được vượt quá 15 ký tự")
  @Pattern(regexp = "^(|0[3|5|7|8|9][0-9]{8})$", message = "Số điện thoại không hợp lệ (ví dụ: 0123456789)")
  private String phoneNumber;

  private String userRole;
}