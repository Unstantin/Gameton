package org.example.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Base {
    public Integer attack;
    public Integer health;
    public String id;
    public Boolean isHead;
    public Coords lastAttack;
    public Integer range;
    public Integer x;
    public Integer y;
}
