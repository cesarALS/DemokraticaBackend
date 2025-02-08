package com.demokratica.backend.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "poll_tags")
@Data
public class PollTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "poll_id")
    private Poll poll;
    @Column(name = "tag_text", length = 30, nullable = false)
    private String tagText;
}
