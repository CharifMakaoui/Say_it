package com.example.cesarsk.say_it;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.speech.tts.TextToSpeech.QUEUE_ADD;
import static com.example.cesarsk.say_it.MainActivity.tts;
import static com.example.cesarsk.say_it.ResultsListCustomAdapter.selected_word_charseq;

public class PlayFragment extends Fragment {

    private static final String AUDIO_RECORDER_FILE_EXT_AAC = ".aac";
    private static final String AUDIO_RECORDER_FOLDER = "Say it";
    private MediaRecorder recorder = null;
    private int currentFormat = 0;
    private int output_formats[] = {MediaRecorder.OutputFormat.DEFAULT};
    private String file_exts[] = {AUDIO_RECORDER_FILE_EXT_AAC};
    public static final int RequestPermissionCode = 1;
    MediaPlayer mediaPlayer;


    public PlayFragment() {
        //TODO SISTEMARE FRAGMENT
        recorder = new MediaRecorder();
        mediaPlayer = new MediaPlayer();
        //word = new String(getArguments().getCharSequence("word").toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_play,
                container, false);

        TextView selected_word = (TextView) view.findViewById(R.id.selected_word);
        selected_word.setText(selected_word_charseq);

        ImageButton play_original_button = (ImageButton) view.findViewById(R.id.play_original);
        play_original_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tts.speak(selected_word_charseq, QUEUE_ADD, null, null);
            }
        });

        ImageButton play_rec_button = (ImageButton) view.findViewById(R.id.play_rec);
        play_rec_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecording();
            }
        });

        ImageButton rec_button = (ImageButton) view.findViewById(R.id.rec_button);

        rec_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if (checkPermission())
                {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.i("Say it!", "Start Recording");
                            startRecording();
                            break;

                        case MotionEvent.ACTION_UP:
                            Log.i("Say it!", "Stop Recording");
                            stopRecording();
                            break;
                    }
                }

                else
                {
                    requestPermission();
                }
                return false;
            }
        });

        return view;
    }

    //FUNZIONI DI REGISTRAZIONE
    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
            File nomedia = new File(Environment.getExternalStorageDirectory().getPath()+"/"+AUDIO_RECORDER_FOLDER+"/.nomedia");
           try { nomedia.createNewFile(); } catch (IOException e) {Log.i("LOG:",".nomedia not created");}
        }

        return (file.getAbsolutePath() + "/" + selected_word_charseq + file_exts[currentFormat]);
    }

    private void startRecording() {
        //TODO SE IL FILE GIA' ESISTE, CANCELLALO E REGISTRA NUOVAMENTE
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);
        recorder.setOutputFile(getFilename());
        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Log.i("Say it!","Error: " + what + ", " + extra);
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
           Log.i("Say it!","Warning: " + what + ", " + extra);
        }
    };

    private void stopRecording() {
        if (null != recorder) {
            recorder.stop();
            recorder.reset();
            recorder.release();

            recorder = null;
        }
    }

    private void playRecording() {
        try {
            //TODO CHECK IF RECORDING ALREADY EXISTS. IF DOES NOT, DO NOT PLAY.
            Log.i("Say it!","Playing recording: "+Environment.getExternalStorageDirectory().getPath()+"/"+AUDIO_RECORDER_FOLDER+"/"+selected_word_charseq+".aac");
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory().getPath()+"/"+AUDIO_RECORDER_FOLDER+"/"+selected_word_charseq+".aac");
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    //FUNZIONI PER RICHIESTA PERMESSI
    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getActivity(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getActivity(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(),"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

}