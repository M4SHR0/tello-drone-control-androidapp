package com.example.m4shr0.dronecontroller

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.m4shr0.dronecontroller.databinding.ActivityMainBinding
import com.example.m4shr0.dronecontroller.databinding.ActivityMainBinding.inflate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.floor

class MainActivity : AppCompatActivity(), SensorEventListener {
    private val matrixSize = 16
    private var mgValues = FloatArray(3)
    private var acValues = FloatArray(3)

    private val sendScope = CoroutineScope(Dispatchers.Default)
    private val udp: UDP = UDP
    private var command: String = ""

    private var tookOff: Boolean = false

    private val takeOffAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.takeoff_animation
        )
    }
    private val landingAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.landing_animation
        )
    }
    private val visDownFABAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.vis_down_fab_animation
        )
    }
    private val visUpFABAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.vis_up_fab_animation
        )
    }
    private val invisDownFABAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.invis_down_fab_animation
        )
    }
    private val invisUpFABAnim: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.invis_up_fab_animation
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // no function
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val inR = FloatArray(matrixSize)
        val outR = FloatArray(matrixSize)
        val i = FloatArray(matrixSize)
        val orValues = FloatArray(3)

        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> acValues = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> mgValues = event.values.clone()
        }
        SensorManager.getRotationMatrix(inR, i, acValues, mgValues)
        SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR)
        SensorManager.getOrientation(outR, orValues)

        val pitch = rad2Deg(orValues[1])
        val roll = rad2Deg(orValues[2])
        val border = 70

        when {
            pitch < border && roll < -160 || roll > 160 -> {
                forward()
            }
            pitch < border && (roll in -10..10) -> {
                back()
            }
            (roll in 85..95) -> {
                right()
            }
            (roll in -95..-85) -> {
                left()
            }
            else -> {
                standBy()
            }
        }
    }

    private fun left() { // TODO UDP
        binding.arrowLeft1.visibility = View.VISIBLE
        binding.arrowUp1.visibility = View.INVISIBLE
        binding.arrowDown1.visibility = View.INVISIBLE
        binding.arrowRight1.visibility = View.INVISIBLE
        command = "ccw 30"
        sendScope.launch {
            udp.send(command)
        }
        binding.sendView.text = command

    }

    private fun right() { // TODO UDP
        binding.arrowRight1.visibility = View.VISIBLE
        binding.arrowUp1.visibility = View.INVISIBLE
        binding.arrowDown1.visibility = View.INVISIBLE
        binding.arrowLeft1.visibility = View.INVISIBLE
        command = "cw 30"
        sendScope.launch {
            udp.send(command)
        }
        binding.sendView.text = command
    }

    private fun back() { // TODO UDP
        binding.arrowDown1.visibility = View.VISIBLE
        binding.arrowUp1.visibility = View.INVISIBLE
        binding.arrowRight1.visibility = View.INVISIBLE
        binding.arrowLeft1.visibility = View.INVISIBLE
        command = "back 30"
        sendScope.launch {
            udp.send(command)
        }
        binding.sendView.text = command
    }

    private fun forward() { // TODO UDP
        binding.arrowUp1.visibility = View.VISIBLE
        binding.arrowDown1.visibility = View.INVISIBLE
        binding.arrowRight1.visibility = View.INVISIBLE
        binding.arrowLeft1.visibility = View.INVISIBLE
        command = "forward 30"
        sendScope.launch {
            udp.send(command)
        }
        binding.sendView.text = command
    }

    private fun up(){
        command = "up 30"
        sendScope.launch{
            udp.send(command)
        }
        binding.sendView.text = command
    }

    private fun down(){
        command = "down 30"
        sendScope.launch {
            udp.send(command)
        }
        binding.sendView.text = command
    }

    private fun standBy() {
        binding.arrowUp1.visibility = View.INVISIBLE
        binding.arrowDown1.visibility = View.INVISIBLE
        binding.arrowRight1.visibility = View.INVISIBLE
        binding.arrowLeft1.visibility = View.INVISIBLE
    }

    private fun rad2Deg(radValue: Float): Int {
        return floor(Math.toDegrees(radValue.toDouble())).toInt()
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        sendScope.launch{
            startUp()
        }
        binding.sendView.text = "command"

        binding.flightFAB.setOnClickListener{
            sendScope.launch{
                flightCommand(tookOff)
            }
            if (!tookOff){
                Toast.makeText(this,"Sent TakeOff Command!",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"Sent Landing Command!",Toast.LENGTH_SHORT).show()
            }
            setAnimation(tookOff)
            tookOff = !tookOff
        }

        binding.flightFAB.setOnLongClickListener {
            if (tookOff) {
                sendScope.launch {
                    udp.send("emergency")
                }

                Toast.makeText(this, "Sent Emergency Command!!", Toast.LENGTH_SHORT).show()
                setAnimation(tookOff)
                tookOff = !tookOff
            }
            true
        }

        binding.upFAB.setOnClickListener{
            up()
        }

        binding.downFAB.setOnClickListener{
            down()
        }
    }

    private fun flightCommand(tookOff: Boolean) {
        if (!tookOff){ // takeoff
            udp.send("takeoff")
        }else{ // landing
            udp.send("land")
        }
    }

    private fun startUp(){
        udp.send("command")
    }

    private fun setAnimation(tookOff: Boolean) {
        if (!tookOff){ // takeoff
            binding.flightFAB.startAnimation(takeOffAnim)
            binding.downFAB.visibility = View.VISIBLE
            binding.upFAB.visibility = View.VISIBLE
            binding.downFAB.startAnimation(visDownFABAnim)
            binding.upFAB.startAnimation(visUpFABAnim)
        }else{ // landing
            binding.flightFAB.startAnimation(landingAnim)
            binding.downFAB.startAnimation(invisDownFABAnim)
            binding.upFAB.startAnimation(invisUpFABAnim)
            binding.downFAB.visibility = View.INVISIBLE
            binding.upFAB.visibility = View.INVISIBLE
        }
    }

    public override fun onResume() {
        super.onResume()
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this,magField,SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.unregisterListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        sendScope.cancel()
        udp.close()
    }
}
