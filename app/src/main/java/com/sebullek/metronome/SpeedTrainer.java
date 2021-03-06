package com.sebullek.metronome;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.lang.*;

/**
 * Created by Sebullek on 01.05.2017.
 */

public class SpeedTrainer extends AppCompatActivity implements Runnable {


    private static final String TAG = "Metronome";


    private ToneGenerator tone;
    private MediaPlayer mp_tick;


    private Thread runner;        //the animator
    private Button start;

    private int bpm;  //the current tempo
    private int counter = 0;


    private boolean play = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speed_trainer);


        mp_tick = new MediaPlayer().create(this, R.raw.tick);
        //ClickSound = getAudioClip(getCodeBase(), "ClickSound.au");


        start = (Button)findViewById(R.id.start);

        tone = new ToneGenerator(1, 100);   //ToneGenerator (int streamType, int volume)
        Log.i(TAG, "onCreate");

        // PLAY METRONOM
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!play) {
                    start.setText("STOP");
                    play = true;
                    current_bpm = Integer.parseInt(es_min_bpm.getText().toString());
                    start();
                } else {
                    start.setText("START");
                    play = false;
                    counter = 0;
                    stop();
                }
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

        String[] a_loop = new String[10];
        for (int i = 0; i < a_loop.length; i++){
            a_loop[i] = "" + (i + 1);
        }

        String[] a_increase = new String[50];
        for (int i = 0; i < a_increase.length; i++){
            a_increase[i] = "" + (i + 1);
        }


        ListAdapter adapter_bpm = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, a_bpm);
        ListAdapter adapter_metrum = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, a_metrum);
        ListAdapter adapter_loop = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, a_loop);
        ListAdapter adapter_increase = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, a_increase);

        es_metrum = (EditSpinner) findViewById(R.id.es_metrum);

        es_min_bpm = (EditSpinner) findViewById(R.id.es_min_bpm);
        es_max_bpm = (EditSpinner) findViewById(R.id.es_max_bpm);
        es_loop = (EditSpinner) findViewById(R.id.es_loop);
        es_increase = (EditSpinner) findViewById(R.id.es_increase);

        es_metrum.setAdapter(adapter_metrum);
        es_min_bpm.setAdapter(adapter_bpm);
        es_max_bpm.setAdapter(adapter_bpm);
        es_loop.setAdapter(adapter_loop);
        es_increase.setAdapter(adapter_increase);

        setDefaultValues();

        es_metrum.setOnClickListener(doubleClickListener);
        es_min_bpm.setOnClickListener(doubleClickListener);
        es_max_bpm.setOnClickListener(doubleClickListener);
        es_loop.setOnClickListener(doubleClickListener);
        es_increase.setOnClickListener(doubleClickListener);

        es_metrum.setOnEditorActionListener(onEditorActionListener);
        es_min_bpm.setOnEditorActionListener(onEditorActionListener);
        es_max_bpm.setOnEditorActionListener(onEditorActionListener);
        es_loop.setOnEditorActionListener(onEditorActionListener);
        es_increase.setOnEditorActionListener(onEditorActionListener);

        es_metrum.setOnFocusChangeListener(onFocusChangeListener);
        es_min_bpm.setOnFocusChangeListener(onFocusChangeListener);
        es_max_bpm.setOnFocusChangeListener(onFocusChangeListener);
        es_loop.setOnFocusChangeListener(onFocusChangeListener);
        es_increase.setOnFocusChangeListener(onFocusChangeListener);
    }


    EditSpinner es_metrum;
    EditSpinner es_min_bpm;
    EditSpinner es_max_bpm;
    EditSpinner es_loop;
    EditSpinner es_increase;

    private static int DEFAULT_METRUM = 4;
    private static int DEFAULT_MIN_BPM = 60;
    private static int DEFAULT_MAX_BPM = 300;
    private static int DEFAULT_LOOP_NUMBER = 3;
    private static int DEFAULT_INCREASE = 20;

    public void setDefaultValues() {
        es_metrum.setText(String.valueOf(DEFAULT_METRUM));;
        es_min_bpm.setText(String.valueOf(DEFAULT_MIN_BPM));;
        es_max_bpm.setText(String.valueOf(DEFAULT_MAX_BPM));;
        es_loop.setText(String.valueOf(DEFAULT_LOOP_NUMBER));;
        es_increase.setText(String.valueOf(DEFAULT_INCREASE));;
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
            bpm = Integer.parseInt(es_min_bpm.getText().toString());

            if (play) {
                PlayMetro();
            }
        }

    }

    //the PlayMetro() method is what makes the sound play
    public void PlayMetro( )
    {
        int copy_bpm = current_bpm;
        copy_bpm = 60*1000/copy_bpm;   //convert to correct time

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

        Log.i(TAG, "counter = " + counter);
        try{Thread.sleep(copy_bpm);} catch(InterruptedException e){}


        if (mp_tick.isPlaying()){
            mp_tick.stop();
        }


        if (counter == metrum && cb_speed_trainer.isChecked()) {

            Log.i(TAG, "BUJA = " + counter);
            loop_counter++;
            //Log.i(TAG, "loop_counter = " + loop_counter);

            int loop_number = Integer.parseInt(es_loop.getText().toString());
            int increase = Integer.parseInt(es_increase.getText().toString());
            int max_bpm = Integer.parseInt(es_max_bpm.getText().toString());

            if (loop_counter == loop_number ) {
                current_bpm = current_bpm + increase;


                if (current_bpm > max_bpm) {
                    current_bpm = max_bpm;
                } else {
                }
                //sb_bpm.setProgress(current_bpm);
                //bpm = sb_bpm.getProgress();
                loop_counter = 0;
            }
        }
        Log.i(TAG, "current_bpm = " + current_bpm);
    }




    private int current_bpm;
    private int loop_counter = 0;


    private CheckBox cb_speed_trainer;

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
