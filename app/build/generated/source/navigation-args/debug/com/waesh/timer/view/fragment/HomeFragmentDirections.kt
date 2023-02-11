package com.waesh.timer.view.fragment

import android.os.Bundle
import androidx.navigation.NavDirections
import com.waesh.timer.R
import kotlin.Int
import kotlin.Long
import kotlin.String

public class HomeFragmentDirections private constructor() {
  private data class ActionHomeFragmentToTimerFragment(
    public val timeInMillis: Long = 0L,
    public val ringtoneUri: String = "\"content://settings/system/alarm_alert\""
  ) : NavDirections {
    public override val actionId: Int = R.id.action_homeFragment_to_timerFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putLong("timeInMillis", this.timeInMillis)
        result.putString("ringtoneUri", this.ringtoneUri)
        return result
      }
  }

  public companion object {
    public fun actionHomeFragmentToTimerFragment(timeInMillis: Long = 0L, ringtoneUri: String =
        "\"content://settings/system/alarm_alert\""): NavDirections =
        ActionHomeFragmentToTimerFragment(timeInMillis, ringtoneUri)
  }
}
