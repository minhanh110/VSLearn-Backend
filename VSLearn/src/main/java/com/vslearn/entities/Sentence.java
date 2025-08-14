package com.vslearn.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "sentences", indexes = {
        @Index(name = "sentence_topic_id", columnList = "sentence_topic_id")
})
public class Sentence {
    @Id
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Size(max = 255)
    @Column(name = "sentence_video")
    private String sentenceVideo;

    @Lob
    @Column(name = "sentence_meaning")
    private String sentenceMeaning;

    @Size(max = 255)
    @Column(name = "sentence_description")
    private String sentenceDescription;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sentence_topic_id", nullable = false)
    private Topic sentenceTopic;

    @Column(name = "created_at")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Sentence parent;
}