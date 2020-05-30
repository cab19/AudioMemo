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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Looper Test";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null;

    // private Button playButton,recordButton;
    private FloatingActionButton fab_add,fab_stop;
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
        fab_add = findViewById(R.id.fab_add); // reference to add recording button
        fab_stop = findViewById(R.id.fab_stop); // reference to stop recording button
        //playButton = findViewById(R.id.playButton); // old testing buttons
        //recordButton = findViewById(R.id.recordButton); // old testing buttons

        // CLICK LISTENER FOR FAB (Floating Action Button)
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this starts the recording
                openDialog();
                //Log.e("CLICK", "FAB add clicked: ");
            }
        });

        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop recording, present saveMemoDialog, user can confirm and add description or cancel
                Log.e("CLICK", "FAB stop clicked: ");
                // save should add to data to db

                // cancel should delete recording file
            }
        });

/*
        // DB TESTING
        if(db.insert(fileName, "test description") < 1) // timestamp automatic
            Log.e(LOG_TAG, "SQLite ERROR");

 */
        // RECYCLER VIEW
        List<Recording> recordings = db.getRecordings(); // get all recordings
        RecyclerView recyclerView; // create recyclerView
        recyclerView = findViewById(R.id.recordRecycler); // link to recycler UI element

        RecordingAdapter myAdapter = new RecordingAdapter(this, recordings); // create an adapter
        LinearLayoutManager t = new LinearLayoutManager(this); // create linear layout manager
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                t.getOrientation()); // creating divider for between rows in recycler
        recyclerView.addItemDecoration(mDividerItemDecoration); // adding divider
        recyclerView.setLayoutManager(t); // set layout manager
        recyclerView.setAdapter(myAdapter); // set adapter to one instantiated above

        recyclerView.addOnItemTouchListener(new RecyclerViewListener(this, // adding click lister via interface for recycler
                recyclerView, new RecyclerViewListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Log.e("CLICK", "recycler clicked: "+position);
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.e("CLICK", "recycler LONG clicked: "+position);
            }
        }));




        // AUDIO PERMISSION
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED; // set permission granted
                break;
        }
        if (!permissionToRecordAccepted) finish();

    }

    // DIALOG
    // just need a description
    public void openDialog() {
        Log.e("CLICK", "open dialog: ");
        SaveRecordingDialog saveDialog = new SaveRecordingDialog();
        saveDialog.show(getSupportFragmentManager(), "examplez");
    }



    public void handlePlaying(View v) { // this is the start playing event handler
        boolPlaying = (boolPlaying) ? false : true; // toggle playing
        if(boolPlaying) {
            //playButton.setText("Stop playing");
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
            //playButton.setText("Start playing");
            boolPlaying = false;
            player.release();
            player = null;
        }
    }

    // The start recording event handler
    // Handles creation of path/filename, toggling buttons, recording, etc
    public void startRecording(View v) {
        Log.e(LOG_TAG, "fab_add clicked");

        // Set recording directory & filename
        fileName = getExternalCacheDir().getAbsolutePath(); // setting path for the audio file
        fileName += "/"+Calendar.getInstance().getTimeInMillis()+".m4a"; // setting filename for audio using time

        //fab_add.setVisibility(View.INVISIBLE); // hide add fab
        //fab_stop.setVisibility(View.VISIBLE); // show stop fab
        fab_add.hide();
        fab_stop.show();

        // MP4 quality settings from https://stackoverflow.com/questions/56854199/how-to-record-good-quality-audio-using-mediarecoder-in-android
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncodingBitRate(16*44100);
        recorder.setAudioSamplingRate(44100);
        recorder.setOutputFile(fileName);
        // try to prepare object otherwise print error
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        recorder.start(); // start recording
    }

    // The stop recording event handler
    // Toggle buttons, stop recording and release resources, present saveMemoDialog
    public void stopRecording(View v) { // this is the start recording event handler
        Log.e(LOG_TAG, "fab_stop clicked");
        //fab_add.setVisibility(View.VISIBLE); // show add fab
        //fab_stop.setVisibility(View.INVISIBLE); // hide stop fab
        fab_add.show();
        fab_stop.hide();
        recorder.stop(); // stop recording
        recorder.release(); // release recording resources
        recorder = null; // unlink recorder

        // saveMemoDialog
    }

    public void handleRecording(View v) { // this is the start recording event handler
        Log.i(LOG_TAG, "recording clicked"+fileName);
        boolRecording = (boolRecording) ? false : true; // toggle recording boolean
        if(boolRecording) {
            //fab_add.setVisibility(View.INVISIBLE); // hide add fab
            //fab_stop.setVisibility(View.VISIBLE); // show stop fab
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
            //recordButton.setText("Start recording");
            boolRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    @Override // overriding the onstop method to make sure all resources are released when stopped.
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
