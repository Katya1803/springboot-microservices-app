package com.example.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVerifiedEvent implements Serializable {

    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;

    @Builder.Default
    private Instant verifiedAt = Instant.now();

    @Builder.Default
    private String eventId = java.util.UUID.randomUUID().toString();
}