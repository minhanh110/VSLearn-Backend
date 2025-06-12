package com.vslearn.dto.response;

import lombok.Data;

@Data
public class UserResponseDTO {
  private Long id;
  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private String phoneNumber;
  private String userAvatar;
  private String userRole;
  private Boolean isActive;
}