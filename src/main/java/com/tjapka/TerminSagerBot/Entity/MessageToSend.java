package com.tjapka.TerminSagerBot.Entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
@Data
@Builder
@AllArgsConstructor
public class MessageToSend {
    Long chatId;
    String messageTyp;
    String messageToSend;
    String messageToLog;
    LocalTime time;

}
