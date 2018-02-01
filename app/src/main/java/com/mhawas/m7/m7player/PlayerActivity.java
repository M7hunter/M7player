package com.mhawas.m7.m7player;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.mp4.Mp4Extractor;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.*;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    // bandwidth meter to measure and estimate bandwidth
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private SimpleExoPlayer player;
    private SimpleExoPlayerView playerView;

    private Uri videoUri;

    private ComponentListener componentListener;

    private long playbackPosition;
    private int currentWindow;
    private boolean playWhenReady = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Intent callingIntent = this.getIntent();
        if(callingIntent != null) {
            videoUri = callingIntent.getData();
            Log.e("video uri",""+videoUri);
        }

        componentListener = new ComponentListener();

        playerView = findViewById(R.id.video_view);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hideSystemUi();
        if ((Util.SDK_INT <= 23 || player == null)) {
            initializePlayer();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }

    private void initializePlayer() {
        if (player == null) {
            // a factory to create an AdaptiveVideoTrackSelection
            TrackSelection.Factory adaptiveTrackSelectionFactory =
                    new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
            // let the factory create a player instance with default components
            player = ExoPlayerFactory.newSimpleInstance(
                    new DefaultRenderersFactory(this),
                    new DefaultTrackSelector(adaptiveTrackSelectionFactory),
                    new DefaultLoadControl());

            player.addListener(componentListener);
            player.addVideoDebugListener(componentListener);
            player.addAudioDebugListener(componentListener);

            playerView.setPlayer(player);
            player.setPlayWhenReady(playWhenReady);
            player.seekTo(currentWindow, playbackPosition);
        }

//        String urimp4 = "path/FileName.mp4"; //upload file to device and add path/name.mp4

        player.prepare(buildMediaSource(videoUri), true, false);
    }

    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.removeListener(componentListener);
            player.removeVideoDebugListener(componentListener);
            player.removeAudioDebugListener(componentListener);
            player.release();
            player = null;
        }
    }

    private MediaSource buildMediaSource(Uri uri) {

        //to run local videos.

        return new ExtractorMediaSource(uri,
                new DefaultDataSourceFactory(this,"ua"),
                new DefaultExtractorsFactory(), null, null);

        //to stream from the web.

//        DataSource.Factory manifestDataSourceFactory =
//                new DefaultHttpDataSourceFactory("ua");
//        DashChunkSource.Factory dashChunkSourceFactory =
//                new DefaultDashChunkSource.Factory(
//                        new DefaultHttpDataSourceFactory("ua", BANDWIDTH_METER));
//        return new DashMediaSource.Factory(dashChunkSourceFactory,
//                manifestDataSourceFactory).createMediaSource(uri);
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    private class ComponentListener extends Player.DefaultEventListener implements VideoRendererEventListener, AudioRendererEventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case ExoPlayer.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE";
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING";
                    break;
                case ExoPlayer.STATE_READY:
                    stateString = "ExoPlayer.STATE_READY";
                    break;
                case ExoPlayer.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED";
                    break;
                default:
                    stateString = "UNKNOWN_STATE";
            }
            Log.d("PlayerActivity","changed state to: " + stateString + "play when ready: " + playWhenReady);
        }

        //--------- v
        @Override
        public void onVideoEnabled(DecoderCounters counters) {

        }

        @Override
        public void onVideoDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onVideoInputFormatChanged(Format format) {

        }

        @Override
        public void onDroppedFrames(int count, long elapsedMs) {

        }

        @Override
        public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onRenderedFirstFrame(Surface surface) {

        }

        @Override
        public void onVideoDisabled(DecoderCounters counters) {

        }

        //---------- a
        @Override
        public void onAudioEnabled(DecoderCounters counters) {

        }

        @Override
        public void onAudioSessionId(int audioSessionId) {

        }

        @Override
        public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {

        }

        @Override
        public void onAudioInputFormatChanged(Format format) {

        }

        @Override
        public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {

        }

        @Override
        public void onAudioDisabled(DecoderCounters counters) {

        }
    }
}
