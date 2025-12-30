package com.example.project_ez_talk.webrtc;

public enum DataModelType {
    OFFER,           // ✅ Matches CallData.Type
    ANSWER,          // ✅ Matches CallData.Type
    ICE_CANDIDATE,   // ✅ Matches CallData.Type (renamed from IceCandidate)
    ACCEPT,          // ✅ Added from CallData.Type
    REJECT,          // ✅ Added from CallData.Type
    END,             // ✅ Added from CallData.Type
    START_CALL       // ✅ Renamed from StartCall for consistency
}