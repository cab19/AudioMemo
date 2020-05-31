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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class PlayRecordingDialog extends AppCompatDialogFragment {
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
        Recording recording = (Recording)getArguments().getSerializable("recording");
        if(recording!=null){ // recording passed in
            recordingID = recording.getID(); // get recordings ID
        }

        Log.e("DIALOG", "RecordingID = "+ recordingID);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // create alert dialog builder
        LayoutInflater inflater = getActivity().getLayoutInflater(); // create layout inflater
        View view = inflater.inflate(R.layout.play_dialog, null); // create view, using inflater and passing layout xml
        final int finalRecordingID = recordingID;
        builder.setView(view) // use builder, set view
                .setTitle("Play Memo"); // set title to
        //etDescription = view.findViewById(R.id.et_description); // link to ui edit text
        return builder.create();
    }
}