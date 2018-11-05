package com.model.connection;

import java.net.DatagramPacket;

public interface IUDPListener {

    public void onInputDatagram(DatagramPacket packet, IUDPListener callback);
}
