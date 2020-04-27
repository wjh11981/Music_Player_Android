package com.example.mp3_test;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    final int CNT=4800;  //代表SeekBar能调节的级数

    //重写返回键的方法，返回不会调用onDestroy方法，因此不会出现数据清空的情况
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SeekBar滑动的实现，我也不知道为什么必须将其放到onCreat里面，但是单独放在外面SeekBar滑动没响应
        final SeekBar seekBar=findViewById(R.id.seekBar1);
        final TextView Begin=findViewById(R.id.textView);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //滑动过程的回调函数，应当修改左端的时间
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //当歌曲时长过长时，两者相乘容易出现越界，因此先做除法转化为double来避免
                if (mediaPlayer != null) {
                    Begin.setText("         "+Caculate_Time((int) (mediaPlayer.getDuration() / (double) CNT * seekBar.getProgress())));
                }
            }

            //开始滑动的回调函数
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            //停止滑动的回调函数，此时使用seekTo函数
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                   mediaPlayer.seekTo((int)(mediaPlayer.getDuration()/(double)CNT*seekBar.getProgress()));
                }
            }
        });
        //利用TimerTask，每隔1s将SeekBar移动
        Timer t =new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if(mediaPlayer!=null&&mediaPlayer.isPlaying()) {
                    if (seekBar.getProgress() <= CNT) {
                        //先计算出每一级的时间，再用已经播放时间/每一级时间，得到需要将SeekBar往前移的级数
                        seekBar.setProgress(mediaPlayer.getCurrentPosition() / (mediaPlayer.getDuration() / CNT));
                    }
                }
            }
        },0,1000);
    }

    static final int REQUEST_MUSIC_OPEN = 1;
    MediaPlayer mediaPlayer;        //先不进行初始化，需要播放时再进行初始化，以便判断播放器目前状态

    //第一次打开时需要进行的操作
    public void First_Play(View v){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent,REQUEST_MUSIC_OPEN);
    }

    //为Intend的回调，去系统文件管理器中寻找指定文件，并直接打开播放
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MUSIC_OPEN && resultCode == RESULT_OK) {
            Uri MusicUri = data.getData();
            //每次重新选取音乐时，需要释放MediaPlayer
            if(mediaPlayer!=null){
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer=new MediaPlayer();
            try {
                mediaPlayer.setDataSource(getApplicationContext(),MusicUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
            mediaPlayer.setLooping(true);   //默认单曲循环
            ImageView imageView=findViewById(R.id.ImageView);   //此时修改
            if(mediaPlayer.isPlaying()){
                imageView.setActivated(true);
            }
            TextView End=findViewById(R.id.textView2);
            End.setText("    "+Caculate_Time(mediaPlayer.getDuration()));
        }
    }

    //判断当前播放状态，从而进行暂停或播放操作,并进行播放暂停图标的更改
    public void Play_or_Pause(View v) {
        if(mediaPlayer!=null) {
            ImageView imageView = findViewById(R.id.ImageView);
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                imageView.setActivated(true);
            } else {
                mediaPlayer.pause();
                imageView.setActivated(false);
            }
        }
    }

    //将播放时间时间转化为xx：xx的形式
    public String Caculate_Time(int times){
        times=times/1000;    //毫秒转化为秒
        int minutes,seconds;
        seconds=times%60;
        minutes=times/60;
        String min;
        String sec;
        String ans;
        if(seconds<10){
            sec="0"+seconds;
        } else{
            sec=""+seconds;
        }
        if(minutes<10){
            min="0"+minutes;
        }else{
            min=""+minutes;
        }
        ans=min+':'+sec;
        return ans;
    }
}
