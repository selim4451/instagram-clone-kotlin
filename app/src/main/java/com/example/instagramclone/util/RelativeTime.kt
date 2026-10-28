package com.example.instagramclone.util

import android.content.Context
import android.text.format.DateUtils
import com.example.instagramclone.R

fun relativeTime(context: Context, timestamp: Long): CharSequence {
    val now = System.currentTimeMillis()
    return if (now - timestamp < DateUtils.MINUTE_IN_MILLIS) {
        context.getString(R.string.time_just_now)
    } else {
        DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.MINUTE_IN_MILLIS)
    }
}
