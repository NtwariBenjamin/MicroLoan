package com.example.userManagement.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "session")
@Entity
public class Session {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    @JsonProperty
    private  String  sessionId;

    @ManyToOne
    @JoinColumn(name = "msisdn", nullable = false)
    @JsonProperty
    private User user;

    @CreationTimestamp
    @Column
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column
    private Timestamp expiresAt;

    @Column
    @JsonProperty
    private Integer currentOption;

    @Column
    @JsonProperty
    @Lob
    private String data;

    @Column
    @JsonProperty
    private Boolean isActive;


}
