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
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.List;

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

    // setup create db helper
    DatabaseHelper db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        db = new DatabaseHelper(this); // instantiate dbhelper object

        // link to UI elements
        playButton = findViewById(R.id.playButton);
        recordButton = findViewById(R.id.recordButton);

        // Record to the external cache directory for visibility
        fileName = getExternalCacheDir().getAbsolutePath(); // setting path for the audio file
        fileName += "/"+Calendar.getInstance().getTimeInMillis()+".m4a"; // setting filename for audio using time
        Log.e(LOG_TAG, "filename: "+fileName); // date testing

        // DB TESTING
        if(db.insert(fileName, "test description") < 1)
            Log.e(LOG_TAG, "SQLite ERROR");

        List<Recording> recordings = db.getRecordings(); // get all recordings
        RecyclerView recyclerView; // create recyclerView
        recyclerView = findViewById(R.id.recordRecycler); // link to recycler UI element



        RecordingAdapter myAdapter = new RecordingAdapter(this, recordings); // create an adapter
        LinearLayoutManager t = new LinearLayoutManager(this);
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                t.getOrientation());
        recyclerView.addItemDecoration(mDividerItemDecoration);
        recyclerView.setLayoutManager(t); // set layout manager
        recyclerView.setAdapter(myAdapter); // set adapter to one instantiated above






        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) finish(); // CHECK?? didn't get permission so close app, think it might destroy the dialog

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
        Log.i(LOG_TAG, "recording clicked"+fileName);
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
