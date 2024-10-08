package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.models.*;
import org.example.responses.ActionsResponse;
import org.example.responses.ChangingEnvironmentResponse;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class Controller implements Runnable {
    String BASE_URL = "https://games.datsteam.dev/";
    String token = "";

    @Override
    public void run() {
        Properties prop = new Properties();
        try {
            prop.load(Main.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        token = prop.getProperty("token");

        Gson gson = new GsonBuilder().setLenient().create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        Api api = retrofit.create(Api.class);

        List<Block> prevBase = null;

        while(true) {
            List<Zpot> zpots;
            try {
                zpots = getZpots(api);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            ChangingEnvironmentResponse changingEnvironmentResponse;
            try {
                changingEnvironmentResponse = getChanging(api);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if(prevBase == null && changingEnvironmentResponse != null) {
                prevBase = changingEnvironmentResponse.base;
            }

            //ДЕБАХ
            /*if(changingEnvironmentResponse != null && zpots != null) {
                System.out.println("\nНаша база");
                try {
                    for(Block b: changingEnvironmentResponse.base) {
                        System.out.print("(" + b.x + " " + b.y + ") ");
                    }
                } catch(NullPointerException e) {
                    System.out.println("enemy blocks null");
                }

                System.out.println("\nБазы противника");
                try {
                    for(EnemyBlock b: changingEnvironmentResponse.enemyBlocks) {
                        System.out.print("(" + b.x + " " + b.y + ") ");
                    }
                } catch(NullPointerException e) {
                    System.out.println("enemy blocks null");
                }

                System.out.println("\nЗомби");
                try {
                    for(Zombie z: changingEnvironmentResponse.zombies) {
                        System.out.print("(" + z.x + " " + z.y + ") ");
                    }
                } catch (NullPointerException e) {
                    System.out.println("zombie is null");
                }

                System.out.println("\nспавны и стены");
                try {
                    for(Zpot z: zpots) {
                        System.out.print("(" + z.x + " " + z.y + ") ");
                    }
                } catch (NullPointerException e) {
                    System.out.println("спотов нет");
                }

            } else { System.out.println("ДЕБАГ ПОКА ВСЕ ПУСТО");}*/

            List<Attack> attacks = null;
            List<Coords> builds = null;
            if(changingEnvironmentResponse != null) {
                attacks = createAttack(changingEnvironmentResponse);
            }
            if(zpots != null && changingEnvironmentResponse != null) {
                builds = createBuild(changingEnvironmentResponse, zpots, prevBase);
            } else {
                System.out.println("BUILD НЕВОЗМОЖЕН " + zpots + " " + changingEnvironmentResponse);
            }



            //ДЕБАХ
            System.out.println("ЧТО ДОБАВИЛИ В БИЛД");
            if(builds != null) {
                for(Coords b: builds) {
                    System.out.print("(" + b.x + " " + b.y + ") ");
                }
            } else { System.out.println("БИЛД НЕ СУЩЕСТВУЕТ ВООБЩЕ"); }
            System.out.println();

            try {
                Coords move = null;
                if(changingEnvironmentResponse != null && zpots != null) {
                    move = createMoveBase(changingEnvironmentResponse);
                }
                ActionsResponse actionsResponse = api.makeAction(new ActionsDTO(attacks, builds, move), token).execute().body();
                System.out.println("ОШИБКИ:");
                if(actionsResponse != null && actionsResponse.errors != null) {
                    for(String err : actionsResponse.errors) {
                        System.out.println(err);
                    }
                } else { System.out.println("ОШИБОК НЕТ");}

            } catch (IOException e) {
                System.out.println("ЧЕ ТО НЕ ТАК С ОТПРАВКОЙ ACTIONS");
                throw new RuntimeException(e);
            }


            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            System.out.println();
            System.out.println();
        }
    }

    //ОРИГИНАЛЬНАЯ АТАКА
    List<Attack> createAttack(ChangingEnvironmentResponse changingEnvironmentResponse) {
        List<Block> alreadyAttacked = new ArrayList<>();
        List<Attack> attacks = new ArrayList<>();

        if(changingEnvironmentResponse.enemyBlocks != null) {
            for(EnemyBlock e : changingEnvironmentResponse.enemyBlocks) {
                if(e.isHead != null && e.isHead) {
                    Integer currentHealth = e.health;
                    for(Block b : changingEnvironmentResponse.base) {
                        if(alreadyAttacked.contains(b)) {
                            continue;
                        }
                        if(Math.sqrt(Math.pow(e.x - b.x, 2) + Math.pow(e.y - b.y, 2)) < b.range) {
                            attacks.add(new Attack(b.id, new Coords(e.x, e.y)));
                            alreadyAttacked.add(b);
                            if(currentHealth - b.attack <= 0) {
                                break;
                            } else {
                                currentHealth -= b.attack;
                            }
                        }
                    }
                }
            }
        }

        if(changingEnvironmentResponse.zombies != null) {
            List<Zombie> bombers = new ArrayList<>();
            List<Zombie> choos = new ArrayList<>();
            List<Zombie> liner = new ArrayList<>();
            List<Zombie> jagger = new ArrayList<>();
            List<Zombie> others = new ArrayList<>();

            for(Zombie z : changingEnvironmentResponse.zombies) {
                if(Objects.equals(z.type, "bomber")) {
                    bombers.add(z);
                }
                else if(Objects.equals(z.type, "chaos_knight")) {
                    choos.add(z);
                }
                else if(Objects.equals(z.type, "liner")) {
                    liner.add(z);
                }
                else if(Objects.equals(z.type, "juggernaut")) {
                    jagger.add(z);
                }
                else {
                    others.add(z);
                }
            }

            for(Zombie z : bombers) {
                Integer currentHealth = z.health;
                for(Block b : changingEnvironmentResponse.base) {
                    if(alreadyAttacked.contains(b)) {
                        continue;
                    }
                    if(Math.sqrt(Math.pow(z.x - b.x, 2) + Math.pow(z.y - b.y, 2)) < b.range) {
                        attacks.add(new Attack(b.id, new Coords(z.x, z.y)));
                        alreadyAttacked.add(b);
                        if(currentHealth - b.attack <= 0) {
                            break;
                        } else {
                            currentHealth -= b.attack;
                        }
                    }
                }
            }

            for(Zombie z : choos) {
                Integer currentHealth = z.health;
                for(Block b : changingEnvironmentResponse.base) {
                    if(alreadyAttacked.contains(b)) {
                        continue;
                    }
                    if(Math.sqrt(Math.pow(z.x - b.x, 2) + Math.pow(z.y - b.y, 2)) < b.range) {
                        attacks.add(new Attack(b.id, new Coords(z.x, z.y)));
                        alreadyAttacked.add(b);
                        if(currentHealth - b.attack <= 0) {
                            break;
                        } else {
                            currentHealth -= b.attack;
                        }
                    }
                }
            }

            for(Zombie z : liner) {
                Integer currentHealth = z.health;
                for(Block b : changingEnvironmentResponse.base) {
                    if(alreadyAttacked.contains(b)) {
                        continue;
                    }
                    if(Math.sqrt(Math.pow(z.x - b.x, 2) + Math.pow(z.y - b.y, 2)) < b.range) {
                        attacks.add(new Attack(b.id, new Coords(z.x, z.y)));
                        alreadyAttacked.add(b);
                        if(currentHealth - b.attack <= 0) {
                            break;
                        } else {
                            currentHealth -= b.attack;
                        }
                    }
                }
            }

            for(Zombie z : jagger) {
                Integer currentHealth = z.health;
                for(Block b : changingEnvironmentResponse.base) {
                    if(alreadyAttacked.contains(b)) {
                        continue;
                    }
                    if(Math.sqrt(Math.pow(z.x - b.x, 2) + Math.pow(z.y - b.y, 2)) < b.range) {
                        attacks.add(new Attack(b.id, new Coords(z.x, z.y)));
                        alreadyAttacked.add(b);
                        if(currentHealth - b.attack <= 0) {
                            break;
                        } else {
                            currentHealth -= b.attack;
                        }
                    }
                }
            }

            for(Zombie z : others) {
                Integer currentHealth = z.health;
                for(Block b : changingEnvironmentResponse.base) {
                    if(alreadyAttacked.contains(b)) {
                        continue;
                    }
                    if(Math.sqrt(Math.pow(z.x - b.x, 2) + Math.pow(z.y - b.y, 2)) < b.range) {
                        attacks.add(new Attack(b.id, new Coords(z.x, z.y)));
                        alreadyAttacked.add(b);
                        if(currentHealth - b.attack <= 0) {
                            break;
                        } else {
                            currentHealth -= b.attack;
                        }
                    }
                }
            }

            //старая тактика
            /*for(Zombie z : changingEnvironmentResponse.zombies) {
                Integer currentHealth = z.health;
                for(Block b : changingEnvironmentResponse.base) {
                    if(alreadyAttacked.contains(b)) {
                        continue;
                    }
                    if(Math.sqrt(Math.pow(z.x - b.x, 2) + Math.pow(z.y - b.y, 2)) < b.range) {
                        attacks.add(new Attack(b.id, new Coords(z.x, z.y)));
                        alreadyAttacked.add(b);
                        if(currentHealth - b.attack <= 0) {
                            break;
                        } else {
                            currentHealth -= b.attack;
                        }
                    }
                }
            }*/
        }

        if(changingEnvironmentResponse.enemyBlocks != null) {
            for(EnemyBlock e : changingEnvironmentResponse.enemyBlocks) {
                Integer currentHealth = e.health;
                for(Block b : changingEnvironmentResponse.base) {
                    if(alreadyAttacked.contains(b)) {
                        continue;
                    }
                    if(Math.sqrt(Math.pow(e.x - b.x, 2) + Math.pow(e.y - b.y, 2)) < b.range) {
                        attacks.add(new Attack(b.id, new Coords(e.x, e.y)));
                        alreadyAttacked.add(b);
                        if(currentHealth - b.attack <= 0) {
                            break;
                        } else {
                            currentHealth -= b.attack;
                        }
                    }
                }
            }
        }

        return attacks;
    }

    List<Coords> createBuild(
                     ChangingEnvironmentResponse changingEnvironmentResponse,
                     List<Zpot> zpots,
                     List<Block> prevBase) {
        System.out.println("ЗАШЛИ В BUILD");
        List<Coords> builds = new ArrayList<>();
        List<Coords> alreadyCheakedCells = new ArrayList<>();
        for(int i = 0; i < changingEnvironmentResponse.player.gold; i++) {
            if(changingEnvironmentResponse.turn > 100) {
                if(prevBase != null) {
                    for(Block b_prev : prevBase) {
                        boolean isStillAlive = false;
                        for(Block b_now: changingEnvironmentResponse.base) {
                            if(b_now == b_prev) {
                                isStillAlive = true;
                                break;
                            }
                        }
                        if(!isStillAlive) {
                            List<Coords> neighbors = new ArrayList<>();
                            neighbors.add(new Coords(b_prev.x + 1, b_prev.y));
                            neighbors.add(new Coords(b_prev.x - 1, b_prev.y));
                            neighbors.add(new Coords(b_prev.x, b_prev.y + 1));
                            neighbors.add(new Coords(b_prev.x, b_prev.y - 1));
                            for(Coords n : neighbors) {
                                boolean isAlredyCheaked = false;
                                for (Coords c : alreadyCheakedCells) {
                                    if (Objects.equals(c.x, n.x) && Objects.equals(c.y, n.y)) {
                                        isAlredyCheaked = true;
                                        break;
                                    }
                                }
                                if (isAlredyCheaked) continue;

                                boolean isGoodPlace = true;
                                if (changingEnvironmentResponse.zombies != null) {
                                    for (Zombie z : changingEnvironmentResponse.zombies) {
                                        if (Objects.equals(n.x, z.x) && Objects.equals(n.y, z.y)) {
                                            isGoodPlace = false;
                                            alreadyCheakedCells.add(n);
                                            System.out.println("НЕЛЬЗЯ ТК ЗОМБИ");
                                            break;
                                        }
                                    }
                                } else {
                                    System.out.println("zombi is null");
                                }

                                if (!isGoodPlace) {
                                    continue;
                                }

                                for (Block b_another : changingEnvironmentResponse.base) {
                                    if (Objects.equals(n.x, b_another.x) && Objects.equals(n.y, b_another.y)) {
                                        isGoodPlace = false;
                                        alreadyCheakedCells.add(n);
                                        System.out.println("НЕЛЬЗЯ ТК МЫ ЖЕ");
                                        break;
                                    }
                                }

                                if (!isGoodPlace) {
                                    continue;
                                }

                                for (Zpot z : zpots) {
                                    if (n.x == z.x && n.y == z.y) {
                                        isGoodPlace = false;
                                        alreadyCheakedCells.add(n);
                                        break;
                                    }
                                    if (z.type == "default") {
                                        if (
                                                (z.x + 1 == n.x && z.y == n.y) ||
                                                        (z.x - 1 == n.x && z.y == n.y) ||
                                                        (z.y + 1 == n.y && z.x == n.x) ||
                                                        (z.y - 1 == n.y && z.x == n.x)
                                        ) {
                                            isGoodPlace = false;
                                            alreadyCheakedCells.add(n);
                                            System.out.println("НЕЛЬЗЯ ТК СПАВНЕР");
                                            break;
                                        }
                                    }
                                }

                                if (!isGoodPlace) {
                                    continue;
                                }

                                if (changingEnvironmentResponse.enemyBlocks != null) {
                                    for (EnemyBlock e : changingEnvironmentResponse.enemyBlocks) {
                                        if (
                                                (e.x + 1 == n.x && e.y == n.y) ||
                                                        (e.x - 1 == n.x && e.y == n.y) ||
                                                        (e.y + 1 == n.y && e.x == n.x) ||
                                                        (e.y - 1 == n.y && e.x == n.x) ||
                                                        (e.x + 1 == n.x && e.y + 1 == n.y) ||
                                                        (e.x - 1 == n.x && e.y - 1 == n.y) ||
                                                        (e.x + 1 == n.x && e.y - 1 == n.y) ||
                                                        (e.x - 1 == n.x && e.y + 1 == n.y)
                                        ) {
                                            isGoodPlace = false;
                                            alreadyCheakedCells.add(n);
                                            System.out.println("НЕЛЬЗЯ ТК ПРОТИВНИК");
                                            break;
                                        }
                                    }
                                } else {
                                    System.out.println("enemy blocks is null");
                                }

                                if (isGoodPlace) {
                                    builds.add(new Coords(n.x, n.y));
                                }
                                alreadyCheakedCells.add(n);
                            }
                        }
                    }
                }
            }

            if(changingEnvironmentResponse.base != null) {
                for(Block b : changingEnvironmentResponse.base) {
                    List<Coords> neighbors = new ArrayList<>();
                    neighbors.add(new Coords(b.x + 1, b.y));
                    neighbors.add(new Coords(b.x - 1, b.y));
                    neighbors.add(new Coords(b.x, b.y + 1));
                    neighbors.add(new Coords(b.x, b.y - 1));
                    for(Coords n : neighbors) {
                        boolean isAlredyCheaked = false;
                        for(Coords c : alreadyCheakedCells) {
                            if(Objects.equals(c.x, n.x) && Objects.equals(c.y, n.y)) {
                                isAlredyCheaked = true;
                                break;
                            }
                        }
                        if(isAlredyCheaked) continue;

                        boolean isGoodPlace = true;
                        if(changingEnvironmentResponse.zombies != null) {
                            for(Zombie z : changingEnvironmentResponse.zombies) {
                                if(Objects.equals(n.x, z.x) && Objects.equals(n.y, z.y)) {
                                    isGoodPlace = false;
                                    alreadyCheakedCells.add(n);
                                    System.out.println("НЕЛЬЗЯ ТК ЗОМБИ");
                                    break;
                                }
                            }
                        } else {
                            System.out.println("zombi is null");
                        }

                        if(!isGoodPlace) { continue; }

                        for(Block b_another : changingEnvironmentResponse.base) {
                            if(Objects.equals(n.x, b_another.x) && Objects.equals(n.y, b_another.y)) {
                                isGoodPlace = false;
                                alreadyCheakedCells.add(n);
                                System.out.println("НЕЛЬЗЯ ТК МЫ ЖЕ");
                                break;
                            }
                        }

                        if(!isGoodPlace) { continue; }

                        for(Zpot z : zpots) {
                            if(n.x == z.x && n.y == z.y) {
                                isGoodPlace = false;
                                alreadyCheakedCells.add(n);
                                break;
                            }
                            if(z.type == "default") {
                                if(
                                        (z.x + 1 == n.x && z.y == n.y) ||
                                        (z.x - 1 == n.x && z.y == n.y) ||
                                        (z.y + 1 == n.y && z.x == n.x) ||
                                        (z.y - 1 == n.y && z.x == n.x)
                                ) {
                                    isGoodPlace = false;
                                    alreadyCheakedCells.add(n);
                                    System.out.println("НЕЛЬЗЯ ТК СПАВНЕР");
                                    break;
                                }
                            }
                        }

                        if(!isGoodPlace) { continue; }

                        if(changingEnvironmentResponse.enemyBlocks != null) {
                            for(EnemyBlock e : changingEnvironmentResponse.enemyBlocks) {
                                if(
                                        (e.x + 1 == n.x && e.y == n.y) ||
                                        (e.x - 1 == n.x && e.y == n.y) ||
                                        (e.y + 1 == n.y && e.x == n.x) ||
                                        (e.y - 1 == n.y && e.x == n.x) ||
                                        (e.x + 1 == n.x && e.y + 1 == n.y) ||
                                        (e.x - 1 == n.x && e.y - 1 == n.y) ||
                                        (e.x + 1 == n.x && e.y - 1 == n.y) ||
                                        (e.x - 1 == n.x && e.y + 1 == n.y)
                                ) {
                                    isGoodPlace = false;
                                    alreadyCheakedCells.add(n);
                                    System.out.println("НЕЛЬЗЯ ТК ПРОТИВНИК");
                                    break;
                                }
                            }
                        } else { System.out.println("enemy blocks is null"); }

                        if(isGoodPlace) {
                            builds.add(new Coords(n.x, n.y));
                        }
                        alreadyCheakedCells.add(n);
                    }
                }
            } else { System.out.println("base is null");}
        }

        return builds;
    }

    Coords createMoveBase(ChangingEnvironmentResponse changingEnvironmentResponse) {
        Block base=null;
        int x=0;
        int y=0;
        int i=0;
        Boolean left=false;
        Boolean right=false;
        Boolean up= false;
        Boolean down=false;
        if(changingEnvironmentResponse.base != null) {
            for(Block block : changingEnvironmentResponse.base) {
                if (block.isHead=true){
                    base = block;
                    i++;
                }
            }
        }

        if (changingEnvironmentResponse.enemyBlocks != null) {
            for (EnemyBlock enemyBlock :changingEnvironmentResponse.enemyBlocks){
                if((enemyBlock.x - base.x)<5){
                    left=true;
                }
                if((enemyBlock.x - base.x)< -5){
                    right=true;
                }
                if((enemyBlock.y - base.y)<5){
                    down=true;
                }
                if((enemyBlock.y - base.y)< -5){
                    up=true;
                }

            }
        }

        if(changingEnvironmentResponse.zombies != null) {
            for (Zombie zombie: changingEnvironmentResponse.zombies){
                if(zombie.type.equals("juggernaut ")){
                    if(zombie.direction.equals("left")){
                        left=true;
                    }
                    if(zombie.direction.equals("right")){
                        right=true;
                    }
                    if(zombie.direction.equals("down")){
                        down=true;
                    }
                    if(zombie.direction.equals("up")){
                        up=true;
                    }
                }
            }
        }

        if(left){x-=3;}
        if(right){x+=3;}
        if(down){y-=3;}
        if(up){y+=3;}
        if(changingEnvironmentResponse.base != null) {
            for(Block block : changingEnvironmentResponse.base){
                if(block.x.equals(x)&(block.y.equals(y))){
                    return new Coords(x,y);
                }
            }
        }
        return null;
    }

    ChangingEnvironmentResponse getChanging(Api api) throws IOException {
        return api.getChangingEnvironment(token).execute().body();
    }

    List<Zpot> getZpots(Api api) throws IOException, InterruptedException, NullPointerException {
        try {
            return api.getConstantEnvironment(token).execute().body().zpots;
        } catch (NullPointerException e) {
            System.out.println("zpots is null");
        }

        return null;
    }
}
