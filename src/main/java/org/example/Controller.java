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
            try {
                getSpawns(api);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            ChangingEnvironmentResponse changingEnvironmentResponse;
            try {
                changingEnvironmentResponse = getChanging(api);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            attack(api, changingEnvironmentResponse.zombies, changingEnvironmentResponse.base.get(0));
            System.out.println();
        }
    }

    void attack(Api api, List<Zombie> zombies, Block base) {
        List<Attack> attacks = new ArrayList();

        for(Zombie z : zombies) {
            attacks.add(
                    new Attack(base.id, new Coords(z.x, z.y))
            );
            break;
        }
        ActionsDTO actionsDTO = new ActionsDTO(
            attacks, null, null
        );

        api.makeAction(actionsDTO, token);
    }

    void build(Api api, List<Coords> coords) {

    }


    ChangingEnvironmentResponse getChanging(Api api) throws IOException {
        return api.getChangingEnvironment(token).execute().body();
    }

    void getSpawns(Api api) throws IOException, InterruptedException {
        List<Zpot> zpotList;
        try {
            zpotList = api.getConstantEnvironment(token).execute().body().zpots;

            System.out.println("COORDS");
            for(Zpot zpot : zpotList) {
                System.out.println(zpot.x + " " + zpot.y);
            }
            System.out.println("END COORDS");
        } catch (NullPointerException e) {
            System.out.println("ZPOTS NULL");
        }
    }
}
