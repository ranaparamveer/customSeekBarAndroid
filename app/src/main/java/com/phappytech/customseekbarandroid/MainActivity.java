package com.phappytech.customseekbarandroid;

import android.graphics.Color;
import android.os.Parcel;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<ProgressSegment> progressSegments = new ArrayList<>();
        int[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.GRAY, Color.YELLOW};
        for (int i = 0; i < 5; i++) {
            ProgressSegment progressSegment = new ProgressSegment(Parcel.obtain());
            progressSegment.name = "progress" + i;
            progressSegment.progress = (i + 3) * 10;
            progressSegment.color = colors[i];
            progressSegments.add(progressSegment);
        }
        CustomSeekBar customSeekBar = findViewById(R.id.custom);
        customSeekBar.setProgressSegments(progressSegments);
        customSeekBar.setSegmentClickedListener(new SegmentClickedListener() {
            @Override
            public void onClickSegment(int pos) {
                Toast.makeText(MainActivity.this, "seg: " + pos, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
