package site.ridzhan.glassdroid

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var root: FrameLayout
    private fun dp(x: Int) = (x * resources.displayMetrics.density).toInt()
    private fun glass(dark: Boolean = false): GradientDrawable =
        GradientDrawable().apply {
            setColor(if (dark) Color.argb(130, 8, 22, 45) else Color.argb(95, 225, 245, 255))
            cornerRadius = dp(22).toFloat()
            setStroke(dp(1), Color.argb(130, 255, 255, 255))
        }
    private fun label(s: String, size: Float = 16f, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = s
            textSize = size
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            if (bold) setTypeface(null, Typeface.BOLD)
        }
    private fun column(): LinearLayout = LinearLayout(this).apply { orientation = 1 }
    private fun textLine(parent: LinearLayout, s: String, size: Float = 16f, bold: Boolean = false) {
        parent.addView(label(s, size, bold), LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(15) })
    }
    private fun tile(parent: LinearLayout, s: String, action: () -> Unit) {
        val v = label(s, 16f, true).apply {
            background = glass()
            setPadding(dp(18), 0, dp(12), 0)
            setOnClickListener { action() }
        }
        parent.addView(v, LinearLayout.LayoutParams(-1, dp(56)).apply { bottomMargin = dp(10) })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        root = FrameLayout(this)
        setContentView(root)
        window.statusBarColor = Color.rgb(13, 47, 94)
        window.navigationBarColor = Color.rgb(6, 29, 63)
        showHome()
    }
    private fun background() {
        root.removeAllViews()
        root.background = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(Color.rgb(5, 28, 77), Color.rgb(27, 101, 158),
                Color.rgb(12, 173, 169), Color.rgb(7, 40, 105))
        )
    }
    private fun page(title: String, subtitle: String): LinearLayout {
        background()
        val scroll = ScrollView(this)
        val body = column().apply { setPadding(dp(20), dp(22), dp(20), dp(30)) }
        scroll.addView(body)
        root.addView(scroll)
        tile(body, "‹  Home") { showHome() }
        textLine(body, title, 30f, true)
        textLine(body, subtitle, 13f)
        return body
    }
    private fun showHome() {
        background()
        val body = column().apply { setPadding(dp(16), dp(24), dp(16), dp(15)) }
        root.addView(body, FrameLayout.LayoutParams(-1, -1))
        val widgets = LinearLayout(this)
        val weather = label("Weather\n50°\nPartly Cloudy", 20f, true).apply {
            background = glass()
            setPadding(dp(14), dp(12), dp(8), dp(12))
        }
        val calendar = label("NOVEMBER\n21\nSaturday", 19f, true).apply {
            background = glass(true)
            setPadding(dp(14), dp(12), dp(8), dp(12))
        }
        widgets.addView(weather, LinearLayout.LayoutParams(0, dp(140), 1f).apply { rightMargin = dp(8) })
        widgets.addView(calendar, LinearLayout.LayoutParams(0, dp(140), 1f))
        body.addView(widgets)
        textLine(body, "GlassDroid · Android launcher alpha", 13f)
        val apps = packageManager.queryIntentActivities(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0
        ).filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
        val grid = GridLayout(this).apply { columnCount = 4 }
        val cellWidth = (resources.displayMetrics.widthPixels - dp(32)) / 4
        for (item in apps) {
            val cell = column().apply { gravity = Gravity.CENTER }
            cell.addView(ImageView(this).apply {
                setImageDrawable(item.loadIcon(packageManager))
                setPadding(dp(5), dp(5), dp(5), dp(5))
                background = glass()
            }, LinearLayout.LayoutParams(dp(52), dp(52)))
            cell.addView(label(item.loadLabel(packageManager).toString().take(12), 10f).apply {
                gravity = Gravity.CENTER
                maxLines = 1
            }, LinearLayout.LayoutParams(-1, dp(28)))
            cell.setOnClickListener {
                try {
                    val intent = packageManager.getLaunchIntentForPackage(item.activityInfo.packageName)
                    if (intent != null) startActivity(intent)
                } catch (_: Exception) {
                    Toast.makeText(this, "Cannot open app", Toast.LENGTH_SHORT).show()
                }
            }
            grid.addView(cell, GridLayout.LayoutParams().apply {
                width = cellWidth
                height = dp(89)
            })
        }
        body.addView(ScrollView(this).apply { addView(grid) }, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { background = glass() }
        listOf(
            "Control" to { showControl() },
            "Settings" to { showSettings() },
            "Lock" to { showLockPreview() }
        ).forEach { (title, action) ->
            nav.addView(label(title, 14f, true).apply {
                gravity = Gravity.CENTER
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(0, dp(60), 1f))
        }
        body.addView(nav)
    }
    private fun showControl() {
        val body = page("Control Center", "Visual panel with safe Android system controls")
        tile(body, "Wi-Fi and internet") { open(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)) }
        tile(body, "Bluetooth") { open(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }
        tile(body, "Brightness") { open(Intent(Settings.ACTION_DISPLAY_SETTINGS)) }
        tile(body, "Sound") { open(Intent(Settings.ACTION_SOUND_SETTINGS)) }
        tile(body, "Airplane mode") { open(Intent(Settings.ACTION_WIRELESS_SETTINGS)) }
        textLine(body, "Samsung's original Quick Settings remains available by swiping down from the top.", 14f)
    }
    private fun showLockPreview() {
        val body = page("Lock Screen Preview", "This is not the secure Android lock screen.")
        textLine(body, SimpleDateFormat("EEEE, MMMM d", Locale.ENGLISH).format(Date()), 22f)
        textLine(body, SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), 70f, true)
    }
    private fun showSettings() {
        val body = page("GlassDroid Settings", "Android permissions are optional and granted by you.")
        tile(body, "Default Home app") { requestHome() }
        tile(body, "Display over other apps: " + if (Settings.canDrawOverlays(this)) "Granted" else "Not granted") {
            open(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
        }
        tile(body, "Notification access") { open(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
        if (Build.VERSION.SDK_INT >= 33) {
            tile(body, "App notification permission") {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }
        tile(body, "Battery optimization") { open(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)) }
        tile(body, "Restore Samsung Home app") { open(Intent(Settings.ACTION_HOME_SETTINGS)) }
        textLine(body, "This version does not store notification contents or transmit data. Permissions do not grant access to replace Android's security screen.", 13f)
    }
    private fun requestHome() {
        if (Build.VERSION.SDK_INT >= 29) {
            val roles = getSystemService(RoleManager::class.java)
            if (roles.isRoleAvailable(RoleManager.ROLE_HOME) && !roles.isRoleHeld(RoleManager.ROLE_HOME)) {
                open(roles.createRequestRoleIntent(RoleManager.ROLE_HOME))
                return
            }
        }
        open(Intent(Settings.ACTION_HOME_SETTINGS))
    }
    private fun open(intent: Intent) {
        try { startActivity(intent) }
        catch (_: Exception) { Toast.makeText(this, "Setting unavailable", Toast.LENGTH_SHORT).show() }
    }
    @Deprecated("Legacy device compatibility")
    override fun onBackPressed() { showHome() }
}
