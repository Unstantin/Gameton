package org.example.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EnemyBlocks {
    public Integer attack;
    public Integer health;
    public Boolean isHead;
    public Coords lastAttack;
    public String name;
    public Integer x;
    public Integer y;
}
