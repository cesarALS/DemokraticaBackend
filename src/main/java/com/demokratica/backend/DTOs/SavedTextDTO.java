package com.demokratica.backend.DTOs;

import java.util.ArrayList;

import com.demokratica.backend.RestControllers.SessionController.TagDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedTextDTO extends TextCreationDTO {
    
    private Long id;
    
    public SavedTextDTO (Long id, String content, ArrayList<TagDTO> tags) {
        super(content, tags);
        this.id = id;
    }
}
