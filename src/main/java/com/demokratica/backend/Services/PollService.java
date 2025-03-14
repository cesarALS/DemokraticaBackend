package com.demokratica.backend.Services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.demokratica.backend.Model.PollTag;
import com.demokratica.backend.DTOs.SavedActivityDTO;
import com.demokratica.backend.Exceptions.SessionNotFoundException;
import com.demokratica.backend.Model.Invitation;
import com.demokratica.backend.Model.Placeholder;
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
import com.demokratica.backend.RestControllers.ActivitiesController.NewPollDTO;
import com.demokratica.backend.RestControllers.SessionController.TagDTO;
import com.demokratica.backend.Security.SecurityConfig;

import jakarta.transaction.Transactional;

@Service
public class PollService {
    
    private SessionsRepository sessionsRepository;
    private PollsRepository pollsRepository;
    private UsersRepository usersRepository;
    private UserVoteRepository userVoteRepository;
    private PollOptionsRepository pollOptionsRepository;

    public PollService (SessionsRepository sessionsRepository, PollsRepository pollsRepository,
                        UsersRepository usersRepository, UserVoteRepository userVoteRepository,
                        PollOptionsRepository pollOptionsRepository) {
        
        this.sessionsRepository = sessionsRepository;
        this.pollsRepository = pollsRepository;
        this.usersRepository = usersRepository;
        this.pollOptionsRepository = pollOptionsRepository;
        this.userVoteRepository = userVoteRepository;                 
    }

    @Transactional
    public Poll createPoll(NewPollDTO dto, Long sessionId) {
        //TODO: verificar que el usuario que crea la votación en esa sesión tiene rol de dueño
        Session session = sessionsRepository.findById(sessionId).orElseThrow(() -> 
            new RuntimeException("Couldn't find session with id " + String.valueOf(sessionId)));

        Poll poll = new Poll();
        poll.setSession(session);
        
        poll.setQuestion(dto.question());
        poll.setStartTime(dto.startTime());
        poll.setEndTime(dto.endTime());
        
        poll.setTags(dto.tags().stream().map(tagDto -> {
            PollTag tag = new PollTag();
            String tagText = tagDto.text();
            tag.setTagText(tagText);

            return tag;
        }).collect(Collectors.toList()));

        poll.setOptions(dto.pollOptions().stream().map(optionDto -> {
            PollOption option = new PollOption();
            String optionDescription = optionDto.description();
            option.setDescription(optionDescription);
            option.setVotes(new ArrayList<>());
            option.setPoll(poll);

            return option;
        }).collect(Collectors.toList()));
        
        return pollsRepository.save(poll);
    }

    @Transactional
    public ArrayList<SavedActivityDTO> getSessionPolls (Long sessionId) {
        String userEmail = SecurityConfig.getUsernameFromAuthentication();

        Session session = sessionsRepository.findById(sessionId).orElseThrow(() -> 
            new SessionNotFoundException(sessionId));

        ArrayList<SavedActivityDTO> polls = session.getPolls().stream().map(poll -> {
            Long pollId = poll.getId();
            String pollQuestion = poll.getQuestion();
            LocalDateTime startTime = poll.getStartTime();
            LocalDateTime creationTime = poll.getCreationTime();
            LocalDateTime endTime = poll.getEndTime();

            ArrayList<TagDTO> tags = poll.getTags().stream().map(tag -> {
                return new TagDTO(tag.getTagText());
            }).collect(Collectors.toCollection(ArrayList::new));

            ArrayList<PollResultDTO> pollResults = pollsRepository.getPollResults(pollId);
            Long totalVotes = pollResults.stream().mapToLong(PollResultDTO::numVotes).sum();
            Long nonVoters = pollsRepository.getTotalInvitedUsers(sessionId) - totalVotes;
            pollResults.add(new PollResultDTO(null, null, nonVoters));
            
            boolean alreadyParticipated = false;
            if (pollsRepository.findUserVoteByUserAndPoll(userEmail, pollId).isPresent()) {
                alreadyParticipated = true;
            }

            SavedActivityDTO dto = new SavedActivityDTO(pollId, pollQuestion, alreadyParticipated, startTime, 
                            endTime, creationTime, tags, Placeholder.ActivityType.POLL);
            dto.setResults(new ArrayList<>(pollResults));
            return dto;
        }).collect(Collectors.toCollection(ArrayList::new));

        return polls;
    }

    @Transactional
    public void deletePoll(String userEmail, Long id) {
        //Por razones de seguridad un usuario solo debería poder borrar una votación de una sesión de la que es dueño/host/anfitrión
        Optional<Invitation.Role> role = pollsRepository.findPollOwner(id, userEmail);
        if (role.isPresent() && role.get() == Invitation.Role.DUEÑO) {
            pollsRepository.deleteById(id);
        } else {
            throw new RuntimeException("User with email " + userEmail + " doesn't have permission to delete poll with id " + id);
        }
    }

    @Transactional
    public void voteForOption(Long pollId, String userEmail, Long optionId) {
        //TODO: asegurarse de que el usuario ha sido invitado a la sesión de la que forma parte esta votación
        Poll poll = pollsRepository.findById(pollId).orElseThrow(() -> 
            new RuntimeException("Couldn't find poll with id " + String.valueOf(pollId) + " in the database"));

        User user = usersRepository.findById(userEmail).orElseThrow(() -> 
            new RuntimeException("Couldn't find user with email " + userEmail + " in the database"));

        //Vamos a averiguar a qué sesión corresponde la votación, luego cuáles fueron los usuarios invitados a esa sesión y por último ver si 
        //nuestro usuario forma parte de la lista de invitados. El objetivo es evitar que un usuario participe en votaciones a las que no ha
        //sido invitado
        //TODO: hacer este código más elegante, porque ahora mismo todo está hecho muy manualmente. Hibernate debe tener formas de hacerlo 
        //con una sola línea de código o algo similar
        Session pollSession = sessionsRepository.findById(poll.getSession().getId()).orElseThrow(() -> 
            new RuntimeException("Couldn't find an associated session to the poll session"));
        ArrayList<Invitation> invitedUsers = new ArrayList<>(pollSession.getInvitations());
        ArrayList<Invitation> matchingInvitations = invitedUsers.stream().filter(invitation -> {
            return invitation.getInvitedUser().equals(user);    
        }).collect(Collectors.toCollection(ArrayList::new));
        if (matchingInvitations.size() > 1) {
            throw new RuntimeException("Expected the user with email " + user.getEmail() + 
                                    "to have been invited to session with id" + String.valueOf(pollSession.getId()) + " only once");
        }
        if (matchingInvitations.size() == 0) {
            throw new RuntimeException("User with email " + user.getEmail() + " wasn't invited to vote in the poll with id " + String.valueOf(poll.getId()));
        }

        //Primero miramos que la opción por la que va a votar sí exista. Si sí existe entonces podemos borrar el voto previo en caso de que 
        //haya uno y agregar un nuevo voto normalmente
        PollOption pollOption = pollOptionsRepository.findById(optionId).orElseThrow(() ->
            new RuntimeException("Couldnt't find poll option with id " + String.valueOf(optionId) + " in the database"));
        ArrayList<UserVote> existingVotes = new ArrayList<>(userVoteRepository.findByUserAndPoll(user, poll));
        if (existingVotes.size() > 1) {
            throw new RuntimeException("The user has voted more than once and therefore the DB is in an invalid state");
        }
        if (existingVotes.size() == 1) {
            userVoteRepository.deleteById(existingVotes.get(0).getId());
        }
        
        UserVote vote = new UserVote();
        vote.setPoll(poll);
        vote.setUser(user);
        vote.setOption(pollOption);
        userVoteRepository.save(vote);
    }

    public record PollDTO (Long id, Placeholder.ActivityType type, Placeholder.EventStatus activityStatus, 
                            boolean alreadyParticipated, String question,  
                            LocalDateTime startTime, LocalDateTime endTime, 
                            ArrayList<TagDTO> tags, ArrayList<PollResultDTO> pollResults) {
    }

    public record PollResultDTO (Long id, String description, Long numVotes) {
    }

}
