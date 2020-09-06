package com.example.spintracks.dal;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.example.spintracks.dal.BeatsInfoFetcher.HttpFailureReason.*;

public final class BeatsInfoFetcher {
    public enum HttpFailureReason {
        BAD_RESPONSE, NETWORK_FAILURE
    };

    public interface BeatsInfoCallback {
        void onSuccess(String beatsInfoString);
        void onFailure(HttpFailureReason reason);
    }

    private static final String TAG = "BeatsInfoFetcher";
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();
    private static final ConcurrentHashMap<Long, String> pendingFetches = new ConcurrentHashMap<>();
    private static final Semaphore sem = new Semaphore(3);
    private ContentResolver contentResolver;

    public BeatsInfoFetcher(Context context) {
        contentResolver = context.getContentResolver();
    }

    private void fetchBeats(Uri contentUri, BeatsInfoCallback callback) {
        byte[] buf;
        try {
            ParcelFileDescriptor pfd = contentResolver.openFileDescriptor(contentUri, "r");
            int fileSize = (int)pfd.getStatSize();
            buf = new byte[fileSize];
            AutoCloseInputStream inputStream = new AutoCloseInputStream(pfd);
            int readSoFar = 0;
            while ((readSoFar += inputStream.read(buf)) < fileSize);
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return;
        }
        Request request = new Request.Builder()
                .url("https://spintracks.azurewebsites.net/beats")
                .post(RequestBody.create(buf, getMediaType(contentUri)))
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "HTTP request returned status code " + response.code());
                        callback.onFailure(BAD_RESPONSE);
                        return;
                    }
                    String beatsInfoString = null;
                    try {
                        beatsInfoString = body.string();
                    } catch (IOException e) {
                        Log.e(TAG, "Reading HTTP string failed: " + e.toString());
                        callback.onFailure(NETWORK_FAILURE);
                        return;
                    }
                    callback.onSuccess(beatsInfoString);
                }
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e(TAG, "HTTP request failed: " + e.toString());
                callback.onFailure(NETWORK_FAILURE);
            }
        });
    }

    private MediaType getMediaType(Uri uri) {
        return MediaType.get(contentResolver.getType(uri));
    }

    // This method should not be called from the main thread as it uses file IO.
    public void fetchAndSaveBeatsInfo(MusicDao dao, List<Song> songs) {
        for (Song song : songs) {
            String beatsString = song.getBeatsInfo();
            if (beatsString != null) {
                try {
                    JSONObject beatsInfo = new JSONObject(beatsString);
                    if (!beatsInfo.has("_error")) {
                        // we already have the beats info - no need to fetch it again
                        continue;
                    }
                } catch (JSONException ignored) {}
            }
            if (pendingFetches.getOrDefault(song.getSpinId(), "").equals("pending")) {
                continue;
            }
            pendingFetches.put(song.getSpinId(), "pending");
            // Use a semaphore to throttle the number of requests to avoid running out of memory
            try {
                sem.acquire();
            } catch (InterruptedException e) {
                Log.e(TAG, e.toString());
                return;
            }
            Log.w(TAG, "Fetching beats info for " + song.getTitle());
            fetchBeats(Uri.parse(song.getUri()), new BeatsInfoCallback() {
                @Override
                public void onSuccess(String beatsInfoString) {
                    dao.updateBeatsInfo(song.getSpinId(), beatsInfoString);
                    Log.w(TAG, "Successfully retrieved beats info for song " + song.getTitle());
                    onFinish();
                }

                @Override
                public void onFailure(BeatsInfoFetcher.HttpFailureReason reason) {
                    if (reason == NETWORK_FAILURE) {
                        dao.updateBeatsInfo(song.getSpinId(),
                                "{\"_error\": \"NETWORK_FAILURE\"}");
                    } else if (reason == BAD_RESPONSE) {
                        dao.updateBeatsInfo(song.getSpinId(),
                                "{\"_error\": \"BAD_RESPONSE\"}");
                    }
                    Log.w(TAG, "Failed to retrieve beats info for song " + song.getTitle());
                    onFinish();
                }

                private void onFinish() {
                    pendingFetches.remove(song.getSpinId());
                    sem.release();
                }
            });
        }
    }
}
