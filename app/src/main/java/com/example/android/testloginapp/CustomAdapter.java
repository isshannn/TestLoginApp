package com.example.android.testloginapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapter extends  RecyclerView.Adapter<CustomAdapter.CustomViewHolder> {
    Context context;
    ArrayList<User_Details> UserInfo;

    public CustomAdapter(Context context1,ArrayList<User_Details> UserData){
        context = context1;
        UserInfo = UserData;
    }

    @NonNull
    @Override
    public CustomAdapter.CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.common_userinfo,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.CustomViewHolder holder, int position) {
        holder.name.setText(UserInfo.get(position).getName());
        holder.email.setText(UserInfo.get(position).getEmail());
        holder.phone.setText(UserInfo.get(position).getMobileNumber());
        holder.userName.setText(UserInfo.get(position).getUserName());
    }


    @Override
    public int getItemCount() {
        return UserInfo.size();
    }
    class CustomViewHolder extends RecyclerView.ViewHolder{
        TextView name,email,phone,userName;
        public CustomViewHolder(@NonNull View itemView){
            super(itemView);
            name=itemView.findViewById(R.id.common_user_name);
            email=itemView.findViewById(R.id.common_user_email);
            phone=itemView.findViewById(R.id.common_user_mobile);
            userName=itemView.findViewById(R.id.common_user_username);

        }
    }

}
