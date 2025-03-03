package com.demokratica.backend.DTOs;

import java.util.ArrayList;

import com.demokratica.backend.RestControllers.SessionController.TagDTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TextCreationDTO {
    
    private String content;
    private ArrayList<TagDTO> tags;

    public TextCreationDTO (String content, ArrayList<TagDTO> tags) {
        this.content = content;
        this.tags = tags;
    }
}
