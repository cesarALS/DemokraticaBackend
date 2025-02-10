package com.demokratica.backend.Model;

import java.util.List;

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
@Table(name = "poll_options")
@Data
public class PollOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poll_option_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "poll_id")
    Poll poll;

    @Column(name = "option_description", length = 80)
    private String description;

    @OneToMany(mappedBy = "option", cascade =  CascadeType.ALL)
    private List<UserVote> votes;
}
