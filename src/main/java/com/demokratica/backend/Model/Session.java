package com.demokratica.backend.Model;

import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "session")
    private List<Tag> tags;
    @OneToMany(mappedBy = "session")
    private List<Invitation> invitedUsers;
    @OneToMany(mappedBy = "session")
    private List<Activity> activities;
}
