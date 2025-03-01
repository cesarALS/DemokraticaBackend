package com.demokratica.backend.DTOs;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.demokratica.backend.RestControllers.SessionController.TagDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ActivityDTO {
    
    private String question;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ArrayList<TagDTO> tags;

    public ActivityDTO (String question, LocalDateTime startTime, LocalDateTime endTime, ArrayList<TagDTO> tags) {
        this.question = question;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tags = tags;
    }
}