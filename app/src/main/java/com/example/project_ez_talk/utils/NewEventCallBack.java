package com.example.project_ez_talk.utils;

import com.example.project_ez_talk.webrtc.DataModel;

public interface NewEventCallBack {
    void onNewEventReceived(DataModel model);
}