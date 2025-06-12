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
@Table(name = "sentence", indexes = {
        @Index(name = "sentence_sub_topic_id", columnList = "sentence_sub_topic_id")
})
public class Sentence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "int UNSIGNED not null")
    private Long id;

    @Column(name = "sentence_gif")
    private String sentenceGif;

    @Lob
    @Column(name = "sentence_description")
    private String sentenceDescription;

    @Column(name = "sentence_type")
    private String sentenceType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sentence_sub_topic_id", nullable = false)
    private SubTopic sentenceSubTopic;

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

}