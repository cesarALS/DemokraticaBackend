package com.demokratica.backend.DTOs;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.demokratica.backend.Model.Placeholder;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SavedActivityDTO extends ActivityCreationDTO {
    
    private Long id;
    private LocalDateTime creationTime;
    private Placeholder.EventStatus status;
    private Placeholder.ActivityType type;
    private boolean alreadyParticipated;
    private ArrayList<Object> results;

    public SavedActivityDTO (Long id, String question, boolean alreadyParticipated, LocalDateTime startTime, LocalDateTime endTime, 
                            LocalDateTime creationTime, ArrayList<TagDTO> tags, Placeholder.ActivityType type) {

        super(question, startTime, endTime, tags);
        this.alreadyParticipated = alreadyParticipated;
        this.id = id;
        this.creationTime = creationTime;
        this.status = Placeholder.getEventStatus(startTime, endTime);
        this.type = type;
    }
}
