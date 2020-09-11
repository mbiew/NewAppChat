package com.example.newappchat;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AdapterGroupChatList extends RecyclerView.Adapter<AdapterGroupChatList.HolderGroupChatList> {

    private Context context;
    private ArrayList<ModelGroupChatList> groupChatLists;

    public AdapterGroupChatList(Context context, ArrayList<ModelGroupChatList> groupChatLists){
        this.context = context;
        this.groupChatLists = groupChatLists;
    }

    @NonNull
    @Override
    public HolderGroupChatList onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        //inflate layout
        View view = LayoutInflater.from(context).inflate(R.layout.row_groupchats_list,parent,false);

        return new HolderGroupChatList(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderGroupChatList holder, int position) {

        //getData
        ModelGroupChatList model = groupChatLists.get(position);
        final String groupId = model.getGroupId();
        String groupIcon = model.getGroupIcon();
        String groupTitle = model.getGroupTitle();

        holder.nameTv.setText("");
        holder.timeTv.setText("");
        holder.messageTv.setText("");


        //load last message and message-time
        loadLastMessage(model, holder);

        //setData
        holder.groupTitleTv.setText(groupTitle);
        try{
            Picasso.get().load(groupIcon).placeholder(R.drawable.ic_group_black).into(holder.groupIconIv);
        } catch (Exception e) {
            holder.groupIconIv.setImageResource(R.drawable.ic_group_black);
        }

        //handle groupClick
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open group chat
                Intent intent = new Intent(context, GroupChatActivity.class);
                intent.putExtra("groupId", groupId);
                context.startActivity(intent);
            }
        });

    }

    private void loadLastMessage(ModelGroupChatList model, final HolderGroupChatList holder) {
        //get last message from group
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Groups");
        ref.child(model.getGroupId()).child("Messages").limitToLast(1)//getLast item(message)
        .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds:snapshot.getChildren()){

                    //getData
                    String message = ""+ds.child("message").getValue();
                    String timestamp = ""+ds.child("timestamp").getValue();
                    String sender = ""+ds.child("sender").getValue();
                    String messageType = ""+ds.child("type").getValue();

                    //convertTime
                    Calendar cal = Calendar.getInstance(Locale.ENGLISH);

                    try {
                        cal.setTimeInMillis(Long.parseLong(timestamp));
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                    String dateTime = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();


                    if (messageType.equals("image")){
                        holder.messageTv.setText("Sent Photo");
                    }
                    else {
                        holder.messageTv.setText(message);
                    }
                    
                    holder.timeTv.setText(dateTime);

                    //get info of sender of last message
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.orderByChild("uid").equalTo(sender).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds:snapshot.getChildren()){
                                String name = ""+ds.child("name").getValue();
                                holder.nameTv.setText(name);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return groupChatLists.size();
    }

    //viewHolder class
    class HolderGroupChatList extends RecyclerView.ViewHolder{

        //ui Views
        private ImageView groupIconIv;
        private TextView groupTitleTv, nameTv, messageTv, timeTv;

        public HolderGroupChatList(@NonNull View itemView) {
            super(itemView);

            groupIconIv = itemView.findViewById(R.id.groupIconIv);
            groupTitleTv = itemView.findViewById(R.id.groupTitleTv);
            nameTv = itemView.findViewById(R.id.nameTv);
            messageTv = itemView.findViewById(R.id.messageTv);
            timeTv = itemView.findViewById(R.id.timeTv);

        }
    }

}
