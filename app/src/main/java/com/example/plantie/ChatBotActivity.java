package com.example.plantie;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBotActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private EditText editTextMessage;
    private Button buttonSend;
    private ImageButton buttonCamera;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    private OpenAIService openAIService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        editTextMessage = findViewById(R.id.editText_message);
        buttonSend = findViewById(R.id.button_send);
        buttonCamera = findViewById(R.id.button_camera);
        recyclerView = findViewById(R.id.recyclerView);

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        openAIService = ApiClient.getClient().create(OpenAIService.class);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editTextMessage.getText().toString();
                if (!message.isEmpty()) {
                    sendMessageToChatGPT(message);
                }
            }
        });

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            sendPhotoToChatGPT(photo);
        }
    }

    private void sendMessageToChatGPT(String message) {
        chatMessages.add(new ChatMessage(message, ChatMessage.SENT_BY_USER));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        editTextMessage.setText("");

        ChatRequest chatRequest = new ChatRequest(message);

        Call<ChatResponse> call = openAIService.sendMessage(chatRequest);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getChoices().get(0).getMessage().getContent();
                    chatMessages.add(new ChatMessage(reply, ChatMessage.SENT_BY_BOT));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                } else {
                    try {
                        String error = "Error: " + response.errorBody().string();
                        chatMessages.add(new ChatMessage(error, ChatMessage.SENT_BY_BOT));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                String error = "Error: " + t.getMessage();
                chatMessages.add(new ChatMessage(error, ChatMessage.SENT_BY_BOT));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
        });
    }

    private void sendPhotoToChatGPT(Bitmap photo) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), byteArray);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);

        Call<ImageAnalysisResponse> call = openAIService.analyzeImage(body);
        call.enqueue(new Callback<ImageAnalysisResponse>() {
            @Override
            public void onResponse(Call<ImageAnalysisResponse> call, Response<ImageAnalysisResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String analysis = response.body().getAnalysis();
                    chatMessages.add(new ChatMessage(analysis, ChatMessage.SENT_BY_BOT));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                } else {
                    try {
                        String error = "Error: " + response.errorBody().string();
                        chatMessages.add(new ChatMessage(error, ChatMessage.SENT_BY_BOT));
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ImageAnalysisResponse> call, Throwable t) {
                String error = "Error: " + t.getMessage();
                chatMessages.add(new ChatMessage(error, ChatMessage.SENT_BY_BOT));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
        });
    }

}
