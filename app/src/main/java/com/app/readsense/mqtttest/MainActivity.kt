package com.app.readsense.mqtttest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val URL = "tcp://iot.eclipse.org:1883"
        const val SUB_TOPIC = "Pub"
        const val PUB_TOPIC = "Sub"
    }

    private lateinit var mClient: MqttAndroidClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mClient = MqttAndroidClient(applicationContext, URL, "lens_mgL91SSHCLPJ4rOqAzD4ih25m89")
        mClient.setCallback(object : MqttCallbackExtended {
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(TAG, "messageArrived $topic : $message")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "connectionLost ${cause.toString()}")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d(TAG, "deliveryComplete $token")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                if (reconnect) subscribeToTopic()
            }

        })

        val mqttConnectOptions = MqttConnectOptions()
        mqttConnectOptions.isAutomaticReconnect = true
        mqttConnectOptions.isCleanSession = false

        try {
            mClient.connect(mqttConnectOptions, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {
                    val disconnectedBufferOptions = DisconnectedBufferOptions()
                    disconnectedBufferOptions.isBufferEnabled = true
                    disconnectedBufferOptions.bufferSize = 128
                    disconnectedBufferOptions.isPersistBuffer = false
                    disconnectedBufferOptions.isDeleteOldestMessages = false
                    mClient.setBufferOpts(disconnectedBufferOptions)
                    subscribeToTopic()
                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                    Log.e(TAG, "connect onFailure ${exception.message}")
                }
            })
        } catch (ex: MqttException) {
            Log.e(TAG, "")
        }

        pub.setOnClickListener({
            publishMessage()
        })


    }

    fun subscribeToTopic() {
        try {
            mClient.subscribe(SUB_TOPIC, 1, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken) {

                }

                override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                }
            })

            // THIS DOES NOT WORK!
            mClient.subscribe(SUB_TOPIC, 0, { topic, message ->
                // message Arrived!
                Log.d(TAG, "Message: " + topic + " : " + String(message.payload))
            })

        } catch (ex: MqttException) {
            Log.e(TAG, "${ex.message}")
        }

    }

    private fun publishMessage() {
        try {
            val message = MqttMessage()
            val publishMessage = "你好"
            message.payload = publishMessage.toByteArray()
            mClient.publish(PUB_TOPIC, message)
            if (!mClient.isConnected) {
                Log.d(TAG, "${mClient.bufferedMessageCount} messages in buffer.")
            }
        } catch (e: MqttException) {
            Log.e(TAG, "${e.message}")
        }

    }
}
