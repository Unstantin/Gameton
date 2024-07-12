package org.example.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Zombie {
    public Integer attack;
    public String direction;
    public Integer health;
    public String id;
    public Integer speed;
    public String type;
    public Integer waitTurns;
    public Integer x;
    public Integer y;
}
