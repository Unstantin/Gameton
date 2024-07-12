package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.models.Round;
import org.example.responses.ParticipateResponse;
import org.example.responses.RoundsResponse;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.Properties;

public class Registration implements Runnable {
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

        while(true) {
            Response<ParticipateResponse> response;
            try {
                response = api.participate(token).execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println(response.code());
            if(response.code() != 200) {
                try {
                    System.out.println(response.errorBody().string());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
