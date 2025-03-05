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
@Table(name = "polls")
@Data
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poll_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(length = 500)
    private String question;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime creationTime;

    @NotAudited
    @OneToMany(cascade = CascadeType.ALL)
    private List<PollTag> tags;
    
    @NotAudited
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
    private List<PollOption> options;
}
