import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.quartic.app.sensors.SensorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        SensorService.startService(context)
    }
}
