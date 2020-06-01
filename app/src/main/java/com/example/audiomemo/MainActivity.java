package com.example.audiomemo;

import android.Manifest;
import android.content.DialogInterface;
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
import android.widget.ImageButton;

import java.io.File;
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SaveRecordingDialog.SaveDialogListener, PlayRecordingDialog.PlayDialogListener {

    private static final String LOG_TAG = "AudioMemo";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static String fileName = null; // recording filename
    private String description = null; // description for the recording
    private RecordingAdapter mAdapter; // recording adapter for recyclerview
    private List<Recording> recordings;

    private Button playButton,recordButton;
    private FloatingActionButton fab_add,fab_stop; // floating action buttons
    private MediaRecorder recorder = null; // recorder object
    private MediaPlayer   player = null; // player object
    // might need these to determine if playing/recording or not, THESE ARE TO BE DELETED THEY'RE NOW OBSOLETE
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

        //ImageButton ib = findViewById(R.id.playButton);

        // link to UI elements
        fab_add = findViewById(R.id.fab_add); // reference to add recording button
        fab_stop = findViewById(R.id.fab_stop); // reference to stop recording button

        // CLICK LISTENER FOR FAB (Floating Action Button)
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // this starts the recording
                startRecording(view);
                //Log.e("CLICK", "FAB add clicked: ");
            }
        });

        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop recording, present saveMemoDialog, user can confirm and add description or cancel
                Log.e("CLICK", "FAB stop clicked: ");
                stopRecording(view);
                openDialog(false, null, -1);
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
        recordings = db.getAllRecordings(); // get all recordings
        RecyclerView recyclerView; // create recyclerView
        recyclerView = findViewById(R.id.recordRecycler); // link to recycler UI element

        mAdapter = new RecordingAdapter(this, recordings); // create an adapter
        LinearLayoutManager t = new LinearLayoutManager(this); // create linear layout manager
        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                t.getOrientation()); // creating divider for between rows in recycler
        recyclerView.addItemDecoration(mDividerItemDecoration); // adding divider
        recyclerView.setLayoutManager(t); // set layout manager
        recyclerView.setAdapter(mAdapter); // set adapter to one instantiated above

        recyclerView.addOnItemTouchListener(new RecyclerViewListener(this, // adding click listener via interface for recycler
                recyclerView, new RecyclerViewListener.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                Log.e("CLICK", "recycler clicked: "+position);
                openPlayDialog(position);
            }

            @Override
            public void onLongClick(View view, int position) {
                Log.e("CLICK", "recycler LONG clicked: "+position);
                showOptionsDialog(position); // open dialog, position is order in list, need to get id
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

    // The start recording event handler
    // Handles creation of path/filename, toggling buttons, recording, etc
    public void startRecording(View v) {


        // Set recording directory & filename
        fileName = getExternalCacheDir().getAbsolutePath(); // setting path for the audio file
        fileName += "/"+Calendar.getInstance().getTimeInMillis()+".m4a"; // setting filename for audio using time
        Log.e(LOG_TAG, "START RECORDING: "+fileName);

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

        //fileName = getExternalCacheDir().getAbsolutePath(); // setting path for the audio file
        //fileName += "/"+Calendar.getInstance().getTimeInMillis()+".m4a"; // setting filename for audio using time

        Log.e(LOG_TAG, "recording clicked"+fileName);
        boolRecording = (boolRecording) ? false : true; // toggle recording boolean
        if(boolRecording) {
            //fab_add.setVisibility(View.INVISIBLE); // hide add fab
            //fab_stop.setVisibility(View.VISIBLE); // show stop fab
            recordButton.setText("Stop recording");
            boolRecording = true;
            playButton.setEnabled(false); // testing
            recordButton.setEnabled(true); // testing



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
            playButton.setEnabled(true); // testing
            boolRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
        }
    }

    public void handlePlaying(View v) { // this is the start playing event handler
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

    // DIALOG
    // Opens save/edit dialog
    public void openDialog(Boolean error, Recording recording, int position) {
        Log.e("CLICK", "open SAVE/EDIT dialog: ");
        SaveRecordingDialog saveDialog = SaveRecordingDialog.newInstance(error, recording, position);
        saveDialog.show(getSupportFragmentManager(), "Save Dialog");
    }

    // Opens play dialog
    public void openPlayDialog(int position) {
        Log.e("CLICK", "open PLAY dialog: ");
        Recording tempRec = db.getRecording(recordings.get(position).getID());
        PlayRecordingDialog playDialog = PlayRecordingDialog.newInstance(tempRec);
        playDialog.show(getSupportFragmentManager(), "Play Dialog");
    }

    // OVERRIDE SaveRecordingDialog listener interface to handle button events
    @Override
    public void onDialogPositiveClick(int recordingID, String strDescription, int position) { // ok button clicked
        // EITHER INSERT OR UPDATE
        // INSERT recordingID == -1
        //description = strDescription; // update member variable to contents of description edittext in save dialog
        Recording temp;
        // Check that description is not null
        if(strDescription.length() == 0)
            openDialog(true,null, -1); // show dialog again with error message
        else{ // save memo
            // need to ascertain if this is an edit or insert...
            if(recordingID!=-1){ //UPDATE
                temp = db.getRecording(recordingID); //get recording
                temp.setDescription(strDescription); //update description
                db.updateRecording(temp); //up db
                // refreshing the list
                recordings.set(position, temp);
                mAdapter.notifyItemChanged(position);
            }
            else { // INSERT
                long id = db.insertRecording(fileName, strDescription); // insert new memo
                if (id < 1)  // error with insert
                    Log.e(LOG_TAG, "SQLite ERROR"); // print error to log
                else {
                    Log.e(LOG_TAG, "ID: " + id); // print id to log
                    temp = db.getRecording(id); //get recording
                    if (temp != null) {
                        Log.e(LOG_TAG, "descrip???? " + temp.getDescription()); // print id to log
                        recordings.add(0, temp); // add recording to list
                        mAdapter.notifyDataSetChanged(); // update recyclerview
                    }
                }
            }
        }
    }

    @Override
    public void onDialogNegativeClick(int id) { // cancel button clicked
        // check if there's a valid id.
        Log.e(LOG_TAG, "CANCEL WAS CLICKED");
        if(id==-1) {
            Log.e(LOG_TAG, "DELETE");
            deleteRecording(fileName);
        }
    }

    private void deleteRecording(String fileRef)
    {
        File file = new File(fileRef); // get reference to file
        file.delete(); // delete file
    }


    private void showOptionsDialog(final int position) {
        CharSequence options[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose option")
        .setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                Log.e(LOG_TAG, "SELECTION "+selection); // testing remove
                Recording tempRec = db.getRecording(recordings.get(position).getID());
                if (selection == 0) { // reference to selection, 0=edit or 1=delete
                    //showNoteDialog(true, notesList.get(position), position);
                    //saveRecordingDialog - pass recording in, if recording not null insert to db otherwise update
                    openDialog(false, tempRec, position);

                    Log.e(LOG_TAG, "Open saverecordingdialog "+tempRec.getID()); // testing remove
                } else {
                    Log.e(LOG_TAG, "delete recording "+tempRec.getID()); // testing remove
                    deleteRecording(tempRec.getFilename());  // delete file
                    recordings.remove(position); // delete reference from recordings list
                    mAdapter.notifyDataSetChanged(); // update recyclerview
                    db.deleteRecording(tempRec); // delete from db
                }
            }
        });
        builder.show();
    }

    @Override
    public void playRecording(String playFile) {
        Log.e("BUTTON", "PLAY CLICKED "+playFile); // testing remove
        if(player == null) {
            player = new MediaPlayer();
            try {
                player.setDataSource(playFile);
                player.prepare();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
        }
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                destroyPlayer(); // when file ends we stop and destroy player
            }
        });
        player.start();
    }

    @Override
    public void pauseRecording() {
        Log.e("BUTTON", "PAUSE CLICKED ");
        if(player!=null){
            player.pause();
        }
    }

    @Override
    public void stopRecording() {
        Log.e("BUTTON", "STOP CLICKED ");
        destroyPlayer();
    }

    private void destroyPlayer(){
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override // overriding the onstop method to make sure all resources are released when stopped.
    public void onStop() {
        super.onStop();
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null)
            destroyPlayer(); // destroy player object
    }
}
