package com.example.userManagement.model.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotNull(message = "ID Cannot be Null")
    @JsonProperty
    private String id;

    @NotNull(message = "The MSISDN Cannot be Null")
    @JsonProperty
    private String msisdn;

    @NotNull(message = "Names must be Included!")
    @JsonProperty
    private String name;

    @JsonProperty
    @NotNull(message = "Password Cannot Be Null")
    private String password;

    @JsonProperty
    @NotNull(message = "Confirm Password Cannot Be Null")
    private String confirmPassword;


    @JsonProperty
    private String location;

    @Enumerated(EnumType.STRING)
    @JsonProperty
    @NotNull
    private Role role;

    @JsonProperty
    private Boolean loanEligibility;
}
