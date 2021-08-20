package com.think.emicalculator.broadcastreciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class VolumeChangeReceiver: BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        println("Volume Changed")

        if (p1?.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
            println("Volume Changed")
        }
    }
}