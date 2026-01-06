package com.simats.wellnesshome.utils

import android.content.Context
import android.media.RingtoneManager
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.simats.wellnesshome.R

object FeedbackUtils {

    fun playSuccessSound(context: Context) {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playErrorSound(context: Context) {
        try {
            // Using notification sound for error as well for now, or could use ToneGenerator
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showSuccessVisual(context: Context, message: String) {
        val toaster = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        toaster.setGravity(Gravity.CENTER, 0, 0)
        // We could create a custom view here for the Tick, but standard Toast is simplest first.
        // User requested Tick Symbol specifically. Let's try custom layout.
        
        // Since we don't have a layout file for toast yet, let's just use Text with Emoji ✅ for now strictly?
        // Or better: Create a custom layout programmatically or inflate one.
        // Let's create `toast_success.xml` next step and inflate it here.
        // For now, I will use Emoji in Toast to ensure immediate feedback without crashing on missing layout.
        
        // Actually, let's create layout dynamically to avoid file dependency cycle issues in this tool call.
        val layout = android.widget.LinearLayout(context)
        layout.background = context.getDrawable(R.drawable.rounded_card)
        layout.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.WHITE)
        layout.orientation = android.widget.LinearLayout.HORIZONTAL
        layout.setPadding(32, 16, 32, 16)
        layout.gravity = Gravity.CENTER_VERTICAL

        val img = ImageView(context)
        img.setImageResource(R.drawable.ic_check_circle)
        img.imageTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50"))
        val params = android.widget.LinearLayout.LayoutParams(64, 64)
        params.marginEnd = 16
        img.layoutParams = params
        layout.addView(img)

        val text = TextView(context)
        text.text = message
        text.setTextColor(android.graphics.Color.BLACK)
        text.textSize = 16f
        text.typeface = android.graphics.Typeface.DEFAULT_BOLD
        layout.addView(text)

        toaster.view = layout
        toaster.show()
    }
}
