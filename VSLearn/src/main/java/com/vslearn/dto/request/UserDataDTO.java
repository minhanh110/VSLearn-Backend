package com.vslearn.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDataDTO {
  @NotBlank
  @Size(min = 3, max = 20)
  private String username;

  @NotBlank
  @Size(max = 50)
  @Email
  private String email;

  @NotBlank
  @Size(min = 6, max = 40)
  private String password;

  @Size(max = 50)
  private String firstName;

  @Size(max = 50)
  private String lastName;

  @Size(max = 15)
  private String phoneNumber;

  private String userRole;
}