package com.demokratica.backend.Model;

import java.time.LocalDateTime;
import java.util.List;

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

    @OneToMany(cascade = CascadeType.ALL)
    private List<WordCloudTag> tags;

    @OneToMany(cascade = CascadeType.ALL)
    private List<UserWord> words;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;
    
}
