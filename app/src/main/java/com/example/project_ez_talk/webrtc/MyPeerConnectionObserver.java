package com.example.project_ez_talk.webrtc;

import android.util.Log;

import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.RtpReceiver;

/**
 * ✅ Enhanced Observer for PeerConnection events
 * Handles ICE candidates, audio streams, and connection state changes
 */
public class MyPeerConnectionObserver implements PeerConnection.Observer {

    private static final String TAG = "PeerConnectionObserver";
    private OnPeerConnectionCallback callback;

    public interface OnPeerConnectionCallback {
        void onIceCandidate(IceCandidate candidate);
        void onMediaStreamAdded(MediaStream mediaStream);
        void onConnectionStateChange(PeerConnection.PeerConnectionState newState);
        void onIceConnectionStateChange(PeerConnection.IceConnectionState iceConnectionState);
    }

    public MyPeerConnectionObserver(OnPeerConnectionCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onSignalingChange(PeerConnection.SignalingState signalingState) {
        Log.d(TAG, "🔄 Signaling State: " + signalingState);
    }

    @Override
    public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
        Log.d(TAG, "🌐 ICE Connection State: " + iceConnectionState);

        if (callback != null) {
            callback.onIceConnectionStateChange(iceConnectionState);
        }

        // Log specific states
        switch (iceConnectionState) {
            case NEW:
                Log.d(TAG, "   ℹ️ ICE gathering starting...");
                break;
            case CHECKING:
                Log.d(TAG, "   🔍 ICE candidates being checked...");
                break;
            case CONNECTED:
                Log.d(TAG, "   ✅ ICE connection established!");
                break;
            case COMPLETED:
                Log.d(TAG, "   ✅ ICE connection completed!");
                break;
            case FAILED:
                Log.e(TAG, "   ❌ ICE connection failed!");
                break;
            case DISCONNECTED:
                Log.w(TAG, "   ⚠️ ICE disconnected");
                break;
            case CLOSED:
                Log.d(TAG, "   🔌 ICE connection closed");
                break;
        }
    }

    @Override
    public void onIceConnectionReceivingChange(boolean isReceiving) {
        Log.d(TAG, "📡 ICE Receiving: " + (isReceiving ? "YES" : "NO"));
    }

    @Override
    public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
        Log.d(TAG, "📍 ICE Gathering State: " + iceGatheringState);
    }

    @Override
    public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d(TAG, "🎯 New ICE Candidate: " + iceCandidate.sdpMLineIndex + " / " + iceCandidate.sdpMid);
        if (callback != null) {
            callback.onIceCandidate(iceCandidate);
        }
    }
    @Override
    public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
        Log.d(TAG, "🗑️ ICE Candidates Removed: " + iceCandidates.length);
    }

    @Override
    public void onAddStream(MediaStream mediaStream) {
        Log.d(TAG, "🔊 Remote Stream Added!");
        Log.d(TAG, "   Audio Tracks: " + mediaStream.audioTracks.size());
        Log.d(TAG, "   Video Tracks: " + mediaStream.videoTracks.size());

        if (callback != null) {
            callback.onMediaStreamAdded(mediaStream);
        }
    }

    @Override
    public void onRemoveStream(MediaStream mediaStream) {
        Log.d(TAG, "🔇 Remote Stream Removed");
    }

    @Override
    public void onDataChannel(DataChannel dataChannel) {
        Log.d(TAG, "📊 Data Channel Created: " + dataChannel.label());
    }

    @Override
    public void onRenegotiationNeeded() {
        Log.d(TAG, "🔄 Renegotiation Needed");
    }

    @Override
    public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
        Log.d(TAG, "🎵 RTP Track Added!");
        Log.d(TAG, "   Track: " + rtpReceiver.track().kind());
        Log.d(TAG, "   Streams: " + mediaStreams.length);

        // Notify callback about media stream
        if (mediaStreams.length > 0 && callback != null) {
            callback.onMediaStreamAdded(mediaStreams[0]);
        }
    }

    @Override
    public void onConnectionChange(PeerConnection.PeerConnectionState newState) {
        Log.d(TAG, "🔌 Connection State: " + newState);

        if (callback != null) {
            callback.onConnectionStateChange(newState);
        }

        switch (newState) {
            case NEW:
                Log.d(TAG, "   ℹ️ Connection initializing...");
                break;
            case CONNECTING:
                Log.d(TAG, "   🔗 Connecting...");
                break;
            case CONNECTED:
                Log.d(TAG, "   ✅ Connected!");
                break;
            case DISCONNECTED:
                Log.w(TAG, "   ⚠️ Disconnected");
                break;
            case FAILED:
                Log.e(TAG, "   ❌ Connection Failed!");
                break;
            case CLOSED:
                Log.d(TAG, "   🔌 Connection Closed");
                break;
        }
    }
}