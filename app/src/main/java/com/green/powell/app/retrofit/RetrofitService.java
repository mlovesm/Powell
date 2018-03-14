package com.green.powell.app.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.green.powell.app.menu.MainActivity;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
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

    @GET("local/geo/transcoord")
    Call<MapResponseData> geoTransCoord(@Query("x") double longitude, @Query("y") double latitude
        , @Query("apikey") String apikey, @Query("fromCoord") String fromCoord, @Query("toCoord") String toCoord, @Query("output") String output);

    @GET("{title}/{sub}")
    Call<Datas> listData(@Path("title") String title, @Path("sub") String sub);

    @GET("{title}/{sub}/{path}")
    Call<Datas> listData(@Path("title") String title, @Path("sub") String sub, @Path(value = "path", encoded = true) String path);

    @GET("{title}/{sub}/{path}/{path2}")
    Call<Datas> listData(@Path("title") String title, @Path("sub") String sub, @Path("path") String path, @Path(value = "path2", encoded = true) String path2);

    @GET("{title}/{sub}/{path}/{path2}/{path3}")
    Call<Datas> listData(@Path("title") String title, @Path("sub") String sub, @Path(value = "path", encoded = true) String path,
                         @Path(value = "path2", encoded = true) String path2, @Path(value = "path3", encoded = true) String path3);

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

    @Multipart
    @POST("/v1/vision/face")
    Call<Datas> naverRepo2(@Header("X-Naver-Client-Id") String id
            ,@Header("X-Naver-Client-Secret") String secret
            ,@Part MultipartBody.Part file);

    static final int CONNECT_TIMEOUT = 15;
    static final int WRITE_TIMEOUT = 15;
    static final int READ_TIMEOUT = 15;

    RequestInterceptor inter = new RequestInterceptor();
    NetworkInterceptor netInter = new NetworkInterceptor();
    OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(inter)
            .build();

    public String DATE_FORMAT = "yyyy-MM-dd";

    GsonBuilder gsonBuilder = new GsonBuilder()
        .setDateFormat(DATE_FORMAT);
    Gson gson = gsonBuilder.create();

    public static final Retrofit daum_api = new Retrofit.Builder()
            .baseUrl("https://apis.daum.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static final Retrofit rest_api = new Retrofit.Builder()
            .baseUrl(MainActivity.ipAddress+MainActivity.contextPath+"/rest/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build();

//    int proxyPort = 8080;
//    String proxyHost = "proxyHost";
//    final String username = "username";
//    final String password = "password";
//
//    Authenticator proxyAuthenticator = new Authenticator() {
//        @Override public Request authenticate(Route route, Response response) throws IOException {
//            String credential = Credentials.basic(username, password);
//            return response.request().newBuilder()
//                    .header("Proxy-Authorization", credential)
//                    .build();
//        }
//    };
//
//    OkHttpClient client = new OkHttpClient.Builder()
//            .connectTimeout(60, TimeUnit.SECONDS)
//            .writeTimeout(60, TimeUnit.SECONDS)
//            .readTimeout(60, TimeUnit.SECONDS)
//            .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)))
//            .proxyAuthenticator(proxyAuthenticator)
//            .build();


}
