package org.example.responses;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ActionsResponse {
    public AcceptedCommands acceptedCommands;
    public List<String> errors;
}
