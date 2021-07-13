package com.example.m4shr0.dronecontroller

import java.net.DatagramSocket
import java.net.DatagramPacket
import java.net.InetAddress

object UDP {
    private val ipAddrTello = InetAddress.getByName("192.168.10.1")
    private const val portnum = 8889

    private val socket = DatagramSocket(portnum)

    fun send(text : String){
        val byteArray = text.toByteArray()
        val packet = DatagramPacket(byteArray,byteArray.size,ipAddrTello,portnum)

        socket.send(packet)
    }
    /**
    fun receive():String{
        val buffer = ByteArray(8192)
        val packet = DatagramPacket(buffer,buffer.size)

        socket.receive(packet)
        socket.close()

        return buffer.toString()
    }
    **/
    fun close(){
        socket.close()
    }
}