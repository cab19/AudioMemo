package com.example.audiomemo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class PlayRecordingDialog extends AppCompatDialogFragment {
    private PlayDialogListener listener; // member variable to hold listener interface
    private TextView tvDescription; // textview variable for UI element
    private TextView tvDate; // textview variable for UI element
    private ImageButton playButton, pauseButton, stopButton;
    // creating a bundle to pass recording to dialog
    public static PlayRecordingDialog newInstance(Recording recording) {
        PlayRecordingDialog dialog = new PlayRecordingDialog();
        Bundle args = new Bundle();
        args.putSerializable("recording", recording); // add recording to arguments if it exists
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        int recordingID=-1; // testing can remove -1?
        final Recording recording = (Recording)getArguments().getSerializable("recording");
        if(recording!=null){ // recording passed in
            recordingID = recording.getID(); // get recordings ID
        }

        Log.e("DIALOG", "RecordingID = "+ recordingID);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // create alert dialog builder
        LayoutInflater inflater = getActivity().getLayoutInflater(); // create layout inflater
        View view = inflater.inflate(R.layout.play_dialog, null); // create view, using inflater and passing layout xml
        final int finalRecordingID = recordingID;
        builder.setView(view); // use builder, set view
        // Assigning and updating UI elements
        tvDescription = view.findViewById(R.id.tvDescription); // link to ui edit text
        tvDescription.setText(recording.getDescription());
        tvDate = view.findViewById(R.id.tvDate); // link to ui edit text
        tvDate.setText(formatDate(recording.getTimeStamp()));
        // Buttons
        playButton = view.findViewById(R.id.playButton); // link to ui imagebutton
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.playRecording(recording.getFilename());
            }
        });

        pauseButton = view.findViewById(R.id.pauseButton); // link to ui imagebutton
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.pauseRecording();
            }
        });

        stopButton = view.findViewById(R.id.stopButton); // link to ui imagebutton
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.stopRecording();
            }
        });

        return builder.create();
    }

    private String formatDate(String strDate) {
        try {
            SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH); // set parse mask
            sdformat.setTimeZone(TimeZone.getTimeZone("UTC")); // declare time in UTC
            Date date = sdformat.parse(strDate); // parse db data into date object
            sdformat.setTimeZone(TimeZone.getDefault()); // set time to local timezone
            SimpleDateFormat sdfOutput = new SimpleDateFormat("d/MM/yyyy  H:mm:ss a"); // update format of date
            return sdfOutput.format(date); // convert to string and return
        } catch (ParseException e) {
            e.printStackTrace(); // print error trace
        }
        return ""; // required only if error...
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try { // ensures calling activity implements the interface
            listener = (PlayDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement PlayDialogListener");
        }
    }

    public interface PlayDialogListener{ //interface, this forces implementation of interface methods which handle clicks in dialog
        void playRecording(String filename); //
        void pauseRecording(); //
        void stopRecording(); //
    }
}