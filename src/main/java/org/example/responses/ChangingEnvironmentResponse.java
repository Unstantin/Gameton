package org.example.responses;

import lombok.AllArgsConstructor;
import org.example.models.Base;
import org.example.models.EnemyBlocks;
import org.example.models.Player;
import org.example.models.Zombie;

import java.util.List;

@AllArgsConstructor
public class ChangingEnvironmentResponse {
    public Base base;
    public EnemyBlocks enemyBlocks;
    public Player player;
    public String realmName;
    public Integer turnEndsInMs;
    public List<Zombie> zombies;
}
