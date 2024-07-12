package org.example.responses;

import lombok.AllArgsConstructor;
import org.example.models.Round;

import java.util.List;

@AllArgsConstructor
public class RoundsResponse {
    public String gameName;
    public String now;

    public List<Round> rounds;

}
