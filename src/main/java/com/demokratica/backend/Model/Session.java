package com.demokratica.backend.Model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "sessions")
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long id;

    @Column(length = 80, nullable = false)
    private String title;
    @Column(length = 500, nullable = false)
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<SessionTag> tags;
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<Invitation> invitedUsers;
    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    private List<Activity> activities;
}
