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

public class SaveRecordingDialog extends AppCompatDialogFragment {
    private EditText etDescription; // edittext variable for UI element
    private SaveDialogListener listener; // member variable to hold listener interface

    // creating a bundle to pass arguments to dialog
    public static SaveRecordingDialog newInstance(boolean error, Recording recording, int position) {
        SaveRecordingDialog dialog = new SaveRecordingDialog(); // create new dialog
        Bundle args = new Bundle(); // new bundle for arguments
        args.putBoolean("error", error); // add error bool to arguments
        args.putInt("position", position); // add clicked recycler position to arguments
        if(recording!=null) // check if there was a recording passed in
            args.putSerializable("recording", recording); // add recording to arguments if it exists
        dialog.setArguments(args); // set arguments
        return dialog; // return dialog
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        boolean error = getArguments().getBoolean("error"); // used to flag error and change hint text/colour
        final int position = getArguments().getInt("position"); // used to flag error and change hint text/colour
        int recordingID = -1; // initialise recordingId to -1
        Recording recording = (Recording)getArguments().getSerializable("recording"); // get recording
        if(recording!=null)// recording passed in
            recordingID = recording.getID(); // get recordings ID

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // create alert dialog builder
        LayoutInflater inflater = getActivity().getLayoutInflater(); // create layout inflater
        View view = inflater.inflate(R.layout.save_dialog, null); // create view, using inflater and passing layout xml
        final int finalRecordingID = recordingID; // used in onclick listener, needs to be final to flag it won't change, due to referencing in internal class
        builder.setView(view) // use builder, set view
                .setTitle("Save Memo") // set title
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() { // create negative button and create anonymous click listener
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        listener.onDialogNegativeClick(finalRecordingID); // call negative interface method
                    }
                })
                .setPositiveButton("save", new DialogInterface.OnClickListener() { // create positive button and create anonymous click listener
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String description = etDescription.getText().toString(); // get contents of description
                        listener.onDialogPositiveClick(finalRecordingID, description, position); // call positive interface method
                    }
                });

        etDescription = view.findViewById(R.id.et_description); // link to ui edit text

        if(error) { // no description provided, thus error is true
            etDescription.setHint("Please provide a description"); // update hint
            etDescription.setHintTextColor(Color.RED); // set hint text to red...
        }

        if(recordingID!=-1) { // recording passed in, update description
            etDescription.setText(recording.getDescription()); // update text
        }

        return builder.create(); // return builder
    }

    // override onattach to instantiate listener for interface
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try { // ensures calling activity implements the interface
            listener = (SaveDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SaveDialogListener");
        }
    }

    // interface methods
    public interface SaveDialogListener{ //interface, this forces implementation of interface methods which handle clicks in dialog
        void onDialogPositiveClick(int id, String strDescription, int position); // ok clicked, pass back contents of description
        void onDialogNegativeClick(int id); // cancel clicked
    }
}