package com.angcyo.recyclerviewpager;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "angcyo";
    private RecyclerViewPager mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
//                mRecyclerView.setLayoutFrozen(true);
                mRecyclerView.setCurrentPager(2);
            }
        });

        mRecyclerView = (RecyclerViewPager) findViewById(R.id.recycler_view);
        List<String> datas = new ArrayList<>();
        for (int i = 0; i < 33; i++) {
            datas.add(i + "");
        }
        mRecyclerView.setAdapter(new MyAdapter(mRecyclerView, datas));
        mRecyclerView.setHasFixedSize(true);

        RecyclerViewPagerIndicator indicator = (RecyclerViewPagerIndicator) findViewById(R.id.indicator);
        indicator.setupRecyclerViewPager(mRecyclerView);

        final int horizontal = LinearLayoutManager.HORIZONTAL;

        set(R.id.linear, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, horizontal, false));
            }
        });
        set(R.id.grid, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 4, horizontal, false));
            }
        });
        set(R.id.stagger, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //暂不支持StaggeredGridLayoutManager布局
                //mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, horizontal));
            }
        });
    }

    void set(int id, View.OnClickListener clickListener) {
        findViewById(id).setOnClickListener(clickListener);
    }

    private void e(String msg) {
        Log.e(TAG, "-->" + msg);
    }

    class MyAdapter extends RecyclerViewPagerAdapter<String> {

        public MyAdapter(RecyclerViewPager recyclerViewPager, List<String> datas) {
            super(recyclerViewPager, datas);
        }

        @Override
        protected int getItemLayoutId(int viewType) {
            return 0;
        }

        @Override
        protected View createContentView(ViewGroup parent, int viewType) {
            LinearLayout linearLayout = new LinearLayout(MainActivity.this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setPadding(40, 40, 40, 40);
            linearLayout.setBackgroundColor(Color.GRAY);

            ImageView imageView = new ImageView(MainActivity.this);
            imageView.setImageResource(R.mipmap.ic_launcher);

            TextView textView = new TextView(MainActivity.this);
            textView.setTag("text");

            linearLayout.addView(imageView);
            linearLayout.addView(textView);

            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(Color.BLUE));
            stateListDrawable.addState(new int[]{}, new ColorDrawable(Color.GRAY));

            linearLayout.setClickable(true);
            linearLayout.setBackground(stateListDrawable);
            return linearLayout;
        }

        @Override
        protected void onBindRawView(RBaseViewHolder holder, int position, String bean) {
            ((TextView) holder.tag("text")).setText("文本-->" + position);
        }
    }
}
