package com.example.project_ez_talk.webrtc;

import com.google.gson.annotations.SerializedName;

import org.webrtc.IceCandidate;

/**
 * ✅ IceCandidate DTO (Data Transfer Object)
 * Wrapper for serializing IceCandidate to/from JSON
 *
 * The native WebRTC IceCandidate doesn't serialize well with Gson,
 * so we use this DTO to convert between formats.
 */
public class IceCandidateModel {

    @SerializedName("sdpMLineIndex")
    public int sdpMLineIndex;

    @SerializedName("sdpMid")
    public String sdpMid;

    @SerializedName("sdp")
    public String sdp;

    /**
     * Empty constructor for Gson
     */
    public IceCandidateModel() {
    }

    /**
     * Constructor from WebRTC IceCandidate
     */
    public IceCandidateModel(IceCandidate candidate) {
        this.sdpMLineIndex = candidate.sdpMLineIndex;
        this.sdpMid = candidate.sdpMid;
        this.sdp = candidate.sdp;
    }

    /**
     * Convert to WebRTC IceCandidate
     */
    public IceCandidate toIceCandidate() {
        return new IceCandidate(sdpMid, sdpMLineIndex, sdp);
    }

    /**
     * Get candidate string for logging
     */
    public String getCandidateString() {
        return "candidate:" + sdpMLineIndex + " " + sdpMid + " " + sdp;
    }

    @Override
    public String toString() {
        return "IceCandidateModel{" +
                "sdpMLineIndex=" + sdpMLineIndex +
                ", sdpMid='" + sdpMid + '\'' +
                ", sdp='" + sdp + '\'' +
                '}';
    }
}