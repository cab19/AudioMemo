package com.example.audiomemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
// import android.support.annotation.NonNull;
// import android.support.v4.app.ActivityCompat;
// import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Looper Test";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    private Button playButton,recordButton;
    private MediaRecorder recorder = null;
    private MediaPlayer   player = null;
    // might need these to determine if playing/recording or not
    private boolean boolPlaying = false;
    private boolean boolRecording = false;

    // Requesting permission to RECORD_AUDIO
    private boolean permissionToRecordAccepted = false;
    private String [] permissions = {Manifest.permission.RECORD_AUDIO};

    // this is a git test...

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    public void handlePlaying(View v) {
        boolPlaying = (boolPlaying) ? false : true; // toggle playing
        if(boolPlaying) {
            playButton.setText("Stop playing");
            player = new MediaPlayer();
            try {
                player.setDataSource(fileName);
                player.prepare();
                player.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
        }
        else{
            playButton.setText("Start playing");
            boolPlaying = false;
            player.release();
            player = null;
        }
    }


    public void handleRecording(View v) {
        Log.e(LOG_TAG, "recording clicked"+fileName);
        boolRecording = (boolRecording) ? false : true; // toggle recording boolean
        if(boolRecording) {
            recordButton.setText("Stop recording");
            boolRecording = true;
            //playButton.setEnabled(false);
            //recordButton.setEnabled(true);
            // MP4 quality settings from https://stackoverflow.com/questions/56854199/how-to-record-good-quality-audio-using-mediarecoder-in-android
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setAudioEncodingBitRate(16*44100);
            recorder.setAudioSamplingRate(44100);
            recorder.setOutputFile(fileName);






            try {
                recorder.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
            recorder.start();
        }
        else{
            recordButton.setText("Start recording");
            boolRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playButton = findViewById(R.id.playButton);
        recordButton = findViewById(R.id.recordButton);

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.m4a";

        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

    }

    @Override // overriding the onstop method to make sure all resources are released when app is stopped.
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }
    }
}
