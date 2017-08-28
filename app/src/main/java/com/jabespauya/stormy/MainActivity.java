package com.jabespauya.stormy;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jabespauya.stormy.Weather.Current;
import com.jabespauya.stormy.Weather.Day;
import com.jabespauya.stormy.Weather.Forecast;
import com.jabespauya.stormy.Weather.Hour;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();


    private String api = "3c9a8b700c60128a24cf8379b5424ac2";
    private double latitude = 37.8267;
    private double longitude = -122.4233;
    private String forecastUrl;

    private Forecast mForecast;

    @BindView(R.id.timeLabel) TextView mTimeLabel;
    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @BindView(R.id.humidityValue) TextView mHumidityValue;
    @BindView(R.id.precipValue) TextView mPrecipValue;
    @BindView(R.id.summaryLabel) TextView mSummaryLabel;
    @BindView(R.id.iconImageView) ImageView mIconImageView;
    @BindView(R.id.locationLabel) TextView mLocationLabel;
    @BindView(R.id.refreshView) ImageView mRefreshView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mProgressBar.setVisibility(View.INVISIBLE);

        mRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getForecastServiceApi(latitude,longitude,api);
            }
        });


        getForecastServiceApi(latitude,longitude,api);

    }

    @OnClick(R.id.btnDaily)
    public void DailyForecast(View view){
        Intent intent = new Intent(this, DailyForecastActivity.class);
        startActivity(intent);
    }

    private void getForecastServiceApi(double latitude, double longitude, String api){

        //check if network is available
        forecastUrl = "https://api.darksky.net/forecast/" + api + "/" + latitude + "," + longitude;

        if(isNetworkAvailable()) {

            toggleRefresh();

            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(forecastUrl)
                    .build();

            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    AlertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                toggleRefresh();
                            }
                        });

                        String jsonData = response.body().string();
                        if (response.isSuccessful()) {
                            mForecast = parseForecastDetails(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });

                        } else {
                            AlertUserAboutError();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception caught :", e);
                    }
                    catch (JSONException e){
                        Log.e(TAG, "Exception caught :", e);
                    }
                }
            });
        }//end of isNetworkAvailable
        else{
            AlertUserAboutError();
        }

    }

    private void toggleRefresh() {
        if(mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshView.setVisibility(View.INVISIBLE);
        }else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshView.setVisibility(View.VISIBLE);
        }
    }


    private Forecast parseForecastDetails(String jsonData) throws JSONException{
        Forecast forecast = new Forecast();
        forecast.setCurrent(getCurrentDetails(jsonData));
        forecast.setDailyForecast(getDailyForecast(jsonData));
        forecast.setHourlyForecast(getHourlyForecast(jsonData));

        return forecast;
    }

    private Day[] getDailyForecast(String jsonData) throws JSONException {

        JSONObject forecast = new JSONObject(jsonData);

        //jsonObjects
        String timezone = forecast.getString("timezone");
        JSONObject daily = forecast.getJSONObject("daily");
        JSONArray data = daily.getJSONArray("data");

        Day[] days = new Day[data.length()];

        for (int x = 0; x < data.length(); x++) {
            JSONObject jsonDay = data.getJSONObject(x);
            Day day = new Day();
            day.setSummary(jsonDay.getString("summary"));
            day.setTemperature(jsonDay.getDouble("temperatureMax"));
            day.setIcon(jsonDay.getString("icon"));
            day.setTime(jsonDay.getLong("time"));
            day.setTimezone(timezone);

            days[x] = day;

        }

         return days;

    }

    private Hour[] getHourlyForecast(String jsonData) throws JSONException{
        JSONObject forecast = new JSONObject(jsonData);

        //jsonObjects
        String timezone = forecast.getString("timezone");
        JSONObject hourly = forecast.getJSONObject("hourly");
        JSONArray data = hourly.getJSONArray("data");


        Hour[] hours = new Hour[data.length()];

        for(int i=0; i < data.length(); i++){
            JSONObject hourlyObject = data.getJSONObject(i);

            Hour hour = new Hour();
            hour.setSummary(hourlyObject.getString("summary"));
            hour.setTemperature(hourlyObject.getDouble("temperature"));
            hour.setIcon(hourlyObject.getString("icon"));
            hour.setTime(hourlyObject.getLong("time"));
            hour.setTimezone(timezone);

            hours[i] = hour;

        }

        return hours;
    }

    private Current getCurrentDetails(String jsonData) throws JSONException {

        JSONObject forecast = new JSONObject(jsonData);

        //jsonObjects
        String timezone = forecast.getString("timezone");
        Log.d(TAG,timezone);

        //get the another json format from api
        JSONObject currently = forecast.getJSONObject("currently");

        Current mCurrent = new Current();
        mCurrent.setTimezone(timezone);
        mCurrent.setHumidity(currently.getDouble("humidity"));
        mCurrent.setSummary(currently.getString("summary"));
        mCurrent.setTime(currently.getLong("time"));
        mCurrent.setPrecipChance(currently.getDouble("precipProbability"));
        mCurrent.setIcon(currently.getString("icon"));
        mCurrent.setTemperature(currently.getDouble("temperature"));

        Log.d(TAG, mCurrent.getFormattedTime());
        Log.d(TAG,String.valueOf(mCurrent.getIcon() + mCurrent.getIconId()) + "Current value");
        return mCurrent;
    }

    //check if network is available
    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailable = true;
        }
        return isAvailable;
    }

    private void updateDisplay() {
        Current mCurrent = mForecast.getCurrent();
        mTemperatureLabel.setText(mCurrent.getTemperature() + "");
        mHumidityValue.setText(mCurrent.getHumidity() + "");
        mPrecipValue.setText(mCurrent.getPrecipChance() + "%");
        mSummaryLabel.setText(mCurrent.getSummary());
        mTimeLabel.setText("At :" + mCurrent.getFormattedTime() + " it will be");
        //mLocationLabel.setText(mCurrentWeather.getTimezone());


        Drawable drawable = getResources().getDrawable(mCurrent.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }



    private void AlertUserAboutError() {
        AlertDialogFragment alertDialogFragment = new AlertDialogFragment();
        alertDialogFragment.show(getFragmentManager(), getString(R.string.error_dialog));
    }


}
