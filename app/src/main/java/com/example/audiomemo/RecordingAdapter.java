package com.example.audiomemo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class RecordingAdapter extends RecyclerView.Adapter<RecordingAdapter.MyViewHolder> {
    // member variables to hold passed in data
    private List<Recording> recordingList;
    private Context context;

    // constructor
    public CategoryAdapter(Context context, List<Recording> recordingList) {
        this.context = context;
        this.recordingList = categoryList;
    }


    // handles the linking of UI elements
    public class MyViewHolder extends RecyclerView.ViewHolder{
        // member variables to hold title and logo
        //TextView tvTitle;
        //ImageView ivLogo;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            // link to UI elements
            tvTitle = itemView.findViewById(R.id.tvTitle);
            ivLogo = itemView.findViewById(R.id.ivLogo);
        }
    }

    @NonNull
    @Override // viewholder
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context); // create layout inflater object
        View view = inflater.inflate(R.layout.category_row, parent, false); // assign xml layout into view
        return new MyViewHolder(view); // return the view
    }

    @Override // populates each view with provided data
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.tvTitle.setText(categoryList.get(position).getTitle()); //updates title to title value in array at position
        holder.ivLogo.setImageResource(categoryList.get(position).getLogo()); //updates logo to image value in array at position
    }

    @Override
    public int getItemCount() {
        return categoryList.size(); // returns amount of items in list
    }
}

