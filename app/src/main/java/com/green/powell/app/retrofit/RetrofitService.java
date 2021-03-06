package com.green.powell.app.retrofit;

import com.green.powell.app.menu.MainActivity;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by GS on 2017-05-31.
 */
public interface RetrofitService {

    @FormUrlEncoded
    @POST("Login/loginCheckApp")
    Call<LoginDatas> loginData(@FieldMap Map<String, Object> fields);

    @FormUrlEncoded
    @POST("{title}/{sub}")
    Call<Datas> sendData(@Path("title") String title, @Path("sub") String sub, @FieldMap Map<String, Object> fields);

    @GET("{title}/{sub}")
    Call<Datas> listData(@Path("title") String title, @Path("sub") String sub);

    @GET("{title}/{sub}")
    Call<DatasB> listDataB(@Path("title") String title, @Path("sub") String sub);

    @GET("{title}/{sub}/{path}/{path2}/{path3}")
    Call<DatasB> listDataB(@Path("title") String title, @Path("sub") String sub, @Path(value = "path", encoded = true) String path,
                          @Path(value = "path2", encoded = true) String path2, @Path(value = "path3", encoded = true) String path3);

    @GET("{title}/{sub}/{path}")
    Call<Datas> listData(@Path("title") String title, @Path("sub") String sub, @Path(value = "path", encoded = true) String path);

    @GET("{title}/{sub}")
    Call<Datas> listDataQuery(@Path("title") String title, @Path("sub") String sub, @Query("pc_type") String pc_type);

    @GET("{title}/{sub}/{path}")
    Call<Datas> listDataQuery(@Path("title") String title, @Path("sub") String sub, @Path(value = "path", encoded = true) String path, @Query("pc_type") String pc_type);

    @GET("{title}/{sub}/{path}/{path2}")
    Call<Datas> listData(@Path("title") String title, @Path("sub") String sub, @Path("path") String path, @Path(value = "path2", encoded = true) String path2);

    @GET("{title}/{sub}/{path}/{path2}/{path3}")
    Call<Datas> listData(@Path("title") String title, @Path("sub") String sub, @Path(value = "path", encoded = true) String path,
                         @Path(value = "path2", encoded = true) String path2, @Path(value = "path3", encoded = true) String path3);

    @GET("{title}/{sub}/{path}/{path2}/{path3}/{path4}")
    Call<Datas> listData(@Path("title") String title, @Path("sub") String sub, @Path(value = "path", encoded = true) String path,
                         @Path(value = "path2", encoded = true) String path2, @Path(value = "path3", encoded = true) String path3, @Path("path4") String path4);

    @FormUrlEncoded
    @POST("{title}/{sub}Insert")
    Call<Datas> insertData(@Path("title") String title, @Path("sub") String sub, @FieldMap Map<String, Object> fields);

    @FormUrlEncoded
    @PUT("{title}/{sub}Update")
    Call<Datas> updateData(@Path("title") String title, @Path("sub") String sub, @FieldMap Map<String, Object> fields);

    @DELETE("{title}/{sub}Delete/{sub}")
    Call<Datas> deleteData(@Path("title") String title, @Path("sub") String sub, @Query("check_date") String check_date, @Query("chk_no") String chk_no);

    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);

    static final int CONNECT_TIMEOUT = 15;
    static final int WRITE_TIMEOUT = 15;
    static final int READ_TIMEOUT = 15;

    OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .build();

    public static final Retrofit rest_api = new Retrofit.Builder()
            .baseUrl(MainActivity.ipAddress+MainActivity.contextPath+"/rest/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();

}
