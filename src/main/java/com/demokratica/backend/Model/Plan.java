package com.demokratica.backend.Model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "plans")
@Data
public class Plan {
    @Id
    @GeneratedValue(strategy =  GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;
    private Type planType;
    public enum Type {GRATUITO, PLUS, PREMIUM, PROFESIONAL};
    private LocalDateTime expirationDate; //Puede ser null en el caso del plan gratuito
}
