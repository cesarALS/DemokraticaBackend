package com.demokratica.backend.IntegrationTests;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.demokratica.backend.RestControllers.SessionController.InvitationDTO;
import com.demokratica.backend.RestControllers.SessionController.NewSessionDTO;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

public class NewSessionDTOBuilder {

    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ArrayList<InvitationDTO> invitationDTOs;
    private ArrayList<TagDTO> tagDTOs; 

    public NewSessionDTOBuilder (int sessionNumber) {
        //Inicializa todos los atributos con valores por defecto
        //Así cuando se llame el método build se creará un objeto en el que solo varían los atributos
        //que el usuario deseaba personalizar
        this.title = "title" + String.valueOf(sessionNumber);
        this.description = "title" + String.valueOf(sessionNumber);
        this.startTime = LocalDateTime.now();
        this.endTime = startTime.plusHours(1);
        this.invitationDTOs = new ArrayList<>(Collections.emptyList());
        this.tagDTOs = new ArrayList<>(List.of(new TagDTO("tag")));
    }

    public NewSessionDTOBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public NewSessionDTOBuilder setDescription(String description) {
        this.description = description;
        return this;
    }
    
    public NewSessionDTOBuilder setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public NewSessionDTOBuilder setEndTime (LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public NewSessionDTOBuilder setTagDTOs (ArrayList<TagDTO> tagDTOs) {
        this.tagDTOs = tagDTOs;
        return this;
    }

    public NewSessionDTOBuilder setInvitationDTOs (ArrayList<InvitationDTO> invitationDTOs) {
        this.invitationDTOs = invitationDTOs;
        return this;
    }

    public NewSessionDTO build() {
        return new NewSessionDTO(this.title, this.description, this.startTime, this.endTime, this.invitationDTOs, this.tagDTOs);
    }
}
