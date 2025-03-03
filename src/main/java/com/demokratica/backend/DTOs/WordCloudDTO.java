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
public class WordCloudDTO extends SavedActivityDTO {
    
    private ArrayList<String> words;

    public WordCloudDTO (Long id, String question, boolean alreadyParticipated, LocalDateTime startTime, 
                        LocalDateTime endTime, LocalDateTime creationTime, ArrayList<TagDTO> tags) {
        
        super(id, question, alreadyParticipated, startTime, endTime, creationTime, tags, Placeholder.ActivityType.WORD_CLOUD);
        this.words = new ArrayList<>(Collections.emptyList());
    }
}
