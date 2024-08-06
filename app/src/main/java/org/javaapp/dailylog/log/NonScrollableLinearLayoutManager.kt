package org.javaapp.dailylog.log
import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class NonScrollableLinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    override fun canScrollVertically(): Boolean {
        return false
    }
}