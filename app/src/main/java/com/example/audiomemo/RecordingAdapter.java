package com.example.audiomemo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.MyViewHolder> {
    // member variables to hold passed in data
    private List<Recording> recordingList;
    private Context context;

    // constructor
    public RecordingAdapter(Context context, List<Recording> recordingList) {
        this.context = context;
        this.recordingList = recordingList;
    }


    // handles the linking of UI elements
    public class MyViewHolder extends RecyclerView.ViewHolder{
        // member variables to hold title and logo
        TextView tvTimeStamp;
        TextView tvDescription;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // link to UI elements
            tvTimeStamp = itemView.findViewById(R.id.tvDateStamp);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }

    @NonNull
    @Override // viewholder
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context); // create layout inflater object
        View view = inflater.inflate(R.layout.recording_row, parent, false); // assign xml layout into view
        return new MyViewHolder(view); // return the view
    }

    @Override // populates each view with provided data
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        //Log.e("TIME?", "SQLite ERROR");
        holder.tvTimeStamp.setText(formatDate(recordingList.get(position).getTimeStamp())); // update timestamp, using formatted date from db
        holder.tvDescription.setText(recordingList.get(position).getDescription()); //updates description to value at current position
        //holder.tvDescription.setImageResource(categoryList.get(position).getLogo()); //updates logo to image value in array at position
    }

    private String formatDate(String strDate) {
        try {
            SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // set parse mask
            Date date = sdformat.parse(strDate); // parse db data into date object
            SimpleDateFormat sdfOutput = new SimpleDateFormat("d MMM yy"); // update format of date
            return sdfOutput.format(date); // convert to string and return
        } catch (ParseException e) {
            e.printStackTrace(); // print error trace
        }
        return ""; // required but never called...
    }

    @Override
    public int getItemCount() {
        return recordingList.size(); // returns amount of items in list
    }
}

