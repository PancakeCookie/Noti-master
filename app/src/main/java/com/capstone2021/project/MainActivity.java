package com.capstone2021.project;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.ActivityCompat;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int PERMISSION = 1;

    Button mBtnSetting;
    Button ttsSetting;
    Button fButton;
    Button muteButton;
    Button lengthfbutton;


    String string999="";
    String filtering="0000000000000000000000";                                   //필터링 정의
    String mutestart;
    String muteend;
    String lengFilter;
    int mutestartInt = 0;
    int muteendInt = 0;
    int lengFilterInt = 40;

    BroadcastReceiver kakaoReceiver;
    Ringtone rt;
    MediaPlayer player;



    public TextView textView2;
    public TextView textView3;
    public TextView textView4;
    public TextView textView5;
    public static Context mContext;
    public EditText editText1;
    public EditText editText2;
    public EditText editText3;
    public EditText editText4;


    private TextToSpeech tts;              // TTS 변수 선언
    private int isTTSReady = TextToSpeech.STOPPED;
    boolean ttsRun = false;

    TextView recodeView;  // STT record 뷰
    SpeechRecognizer mRecognizer;

    SensorManager mSensorManager;
    Sensor mAccelerometer;
    private long shakeTime;                                     //흔들림 감지 변수선언
    private static final int SHAKE_SKIP_TIME = 500;
    private static final float SHAKE_GRAVITY = 2.7F;

    SimpleDateFormat sdf = new SimpleDateFormat("HH시mm분");
    SimpleDateFormat sdf2 = new SimpleDateFormat("HHmm");




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnSetting = (Button) findViewById(R.id.btnSetting);
        ttsSetting = (Button) findViewById(R.id.ttsSetting);
        fButton = (Button) findViewById(R.id.filtering);
        lengthfbutton = (Button) findViewById(R.id.lengthF);
        muteButton = (Button) findViewById(R.id.MuteTime);

        recodeView = findViewById(R.id.recodeText);
        editText1 = findViewById(R.id.editTextF);
        editText2 = findViewById(R.id.editTextT1);
        editText3 = findViewById(R.id.editTextT2);
        editText4 = findViewById(R.id.editLengthF);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);         // 핸드폰 흔들림 감지를 위한 선언
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);   //알람 울리기
        rt = RingtoneManager.getRingtone(getApplicationContext(),notification);
        player = MediaPlayer.create(this, R.raw.sttstart);

        initializeKakaoReceiver();   //카카오톡 리시버 초기화




        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }

        mBtnSetting.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                    MainActivity.this.startActivity(new Intent("android.settings.BLUETOOTH_SETTINGS"));

            }
        });

        ttsSetting.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainActivity.this.startActivity(new Intent("com.android.settings.TTS_SETTINGS"));

            }
        });

        fButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {                                      //edit text에 필터링 단어를 적은후 버튼을 누르면 필터링이 설정됨.
                if(!(editText1.getText().toString().equals(""))) {
                    filtering = editText1.getText().toString();
                    textView5.setText("필터링 단어: " + filtering);
                    Toast.makeText(getApplicationContext(), "필터링 설정이 완료되었습니다", Toast.LENGTH_SHORT).show();    //필터링 설정 버튼
                }else {
                    filtering = "0000000000000000000000";
                    textView5.setText("필터링 단어");
                    Toast.makeText(getApplicationContext(), "필터링 해제", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lengthfbutton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {
                if (editText4.getText().toString().equals("")) {
                    lengFilterInt = 999;
                    textView4.setText("글자수 제한");
                    Toast.makeText(getApplicationContext(), "글자수 제한 해제", Toast.LENGTH_SHORT).show();
                } else {
                    lengFilter = mutestart = editText4.getText().toString().trim();
                    lengFilterInt = Integer.parseInt(lengFilter);
                    textView4.setText("글자수 제한: " + lengFilter + "자");
                    Toast.makeText(getApplicationContext(), "글자수 제한 설정이 완료되었습니다", Toast.LENGTH_SHORT).show();
                }
            }
        });

        muteButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View v) {

                if(editText2.getText().toString().equals("")) {
                    mutestartInt = 0;
                    muteendInt = 0;
                    textView3.setText("무음시간 설정");
                    Toast.makeText(getApplicationContext(), "무음시간 해제", Toast.LENGTH_SHORT).show();
                }else {
                    mutestart = editText2.getText().toString().trim();
                    muteend = editText3.getText().toString().trim();
                    mutestartInt = Integer.parseInt(mutestart);
                    muteendInt = Integer.parseInt(muteend);
                    textView3.setText("무음시간 " + mutestart + " 부터 "+ muteend + " 까지");
                    Toast.makeText(getApplicationContext(), "무음시간 설정이 완료되었습니다", Toast.LENGTH_SHORT).show();
                }






            }
        });


        //tts = new TextToSpeech(this, (TextToSpeech.OnInitListener) this);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                // 작업 성공시
                if (status == TextToSpeech.SUCCESS) {
                    // 언어 설정
                    int language = tts.setLanguage((Locale.KOREAN));
                    // 만약 데이터가 없거나 지원하지 않는 경우
                    if (language == TextToSpeech.LANG_MISSING_DATA || language == TextToSpeech.LANG_NOT_SUPPORTED) {

                    } else {
                        isTTSReady = status;
                    }
                } else {

                }
            }
        });

        mContext = this;

        textView2 = (TextView) findViewById(R.id.textView2);
        textView3 = (TextView) findViewById(R.id.textView3);
        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);

        registerReceiver(this.kakaoReceiver, new IntentFilter("android.service.notification.NotificationListenerService"));

        if (!permissionGrantred()) {
            Intent intent = new Intent(
                    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);


        }



    }
    private boolean permissionGrantred() {
        Set<String> sets = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (sets != null && sets.contains(getPackageName())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

            tts.stop();
            tts.shutdown();
            tts = null;

        if (mRecognizer != null) {
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer = null;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener((SensorEventListener) this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);



    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this);
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {

            processIntent(intent);
            initializeKakaoReceiver();
            super.onNewIntent(intent);

    }



    private void processIntent(Intent intent) {
        if (intent != null) {
            // 인텐트에서 전달된 데이터를 추출하여, 활용한다.
            if (!ttsRun) {                 //수정 tts가 실행되고 있을때는 중간에 문자가와도 tts가 작동하지않는다.
                String init = "";
                ttsRun = true;



                boolean handler3 = new Handler().postDelayed(new Runnable() {    /////0.5초간 딜레이  바로실행하면 notify를 읽어오는데 시간이있어서 문제가 생김.
                    @Override
                    public void run() {

                        string999 = textView2.getText().toString();
                        String time= sdf2.format(new Date(System.currentTimeMillis()));

                        String textlength = textView2.getText().toString();
                        textlength = textlength.replace(" ","");

                        if (!(( mutestartInt < Integer.parseInt(time)) && (Integer.parseInt(time) < muteendInt))  ){
                            if (!string999.contains(filtering)) {            ///필터링 설정

                                if(textlength.length()< lengFilterInt ){
                                    TextToSpeech(string999);
                                }
                            }


                        }



                    }
                }, 500);


                boolean handler = new Handler().postDelayed(new Runnable() {    /////7초간 딜레이
                    @Override
                    public void run() {


                        String time= sdf2.format(new Date(System.currentTimeMillis()));
                        if (!(( mutestartInt < Integer.parseInt(time)) && (Integer.parseInt(time) < muteendInt))  ){
                            if (!string999.contains(filtering)) {            ///필터링 설정

                                String textlength = textView2.getText().toString();
                                textlength = textlength.replace(" ","");



                                 if(textlength.length()< lengFilterInt ){
                                sttStart();
                                 }
                            }
                        }

                    }
                }, 9000);         //수정 시간늘림

                boolean handler2 = new Handler().postDelayed(new Runnable() {    /////14초간 딜레이후 명령 실행
                    @Override
                    public void run() {
                        SttText(recodeView, string999);

                        recodeView.setText(init);

                    }
                }, 15000);


            }
        }
    }


    // 문자 읽어주기
    public void TextToSpeech(String str) {
        if (isTTSReady == TextToSpeech.SUCCESS) {
            // 음량
            tts.setPitch((float) 1.0);
            // 재생 속도
            tts.setSpeechRate((float) 1.0);
            // 재생
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 안드로이드 버전이 롤리팝 이상이면
                int result = tts.speak(str, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
                if (result >= 0)
                    Log.d("SMSToSpeech", "=====> TextToSpeech LOLLIPOP speak SUCCESS <=====" + str);
                else
                    Log.d("SMSToSpeech", "=====> TextToSpeech LOLLIPOP speak ERROR <=====" + str);
            } else {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "UniqueID");
                tts.speak(str, TextToSpeech.QUEUE_FLUSH, map);
            }
        }
    }

    // STT 실행
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {


        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {


        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

            for (int i = 0; i < matches.size(); i++) {
                recodeView.setText(matches.get(i));    //음성을 recodeview로 옮긴다.
            }

        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    };

    public void sttStart(){

            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "com.package.name");
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "Ko-KR");
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);      //여기 3개가 음성을 인식하고, 텍스트로 출력하는 과정
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
            Toast.makeText(getApplicationContext(), "음성인식 시작", Toast.LENGTH_SHORT).show();
            player.start();


    }

    public void SttText(TextView textView, String string) {       // STT 음성인식 후 명령부분
        String string2 = textView.getText().toString();    //textview( recodeview를 string으로 변환 ->공백 제거하기위함)
        string2 = string2.replace(" ", "");   // 공백제거
        if (string2.indexOf("다시") > -1) {
            TextToSpeech(string);                                           // 다시한번 들려줌

        } else if (string2.indexOf("카톡") > -1 || string2.indexOf("카카오톡") > -1) {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.kakao.talk");    // 카카오톡 실행
            startActivity(launchIntent);
        } else if (string2.indexOf("인스타") > -1){
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
            startActivity(launchIntent);
        }else if (string2.indexOf("지도") > -1){
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.maps");
            startActivity(launchIntent);
        }else if (string2.indexOf("인터넷") > -1){
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.sec.android.app.sbrowser");
            startActivity(launchIntent);
        }


        ttsRun = false;
    }

        @Override
        public void onSensorChanged(SensorEvent event) {                                  //흔들림 감지
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                float gravityX = axisX / SensorManager.GRAVITY_EARTH;
                float gravityY = axisY / SensorManager.GRAVITY_EARTH;
                float gravityZ = axisZ / SensorManager.GRAVITY_EARTH;

                Float f = gravityX * gravityX + gravityY * gravityY + gravityZ * gravityZ;
                double squaredD = Math.sqrt(f.doubleValue());
                float gForce = (float) squaredD;
                if (gForce > (SHAKE_GRAVITY *2) && !ttsRun){             //세게흔들면 STT
                    tts.stop();
                    sttStart();

                    boolean handler = new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SttText(recodeView, string999);

                            recodeView.setText("");

                        }
                    }, 5000);
                } else if (gForce > SHAKE_GRAVITY && !ttsRun) {            //약하게 흔들면 시간알림
                    long currentTime = System.currentTimeMillis();
                    if (shakeTime + SHAKE_SKIP_TIME > currentTime) {
                        return;
                    }
                    shakeTime = currentTime;
                    shakeTime++;
                    String time= sdf.format(new Date(System.currentTimeMillis()));
                    TextToSpeech("현재시각은"+time+"입니다");

                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }




    private void initializeKakaoReceiver() {                             //카카오톡이 오면 리시버
        this.kakaoReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.service.notification.NotificationListenerService")) {    //notify가 오면 (모든 경우이므로 notifyListener에서 제어)

                        Intent intent1 = new Intent();

                        MainActivity.this.processIntent(intent1);           //    process 인텐트를 쏜다.
                    }
                }

        };
    }
    }








