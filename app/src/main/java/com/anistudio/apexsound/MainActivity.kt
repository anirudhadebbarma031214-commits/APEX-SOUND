package com.anistudio.apexsound

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import android.net.Uri

class MainActivity : Activity() {
    private val bg = Color.rgb(8, 11, 20)
    private val panel = Color.rgb(18, 24, 39)
    private val cyan = Color.rgb(51, 220, 255)
    private val purple = Color.rgb(168, 85, 247)
    private var player: MediaPlayer? = null
    private var eq: Equalizer? = null
    private var bass: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var chosenUri: Uri? = null
    private lateinit var status: TextView
    private lateinit var playButton: Button
    private val bandSliders = mutableListOf<SeekBar>()
    private var enabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 22, 22, 28)
            setBackgroundColor(bg)
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val brand = TextView(this).apply {
            text = "APEX  SOUND"; textSize = 28f; setTextColor(cyan)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        }
        content.addView(brand)
        content.addView(TextView(this).apply {
            text = "AUDIO ENGINE  •  ANDROID"; textSize = 11f; setTextColor(Color.LTGRAY)
            setPadding(0, 2, 0, 20)
        })
        status = TextView(this).apply {
            text = "Choose a song to activate the audio engine"
            textSize = 13f; setTextColor(Color.WHITE); setPadding(14, 16, 14, 16)
            setBackgroundColor(panel)
        }
        content.addView(status, matchWrap())
        val choose = button("＋  CHOOSE AUDIO FILE", cyan)
        choose.setOnClickListener {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE); type = "audio/*"
            }
            startActivityForResult(i, 100)
        }
        content.addView(choose, matchWrap())
        playButton = button("▶  PLAY / PAUSE", purple)
        playButton.setOnClickListener { togglePlayback() }
        content.addView(playButton, matchWrap())

        section(content, "SOUND PRESETS")
        val presets = listOf("BALANCED", "PHONK", "BASS", "GAMING", "VOCAL", "CINEMA")
        val presetRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        presets.forEach { name ->
            val b = Button(this).apply {
                text = name; textSize = 9f; setTextColor(cyan)
                setBackgroundColor(panel); isAllCaps = false
                setOnClickListener { applyPreset(name) }
            }
            presetRow.addView(b, LinearLayout.LayoutParams(0, 46, 1f).apply { setMargins(2, 3, 2, 3) })
        }
        content.addView(presetRow, matchWrap())

        section(content, "10-BAND EQUALIZER")
        content.addView(TextView(this).apply {
            text = "Adjust the sound while your selected track plays."
            setTextColor(Color.LTGRAY); textSize = 12f
        })
        val labels = listOf("31 Hz","62 Hz","125 Hz","250 Hz","500 Hz","1 kHz","2 kHz","4 kHz","8 kHz","16 kHz")
        labels.forEachIndexed { index, label ->
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            row.addView(TextView(this).apply {
                text = label; setTextColor(Color.WHITE); textSize = 12f
            }, LinearLayout.LayoutParams(58, 42))
            val seek = SeekBar(this).apply {
                max = 240; progress = 120
                progressTintList = android.content.res.ColorStateList.valueOf(cyan)
                thumbTintList = android.content.res.ColorStateList.valueOf(purple)
                setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                        if (fromUser) setBand(index, p - 120)
                    }
                    override fun onStartTrackingTouch(s: SeekBar?) {}
                    override fun onStopTrackingTouch(s: SeekBar?) {}
                })
            }
            bandSliders.add(seek)
            row.addView(seek, LinearLayout.LayoutParams(0, 42, 1f))
            content.addView(row, matchWrap())
        }
        section(content, "ENHANCEMENTS")
        content.addView(effectToggle("Bass Boost", "Low-frequency enhancement") { strength ->
            try { bass?.setStrength(strength.toShort()) } catch (_: Exception) {}
        }, matchWrap())
        content.addView(effectToggle("Virtual Surround", "Device-supported virtualizer") { strength ->
            try { virtualizer?.setStrength(strength.toShort()) } catch (_: Exception) {}
        }, matchWrap())
        content.addView(TextView(this).apply {
            text = "APEX SOUND processes audio played inside this app. Android does not allow ordinary apps to universally enhance sound from every other app. Effect availability depends on your device."
            setTextColor(Color.LTGRAY); textSize = 11f; setPadding(0, 22, 0, 12)
        })
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun section(parent: LinearLayout, title: String) {
        parent.addView(TextView(this).apply {
            text = title; textSize = 14f; typeface = Typeface.DEFAULT_BOLD
            setTextColor(purple); setPadding(0, 22, 0, 8)
        })
    }
    private fun button(textValue: String, color: Int) = Button(this).apply {
        text = textValue; textSize = 12f; setTextColor(Color.WHITE)
        setBackgroundColor(Color.rgb(27, 36, 55)); isAllCaps = false
        setPadding(8, 5, 8, 5)
    }
    private fun matchWrap() = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
        setMargins(0, 4, 0, 4)
    }
    private fun effectToggle(title: String, subtitle: String, apply: (Int) -> Unit): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(12, 8, 8, 8); setBackgroundColor(panel)
        }
        val labels = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        labels.addView(TextView(this).apply { text = title; setTextColor(Color.WHITE); textSize = 14f })
        labels.addView(TextView(this).apply { text = subtitle; setTextColor(Color.LTGRAY); textSize = 10f })
        row.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
        val sw = Switch(this).apply {
            isChecked = true
            setOnCheckedChangeListener { _, checked -> apply(if (checked) 1000 else 0) }
        }
        row.addView(sw)
        return row
    }
    private fun togglePlayback() {
        val p = player
        if (p == null) {
            status.text = "Choose an audio file first"
            return
        }
        try {
            if (p.isPlaying) { p.pause(); playButton.text = "▶  RESUME" }
            else { p.start(); playButton.text = "Ⅱ  PAUSE" }
        } catch (e: Exception) { status.text = "Playback error: ${e.localizedMessage}" }
    }
    private fun setupEffects(sessionId: Int) {
        try {
            eq?.release()
            bass?.release()
            virtualizer?.release()
            eq = Equalizer(0, sessionId).apply { enabled = true }
            bass = BassBoost(0, sessionId).apply { enabled = true; setStrength(500) }
            virtualizer = Virtualizer(0, sessionId).apply { enabled = true; setStrength(500) }
            val min = eq?.bandLevelRange?.get(0)?.toInt() ?: -1500
            val max = eq?.bandLevelRange?.get(1)?.toInt() ?: 1500
            bandSliders.forEachIndexed { i, seek ->
                seek.max = max - min
                seek.progress = (-min).coerceIn(0, seek.max)
                if (i < (eq?.numberOfBands ?: 0)) {
                    val band = i.toShort()
                    seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                            if (fromUser) try { eq?.setBandLevel(band, (p + min).toShort()) } catch (_: Exception) {}
                        }
                        override fun onStartTrackingTouch(s: SeekBar?) {}
                        override fun onStopTrackingTouch(s: SeekBar?) {}
                    })
                }
            }
            status.text = "Audio engine active • effects depend on device support"
        } catch (e: Exception) {
            status.text = "Some audio effects are unavailable on this device"
        }
    }
    private fun setBand(index: Int, gain: Int) {
        try {
            val effect = eq ?: return
            if (index < effect.numberOfBands) {
                val range = effect.bandLevelRange
                effect.setBandLevel(index.toShort(), gain.coerceIn(range[0].toInt(), range[1].toInt()).toShort())
            }
        } catch (_: Exception) {}
    }
    private fun applyPreset(name: String) {
        val curve = when (name) {
            "PHONK" -> listOf(7, 6, 4, 1, -1, -2, 2, 4, 5, 3)
            "BASS" -> listOf(9, 8, 6, 4, 1, 0, -1, -1, 0, 0)
            "GAMING" -> listOf(3, 2, 0, -2, -1, 2, 4, 5, 3, 2)
            "VOCAL" -> listOf(-3, -2, -1, 1, 4, 5, 4, 2, 0, -1)
            "CINEMA" -> listOf(4, 3, 1, 0, -1, 1, 3, 3, 2, 1)
            else -> List(10) { 0 }
        }
        curve.forEachIndexed { i, gain ->
            val seek = bandSliders.getOrNull(i) ?: return@forEachIndexed
            val effect = eq
            val range = effect?.bandLevelRange
            if (range != null && i < effect.numberOfBands) {
                val target = (gain * 100).coerceIn(range[0].toInt(), range[1].toInt())
                try { effect.setBandLevel(i.toShort(), target.toShort()) } catch (_: Exception) {}
                seek.progress = (target - range[0].toInt()).coerceIn(0, seek.max)
            }
        }
        status.text = "$name preset selected"
    }
    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 100 || resultCode != RESULT_OK) return
        val uri = data?.data ?: return
        chosenUri = uri
        try {
            player?.release()
            player = MediaPlayer().apply {
                setDataSource(this@MainActivity, uri)
                setOnPreparedListener { mp ->
                    setupEffects(mp.audioSessionId)
                    mp.start()
                    playButton.text = "Ⅱ  PAUSE"
                    status.text = "Now playing selected audio"
                }
                setOnCompletionListener { playButton.text = "▶  PLAY AGAIN" }
                prepareAsync()
            }
            status.text = "Loading audio…"
        } catch (e: Exception) {
            status.text = "Couldn't open this file: ${e.localizedMessage}"
        }
    }
    override fun onDestroy() {
        player?.release(); eq?.release(); bass?.release(); virtualizer?.release()
        super.onDestroy()
    }
}
