package com.example.plantie;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.squareup.picasso.Picasso;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView dateTextView;
    private TextView weatherTextView;
    private ImageView weatherIcon;
    private int color;
    private ImageButton buttonChooseColor;
    private int brightness = 0;
    private ImageButton buttonBrightness_up;
    private ImageButton buttonBrightness_down;
    private int autoValue = 0;
    private int manualValue = 0;
    private Button autoButton;
    private Button manualButton;
    private Button fixButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateTextView = findViewById(R.id.date);
        weatherTextView = findViewById(R.id.weather);
        weatherIcon = findViewById(R.id.weatherIcon);
        buttonChooseColor = findViewById(R.id.button_choose_color);
        buttonBrightness_up = findViewById(R.id.btn_brightness_up);
        buttonBrightness_down = findViewById(R.id.btn_brightness_down);
        autoButton = findViewById(R.id.autoButton);
        manualButton = findViewById(R.id.manualButton);
        fixButton = findViewById(R.id.fixButton);

        // 현재 날짜 설정
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);

        // 날씨 정보 가져오기
        getWeatherInfo();

        // 조명 색깔 선택하기
        buttonChooseColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorDialog();
            }
        });

        // 조명 밝기 up
        buttonBrightness_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBrightness(1);
            }
        });

        // 조명 밝기 down
        buttonBrightness_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBrightness(-1);
            }
        });

        // 자동 물주기 버튼 클릭
        autoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoValue = 1;
                showTimePickerDialog();
                autoValue = 0;
            }
        });

        // 수동 물주기 버튼 클릭
        manualButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manualValue = 1;
                Toast.makeText(MainActivity.this, "물주기!", Toast.LENGTH_SHORT).show();
                manualValue = 0;
            }
        });

        // 해결법 버튼 클릭 이벤트 처리
        fixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatBotActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getWeatherInfo() {
        OkHttpClient client = new OkHttpClient();
        String value =  BuildConfig.WEATHER_API_KEY;
        String cityName = "Seoul";
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + value + "&units=metric";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String responseData = response.body().string();
                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            updateWeatherInfo(responseData);
                        }
                    });
                }
            }
        });
    }

    private void updateWeatherInfo(String jsonData) {
        JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
        JsonObject main = jsonObject.getAsJsonObject("main");
        JsonObject weather = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject();

        String temperature = main.get("temp").getAsString() + "°C";
        String iconCode = weather.get("icon").getAsString();

        weatherTextView.setText(temperature);
        updateWeatherIcon(iconCode);
    }

    private void updateWeatherIcon(String iconCode) {
        String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
        Picasso.get().load(iconUrl).into(weatherIcon);
    }

    private void showColorDialog() {
        final String[] colors = {"빨간색", "주황색", "노란색", "초록색", "파란색", "남색", "보라색"};
        final int[] colorValues = {
                Color.RED, Color.rgb(255, 165, 0), Color.YELLOW,
                Color.GREEN, Color.BLUE, Color.rgb(75, 0, 130), Color.rgb(238, 130, 238)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("조명 색깔 선택");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 색상 값 저장
                color = colorValues[which];
            }
        });
        builder.show();
    }

    private void changeBrightness(int change) {
        brightness += change;

        if (brightness < 0) {
            brightness = 0;
        }

        Toast.makeText(this, "현재 밝기 : " + brightness, Toast.LENGTH_SHORT).show();
    }

    private void showTimePickerDialog() {
        final int[] selectedHour = {0};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("시간 선택");

        // Custom view for time picker
        View customLayout = getLayoutInflater().inflate(R.layout.dialog_time_picker, null);
        builder.setView(customLayout);

        final ImageButton hourPlus = customLayout.findViewById(R.id.hour_plus);
        final ImageButton hourMinus = customLayout.findViewById(R.id.hour_minus);
        final TextView hourText = customLayout.findViewById(R.id.hour_text);

        hourPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedHour[0]++;
                hourText.setText(String.valueOf(selectedHour[0]));
            }
        });

        hourMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedHour[0] > 0) {
                    selectedHour[0]--;
                    hourText.setText(String.valueOf(selectedHour[0]));
                }
            }
        });

        builder.setPositiveButton("확인", (dialog, which) -> saveTime(selectedHour[0]));
        builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveTime(int hour) {
        // 시간 값을 저장하는 함수
        String time = hour + "시간";
        Toast.makeText(this, "선택된 시간: " + time, Toast.LENGTH_SHORT).show();
    }
}