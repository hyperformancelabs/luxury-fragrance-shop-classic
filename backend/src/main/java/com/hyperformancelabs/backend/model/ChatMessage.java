package com.hyperformancelabs.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "[ChatMessage]")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_message_id")
    private Integer chatMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "sender_id")
    private Integer senderId;

    @NotBlank(message = "Sender type cannot be empty")
    @Pattern(regexp = "employee|customer|bot", message = "Invalid sender type")
    @Column(name = "sender_type", nullable = false, length = 20)
    private String senderType;

    @Column(name = "receiver_id")
    private Integer receiverId;

    @NotBlank(message = "Receiver type cannot be empty")
    @Pattern(regexp = "employee|customer|bot", message = "Invalid receiver type")
    @Column(name = "receiver_type", nullable = false, length = 20)
    private String receiverType;

    @NotBlank(message = "Message content cannot be empty")
    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @NotNull(message = "Timestamp cannot be empty")
    @PastOrPresent(message = "Timestamp cannot be in the future")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
