package com.snyper.keevaserver.Remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by stephen snyper on 9/29/2018.
 */

public class FCMRetrofitClient {
    private static Retrofit retrofit= null;
    public static Retrofit getClient(String baseUrl)
    {
        if (retrofit==null)
        {
            retrofit= new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }return retrofit;
    }
}
