package com.cmsv6demo;

import net.babelstar.common.play.Playback;
import net.babelstar.common.play.VideoView;
//import net.babelstar.common.play.Playback.PlaybackListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
//import android.util.DisplayMetrics;
import android.os.Environment;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.babelstar.gviewer.NetClient;
import com.google.gson.Gson;

public class PlaybackActivity extends Activity implements Playback.PlaybackListener {
    private boolean mIsPlaying = false;
    private Playback mPlayback;
    private VideoView mVideoView;

    private Button mBtnStart;
    private Button mBtnStop;
    private String mDevIdno;

    private boolean mIsDirect;
    private String mServer;
    private int mPort;
    // /mnt/sd1/rec_dir/REC0404.264;2021;5;18;39531;39666;;0;209691672;0;1;0;15;0;0;0;0;0;0;0;0;0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playback);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int picHeight = (screenWidth / 4) * 3;
		Log.d("alex video size", screenWidth + "-" + picHeight);
        mVideoView = (VideoView) findViewById(R.id.replayVideoView);

        mBtnStart = (Button) findViewById(R.id.btnStart);
        mBtnStop = (Button) findViewById(R.id.btnStop);

        PlayClickListener playClickListen = new PlayClickListener();
        mBtnStart.setOnClickListener(playClickListen);
        mBtnStop.setOnClickListener(playClickListen);

        mPlayback = new Playback(this);
        mPlayback.setVideoView(mVideoView);
        mPlayback.setPlayerListener(this);

        Intent intent = getIntent();
        if (intent.hasExtra("DevIDNO")) {
            mDevIdno = intent.getStringExtra("DevIDNO");
        }


        //mIsDirect = intent.getBooleanExtra("direct", false);
        //if(mIsDirect){
        mServer = intent.getStringExtra("serverIp");
        mPort = intent.getIntExtra("port", 6605);
        mDevIdno = intent.getStringExtra("devIdno");
        //}

        mPort = 6613;
        // mPlayback.setLanInfo(mServer, mPort);

//        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
//        Log.d("path", sdPath);
//		NetClient.Initialize("/mnt/sdcard/");
//        NetClient.Initialize(sdPath);
//        NetClient.SetDirSvr(mServer, mServer, 6605, 0);
        //

        //mPlayback.setVideoView(mVideoView);
        StartPlayback();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseWakeLock();
        StopPlayback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mPlayback.isViewing()) {
            acquireWakeLock();
            this.mPlayback.pause(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.mPlayback.isViewing()) {
            releaseWakeLock();
            this.mPlayback.pause(true);
        }
    }

    protected void StartPlayback() {
        Log.d("alex", "Start Playback");
//
//		if (mPlayback.isViewing()) {
//			mPlayback.StopVod();
//		}
        if (!mIsPlaying) {

            Intent intent = getIntent();
            byte[] file = intent.getByteArrayExtra("File");
            int nLength = intent.getIntExtra("Length", 0);
            int nChannel = intent.getIntExtra("Channel", 0);

            for (int i = 0; i < file.length; i++ ) {
                Log.d("alex value at " + String.valueOf(i) + " :", String.valueOf(file[i]));
            }

            Log.d("alex file length", "value: " + file.length);
            Log.d("alex length", "value: " + nLength);
            Log.d("alex channel", "value: " + nChannel);
            Log.d("alex mdvrId", "value: " + mDevIdno + ", server: " + mServer + ", port: " + mPort);

            mPlayback.setPlayerDevIdno(mDevIdno);
            //nLength = 101;
            Log.d("alex length", "value: " + nLength);
            mPlayback.StartVod(file, nLength, nChannel);


            //mIsPlaying = false;
            mIsPlaying = true;
            Log.d("alex", "server ip " + mServer + "-" + mPort + "-" + file.length + "-" + new Gson().toJson(file));
            Log.d("alex", "Playback Started-" + nLength + "-" + nChannel + "-" + mDevIdno);
            acquireWakeLock();

        }
    }

    private PowerManager.WakeLock mWakelock = null;

    private void acquireWakeLock() {
        if (this.mWakelock == null) {
            this.mWakelock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(6, getClass().getCanonicalName());
            this.mWakelock.acquire();
        }
    }

    private void releaseWakeLock() {
        PowerManager.WakeLock wakeLock = this.mWakelock;
        if (wakeLock != null && wakeLock.isHeld()) {
            Log.d("Alex", "PlaybackVodActivity releaseWakeLock");
            this.mWakelock.release();
            this.mWakelock = null;
        }
    }

    protected void StopPlayback() {
        if (mIsPlaying) {
            mPlayback.StopVod();
            mIsPlaying = false;
            Log.d("alex", "Playback Stopped");
            releaseWakeLock();
        }
    }

    @Override
    public void onBeginPlay() {
        Log.d("alex", "onBeginPlay");
    }

    @Override
    public void onUpdatePlay(int i, int i1) {
        Log.d("alex", "onUpdatePlay" + String.valueOf(i) + " - " + String.valueOf(i1));
    }

    @Override
    public void onEndPlay() {
        Log.d("alex", "onEndPlay");
    }

    @Override
    public void onClick(VideoView videoView, int i) {
        Log.d("alex", "onClick");
    }

    @Override
    public void onDbClick(VideoView videoView, int i) {
        Log.d("alex", "onDbClick");
    }

    final class PlayClickListener implements OnClickListener {
        public void onClick(View v) {
            if (v.equals(mBtnStart)) {
                StartPlayback();
            } else if (v.equals(mBtnStop)) {
                StopPlayback();
            }
        }
    }
}
