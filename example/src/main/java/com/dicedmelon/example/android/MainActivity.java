package com.dicedmelon.example.android;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.dicedmelon.example.android.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActivityMainBinding viewDataBinding =
        DataBindingUtil.setContentView(this, R.layout.activity_main);

    NumberProvider numberProvider = new NumberProvider();
    viewDataBinding.setNumber(numberProvider.provideNumber());
  }
}
