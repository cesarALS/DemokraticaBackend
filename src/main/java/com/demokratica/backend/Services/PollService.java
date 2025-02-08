package com.demokratica.backend.Services;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.demokratica.backend.Model.PollTag;
import com.demokratica.backend.Model.Poll;
import com.demokratica.backend.Model.PollOption;
import com.demokratica.backend.Model.Session;
import com.demokratica.backend.Model.User;
import com.demokratica.backend.Model.UserVote;
import com.demokratica.backend.Repositories.PollOptionsRepository;
import com.demokratica.backend.Repositories.PollsRepository;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.Repositories.UserVoteRepository;
import com.demokratica.backend.Repositories.UsersRepository;
import com.demokratica.backend.Repositories.SessionsRepository;
import com.demokratica.backend.RestControllers.ActivitiesController.NewPollDTO;
import com.demokratica.backend.RestControllers.ActivitiesController.PollOptionDTO;
import com.demokratica.backend.RestControllers.ActivitiesController.VoterDTO;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;

import jakarta.transaction.Transactional;

@Service
public class PollService {
    
    @Autowired
    private SessionsRepository sessionsRepository;
    @Autowired
    private PollsRepository pollsRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private UserVoteRepository userVoteRepository;
    @Autowired
    private PollOptionsRepository pollOptionsRepository;

    @Transactional
    public void createPoll(NewPollDTO dto, Long sessionId) {
        //TODO: verificar que el usuario que crea la votación en esa sesión tiene rol de dueño
        Session session = sessionsRepository.findById(sessionId).orElseThrow(() -> 
            new RuntimeException("Couldn't find session with id " + String.valueOf(sessionId)));

        Poll poll = new Poll();
        poll.setSession(session);

        String title = dto.title();
        String description = dto.description();
        
        poll.setTitle(title);
        poll.setDescription(description);
        poll.setStartTime(dto.startTime());
        poll.setEndTime(dto.endTime());
        
        poll.setTags(dto.tags().stream().map(tagDto -> {
            PollTag tag = new PollTag();
            String tagText = tagDto.text();
            tag.setTagText(tagText);
            tag.setPoll(poll);

            return tag;
        }).toList());

        poll.setOptions(dto.pollOptions().stream().map(optionDto -> {
            PollOption option = new PollOption();
            String optionDescription = optionDto.description();
            option.setDescription(optionDescription);
            option.setVotes(Collections.emptyList());
            option.setPoll(poll);

            return option;
        }).toList());
        
        pollsRepository.save(poll);
    }

    @Transactional
    public List<PollDTO> getSessionPolls (Long sessionId, String userEmail) {
        //TODO: asegurarse de que el usuario que trata de acceder a la sesión sí ha sido invitado a esa sesión
        //User user = usersRepository.findById(userEmail).orElseThrow(() -> 
          //  new RuntimeException("Couldn't find user with email " + userEmail + "in the database"));

        Session session = sessionsRepository.findById(sessionId).orElseThrow(() -> 
            new RuntimeException("Couldn't find session with session id " + sessionId));

        List<PollDTO> polls = session.getPolls().stream().map(poll -> {
            Long pollId = poll.getId();
            String pollTitle = poll.getTitle();
            String pollDescription = poll.getDescription();
            LocalDateTime startTime = poll.getStartTime();
            LocalDateTime endTime = poll.getEndTime();

            List<TagDTO> tags = poll.getTags().stream().map(tag -> {
                return new TagDTO(tag.getTagText());
            }).toList();

            List<PollOptionDTO> pollOptions = poll.getOptions().stream().map(option -> {
                
                String description = option.getDescription();
                List<VoterDTO> voters = option.getVotes().stream().map(vote -> {
                    return new VoterDTO(vote.getUser().getEmail());
                }).toList();
                Long id = option.getId();

                return new PollOptionDTO(id, description, voters);
            }).toList();

            return new PollDTO(pollId, pollTitle, pollDescription, startTime, endTime, tags, pollOptions);
        }).toList();

        return polls;
    }

    @Transactional
    public void voteForOption(Long pollId, String userEmail, Long optionId) {
        //TODO: asegurarse de que el usuario ha sido invitado a la sesión de la que forma parte esta votación
        //TODO: asegurarse de que solo se puede votar una vez. Cualquier intento de votar nuevamente no hace nada o cambia la opcion previa por la opcion nueva
        Poll poll = pollsRepository.findById(pollId).orElseThrow(() -> 
            new RuntimeException("Couldn't find poll with id " + String.valueOf(pollId) + " in the database"));

        User user = usersRepository.findById(userEmail).orElseThrow(() -> 
            new RuntimeException("Couldn't find user with email " + userEmail + " in the database"));

        PollOption pollOption = pollOptionsRepository.findById(optionId).orElseThrow(() ->
            new RuntimeException("Couldnt't find poll option with id " + String.valueOf(optionId) + " in the database"));

        UserVote vote = new UserVote();
        vote.setPoll(poll);
        vote.setUser(user);
        vote.setOption(pollOption);
        userVoteRepository.save(vote);
    }

    public record PollDTO (Long id, String title, String description, LocalDateTime startTime, LocalDateTime endTime, List<TagDTO> tags, List<PollOptionDTO> pollOptions) {
    }

}
