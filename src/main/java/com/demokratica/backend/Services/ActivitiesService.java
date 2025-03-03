package com.demokratica.backend.Services;

import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.demokratica.backend.DTOs.SavedActivityDTO;


@Service
public class ActivitiesService {
    
    private PollService pollService;
    private WordCloudService wordCloudService;
    public ActivitiesService (PollService pollService, WordCloudService wordCloudService) {
        this.pollService = pollService;
        this.wordCloudService = wordCloudService;
    }

    public ArrayList<SavedActivityDTO> getSessionActivities() {
        //TODO: ordenarlas por fecha de creación
        ArrayList<SavedActivityDTO> activities = new ArrayList<>();

        return activities;
    }
}
