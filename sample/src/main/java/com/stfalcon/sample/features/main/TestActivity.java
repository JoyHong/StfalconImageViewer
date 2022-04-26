package com.stfalcon.sample.features.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.loader.GetViewType;
import com.stfalcon.sample.R;
import com.stfalcon.sample.common.models.Demo;


public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        SubsamplingScaleImageView subsamplingScaleImageView = findViewById(R.id.testImageView);
        subsamplingScaleImageView.setImage(ImageSource.asset("longImage.jpg"));

    }
}