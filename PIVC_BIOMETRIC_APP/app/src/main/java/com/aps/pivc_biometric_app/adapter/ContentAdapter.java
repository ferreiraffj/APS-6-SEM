package com.aps.pivc_biometric_app.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aps.pivc_biometric_app.ContentDetailActivity;
import com.aps.pivc_biometric_app.R;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentViewHolder> {
    private final Context context;
    private final List<DocumentSnapshot> contents;

    public ContentAdapter(Context context, List<DocumentSnapshot> contents) {
        this.context = context;
        this.contents = contents;
    }

    public ContentViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_content, parent, false);
        return new ContentViewHolder(view);
    }

    public void onBindViewHolder(ContentViewHolder holder, int position){
        DocumentSnapshot content = contents.get(position);
        String title = content.getString("title");
        String preview = content.getString("preview");
        int permissionLevel = content.getLong("permissionLevel").intValue();

        holder.tvTitle.setText(title);
        holder.tvPermissionLevel.setText("Nível " + permissionLevel);
        holder.tvPreview.setText(preview);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int itemHeight = (int) (displayMetrics.heightPixels * 0.12); // 20% da altura da tela
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = itemHeight;
        holder.itemView.setLayoutParams(layoutParams);


        holder.itemView.setOnClickListener(v ->{
            Intent intent = new Intent(context, ContentDetailActivity.class);
            intent.putExtra("fullContent", content.getString("fullContent"));
            context.startActivity(intent);
        });
    }

    public int getItemCount(){
        return contents.size();
    }

    public static class ContentViewHolder extends RecyclerView.ViewHolder{
        TextView tvTitle, tvPermissionLevel, tvPreview;

        public ContentViewHolder(View itemView){
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPermissionLevel = itemView.findViewById(R.id.tvPermissionLevel);
            tvPreview = itemView.findViewById(R.id.tvPreview);
        }
    }

}
