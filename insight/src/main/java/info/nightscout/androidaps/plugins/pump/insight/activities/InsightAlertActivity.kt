package info.nightscout.androidaps.plugins.pump.insight.activities

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.core.content.ContextCompat
import javax.inject.Inject
import dagger.android.support.DaggerAppCompatActivity
import info.nightscout.androidaps.insight.databinding.ActivityInsightAlertBinding
import info.nightscout.androidaps.plugins.pump.insight.InsightAlertService
import info.nightscout.androidaps.plugins.pump.insight.descriptors.Alert
import info.nightscout.androidaps.plugins.pump.insight.descriptors.AlertStatus
import info.nightscout.androidaps.plugins.pump.insight.utils.AlertUtils
import info.nightscout.interfaces.utils.HtmlHelper.fromHtml

class InsightAlertActivity : DaggerAppCompatActivity() {

    @Inject lateinit var alertUtils: AlertUtils
    private var alertService: InsightAlertService? = null

    private lateinit var binding: ActivityInsightAlertBinding

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            alertService = (binder as InsightAlertService.LocalBinder).service
            alertService!!.alertLiveData.observe(this@InsightAlertActivity, { alert: Alert? -> if (alert == null) finish() else update(alert) })
        }

        override fun onServiceDisconnected(name: ComponentName) {
            alertService = null
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInsightAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bindService(Intent(this, InsightAlertService::class.java), serviceConnection, BIND_AUTO_CREATE)

        setFinishOnTouchOutside(false)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        super.onDestroy()
    }

    fun update(alert: Alert) {
        binding.mute.isEnabled = true
        binding.mute.visibility = if (alert.alertStatus === AlertStatus.SNOOZED) View.GONE else View.VISIBLE
        binding.confirm.isEnabled = true
        alert.alertCategory?.let {binding.icon.setImageDrawable(ContextCompat.getDrawable(this, alertUtils.getAlertIcon(it)))}
        alert.alertType?.let {binding.errorCode.text = alertUtils.getAlertCode(it)}
        alert.alertType?.let {binding.errorTitle.text = alertUtils.getAlertTitle(it)}
        val description = alertUtils.getAlertDescription(alert)
        if (description == null) binding.errorDescription.visibility = View.GONE else {
            binding.errorDescription.visibility = View.VISIBLE
            binding.errorDescription.text = fromHtml(description)
        }
    }

    fun muteClicked() {
        binding.mute.isEnabled = false
        alertService!!.mute()
    }

    fun confirmClicked() {
        binding.mute.isEnabled = false
        binding.confirm.isEnabled = false
        alertService!!.confirm()
    }
}