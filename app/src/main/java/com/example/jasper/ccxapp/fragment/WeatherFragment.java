package com.example.jasper.ccxapp.fragment;

/**
 * Created by Administrator on 2017/6/5 0005.
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.example.jasper.ccxapp.R;
import com.example.jasper.ccxapp.util.LocationUtil;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WeatherFragment extends Fragment implements SpeechSynthesizerListener {

    private static final String SAMPLE_DIR_NAME = "baiduTTS";
    private static final String SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female.dat";
    private static final String SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male.dat";
    private static final String TEXT_MODEL_NAME = "bd_etts_text.dat";
    private static final String ENGLISH_SPEECH_FEMALE_MODEL_NAME = "bd_etts_speech_female_en.dat";
    private static final String ENGLISH_SPEECH_MALE_MODEL_NAME = "bd_etts_speech_male_en.dat";
    private static final String ENGLISH_TEXT_MODEL_NAME = "bd_etts_text_en.dat";
    private static final String APP_ID = "9735452";
    private static final String API_KEY = "nh5Cc5tgDPaGDDGkHE8GbQ7y";
    private static final String SECRET_KEY = "51c0c5c57f36e7363b6779a9046ab003";

    private TextView weather_city, weather_temperture;
    private ImageView weather_image;
    private ImageView audio_image;
    private static String cityName = "";
    private String result = "";
    private static Context context = null;
    private Bitmap bitmap;
    private static WeatherFragment weather = null;
    public static int weather_hour = 60;
    private static Handler handler3 = new Handler();

    private String needToRead = "";

    private SpeechSynthesizer mSpeechSynthesizer;//百度语音合成客户端
    private String mSampleDirPath;


    @SuppressWarnings("deprecation")
    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            weather.getActivity().removeDialog(0);
            Toast.makeText(weather.getActivity(), "加载失败", Toast.LENGTH_SHORT).show();
        }
    };
    //自动刷新
    private Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            weather.send(cityName);
            Message m = weather.handler.obtainMessage();
            weather.handler.sendMessage(m);
            handler3.postDelayed(this, weather_hour*3600*1000);
        }
    };
    @SuppressLint("HandlerLeak")
    @SuppressWarnings("deprecation")
    public static Handler handler1 = new Handler(){
        public void handleMessage(Message msg){
            weather.getActivity().showDialog(0);
            //启动定时器
            handler3.postDelayed(runnable, 5000);   //五秒后执行
            new Thread(new Runnable() {
                @Override
                public void run() {
                    weather.send(cityName);
                    Message m = weather.handler.obtainMessage();
                    weather.handler.sendMessage(m);
                }
            }).start();
        }
    };
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            if(result != null){
                try {
                    JSONObject datajson = new JSONObject(result);  //第一步，将String格式转换回json格式
                    JSONArray results = datajson.getJSONArray("results");  //获取results数组

                    JSONObject city = results.getJSONObject(0);
                    String currentCity = city.getString("currentCity");  //获取city名字
                    String pm25 = city.getString("pm25");   //获取pm25
                    weather_city.setText("城市："+currentCity+"\n"+"pm2.5："+pm25);  //测试城市和pm25
                    JSONArray index = city.getJSONArray("index"); //获取index里面的JSONArray

                    //weather_data, 未来几天
                    JSONArray weather_data = city.getJSONArray("weather_data");
                    //获取今天
                    JSONObject today = weather_data.getJSONObject(0);
                    String date0 = today.getString("date");
                    final String dayPictureUrl0 = today.getString("dayPictureUrl");
                    final String nightPictureUrl0 = today.getString("nightPictureUrl");
                    String weather0 = today.getString("weather");
                    String wind0 = today.getString("wind");
                    String temperature0 = today.getString("temperature");
                    weather_temperture.setText(date0+"\n"+"天气："+weather0+"\n"+"风力："+
                            wind0+"\n"+"温度范围："+temperature0+"\n");
                    needToRead = "\n"+"今天："+date0+weather0+"\n"+"风力："+
                            wind0+"\n"+"温度范围："+temperature0+"\n";

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Calendar now = Calendar.getInstance();
                            int nowHour = now.HOUR_OF_DAY;
                            if(nowHour < 6 || nowHour >= 18) {
                                bitmap = returnBitMap(nightPictureUrl0);
                            }else {
                                bitmap = returnBitMap(dayPictureUrl0);
                            }
                            Message m = handler2.obtainMessage();
                            handler2.sendMessage(m);
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            super.handleMessage(msg);
        }
    };
    @SuppressWarnings("deprecation")
    @SuppressLint("HandlerLeak")
    private Handler handler2 = new Handler(){
        public void handleMessage(Message msg){
            if(bitmap!=null){
                weather_image.setImageBitmap(bitmap);
                //停止计时器
                handler3.removeCallbacks(runnable);
                weather.getActivity().removeDialog(0);
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(checkPermision(new String[]{
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO})){
            LocationUtil.getCNBylocation(getActivity());
            cityName = LocationUtil.cityName;
            //启动计时器
            handler3.postDelayed(runnable2, weather_hour*3600*1000);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    send(cityName);
                    Message m = handler.obtainMessage();
                    handler.sendMessage(m);
                }
            }).start();
            startTTS();
        }
        context = getActivity();
        weather = this;

        View view = inflater.inflate(R.layout.fragment_weather, container,false);
        weather_city = (TextView)view.findViewById(R.id.weather_city_tv);
        weather_image = (ImageView) view.findViewById(R.id.weather_image_iv);
        weather_temperture = (TextView) view.findViewById(R.id.weather_temperture_tv);
        audio_image=(ImageView) view.findViewById(R.id.start_audio);
        weather_city.setText(cityName);
        audio_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpeechSynthesizer == null) {
                    startTTS();
                }
                if(mSpeechSynthesizer != null){
                    mSpeechSynthesizer.speak(needToRead);
                }else{
                    Toast.makeText(getContext(),"由于权限缺失，所以不能正常播放天气", Toast.LENGTH_SHORT);
                }
            }
        });
        weather_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpeechSynthesizer == null) {
                    startTTS();
                }
                if(mSpeechSynthesizer != null){
                    mSpeechSynthesizer.speak(needToRead);
                }else{
                    Toast.makeText(getContext(),"由于权限缺失，所以不能正常播放天气", Toast.LENGTH_SHORT);
                }
            }
        });
        weather_temperture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpeechSynthesizer == null) {
                    startTTS();
                }
                if(mSpeechSynthesizer != null){
                    mSpeechSynthesizer.speak(needToRead);
                }else{
                    Toast.makeText(getContext(),"由于权限缺失，所以不能正常播放天气", Toast.LENGTH_SHORT);
                }
            }
        });
        weather_city.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSpeechSynthesizer == null) {
                    startTTS();
                }
                if(mSpeechSynthesizer != null){
                    mSpeechSynthesizer.speak(needToRead);
                }else{
                    Toast.makeText(getContext(),"由于权限缺失，所以不能正常播放天气", Toast.LENGTH_SHORT);
                }
            }
        });
        return view;
    }
    private String send(String city){
        String target = TargetUrl.url1+city+TargetUrl.url2;  //要提交的目标地址
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpRequest = new HttpGet(target);  //创建HttpGet对象
        HttpResponse httpResponse = null;
        try {
            httpResponse = httpclient.execute(httpRequest);
            if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                result = EntityUtils.toString(httpResponse.getEntity()).trim();  //获取返回的字符串
            }else{
                result = "fail";
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    //以Bitmap的方式获取一张图片
    public Bitmap returnBitMap(String url){
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try{
            myFileUrl = new URL(url);
        }catch(MalformedURLException e){
            e.printStackTrace();
        }
        try{
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        return bitmap;
    }

    public boolean checkPermision(String[] permissions2) {
        boolean flag = false;
        List<String> permissions3 = new ArrayList<String>();
        for(String permission : permissions2){
            if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED){
                flag = true;
                permissions3.add(permission);
            }
        }
        String[] permissions = new String[permissions3.size()];
        for(int i = 0; i < permissions3.size(); i++){
            permissions[i] = permissions3.get(i);
        }
        if(flag){
            WeatherFragment.this.requestPermissions(permissions, 1);
        }else{
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            Log.i("MainActivity", "permission");
            boolean isAllGranted = false;
            String[] permissions2 = new String[]{
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS};
            String[] permissions3 = new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_WIFI_STATE};
            for(String permission : permissions2){
                if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED){
                    isAllGranted = true;
                }
            }
            if (!isAllGranted) {
                Log.i("MainActivity", "permission1");
                //申请权限成功后需要调用的函数
                LocationUtil.getCNBylocation(getActivity());
                cityName = LocationUtil.cityName;
                //启动计时器
                handler3.postDelayed(runnable2, weather_hour*3600*1000);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        send(cityName);
                        Message m = handler.obtainMessage();
                        handler.sendMessage(m);
                    }
                }).start();
            } else {
                new AlertDialog.Builder(getActivity()).setTitle("系统提示").setMessage("由于未赋予相应的权限，无法正常使用查看天气功能！")
                        .setPositiveButton("确定", null).show();
            }
            isAllGranted = false;
            for(String permission : permissions3){
                if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED){
                    isAllGranted = true;
                }
            }
            if(!isAllGranted){
                Log.i("MainActivity", "permission2");
                startTTS();
            }else {
                new AlertDialog.Builder(getActivity()).setTitle("系统提示").setMessage("由于未赋予相应的权限，无法正常使用播放天气功能！")
                        .setPositiveButton("确定", null).show();
            }
        }
    }

    private void startTTS() {
        initialEnv();
        initialTts();
    }

    @Override
    public void onDestroy() {
        if(mSpeechSynthesizer!=null) {
            this.mSpeechSynthesizer.release();//释放资源
        }
        handler3.removeCallbacks(runnable2);
        super.onDestroy();
    }

    /**
     * 初始化语音合成客户端并启动
     */
    private void initialTts() {
        //获取语音合成对象实例
        this.mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        //设置Context
        this.mSpeechSynthesizer.setContext(getContext());
        //设置语音合成状态监听
        this.mSpeechSynthesizer.setSpeechSynthesizerListener(this);
        //文本模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, mSampleDirPath + "/"
                + TEXT_MODEL_NAME);
        //声学模型文件路径 (离线引擎使用)
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, mSampleDirPath + "/"
                + SPEECH_FEMALE_MODEL_NAME);
        //请替换为语音开发者平台上注册应用得到的App ID (离线授权)
        this.mSpeechSynthesizer.setAppId(APP_ID);
        // 请替换为语音开发者平台注册应用得到的apikey和secretkey (在线授权)
        this.mSpeechSynthesizer.setApiKey(API_KEY, SECRET_KEY);
        //发音人（在线引擎），可用参数为0,1,2,3。。。
        //（服务器端会动态增加，各值含义参考文档，以文档说明为准。0--普通女声，1--普通男声，2--特别男声，3--情感男声。。。）
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置Mix模式的合成策略
        this.mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 授权检测接口(可以不使用，只是验证授权是否成功)
        AuthInfo authInfo = this.mSpeechSynthesizer.auth(TtsMode.MIX);
        if (authInfo.isSuccess()) {
        } else {
            String errorMsg = authInfo.getTtsError().getDetailMessage();
        }
        // 引擎初始化tts接口
        mSpeechSynthesizer.initTts(TtsMode.MIX);
        // 加载离线英文资源（提供离线英文合成功能）
        int result =
                mSpeechSynthesizer.loadEnglishModel(mSampleDirPath + "/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath
                        + "/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
    }

    @Override
    public void onSynthesizeStart(String s) {

    }

    @Override
    public void onSynthesizeDataArrived(String s, byte[] bytes, int i) {

    }

    @Override
    public void onSynthesizeFinish(String s) {

    }

    @Override
    public void onSpeechStart(String s) {

    }

    @Override
    public void onSpeechProgressChanged(String s, int i) {

    }

    @Override
    public void onSpeechFinish(String s) {

    }

    @Override
    public void onError(String s, SpeechError speechError) {

    }

    private void initialEnv() {
        if (mSampleDirPath == null) {
            String sdcardPath = Environment.getExternalStorageDirectory().toString();
            mSampleDirPath = sdcardPath + "/" + SAMPLE_DIR_NAME;
        }
        File file = new File(mSampleDirPath);
        if (!file.exists()) {
            file.mkdirs();
        }
        copyFromAssetsToSdcard(false, SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/" + SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, TEXT_MODEL_NAME, mSampleDirPath + "/" + TEXT_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_FEMALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_FEMALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_SPEECH_MALE_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_SPEECH_MALE_MODEL_NAME);
        copyFromAssetsToSdcard(false, "english/" + ENGLISH_TEXT_MODEL_NAME, mSampleDirPath + "/"
                + ENGLISH_TEXT_MODEL_NAME);
    }

    /**
     * 将工程需要的资源文件拷贝到SD卡中使用（授权文件为临时授权文件，请注册正式授权）
     *
     * @param isCover 是否覆盖已存在的目标文件
     * @param source
     * @param dest
     */
    public void copyFromAssetsToSdcard(boolean isCover, String source, String dest) {
        File file = new File(dest);
        if (isCover || (!isCover && !file.exists())) {
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = getResources().getAssets().open(source);
                String path = dest;
                fos = new FileOutputStream(path);
                byte[] buffer = new byte[1024];
                int size = 0;
                while ((size = is.read(buffer, 0, 1024)) >= 0) {
                    fos.write(buffer, 0, size);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class TargetUrl {
        public final static String url1 = "http://api.map.baidu.com/telematics/v3/weather?location=";
        public final static String url2 = "&output=json&ak=9cCAXQFB468dsH11GOWL8Lx4";
    }
}
