package com.example.project_ez_talk.webrtc;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

/**
 * Observer for Session Description Protocol (SDP) events
 * Handles success/failure of creating and setting SDP descriptions
 */
public class MySdpObserver implements SdpObserver {

    /**
     * Called when SDP creation succeeds
     */
    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        // Override in subclass if needed
    }

    /**
     * Called when SDP is successfully set
     */
    @Override
    public void onSetSuccess() {
        // Override in subclass if needed
    }

    /**
     * Called when SDP creation fails
     */
    @Override
    public void onCreateFailure(String s) {
        // Override in subclass if needed
    }

    /**
     * Called when setting SDP fails
     */
    @Override
    public void onSetFailure(String s) {
        // Override in subclass if needed
    }
}