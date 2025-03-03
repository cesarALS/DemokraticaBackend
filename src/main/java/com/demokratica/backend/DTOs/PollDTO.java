package com.demokratica.backend.DTOs;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.demokratica.backend.Model.Placeholder;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

public class PollDTO extends SavedActivityDTO {
    
    public PollDTO (Long id, String question, boolean alreadyParticipated, LocalDateTime startTime, 
                    LocalDateTime endTime, LocalDateTime creationTime, ArrayList<TagDTO> tags) {
        
        super(id, question, alreadyParticipated, startTime, endTime, creationTime, tags, Placeholder.ActivityType.POLL);
    }
}
