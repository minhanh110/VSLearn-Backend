package com.vslearn.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "progress", indexes = {
        @Index(name = "sub_topic_id", columnList = "sub_topic_id"),
        @Index(name = "created_by", columnList = "created_by")
})
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sub_topic_id", nullable = false)
    private SubTopic subTopic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "is_complete", nullable = false)
    private Boolean isComplete = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}