package com.demokratica.backend.DTOs;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.demokratica.backend.Model.Placeholder;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

import io.jsonwebtoken.lang.Collections;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreatedWordCloudDTO extends ActivityDTO {
    
    private Long id;
    private Placeholder.ActivityType type;
    private ArrayList<String> words;

    public CreatedWordCloudDTO (Long id, String question, LocalDateTime startTime, LocalDateTime endTime, ArrayList<TagDTO> tags) {
        super(question, startTime, endTime, tags);
        this.id = id;
        this.type = Placeholder.ActivityType.WORD_CLOUD;
        this.words = new ArrayList<>(Collections.emptyList());
    }
}
