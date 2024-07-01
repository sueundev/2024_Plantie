package com.example.plantie;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface OpenAIService {
    @POST("/v1/chat/completions")
    Call<ChatResponse> sendMessage(@Body ChatRequest chatRequest);

    @Multipart
    @POST("/v1/images/analysis")
    Call<ImageAnalysisResponse> analyzeImage(@Part MultipartBody.Part image);
}
