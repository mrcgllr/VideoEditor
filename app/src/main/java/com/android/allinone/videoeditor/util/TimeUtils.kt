package com.android.allinone.videoeditor.util

import java.util.concurrent.TimeUnit

object TimeUtils {

    const val ONE_HOUR = 3600000
    const val ONE_MINUTE = 60000
    const val ONE_SECOND = 1000

    fun toFormattedTime(i: Int): String {
        val i2 = i / ONE_HOUR
        val i3 = i - ONE_HOUR * i2
        val i4 = (i3 - i3 / ONE_MINUTE * ONE_MINUTE) / ONE_SECOND
        return if (i2 > 0) {
            String.format(
                "%02d:%02d:%02d",
                *arrayOf<Any>(Integer.valueOf(i2), Integer.valueOf(0), Integer.valueOf(i4))
            )
        } else String.format("%02d:%02d", *arrayOf<Any>(Integer.valueOf(0), Integer.valueOf(i4)))
    }

    fun formatTimeUnit(j2: Long): String {
        return String.format(
            "%02d:%02d", *arrayOf<Any>(
                java.lang.Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(j2)), java.lang.Long.valueOf(
                    TimeUnit.MILLISECONDS.toSeconds(j2) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            j2
                        )
                    )
                )
            )
        )
    }
}