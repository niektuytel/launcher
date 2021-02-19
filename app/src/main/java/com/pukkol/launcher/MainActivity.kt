package com.pukkol.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pukkol.launcher.ui.home.HomeActivity
import com.pukkol.launcher.ui.intro.IntroActivity
import com.pukkol.launcher.util.Display

class MainActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Display.setSize(this)

        // setup main data
        if (!Setup.isInitialised) {
            SetupInit(this)

            // todo(still needed?) device language
            // val contextUtils = ContextUtils(applicationContext)
            // contextUtils.setAppLanguage(Setup.deviceSettings().language)
        }

        // start activity
        if (Setup.deviceSettings().introLaunch) {
            startIntro()
        } else {
            startHome()
        }
    }

    private fun startIntro() {
        val intent = Intent(this@MainActivity, IntroActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }

    private fun startHome() {
        val intent = Intent(this@MainActivity, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        startActivity(intent)
    }
}