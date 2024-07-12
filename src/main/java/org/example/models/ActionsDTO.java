package org.example.models;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ActionsDTO {
    public List<Attack> attack;
    public List<Build> build;
    public Coords moveBase;
}

