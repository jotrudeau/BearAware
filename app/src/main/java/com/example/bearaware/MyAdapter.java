package com.example.bearaware;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;
    InputImage image;
    ImageLabeler labeler;
    ArrayList<Incident> list;

    public MyAdapter(Context context, ArrayList<Incident> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Incident incident = list.get(position);
        holder.datetime.setText(incident.getDateTime());
        storageRef = storage.getReferenceFromUrl("gs://bearawaredatabase.appspot.com/" + incident.getDateTime() + ".jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        storageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                holder.image.setImageBitmap(bmp);
                image = InputImage.fromBitmap(bmp, 0);
                labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
                labeler.process(image).addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                    @Override
                    public void onSuccess(@NonNull List<ImageLabel> imageLabels) {
                        for (ImageLabel label : imageLabels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            holder.label.setText(text);
                            holder.confidence.setText("" + confidence);
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView datetime, label, confidence;
        ImageView image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            datetime = itemView.findViewById(R.id.iddatetime);
            image = itemView.findViewById(R.id.idimageView);
            label = itemView.findViewById(R.id.idlabel);
            confidence = itemView.findViewById(R.id.idconfidence);
        }
    }

}
