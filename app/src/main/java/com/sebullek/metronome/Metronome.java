package com.sebullek.metronome;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;


import java.lang.*;

public class Metronome extends AppCompatActivity implements Runnable {


    private static final String TAG = "Metronome";


    private ToneGenerator tone;
    private MediaPlayer mp_tick;


    private Thread runner;
    private Button start;
    private Button speed_trainer;

    private int bpm;  //the current tempo
    private int counter = 0;


    private boolean play = false;

    private EditSpinner es_bpm;
    private EditSpinner es_metrum;
    private TextView tv_metrum_counter;

    private static int DEFAULT_BPM = 120;
    private static int DEFAULT_METRUM = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metronome);


        mp_tick = new MediaPlayer().create(this, R.raw.tick);
        //ClickSound = getAudioClip(getCodeBase(), "ClickSound.au");


        tv_metrum_counter = (TextView)findViewById(R.id.tv_metrum_counter);
        start = (Button)findViewById(R.id.start);
        speed_trainer = (Button)findViewById(R.id.speed_trainer);


        tone = new ToneGenerator(1, 100);   //ToneGenerator (int streamType, int volume)
        Log.i(TAG, "onCreate");

        // PLAY METRONOM
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!play) {
                    start.setText("STOP");
                    play = true;
                    start();
                } else {
                    start.setText("START");
                    play = false;
                    counter = 0;
                    stop();
                }
            }
        });

        speed_trainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Metronome.this, SpeedTrainer.class);
                startActivity(myIntent);
            }
        });

        ///////////////////////////////////////////////////////////////////////

        ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content);
        disableAllEditText(viewGroup);

        String[] a_bpm = new String[360];
        for (int i = 0; i < a_bpm.length; i++){
            a_bpm[i] = "" + (i + 1);
        }

        String[] a_metrum = new String[8];
        for (int i = 0; i < a_metrum.length; i++){
            a_metrum[i] = "" + (i + 1);
        }


        ListAdapter adapter_bpm = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, a_bpm);
        ListAdapter adapter_metrum = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, a_metrum);

        es_bpm = (EditSpinner) findViewById(R.id.es_bpm);
        es_metrum = (EditSpinner) findViewById(R.id.es_metrum);


        es_bpm.setAdapter(adapter_bpm);
        es_metrum.setAdapter(adapter_metrum);

        setDefaultValues();

        es_bpm.setOnClickListener(doubleClickListener);
        es_metrum.setOnClickListener(doubleClickListener);

        es_bpm.setOnEditorActionListener(onEditorActionListener);
        es_metrum.setOnEditorActionListener(onEditorActionListener);

        es_bpm.setOnFocusChangeListener(onFocusChangeListener);
        es_metrum.setOnFocusChangeListener(onFocusChangeListener);
    }



    public void setDefaultValues() {
        es_bpm.setText(String.valueOf(DEFAULT_BPM));
        es_metrum.setText(String.valueOf(DEFAULT_METRUM));;
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

            //bpm = sb_bpm.getProgress();
            bpm = Integer.parseInt(es_bpm.getText().toString());

            if (play) {
                PlayMetro();
            }
        }

    }

    //the PlayMetro() method is what makes the sound play
    public void PlayMetro( )
    {
        bpm = 60*1000/bpm;   //convert to correct time

        int metrum = Integer.parseInt(es_metrum.getText().toString());

        counter++;

        //if (counter > sb_metrum.getProgress()) {
        if (counter > metrum) {
            counter = 1;
        }

        if (counter == 1) {
            tone.startTone(12, 500);
            //mp_tick.start();
            System.out.println("TICK");
        } else {
            tone.startTone(16, 500);
            //mp_tick.start();
            System.out.println("TOCK");
        }
        /*
        if (!tv_metrum_counter.getText().toString().equals(String.valueOf(counter))) {
            tv_metrum_counter.setText(String.valueOf(counter));
        }
        */

        Log.i(TAG, "counter = " + counter);
        try{Thread.sleep(bpm);} catch(InterruptedException e){}


        if (mp_tick.isPlaying()){
            mp_tick.stop();
        }
    }




    private DoubleClickListener doubleClickListener = new DoubleClickListener() {

        @Override
        public void onSingleClick(View v) {

            disableEditText(v);

            checkSingleClickView(v);

            // show a list
            EditSpinner editSpinner = (EditSpinner) v;
            editSpinner.showDropDown();
        }

        @Override
        public void onDoubleClick(View v) {

            checkDoubleClickView(v);

            enableEditText(v);
        }
    };

    private EditText.OnEditorActionListener onEditorActionListener = new EditText.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {

                // End typing text

                disableEditText(v);
                return true;
            }
            return false;
        }
    };

    private View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            //disableEditText(v);


            if (!hasFocus) {
                // end focusing on View

                disableEditText(v);
            }

            if (hasFocus) {

                // start focusing on View
                // call onClick method

                v.performClick();
            }
        }
    };

    private void disableEditText(View v) {

        // disable typing

        EditText editText = (EditText)findViewById(v.getId());
        editText.setInputType(InputType.TYPE_NULL);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void enableEditText(View v) {

        // enable typing

        EditText editText = (EditText)findViewById(v.getId());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void disableAllEditText(ViewGroup viewGroup) {

        // disable typing in all of the EditTexts

        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ViewGroup)
                disableAllEditText((ViewGroup) view);
            else if (view instanceof EditText) {
                EditText editText = (EditText) view;
                //editText.setText(i + " id: " + editText.getId());
                disableEditText(editText);
            }

        }

    }

    // Check which View was clicked
    private void checkSingleClickView(View v) {
        /*switch (v.getId())
        {
            case R.id.editText:
                textView.setText("Single Click editText");
                break;
            case R.id.editText2:
                textView.setText("Single Click editText2");
                break;
            case R.id.editSpinner:
                textView.setText("Single Click editSpinner");
                break;
            case R.id.editSpinner2:
                textView.setText("Single Click EditSpinner2");
                break;
        }*/
    }

    private void checkDoubleClickView(View v) {
        /*switch (v.getId())
        {
            case R.id.editText:
                textView.setText("Double Click editText");
                break;
            case R.id.editText2:
                textView.setText("Double Click editText2");
                break;
            case R.id.editSpinner:
                textView.setText("Double Click editSpinner");
                break;
            case R.id.editSpinner2:
                textView.setText("Double Click EditSpinner2");
                break;
        }*/
    }

}
