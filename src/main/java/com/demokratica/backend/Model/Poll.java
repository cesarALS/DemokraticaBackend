package com.demokratica.backend.Model;

import java.util.Collections;
import java.util.List;

import javax.crypto.spec.OAEPParameterSpec;

import com.demokratica.backend.RestControllers.ActivitiesController.NewPollDTO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "polls")
@Data
@EqualsAndHashCode(callSuper = true)
public class Poll extends Activity {
    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
    private List<PollOption> options;
}
