package com.tjapka.TerminSagerBot.Entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
public class MessageToSend {
    Long chatId;
    String messageTyp;
    String messageToSend;
    String messageToLog;
    LocalTime time;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MessageToSend that = (MessageToSend) o;
        return Objects.equals(chatId, that.chatId) && Objects.equals(messageTyp, that.messageTyp) && Objects.equals(messageToSend, that.messageToSend) && Objects.equals(messageToLog, that.messageToLog) && Objects.equals(time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chatId, messageTyp, messageToSend, messageToLog, time);
    }
}
