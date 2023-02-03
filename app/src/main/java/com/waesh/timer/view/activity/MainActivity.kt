package com.waesh.timer.view.activity

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.waesh.timer.databinding.ActivityMainBinding
import com.waesh.timer.service.TimerService
import com.waesh.timer.view.fragment.HomeFragmentDirections

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""


        if (intent.action == ACTION_TIMER_FRAGMENT || isTimerServiceRunning(TimerService::class.java)) {
            val navHostFragment =
                supportFragmentManager.findFragmentById(binding.fragmentContainerView.id) as NavHostFragment
            val navController = navHostFragment.navController
            //navController.navigateUp()
            navController.navigate(HomeFragmentDirections.actionHomeFragmentToTimerFragment())
        }
    }

    @Suppress("DEPRECATION")
    fun isTimerServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    companion object {
        const val ACTION_TIMER_FRAGMENT = "com.waesh.timer.ACTION_TIMER_FRAGMENT"
    }
}