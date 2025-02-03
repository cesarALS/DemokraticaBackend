package com.demokratica.backend.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "plans")
@Data
public class Plan {
    @Id
    @Column(name = "plan_id")
    private Long id;
    public enum Type {GRATUITO, PLUS, PREMIUM, PROFESIONAL};
    private LocalDateTime expirationDate; //Puede ser null en el caso del plan gratuito

    @OneToOne(mappedBy = "plan")
    private User user;
}
