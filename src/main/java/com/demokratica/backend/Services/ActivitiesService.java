package com.demokratica.backend.Services;

import java.util.ArrayList;
import java.util.Comparator;

import org.springframework.stereotype.Service;

import com.demokratica.backend.DTOs.CreatedObjectDTO;

@Service
public class ActivitiesService {
    
    private PollService pollService;
    private WordCloudService wordCloudService;
    private TextService textService;
    public ActivitiesService (PollService pollService, WordCloudService wordCloudService,
                            TextService textService) {
        this.pollService = pollService;
        this.wordCloudService = wordCloudService;
        this.textService = textService;
    }

    public ArrayList<CreatedObjectDTO> getSessionActivities(Long sessionId) {
        ArrayList<CreatedObjectDTO> activities = new ArrayList<>();
        activities.addAll(pollService.getSessionPolls(sessionId));
        activities.addAll(wordCloudService.getSessionWordClouds(sessionId));
        activities.addAll(textService.getSessionTexts(sessionId));

        activities.sort(Comparator.comparing((CreatedObjectDTO dto) -> dto.getCreationTime() != null ? dto.getCreationTime() : dto.getStartTime(),
                    Comparator.nullsLast(Comparator.reverseOrder()))); 
        return activities;
    }
}
