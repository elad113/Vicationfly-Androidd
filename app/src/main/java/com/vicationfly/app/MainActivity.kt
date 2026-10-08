package com.vicationfly.app
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
class MainActivity: AppCompatActivity(){ override fun onCreate(b: Bundle?){super.onCreate(b); setContentView(TextView(this).apply{text="Vicationfly"}}) }
