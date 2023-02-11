package com.waesh.timer.view.fragment

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavArgs
import java.lang.IllegalArgumentException
import kotlin.Long
import kotlin.String
import kotlin.jvm.JvmStatic

public data class TimerFragmentArgs(
  public val timeInMillis: Long = 0L,
  public val ringtoneUri: String = "\"content://settings/system/alarm_alert\""
) : NavArgs {
  public fun toBundle(): Bundle {
    val result = Bundle()
    result.putLong("timeInMillis", this.timeInMillis)
    result.putString("ringtoneUri", this.ringtoneUri)
    return result
  }

  public fun toSavedStateHandle(): SavedStateHandle {
    val result = SavedStateHandle()
    result.set("timeInMillis", this.timeInMillis)
    result.set("ringtoneUri", this.ringtoneUri)
    return result
  }

  public companion object {
    @JvmStatic
    public fun fromBundle(bundle: Bundle): TimerFragmentArgs {
      bundle.setClassLoader(TimerFragmentArgs::class.java.classLoader)
      val __timeInMillis : Long
      if (bundle.containsKey("timeInMillis")) {
        __timeInMillis = bundle.getLong("timeInMillis")
      } else {
        __timeInMillis = 0L
      }
      val __ringtoneUri : String?
      if (bundle.containsKey("ringtoneUri")) {
        __ringtoneUri = bundle.getString("ringtoneUri")
        if (__ringtoneUri == null) {
          throw IllegalArgumentException("Argument \"ringtoneUri\" is marked as non-null but was passed a null value.")
        }
      } else {
        __ringtoneUri = "\"content://settings/system/alarm_alert\""
      }
      return TimerFragmentArgs(__timeInMillis, __ringtoneUri)
    }

    @JvmStatic
    public fun fromSavedStateHandle(savedStateHandle: SavedStateHandle): TimerFragmentArgs {
      val __timeInMillis : Long?
      if (savedStateHandle.contains("timeInMillis")) {
        __timeInMillis = savedStateHandle["timeInMillis"]
        if (__timeInMillis == null) {
          throw IllegalArgumentException("Argument \"timeInMillis\" of type long does not support null values")
        }
      } else {
        __timeInMillis = 0L
      }
      val __ringtoneUri : String?
      if (savedStateHandle.contains("ringtoneUri")) {
        __ringtoneUri = savedStateHandle["ringtoneUri"]
        if (__ringtoneUri == null) {
          throw IllegalArgumentException("Argument \"ringtoneUri\" is marked as non-null but was passed a null value")
        }
      } else {
        __ringtoneUri = "\"content://settings/system/alarm_alert\""
      }
      return TimerFragmentArgs(__timeInMillis, __ringtoneUri)
    }
  }
}
