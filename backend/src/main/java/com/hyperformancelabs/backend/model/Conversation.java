package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "[Conversation]")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Integer conversationId;

    @NotNull(message = "Start time cannot be empty")
    @PastOrPresent(message = "Start time cannot be in the future")
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @NotBlank(message = "Status cannot be empty")
    @Pattern(regexp = "active|done", message = "Status must be 'active' or 'done'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Pattern(regexp = "bad|average|good|$", message = "Rating must be 'bad', 'average' or 'good'")
    @Column(name = "rating", length = 20)
    private String rating;

    @Column(name = "channel", length = 50)
    private String channel;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatMessage> chatMessages = new HashSet<>();
}
