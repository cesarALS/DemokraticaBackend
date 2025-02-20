package com.demokratica.backend.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "polls")
@Data
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "poll_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;

    @Column(length = 80)
    private String title;
    @Column(length = 500)
    private String description;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    /*
     * Tanto los tags como las opciones deben ser únicos (bajo cierto criterio de unicidad).
     * Lo más lógico podría ser usar Set en lugar de List para garantizar eso.
     * Sin embargo, ya he tenido problemas varias veces al combinar Sets con relaciones bidireccionales porque
     * surge un bucle infinito: el Poll necesita a los PollTags para calcular el hashCode y los PollTags
     * necesitan el Poll para calcular su hashCode (porque la relación es bidireccional)
     * Por eso relego la responsabilidad de garantizar unicidad al servicio PollServicec
     */
    @OneToMany(mappedBy =  "poll", cascade = CascadeType.ALL)
    private List<PollTag> tags;

    @OneToMany(mappedBy = "poll", cascade = CascadeType.ALL)
    private List<PollOption> options;
}
