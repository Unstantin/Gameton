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
import java.util.Properties;

public class Controller implements Runnable {
    String BASE_URL = "https://games-test.datsteam.dev/";
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

        /*//КАКИЕ РАУНДЫ
        RoundsResponse roundsResponse = api.getRoundsInfo(token).execute().body();
        for (Round round : api.getRoundsInfo(token).execute().body().rounds) {
            System.out.println(round.startAt + " " + round.endAt + " " + round.name);
        }*/

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
            List<Attack> attacks = null;
            List<Build> builds = null;
            if(changingEnvironmentResponse != null) {
                attacks = createAttack(changingEnvironmentResponse);
            }
            if(zpots != null && changingEnvironmentResponse != null) {
                builds = createBuild(changingEnvironmentResponse, zpots);
            } else {
                System.out.println("BUILD НЕВОЗМОЖЕН " + zpots + " " + changingEnvironmentResponse);
            }
            try {
                ActionsResponse actionsResponse = api.makeAction(new ActionsDTO(attacks, builds, null), token).execute().body();
                if(actionsResponse != null) {
                    if(actionsResponse.acceptedCommands.attack != null) {
                        for(Attack a : actionsResponse.acceptedCommands.attack) {
                            System.out.println("A " + a.target.x + " " + a.target.y);
                        }
                    } else { System.out.println("Accepted attacks null");}
                    if(actionsResponse.acceptedCommands.build != null) {
                        for(Build b : actionsResponse.acceptedCommands.build) {
                            System.out.println("B " + b.coords.x + " " + b.coords.y);
                        }
                    } else { System.out.println("Accepted builds null");}
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

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
                            changingEnvironmentResponse.base.remove(b);
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
            for(Zombie z : changingEnvironmentResponse.zombies) {
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
                        changingEnvironmentResponse.base.remove(b);
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

    List<Build> createBuild(
                     ChangingEnvironmentResponse changingEnvironmentResponse,
                     List<Zpot> zpots) {
        System.out.println("ЗАШЛИ В BUILD");
        List<Build> builds = new ArrayList<>();
        List<Coords> alreadyCheakedCells = new ArrayList<>();
        for(int i = 0; i < changingEnvironmentResponse.player.gold; i++) {
            if(changingEnvironmentResponse.base != null) {
                for(Block b : changingEnvironmentResponse.base) {
                    List<Coords> neighbors = new ArrayList<>();
                    neighbors.add(new Coords(b.x + 1, b.y));
                    neighbors.add(new Coords(b.x - 1, b.y));
                    neighbors.add(new Coords(b.x, b.y + 1));
                    neighbors.add(new Coords(b.x, b.y - 1));
                    for(Coords n : neighbors) {
                        if(alreadyCheakedCells.contains(n)) {
                            continue;
                        }
                        boolean isGoodPlace = true;
                        if (changingEnvironmentResponse.zombies != null) {
                            for(Zombie z : changingEnvironmentResponse.zombies) {
                                if(n.x == z.x && n.y == z.y) {
                                    isGoodPlace = false;
                                    alreadyCheakedCells.add(n);
                                    break;
                                }
                            }
                        } else {
                            System.out.println("zombi is null");
                        }

                        if(!isGoodPlace) { continue; }

                        for(Block b_another : changingEnvironmentResponse.base) {
                            if(n.x == b_another.x && n.y == b_another.y) {
                                isGoodPlace = false;
                                alreadyCheakedCells.add(n);
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
                                    break;
                                }
                            }
                        } else { System.out.println("enemy blocks is null"); }

                        if(isGoodPlace) {
                            builds.add(new Build(n));
                        }
                        alreadyCheakedCells.add(n);
                    }
                }
            } else { System.out.println("base is null");}
        }

        return builds;
    }

    void createMoveBase() {

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
