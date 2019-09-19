package com.lossydragon.arduinorgb.bluetooth

/*
 *  Based off of https://github.com/hmartiro/android-arduino-bluetooth
 */

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket

import java.io.InputStream
import java.io.OutputStream

import android.os.Handler
import android.os.Message
import android.util.Log

import java.nio.charset.StandardCharsets
import java.util.*

class BluetoothThread @SuppressLint("HandlerLeak")
constructor(address: String, private val readHandler: Handler) : Thread() {

    private val address: String = address.toUpperCase(Locale.getDefault())
    private lateinit var socket: BluetoothSocket

    private lateinit var outStream: OutputStream
    private lateinit var inStream: InputStream
    /**
     * Return the write handler for this connection. Messages received by this
     * handler will be written to the Bluetooth socket.
     */
    val writeHandler: Handler

    private var rxBuffer = ""

    private var state: Int = 0

    val isConnected: Boolean
        get() {
            Log.v(TAG, "isConnected() called - result:$state")
            return state == 1
        }

    init {

        writeHandler = object : Handler() {
            override fun handleMessage(message: Message) {
                write(message.obj as String)
            }
        }
    }

    /**
     * Connect to a remote Bluetooth socket, or throw an exception if it fails.
     */
    @Throws(Exception::class)
    private fun connect() {

        Log.i(TAG, "Attempting connection to $address...")

        // Get this device's Bluetooth adapter
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            throw Exception("Bluetooth adapter not found or not enabled!")
        }

        // Find the remote device
        val remoteDevice = adapter.getRemoteDevice(address)

        // Create a socket with the remote device using this protocol
        socket = remoteDevice.createRfcommSocketToServiceRecord(uuid)

        // Make sure Bluetooth adapter is not in discovery mode
        adapter.cancelDiscovery()

        // Connect to the socket
        socket.connect()

        // Get input and output streams from the socket
        outStream = socket.outputStream
        inStream = socket.inputStream

        Log.i(TAG, "Connected successfully to $address.")
    }

    /**
     * Disconnect the streams and socket.
     */
    private fun disconnect() {

        try {
            inStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            outStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Return data read from the socket, or a blank string.
     */
    private fun read(): String {

        var s = ""

        try {
            // Check if there are bytes available
            if (inStream.available() > 0) {

                // Read bytes into a buffer
                val inBuffer = ByteArray(1024)
                val bytesRead = inStream.read(inBuffer)

                // Convert read bytes into a string
                s = String(inBuffer, StandardCharsets.US_ASCII)
                s = s.substring(0, bytesRead)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Read failed!", e)
        }

        return s
    }

    /**
     * Write data to the socket.
     */
    private fun write(string: String) {
        var s = string

        try {
            // Add the delimiter
            s += DELIMITER

            // Convert to bytes and write
            outStream.write(s.toByteArray())
            Log.i(TAG, "[SENT] $s")

        } catch (e: Exception) {
            Log.e(TAG, "Write failed!", e)
        }

    }

    /**
     * Pass a message to the read handler.
     */
    private fun sendToReadHandler(s: String) {

        val msg = Message.obtain()
        msg.obj = s
        readHandler.sendMessage(msg)
        Log.i(TAG, "[RECV] $s")
    }

    /**
     * Send complete messages from the rx_buffer to the read handler.
     */
    private fun parseMessages() {

        val inx = rxBuffer.indexOf(DELIMITER)

        if (inx == -1)
            return

        val s = rxBuffer.substring(0, inx)

        rxBuffer = rxBuffer.substring(inx + 1)

        // Send to read handler
        sendToReadHandler(s)

        parseMessages()
    }

    /**
     * Entry point when thread.start() is called.
     */
    override fun run() {

        // Attempt to connect and exit the thread if it failed
        try {
            connect()
            sendToReadHandler("CONNECTED")
            state = 1
        } catch (e: Exception) {
            Log.e(TAG, "Failed to connect!", e)
            sendToReadHandler("CONNECTION FAILED")
            disconnect()
            state = 0
            return
        }

        // Loop continuously, reading data, until thread.interrupt() is called
        while (!this.isInterrupted) {

            // Make sure things haven't gone wrong
            if (!isConnected) {
                Log.e(TAG, "Lost bluetooth connection!")
                break
            }

            // Read data and add it to the buffer
            val s = read()
            if (s.isNotEmpty())
                rxBuffer += s

            // Look for complete messages
            parseMessages()
        }

        // If thread is interrupted, close connections
        disconnect()
        sendToReadHandler("DISCONNECTED")
        state = 0
    }

    companion object {
        private val TAG = BluetoothThread::class.java.simpleName
        private const val DELIMITER = '\n'
        private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}