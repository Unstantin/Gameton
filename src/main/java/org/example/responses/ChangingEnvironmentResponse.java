package org.example.responses;

import lombok.AllArgsConstructor;
import org.example.models.Block;
import org.example.models.EnemyBlock;
import org.example.models.Player;
import org.example.models.Zombie;

import java.util.List;

@AllArgsConstructor
public class ChangingEnvironmentResponse {
    public List<Block> base;
    public List<EnemyBlock> enemyBlocks;
    public Player player;
    public String realmName;
    public Integer turn;
    public Integer turnEndsInMs;
    public List<Zombie> zombies;
}
