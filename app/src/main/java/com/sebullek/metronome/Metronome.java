package com.sebullek.metronome;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import java.lang.*;

public class Metronome extends AppCompatActivity implements Runnable, SeekBar.OnSeekBarChangeListener {


    private static final String TAG = "Metronome";


    private ToneGenerator tone;
    private MediaPlayer mp_tick;


    private Thread runner;        //the animator
    private Button start;

    private SeekBar sb_bpm;  //the seekbar for bmp
    private SeekBar sb_metrum;  //the seekbar for metrum

    private TextView tv_bpm; //the text field where the bpm appears
    private TextView tv_metrum; //the text field where the metrum appears

    private int bpm;  //the current tempo
    private int counter = 0;
    private int metrum = 4;


    private boolean play = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome);


        mp_tick = new MediaPlayer().create(this, R.raw.tick);
        //ClickSound = getAudioClip(getCodeBase(), "ClickSound.au");


        start = (Button)findViewById(R.id.start);

        sb_bpm = (SeekBar)findViewById(R.id.sb_bpm);
        sb_metrum = (SeekBar)findViewById(R.id.sb_metrum);

        tv_bpm = (TextView)findViewById(R.id.bpm);
        tv_metrum = (TextView)findViewById(R.id.metrum);

        tone = new ToneGenerator(1, 100);   //ToneGenerator (int streamType, int volume)
        Log.i(TAG, "onCreate");

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!play) {
                    start.setText("STOP");
                    play = true;
                    if (cb_speed_trainer.isChecked()) {
                        System.out.println("isChecked");
                        sb_bpm.setProgress(sb_min_bpm.getProgress());
                    }
                    start();
                } else {
                    start.setText("START");
                    play = false;
                    counter = 0;
                    stop();
                }
            }
        });


        sb_bpm.setProgress(60);
        tv_bpm.setText("BPM: " + 60);

        sb_metrum.setProgress(metrum);
        tv_metrum.setText("METRUM: " + metrum);






        cb_speed_trainer = (CheckBox) findViewById(R.id.cb_speed_trainer);
        sb_min_bpm = (SeekBar)findViewById(R.id.sb_min_bpm);
        sb_max_bpm = (SeekBar)findViewById(R.id.sb_max_bpm);

        tv_min_bpm = (TextView) findViewById(R.id.tv_min_bpm);
        tv_max_bpm = (TextView)findViewById(R.id.tv_max_bpm);


        sb_loop_number = (SeekBar)findViewById(R.id.sb_loop_number);
        sb_increase = (SeekBar)findViewById(R.id.sb_increase);

        tv_loop_number = (TextView) findViewById(R.id.tv_loop_number);
        tv_increase = (TextView)findViewById(R.id.tv_increase);




        sb_min_bpm.setProgress(60);
        sb_max_bpm.setProgress(300);
        sb_loop_number.setProgress(1);
        sb_increase.setProgress(30);

        tv_min_bpm.setText("Min BPM: " + sb_min_bpm.getProgress());
        tv_max_bpm.setText("Max BPM: " + sb_max_bpm.getProgress());
        tv_loop_number.setText("Loop Number: " + sb_loop_number.getProgress());
        tv_increase.setText("Increase: " + sb_increase.getProgress());

        sb_bpm.setOnSeekBarChangeListener(this);
        sb_metrum.setOnSeekBarChangeListener(this);
        sb_min_bpm.setOnSeekBarChangeListener(this);
        sb_max_bpm.setOnSeekBarChangeListener(this);
        sb_loop_number.setOnSeekBarChangeListener(this);
        sb_increase.setOnSeekBarChangeListener(this);
    }


    @Override
    public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
        // TODO Auto-generated method stub
        Log.v("", "" + bar);

        System.out.println("progress = " + progress);


        if(progress == 0) {
            System.out.println("progress == 0");
            progress = 1;
            bar.setProgress(progress);
        }

        switch (bar.getId()) {

            case R.id.sb_bpm:
                tv_bpm.setText("BPM: " + progress);
                break;

            case R.id.sb_metrum:
                tv_metrum.setText("METRUM: " + progress);
                break;

            case R.id.sb_min_bpm:
                tv_min_bpm.setText("Min BPM: " + progress);
                break;

            case R.id.sb_max_bpm:
                tv_max_bpm.setText("Max BPM: " + progress);
                break;

            case R.id.sb_loop_number:
                tv_loop_number.setText("Loop Number: " + progress);
                break;

            case R.id.sb_increase:
                tv_increase.setText("Increase: " + progress);
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {
    }


    //the start method (called when the applet is called)
    public void start()
    {
        System.out.println("start");
        if (runner == null)
        {
            System.out.println("run");
            runner = new Thread( this);
            runner.start() ;
        }
        else
        {
            System.out.println("runner != null");
            if (runner.isInterrupted() == true)
            {
                //Log.i(TAG, "runner.isInterrupted() = " + runner.isInterrupted());
                runner.start() ;
                //Log.i(TAG, "runner.isInterrupted() = " + runner.isInterrupted());
            }
        }
    }

    //stop() called when the browser interrupt
    public void stop()
    {
        if(runner != null)
        {
            runner.interrupt();
            Log.i(TAG, "runner.isInterrupted() = " + runner.isInterrupted());
        }
    }

    //run() is the animacy, always checking to see if it should
    //play
    public void run ()
    {
        System.out.println("run");

        while(true) {

            //tempo = 60;

            bpm = sb_bpm.getProgress();

            if (play) {
                PlayMetro();
            }
        }

    }

    //the PlayMetro() method is what makes the sound play
    public void PlayMetro( )
    {
        current_bpm = bpm;
        bpm = 60*1000/bpm;   //convert to correct time

        counter++;
        if (counter > sb_metrum.getProgress()) {
            counter = 1;
        }


        if (counter == 1) {
            //tone.startTone(12, 500);
            mp_tick.start();
            System.out.println("TICK");
        } else {
            //tone.startTone(16, 500);
            mp_tick.start();
            System.out.println("TOCK");
        }

        Log.i(TAG, "counter = " + counter);
        try{Thread.sleep(bpm);} catch(InterruptedException e){}


        if (mp_tick.isPlaying()){
            mp_tick.stop();
        }


        if (counter == sb_metrum.getProgress() && cb_speed_trainer.isChecked()) {

            loop_number++;

            //Log.i(TAG, "loop_number = " + loop_number);
            //Log.i(TAG, "sb_loop_number.getProgress() = " + sb_loop_number.getProgress());

            if (loop_number == sb_loop_number.getProgress() ) {
                current_bpm = current_bpm + sb_increase.getProgress();


                if (current_bpm > sb_max_bpm.getProgress()) {
                    current_bpm = sb_max_bpm.getProgress();
                } else {
                }
                sb_bpm.setProgress(current_bpm);
                bpm = sb_bpm.getProgress();
                loop_number = 0;
            }
        }
    }




    private int current_bpm;
    private int loop_number = 0;


    private CheckBox cb_speed_trainer;
    private SeekBar sb_min_bpm;
    private SeekBar sb_max_bpm;

    private TextView tv_min_bpm;
    private TextView tv_max_bpm;


    private SeekBar sb_loop_number;
    private SeekBar sb_increase;

    private TextView tv_loop_number;
    private TextView tv_increase;



}
