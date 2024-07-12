package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.models.*;
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
            List<Zpot> spawns;
            try {
                spawns = getSpawns(api);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            ChangingEnvironmentResponse changingEnvironmentResponse;
            try {
                changingEnvironmentResponse = getChanging(api);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            List<Attack> attacks = new ArrayList();
            if(!changingEnvironmentResponse.zombies.isEmpty()) {
                for(Zombie z : changingEnvironmentResponse.zombies) {
                    Integer currentHealth = z.health;
                    for(Block b : changingEnvironmentResponse.base) {
                        if(Math.sqrt(Math.pow(z.x - b.x, 2) + Math.pow(z.y - b.y, 2)) < b.range) {
                            attacks.add(new Attack(b.id, new Coords(z.x, z.y)));
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





            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void createBuild(Api api, List<Coords> coords) {

    }

    void createMoveBase() {}



    ChangingEnvironmentResponse getChanging(Api api) throws IOException {
        return api.getChangingEnvironment(token).execute().body();
    }

    List<Zpot> getSpawns(Api api) throws IOException, InterruptedException {
        return api.getConstantEnvironment(token).execute().body().zpots;
    }
}
