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
    private Boolean error; // used to change hint text colour in case of error
    private EditText etDescription; // edittext variable for UI element
    private SaveDialogListener listener; // member variable to hold listener interface

    // creating a bundle to pass an argument to dialog, in this case whether there's been an error
    public static SaveRecordingDialog newInstance(boolean error) {
        SaveRecordingDialog dialog = new SaveRecordingDialog();
        Bundle args = new Bundle();
        args.putBoolean("error", error);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        error = getArguments().getBoolean("error");
        Log.e("DIALOG", "Error = "+error);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // create alert dialog builder
        LayoutInflater inflater = getActivity().getLayoutInflater(); // create layout inflater
        View view = inflater.inflate(R.layout.save_dialog, null); // create view, using inflater and passing layout xml
        builder.setView(view) // use builder, set view
                .setTitle("Save Memo") // set title
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() { // create negative button and create anonymous click listener
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // delete
                        listener.onDialogNegativeClick();
                    }
                })
                .setPositiveButton("save", new DialogInterface.OnClickListener() { // create positive button and create anonymous click listener
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String description = etDescription.getText().toString(); // get contents of description
                        //listener.saveDescription(description); // passing description via interface
                        listener.onDialogPositiveClick(description);
                    }
                });

        etDescription = view.findViewById(R.id.et_description); // link to ui edit text
        if(error) { // no description provided, thus error is true
            etDescription.setHint("Please provide a description");
            etDescription.setHintTextColor(Color.RED); // set hint text to red...

        }

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try { // ensures calling activity implements the interface
            listener = (SaveDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement SaveDialogListener");
        }
    }

    public interface SaveDialogListener{ //interface, this forces implementation of interface methods which handle clicks in dialog
        void onDialogPositiveClick(String strDescription); // ok clicked, pass back contents of description
        void onDialogNegativeClick(); // cancel clicked
    }
}