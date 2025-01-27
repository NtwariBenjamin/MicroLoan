package com.benjamin.LoanService.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@Entity
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @NotNull(message = "The MSISDN Cannot be Null")
    @Column(unique = true)
    @JsonProperty
    private String msisdn;

    @JsonProperty
    @Column
    @NotNull(message = "Password Cannot Be Null")
    private String password;

    @NotNull(message = "Names must be Included!")
    @JsonProperty
    @Column
    private String name;

    @JsonProperty
    @Column
    private String location;

    @Enumerated(EnumType.STRING)
    @JsonProperty
    @NotNull
    @Column
    private Role role;

    @Column
    @JsonProperty
    private Boolean loanEligibility;

}
