package com.demokratica.backend.DTOs;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.demokratica.backend.Model.Placeholder;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedTextDTO extends TextCreationDTO implements CreatedObjectDTO {
    
    private Long id;
    private LocalDateTime creationTime;
    private final Placeholder.ActivityType type = Placeholder.ActivityType.TEXT;
    
    public SavedTextDTO (Long id, String content, ArrayList<TagDTO> tags, LocalDateTime creationTime) {
        super(content, tags);
        this.id = id;
        this.creationTime = creationTime;
    }

    //Para implementar la interfaz. Este método nunca se debería llamar porque
    //el método que lo usa primero trata de llamar el getCreationTime y solo si
    //no está presente (que siempre lo está) trata de usar el startTime en su lugar
    public LocalDateTime getStartTime() {
        return null;
    }
}
