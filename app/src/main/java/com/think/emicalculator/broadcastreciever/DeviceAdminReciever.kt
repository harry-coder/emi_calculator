package com.think.emicalculator.broadcastreciever

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.widget.Toast
import com.think.emicalculator.R

class DeviceAdminReciever : DeviceAdminReceiver() {

    /*private fun showToast(context: Context, msg: String) {
        context.getString(R.string.admin_receiver_status, msg).let { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
        }
    }*/

    override fun onEnabled(context: Context, intent: Intent) {

    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
      return  context.getString(R.string.admin_receiver_status_disable_warning)

    }

    override fun onDisabled(context: Context, intent: Intent) {

    }

    override fun onPasswordChanged(context: Context, intent: Intent, userHandle: UserHandle) {

    }


}