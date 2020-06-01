package com.example.audiomemo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import java.io.File;
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SaveRecordingDialog.SaveDialogListener, PlayRecordingDialog.PlayDialogListener {

    private static final String LOG_TAG = "AudioMemo"; // used for testing debugging
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200; // int flag for audio permission
    private static String fileName = null; // recording filename
    private RecordingAdapter mAdapter; // recording adapter for recyclerview
    private List<Recording> recordings; // list of recordings
    private FloatingActionButton fab_add,fab_stop; // floating action buttons
    private MediaRecorder recorder = null; // recorder object
    private MediaPlayer   player = null; // player object

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

        // CLICK LISTENER FOR FAB (Floating Action Button)
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRecording(view); // this starts the recording
            }
        });

        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // stop recording, present saveMemoDialog, user can confirm and add description or cancel
                stopRecording(view); // stops the recording
                openDialog(false, null, -1); // open the save dialog
            }
        });

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
                openPlayDialog(position); // opens play dialog, passing in position in recyclerview
            }

            @Override
            public void onLongClick(View view, int position) {
                showOptionsDialog(position); // open options dialog, passing in position in recyclerview
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
        if (!permissionToRecordAccepted) finish(); // can't run app without recording permission...
    }

    // The start recording event handler
    // Handles creation of path/filename, toggling buttons, recording, etc
    public void startRecording(View v) {
        // Set recording directory & filename
        fileName = getExternalCacheDir().getAbsolutePath(); // setting path for the audio file
        fileName += "/"+Calendar.getInstance().getTimeInMillis()+".m4a"; // setting filename for audio using time
        // toggle fab's
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
    // Toggle buttons, stop recording and release resources
    public void stopRecording(View v) {
        //toggle fab's
        fab_add.show();
        fab_stop.hide();

        recorder.stop(); // stop recording
        recorder.release(); // release recording resources
        recorder = null; // unlink recorder
    }

    // DIALOG
    // Opens save/edit dialog
    public void openDialog(Boolean error, Recording recording, int position) {
        SaveRecordingDialog saveDialog = SaveRecordingDialog.newInstance(error, recording, position); // passing in args
        saveDialog.setCancelable(false); // prevents user clicking off dialog, to prevent orphaned recording files
        saveDialog.show(getSupportFragmentManager(), "Save Dialog"); // show dialog
    }

    // Opens play dialog
    public void openPlayDialog(int position) {
        if (player != null) // delete stale player if it exists
            destroyPlayer(); // destroy player object
        Recording tempRec = db.getRecording(recordings.get(position).getID()); // get recording
        PlayRecordingDialog playDialog = PlayRecordingDialog.newInstance(tempRec); // create play dialog
        playDialog.show(getSupportFragmentManager(), "Play Dialog"); // show dialog
    }

    // OVERRIDE SaveRecordingDialog listener interface to handle button events from savedialog
    // positive button handler
    @Override
    public void onDialogPositiveClick(int recordingID, String strDescription, int position) { // ok button clicked
        // determines if recording is updating (ie edited existing) or inserting (ie new). recordingID will equal -1 to flag insertion
        Recording temp; // temp variable to holder recording
        if(strDescription.length() == 0) // Check that description is not null
            openDialog(true,null, -1); // show dialog again with error message
        else{ // save memo
            if(recordingID!=-1){ // if recordingID doesn't = -1 it is an update to existing recording.
                temp = db.getRecording(recordingID); //get recording
                temp.setDescription(strDescription); //update description
                db.updateRecording(temp); //update db
                recordings.set(position, temp); // change targeted item in recyclerview
                mAdapter.notifyItemChanged(position); // refresh recyclerview
            }
            else { // INSERT
                long id = db.insertRecording(fileName, strDescription); // insert new memo
                if (id < 1)  // error with insert
                    Log.e(LOG_TAG, "SQLite ERROR"); // print error to log
                else {
                    temp = db.getRecording(id); //get recording
                    if (temp != null) {
                        recordings.add(0, temp); // add recording to list
                        mAdapter.notifyDataSetChanged(); // update recyclerview
                    }
                }
            }
        }
    }

    // nagetive button handler
    @Override
    public void onDialogNegativeClick(int id) {
        if(id==-1)  // if there's a valid id, delete recording
            deleteRecording(fileName);
    }

    private void deleteRecording(String fileRef)
    {
        File file = new File(fileRef); // get reference to file
        file.delete(); // delete file
    }

    // options dialog method
    private void showOptionsDialog(final int position) {
        CharSequence options[] = new CharSequence[]{"Edit", "Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Option:")
        .setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selection) {
                Recording tempRec = db.getRecording(recordings.get(position).getID()); // get recording
                // reference to selection made by user, 0=edit or 1=delete
                if (selection == 0) // edit memo
                    openDialog(false, tempRec, position); //saveRecordingDialog - pass recording in, if recording not null insert to db otherwise update
                else { // delete memo
                    deleteRecording(tempRec.getFilename());  // delete file
                    recordings.remove(position); // delete reference from recordings list
                    mAdapter.notifyDataSetChanged(); // update recyclerview
                    db.deleteRecording(tempRec); // delete from db
                }
            }
        });
        builder.show(); // show dialog
    }

    // handle media controls, interface of PlayRecordingDialog
    @Override
    public void playRecording(String playFile) {
        if(player == null) { // no player
            player = new MediaPlayer(); // create player
            try { // try to set source file
                player.setDataSource(playFile);
                player.prepare();
            } catch (IOException e) { // error
                Log.e(LOG_TAG, "prepare() failed");
            }
        }
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) { // handler for destroying player when file completes
                destroyPlayer();
            }
        });
        player.start(); // start the recording
    }

    @Override
    public void pauseRecording() {
        if(player!=null){ // check if player exists
            player.pause();
        }
    }

    @Override
    public void stopRecording() {
        destroyPlayer(); // destroy player
    }

    private void destroyPlayer(){
        if (player != null) { // check if player exists
            player.release(); // release resources
            player = null; // set player to null
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