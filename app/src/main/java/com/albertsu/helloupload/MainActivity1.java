package com.albertsu.helloupload;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity1 extends AppCompatActivity {

    private final int TYPE_GROUP = 0xa01;
    private final int TYPE_CHILD = 0xa02;
    private String[] groupNames = {"A", "B", "C", "D", "E", "F", "G"};
    private ArrayList<Object> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        mItems = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            mItems.add(i);
            mItems.add(i+"");
        }

        RecyclerView mRecyclerView = findViewById(R.id.rv);

        //GridLayoutManager layoutManage = new GridLayoutManager(this, 4);
        LinearLayoutManager layoutManage = new LinearLayoutManager(this);
        layoutManage.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(layoutManage);

        RecyclerViewAdapter mAdapter = new RecyclerViewAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<ItemVH> {

        @NonNull
        @Override
        public ItemVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            ItemVH itemVH = null;
            switch (viewType) {
                case TYPE_GROUP:
                    view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
                    itemVH = new GroupVH(view);
                    break;

                case TYPE_CHILD:
                    view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
                    itemVH = new ChildVH(view);
                    break;
            }

            return itemVH;
        }

        @Override
        public void onBindViewHolder(@NonNull ItemVH holder, int position) {
            Object item = mItems.get(position);
            int type = item instanceof String?TYPE_GROUP:TYPE_CHILD;
            switch (type) {
                case TYPE_GROUP:
                    String g = (String) item;
                    GroupVH groupVH = (GroupVH) holder;
                    groupVH.text1.setText(g);
                    break;

                case TYPE_CHILD:
                    int c = (int) item;
                    ChildVH childVH = (ChildVH) holder;
                    childVH.text1.setText("数字 " + c);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

//        @Override
//        public int getItemViewType(int position) {
//            return mItems.get(position).getType();
//        }


    }

//    private class Group extends Item {
//        public String title;
//
//        @Override
//        public int getType() {
//            return TYPE_GROUP;
//        }
//    }
//
//    private class Child extends Item {
//        int groupPos;
//        public String groupName;
//
//        @Override
//        public int getType() {
//            return TYPE_CHILD;
//        }
//    }
//
//    private abstract class Item {
//        public int position;
//
//        public abstract int getType();
//    }

    private class GroupVH extends ItemVH {
        public TextView text1;

        GroupVH(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text1.setBackgroundColor(Color.RED);
        }

        @Override
        public int getType() {
            return TYPE_GROUP;
        }
    }

    private class ChildVH extends ItemVH {
        public TextView text1;
        public TextView text2;

        ChildVH(View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
            text1.setTextColor(Color.LTGRAY);
            text2.setTextColor(Color.BLUE);
        }

        @Override
        public int getType() {
            return TYPE_CHILD;
        }
    }

    private abstract class ItemVH extends RecyclerView.ViewHolder {
        ItemVH(View itemView) {
            super(itemView);
        }

        public abstract int getType();
    }
}
