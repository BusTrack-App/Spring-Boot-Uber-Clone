package com.mera.apirest.dto.socket;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MessageDTO {

    @JsonProperty("new_message")
    private String newMessage;

}
