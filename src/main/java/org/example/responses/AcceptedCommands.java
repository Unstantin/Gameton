package org.example.responses;

import lombok.AllArgsConstructor;
import org.example.models.Attack;
import org.example.models.Build;
import org.example.models.Coords;

import java.util.List;

@AllArgsConstructor
public class AcceptedCommands {
    public List<Attack> attack;
    public List<Build> build;
    public Coords moveBase;
}
