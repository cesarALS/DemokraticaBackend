package com.demokratica.backend.Model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Audited
@Table(name = "word_clouds")
@Data
public class WordCloud {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime creationTime;

    private String question;

    @NotAudited
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)    
    private List<WordCloudTag> tags;

    @NotAudited
    @OneToMany(mappedBy = "wordCloud", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserWord> words;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;
    
}
