package com.demokratica.backend.Services;

import java.util.ArrayList;
import java.util.Comparator;

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

    public ArrayList<SavedActivityDTO> getSessionActivities(Long sessionId) {
        ArrayList<SavedActivityDTO> activities = new ArrayList<>();
        activities.addAll(pollService.getSessionPolls(sessionId));
        activities.addAll(wordCloudService.getSessionWordClouds(sessionId));

        activities.sort(Comparator.comparing((SavedActivityDTO dto) -> dto.getCreationTime() != null ? dto.getCreationTime() : dto.getStartTime(),
                    Comparator.nullsLast(Comparator.reverseOrder()))); 
        return activities;
    }
}
