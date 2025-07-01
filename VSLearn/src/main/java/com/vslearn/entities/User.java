package com.vslearn.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "user_name", columnNames = {"user_name"}),
        @UniqueConstraint(name = "user_email", columnNames = {"user_email"}),
        @UniqueConstraint(name = "phone_number", columnNames = {"phone_number"})
})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 100)
    @NotNull
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Size(max = 100)
    @NotNull
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 255)
    @NotNull
    @Column(name = "user_name", nullable = false)
    private String userName;

    @Size(max = 255)
    @Column(name = "user_password")
    private String userPassword;

    @Size(max = 255)
    @NotNull
    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Size(max = 12)
    @Column(name = "phone_number", length = 12)
    private String phoneNumber;

    @Size(max = 255)
    @Column(name = "user_avatar")
    private String userAvatar;

    @Size(max = 255)
    @NotNull
    @Column(name = "user_role", nullable = false)
    private String userRole;

    @Size(max = 20)
    @ColumnDefault("'LOCAL'")
    @Column(name = "provider", length = 20)
    private String provider;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "modify_time")
    private Instant modifyTime;

    @Size(max = 10)
    @Column(name = "active_code", length = 10)
    private String activeCode;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "created_by", columnDefinition = "int UNSIGNED")
    private Long createdBy;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by", columnDefinition = "int UNSIGNED")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", columnDefinition = "int UNSIGNED")
    private Long deletedBy;

}