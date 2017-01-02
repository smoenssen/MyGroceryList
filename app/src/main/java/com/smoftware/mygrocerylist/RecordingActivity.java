package com.smoftware.mygrocerylist;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

/*
<com.jjoe64.graphview.GraphView
        android:layout_width="match_parent"
        android:layout_height="200dip"
        android:id="@+id/graph" />

        gradle:
        compile 'com.jjoe64:graphview:4.2.1'
*/

public class RecordingActivity extends AppCompatActivity {

    long listId = 0;
    private SpeechRecognizer mSpeechRecognizer;
    private Intent mSpeechRecognizerIntent;
    private Handler handler;
    private TextView myText;
    private int counter = 0;

    /*
    private GraphView graph;
    private int time = 0;
    private LineGraphSeries<DataPoint> mSeries2;
    private SoundMeter mSensor;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        listId = getIntent().getLongExtra("ListId", 0);

        myText = (TextView)findViewById(R.id.listening);

        /*
        graph = (GraphView) findViewById(graph);
        mSeries2 = new LineGraphSeries<>();
        graph.addSeries(mSeries2);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(40);

        mSensor = new SoundMeter();

        try {
            mSensor.start();
            Toast.makeText(getBaseContext(), "Sound sensor initiated.", Toast.LENGTH_SHORT).show();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        */

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());

        RecordingActivity.SpeechRecognitionListener listener = new RecordingActivity.SpeechRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(listener);
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent);

        handler = new Handler();

        final Runnable r = new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /*
                        // Get the volume from 0 to 255 in 'int'
                        double volume = 10 * mSensor.getTheAmplitude() / 32768;
                        mSensor.stop();
                        mSeries2.appendData(new DataPoint(time++, volume), true, 40);
                        */
                        switch(++counter) {
                            case 1:
                                myText.setText("Listening");
                                break;
                            case 2:
                                myText.setText("Listening .");
                                break;
                            case 3:
                                myText.setText("Listening . .");
                                break;
                            case 4:
                                myText.setText("Listening . . .");
                                break;
                            case 5:
                                myText.setText("Listening . . . .");
                                break;
                            case 6:
                                myText.setText("Listening . . . . .");
                                counter = 0;
                                break;
                        }
                        handler.postDelayed(this, 250); // amount of delay between every cycle
                    }
                });
            }
        };

        // Is this line necessary? --- YES IT IS, or else the loop never runs
        // this tells Java to run "r"
        handler.postDelayed(r, 0);
    }

    @Override
    public void onDestroy() {
        Log.d(Defs.TAG, "RecordingActivity onDestroy");
        if (mSpeechRecognizer != null) mSpeechRecognizer.destroy();
        super.onDestroy();
    }

    protected class SpeechRecognitionListener implements RecognitionListener
    {
        @Override
        public void onBeginningOfSpeech()
        {
            Log.d(Defs.TAG, "onBeginingOfSpeech");
        }

        @Override
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(Defs.TAG, "onBufferReceived");
        }

        @Override
        public void onEndOfSpeech()
        {
            Log.d(Defs.TAG, "onEndOfSpeech");
        }

        @Override
        public void onError(int error) {
            //mSpeechRecognizer.startListening(mSpeechRecognizerIntent);
            Log.d(Defs.TAG, "error = " + error);
        }

        @Override
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(Defs.TAG, "onEvent");
        }

        @Override
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(Defs.TAG, "onPartialResults");
        }

        @Override
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(Defs.TAG, "onReadyForSpeech");
        }

        @Override
        public void onResults(Bundle results) {
            Log.d(Defs.TAG, "onResults");
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            Intent myIntent = new Intent(RecordingActivity.this, CreateListActivity.class);
            myIntent.putExtra("Results", matches.get(0));
            setResult(RESULT_OK, myIntent);
            finish();
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            Log.d(Defs.TAG, String.format("rmsdB = %4.3f", rmsdB));
        }
    }
}
