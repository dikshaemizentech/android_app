package com.vendtech.app.base

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

/**
 */

open class BaseActivity : AppCompatActivity() {


    fun launchActivity(calledActivity: Class<*>) {
        val myIntent = Intent(this, calledActivity)
        this.startActivity(myIntent)
    }
    internal fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
