package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.models.Round;
import org.example.models.Zpot;
import org.example.responses.RoundsResponse;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
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

       /* //КАКИЕ РАУНДЫ
        RoundsResponse roundsResponse = api.getRoundsInfo(token).execute().body();
        for (Round round : api.getRoundsInfo(token).execute().body().rounds) {
            System.out.println(round.startAt + " " + round.endAt + " " + round.name);
        }*/

        System.out.println("AAA");

        while(true) {
            List<Zpot> zpotList = null;
            try {
                zpotList = api.getConstantEnvironment(token).execute().body().zpots;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            for(Zpot zpot : zpotList) {
                System.out.println(zpot.x + " " + zpot.y);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
