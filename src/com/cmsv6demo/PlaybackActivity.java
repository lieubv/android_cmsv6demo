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

public class PlaybackActivity extends Activity {
	private boolean mIsPlaying = false;
	private Playback mPlayback;
	private VideoView mVideoView;
	
	private Button mBtnStart;
	private Button mBtnStop; 
	private String mDevIdno;

	private boolean mIsDirect;
	private String mServer;
	private  int mPort;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_playback);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenWidth = dm.widthPixels;
		int picHeight = (screenWidth / 4) * 3;
		Log.d("alex video size", screenWidth + "-" + picHeight);
		mVideoView = (VideoView) findViewById(R.id.imageView1);
		LayoutParams para = mVideoView.getLayoutParams();
		para.width = screenWidth;
		para.height = picHeight;
		mVideoView.setLayoutParams(para);
//		LayoutParams para = mVideoView.getLayoutParams();
//		para.width = screenWidth;
//		para.height = picHeight;
//		mVideoView.setLayoutParams(para);
		
		mBtnStart = (Button) findViewById(R.id.btnStart);
		mBtnStop = (Button) findViewById(R.id.btnStop);
		PlayClickListener playClickListen = new PlayClickListener();
		mBtnStart.setOnClickListener(playClickListen);
		mBtnStop.setOnClickListener(playClickListen);
		
		mPlayback = new Playback(this);
		mPlayback.setVideoView(mVideoView);
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

//		String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
//		Log.d("path", sdPath);
////		NetClient.Initialize("/mnt/sdcard/");
//		NetClient.Initialize(sdPath);
//		NetClient.SetDirSvr(mServer, mServer, 6605, 0);
		//
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
			mPlayback.setVideoView(mVideoView);
			Intent intent = getIntent();
			byte[] file = intent.getByteArrayExtra("File");
			int nLength = intent.getIntExtra("Length", 0);
			int nChannel = intent.getIntExtra("Channel", 0);
			mPlayback.setPlayerDevIdno(mDevIdno);
			//if(mIsDirect){
				mPlayback.setLanInfo(mServer, mPort);
			//}
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
