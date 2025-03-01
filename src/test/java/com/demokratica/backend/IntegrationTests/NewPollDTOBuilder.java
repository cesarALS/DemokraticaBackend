package com.demokratica.backend.IntegrationTests;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.demokratica.backend.RestControllers.ActivitiesController.NewPollDTO;
import com.demokratica.backend.RestControllers.ActivitiesController.PollOptionDTO;
import com.demokratica.backend.RestControllers.ActivitiesController.VoterDTO;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

//Ahora mismo solo uso el build con los valores por defecto, pero tener el builder
//más general vendrá bien cuando añada las pruebas para validar atributos
public class NewPollDTOBuilder {
    
    private String question;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ArrayList<TagDTO> tagDTOs;
    private ArrayList<PollOptionDTO> pollOptionDTOs;

    public NewPollDTOBuilder (int pollId) {
        question = "poll question " + String.valueOf(pollId);
        startTime = LocalDateTime.now();
        endTime = startTime.plusHours(1);

        tagDTOs = new ArrayList<>(List.of(new TagDTO("tag 1")));

        ArrayList<VoterDTO> voterDTOs = new ArrayList<>();
        pollOptionDTOs = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String pollOptionDescription = "Option " + String.valueOf(i);
            pollOptionDTOs.add(new PollOptionDTO(null, pollOptionDescription, voterDTOs));
        }
    }

    public NewPollDTO build () {
        return new NewPollDTO(this.question, this.startTime, this.endTime, this.tagDTOs, this.pollOptionDTOs);
    }

    public NewPollDTOBuilder setQuestion (String question) {
        this.question = question;
        return this;
    }

    public NewPollDTOBuilder setStartTime (LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public NewPollDTOBuilder setEndTime (LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public NewPollDTOBuilder setTagDTOs (ArrayList<TagDTO> tagDTOs) {
        this.tagDTOs = tagDTOs;
        return this;
    }

    public NewPollDTOBuilder setPollOptionDTOs (ArrayList<PollOptionDTO> pollOptionDTOs) {
        this.pollOptionDTOs = pollOptionDTOs;
        return this;
    }
}
