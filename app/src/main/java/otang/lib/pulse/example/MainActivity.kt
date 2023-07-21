@file:Suppress("DEPRECATION")

package otang.lib.pulse.example

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.Player
import com.google.android.material.color.DynamicColors
import com.google.android.material.internal.EdgeToEdgeUtils
import com.google.android.material.slider.Slider
import otang.lib.pulse.example.databinding.ActivityMainBinding
import otang.pulse.lib.util.PulseConfig
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), Player.Listener {
    private var binding: ActivityMainBinding? = null
    private var player: AudioPlayer? = null
    private var config: PulseConfig? = null
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EdgeToEdgeUtils.applyEdgeToEdge(window, true)
        DynamicColors.applyToActivityIfAvailable(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        config = binding!!.vv.pulsePref
        setContentView(binding!!.root)
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO)) {
            EasyPermissions.requestPermissions(
                this,
                "Rationale",
                10,
                Manifest.permission.RECORD_AUDIO
            )
        }
        // Player
        player = AudioPlayer(this)
        player!!.setSource("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
        player!!.setListener(this)
        setupPreference()
        binding!!.bPlay.setOnClickListener { _: View? ->
            if (player!!.isPlaying) {
                player!!.pause()
            } else {
                player!!.play()
            }
        }
    }

    private fun setupPreference() {
        binding!!.msCenter.isChecked = config!!.isCenterMirror
        binding!!.msCenter.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            config!!.isCenterMirror = b
        }
        binding!!.msEnabled.isChecked = config!!.isPulseEnabled
        binding!!.msEnabled.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            config!!.isPulseEnabled = b
        }
        binding!!.msLeft.isChecked = config!!.isLeftInLandscape
        binding!!.msLeft.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            config!!.isLeftInLandscape = b
        }
        binding!!.msRounded.isChecked = config!!.isSolidRounded
        binding!!.msRounded.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            config!!.isSolidRounded = b
        }
        binding!!.msSmooth.isChecked = config!!.isSmoothEnabled
        binding!!.msSmooth.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            config!!.isSmoothEnabled = b
        }
        binding!!.msVertical.isChecked = config!!.isVerticalMirror
        binding!!.msVertical.setOnCheckedChangeListener { _: CompoundButton?, b: Boolean ->
            config!!.isVerticalMirror = b
        }
        binding!!.sColor.value = config!!.pulseColor.toFloat()
        binding!!.sColor.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.pulseColor = i.toInt()
        })
        binding!!.sCount.value = config!!.solidLineCount.toFloat()
        binding!!.sCount.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.solidLineCount = i.toInt()
        })
        binding!!.sDim.value = config!!.fadingDim.toFloat()
        binding!!.sDim.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.fadingDim = i.toInt()
        })
        binding!!.sDiv.value = config!!.fadingDiv.toFloat()
        binding!!.sDiv.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.fadingDiv = i.toInt()
        })
        binding!!.sEmpty.value = config!!.emptySize.toFloat()
        binding!!.sEmpty.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.emptySize = i.toInt()
        })
        binding!!.sFudge.value = config!!.solidFudge.toFloat()
        binding!!.sFudge.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.fadingFudge = i.toInt()
            config!!.solidFudge = i.toInt()
        })
        binding!!.sGravity.value = config!!.gravity.toFloat()
        binding!!.sGravity.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.gravity = i.toInt()
        })
        binding!!.sLava.value = config!!.lavaSpeed.toFloat()
        binding!!.sLava.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.lavaSpeed = i.toInt()
        })
        binding!!.sOvacity.value = config!!.solidOvacity.toFloat()
        binding!!.sOvacity.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.solidOvacity = i.toInt()
        })
        binding!!.sRender.value = config!!.renderStyle.toFloat()
        binding!!.sRender.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.renderStyle = i.toInt()
        })
        binding!!.sFill.value = config!!.fillSize.toFloat()
        binding!!.sFill.addOnChangeListener(Slider.OnChangeListener { _: Slider?, i: Float, _: Boolean ->
            config!!.fillSize = i.toInt()
        })
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        player?.let { binding!!.vv.setAudioSessionId(it.audioSessionId) }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}