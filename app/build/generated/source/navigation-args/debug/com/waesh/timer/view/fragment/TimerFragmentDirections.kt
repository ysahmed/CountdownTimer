package com.waesh.timer.view.fragment

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.waesh.timer.R

public class TimerFragmentDirections private constructor() {
  public companion object {
    public fun actionTimerFragmentToHomeFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_timerFragment_to_homeFragment)
  }
}
