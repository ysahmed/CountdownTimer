package com.waesh.timer.view.fragment

import android.os.Bundle
import androidx.navigation.NavDirections
import com.waesh.timer.R
import kotlin.Int
import kotlin.Long

public class HomeFragmentDirections private constructor() {
  private data class ActionHomeFragmentToTimerFragment(
    public val timeInMillis: Long = 0L
  ) : NavDirections {
    public override val actionId: Int = R.id.action_homeFragment_to_timerFragment

    public override val arguments: Bundle
      get() {
        val result = Bundle()
        result.putLong("timeInMillis", this.timeInMillis)
        return result
      }
  }

  public companion object {
    public fun actionHomeFragmentToTimerFragment(timeInMillis: Long = 0L): NavDirections =
        ActionHomeFragmentToTimerFragment(timeInMillis)
  }
}
