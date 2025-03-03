package com.demokratica.backend.RestControllers;

import org.springframework.web.bind.annotation.RestController;

import com.demokratica.backend.DTOs.ActivityCreationDTO;
import com.demokratica.backend.DTOs.CreatedObjectDTO;
import com.demokratica.backend.DTOs.SavedTextDTO;
import com.demokratica.backend.DTOs.TextCreationDTO;
import com.demokratica.backend.DTOs.WordCloudDTO;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Poll;
import com.demokratica.backend.Model.PollOption;
import com.demokratica.backend.Model.PollTag;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;
import com.demokratica.backend.Security.AccessController;
import com.demokratica.backend.Security.SecurityConfig;
import com.demokratica.backend.Services.ActivitiesService;
import com.demokratica.backend.Services.PollService;
import com.demokratica.backend.Services.TextService;
import com.demokratica.backend.Services.WordCloudService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * Esta clase va a contener todos los endpoints para crear todos los distintos tipos de actividades.
 * Por ahora está previsto que solo sea una actividad, la votación común.
 * También retorna la información de todas las actividades de una sesión.
 * Otro endpoint que contiene es el de votar por una opción en una votación común.
 */
@RestController
public class ActivitiesController {
    
    private PollService pollService;
    private WordCloudService wordCloudService;
    private ActivitiesService activitiesService;
    private TextService textService;
    private AccessController accessController;
    public ActivitiesController (PollService pollService, WordCloudService wordCloudService, 
                                ActivitiesService activitiesService, TextService textService, 
                                AccessController accessController) {
        this.pollService = pollService;
        this.wordCloudService = wordCloudService;
        this.textService = textService;
        this.activitiesService = activitiesService;
        this.accessController = accessController;
    }

    @PostMapping("/api/sessions/{id}/polls")
    public ResponseEntity<?> createPoll(@PathVariable Long id, @RequestBody NewPollDTO newPollDTO) {
        accessController.checkIfCanCreateActivity(id);
        
        Poll createdPoll = pollService.createPoll(newPollDTO, id);
        CreatedPollResponse response = new CreatedPollResponse(createdPoll);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/api/sessions/{id}/wordclouds")
    public ResponseEntity<?> createWordCloud(@PathVariable Long id, @RequestBody ActivityCreationDTO newWordCloudDTO) {
        accessController.checkIfCanCreateActivity(id);
        WordCloudDTO response = wordCloudService.createWordCloud(id, newWordCloudDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/api/wordclouds/{id}")
    public ResponseEntity<?> postWord(@PathVariable Long id, @RequestBody WordDTO dto) {
        accessController.checkIfCanParticipateInWordCloud(id);
        wordCloudService.postWord(id, dto.word());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @DeleteMapping("/api/wordclouds/{id}")
    public ResponseEntity<?> deleteWordCloud(@PathVariable Long id) {
        accessController.checkIfCanDeleteWordClouds(id);
        wordCloudService.deleteWordCloud(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/api/sessions/{id}/texts")
    public ResponseEntity<?> createText(@PathVariable Long id, @RequestBody TextCreationDTO TextCreationDTO) {
        accessController.checkIfCanCreateActivity(id);
        SavedTextDTO response = textService.createText(id, TextCreationDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @DeleteMapping("/api/texts/{id}")
    public ResponseEntity<?> deleteText(@PathVariable Long id) {
        accessController.checkIfCanDeleteTexts(id);
        textService.deleteText(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/api/sessions/{id}")
    public ResponseEntity<?> getActivities(@PathVariable Long id) {
        Invitation.Role userRole = accessController.checkIfCanParticipate(id);
        ArrayList<CreatedObjectDTO> activities = activitiesService.getSessionActivities(id);
        GetActivitiesDTO response = new GetActivitiesDTO(id, userRole, activities);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/polls/{poll_id}")
    public ResponseEntity<?> voteForAnOption(@PathVariable Long poll_id, @RequestBody VoteDTO vote) {
        String userEmail = SecurityConfig.getUsernameFromAuthentication();
        Long optionId = vote.optionId();
        pollService.voteForOption(poll_id, userEmail, optionId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/api/polls/{poll_id}")
    public ResponseEntity<?> deletePoll (@PathVariable Long poll_id) {
        String userEmail = SecurityConfig.getUsernameFromAuthentication();
        pollService.deletePoll(userEmail, poll_id);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    public record NewPollDTO(String question, LocalDateTime startTime, LocalDateTime endTime, ArrayList<TagDTO> tags, ArrayList<PollOptionDTO> pollOptions) {
    }


    public record PollOptionDTO(Long id, String description, ArrayList<VoterDTO> voters) {
    }
    
    public record VoterDTO (String voterEmail) {
    }

    public record VoteDTO(Long optionId) {

    }

    public record PollOptionsResponse (Long id, String description) {
    }

    public record CreatedPollResponse (Long id, String question, LocalDateTime startTime, 
                                        LocalDateTime endTime, List<String> tags, List<PollOptionsResponse> pollOptions) {
        
        public CreatedPollResponse (Poll createdPoll) {
            this(createdPoll.getId(), createdPoll.getQuestion(), createdPoll.getStartTime(), 
                    createdPoll.getEndTime(), getFormattedTags(createdPoll.getTags()), 
                    getFormattedPollOptions((createdPoll.getOptions())));
        }

        public static List<String> getFormattedTags(List<PollTag> tagDTOs) {
            List<String> tags = tagDTOs.stream().map(dto -> {
                return dto.getTagText();
            }).toList();

            return tags;
        }

        public static List<PollOptionsResponse> getFormattedPollOptions(List<PollOption> pollOptions) {
            List<PollOptionsResponse> response = pollOptions.stream().map(dto -> {
                Long id = dto.getId();
                String description = dto.getDescription();
                return new PollOptionsResponse(id, description);
            }).toList();

            return response;
        }
        
    }

    public record WordDTO (String word) {
    }

    public record GetActivitiesDTO(Long sessionId, Invitation.Role userRole, ArrayList<CreatedObjectDTO> activities) {
    }
    
}
