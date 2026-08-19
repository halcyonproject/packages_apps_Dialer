package com.android.dialer.main.impl.bottomnav

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import com.android.dialer.common.Assert
import com.android.dialer.common.LogUtil
import org.hlcyn.ui.components.HalcyonFloatingBottomBar
import org.hlcyn.ui.components.HalcyonFloatingBottomBarItem
import org.hlcyn.ui.theme.HalcyonTheme

class BottomNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(TabIndex.NONE, TabIndex.SPEED_DIAL, TabIndex.CALL_LOG, TabIndex.CONTACTS, TabIndex.VOICEMAIL)
    annotation class TabIndex {
        companion object {
            const val NONE = -1
            const val SPEED_DIAL = 0
            const val CALL_LOG = 1
            const val CONTACTS = 2
            const val VOICEMAIL = 3
        }
    }

    private val listeners = mutableListOf<OnBottomNavTabSelectedListener>()
    private var onDialpadClickListener: (() -> Unit)? = null
    private val selectedTabState = mutableStateOf(TabIndex.SPEED_DIAL)
    private val showVoicemailState = mutableStateOf(false)

    init {
        val composeView = ComposeView(context)
        addView(composeView)
        composeView.setContent {
            HalcyonTheme {
                HalcyonFloatingBottomBar {
                    HalcyonFloatingBottomBarItem(
                        selected = selectedTabState.value == TabIndex.SPEED_DIAL,
                        onClick = { selectTab(TabIndex.SPEED_DIAL) },
                        icon = Icons.Default.Star
                    )
                    HalcyonFloatingBottomBarItem(
                        selected = selectedTabState.value == TabIndex.CALL_LOG,
                        onClick = { selectTab(TabIndex.CALL_LOG) },
                        icon = Icons.Default.History
                    )
                    HalcyonFloatingBottomBarItem(
                        selected = selectedTabState.value == TabIndex.CONTACTS,
                        onClick = { selectTab(TabIndex.CONTACTS) },
                        icon = Icons.Default.People
                    )
                    if (showVoicemailState.value) {
                        HalcyonFloatingBottomBarItem(
                            selected = selectedTabState.value == TabIndex.VOICEMAIL,
                            onClick = { selectTab(TabIndex.VOICEMAIL) },
                            icon = Icons.Default.Voicemail
                        )
                    }
                    HalcyonFloatingBottomBarItem(
                        selected = false,
                        onClick = {
                            onDialpadClickListener?.invoke()
                        },
                        icon = Icons.Default.Dialpad
                    )
                }
            }
        }
    }

    fun setOnDialpadClickListener(listener: () -> Unit) {
        this.onDialpadClickListener = listener
    }

    fun setOnDialpadClickListener(listener: Runnable) {
        this.onDialpadClickListener = { listener.run() }
    }

    fun selectTab(@TabIndex tab: Int) {
        selectedTabState.value = tab
        updateListeners(tab)
    }

    fun showVoicemail(showTab: Boolean) {
        LogUtil.i("BottomNavBar.showVoicemail", "showing Tab:%b", showTab)
        val previous = showVoicemailState.value
        showVoicemailState.value = showTab

        if (previous != showTab && previous && getSelectedTab() == TabIndex.VOICEMAIL) {
            LogUtil.i("BottomNavBar.showVoicemail", "hid VM tab and moved to speed dial tab")
            selectTab(TabIndex.SPEED_DIAL)
        }
    }

    fun setNotificationCount(@TabIndex tab: Int, count: Int) {
        // TODO: Implement badges if needed
    }

    fun addOnTabSelectedListener(listener: OnBottomNavTabSelectedListener) {
        listeners.add(listener)
    }

    private fun updateListeners(@TabIndex tabIndex: Int) {
        for (listener in listeners) {
            when (tabIndex) {
                TabIndex.SPEED_DIAL -> listener.onSpeedDialSelected()
                TabIndex.CALL_LOG -> listener.onCallLogSelected()
                TabIndex.CONTACTS -> listener.onContactsSelected()
                TabIndex.VOICEMAIL -> listener.onVoicemailSelected()
                else -> throw Assert.createIllegalStateFailException("Invalid tab: $tabIndex")
            }
        }
    }

    @TabIndex
    fun getSelectedTab(): Int {
        return selectedTabState.value
    }

    interface OnBottomNavTabSelectedListener {
        fun onSpeedDialSelected()
        fun onCallLogSelected()
        fun onContactsSelected()
        fun onVoicemailSelected()
    }
}
