package com.demokratica.backend.Model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "session_tags")
@Data
public class SessionTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long id;
    @ManyToOne
    @JoinColumn(name = "session_id")
    private Session session;
    @Column(name = "tag_text", length = 30, nullable = false)
    private String tagText;

    //TODO: incluir la ID en el equals y hashCode, si es posible (podría ser imposible porque podría ser nulo cuando no se ha guardado en la BD)
    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || o.getClass() != getClass()) return false;
        SessionTag that = (SessionTag) o;
        return (that.session.getId() == this.session.getId() && this.tagText.equals(that.tagText));
    }
    @Override
    public int hashCode() {
        return Objects.hash(session.getId(), tagText);
    }
}
