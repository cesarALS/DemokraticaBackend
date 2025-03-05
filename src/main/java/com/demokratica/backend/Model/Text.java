package com.demokratica.backend.Model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
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
@Data
@Table(name = "sessions_texts")
public class Text {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime creationTime;

    @Column(length = 1000)
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;
    
    @NotAudited
    @OneToMany(cascade = CascadeType.ALL)
    private List<TextTag> tags;
}
