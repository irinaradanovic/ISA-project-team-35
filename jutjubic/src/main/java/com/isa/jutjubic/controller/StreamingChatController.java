package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.ChatMessageDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Controller
public class StreamingChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send/{videoId}")
    public void sendMessage(
            @DestinationVariable Integer videoId,
            ChatMessageDto message
    ) {

        //osiguravanje da neulogovan korisnik ne moze poslati chat stream poruku
        if (message.getSender() == null || message.getSender().trim().isEmpty()) {
            System.out.println("Odbijen pokušaj slanja od anonimnog korisnika na videu: " + videoId);
            return; // Nemoj uopste emitovati poruku ako nema posiljaoca
        }

        message.setTimestamp(LocalDateTime.now());

        //svi koji gledaju isti video su u istom chatu
        messagingTemplate.convertAndSend(
                "/topic/chat/" + videoId,   //jedan chat po videu
                message
        );
    }
}
