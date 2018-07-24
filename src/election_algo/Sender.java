package election_algo;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class Sender extends Thread {
	int port1,port2;
	String name;
	DatagramSocket ds;
	public Sender(DatagramSocket ds, String name, int port1, int port2) {
		this.port1 = port1;
		this.port2 = port2;
		this.name = name;
		this.ds = ds;
	}
	public void run() {
		byte[] dataBytes = name.getBytes();
		DatagramPacket dp;
		try {
			dp = new DatagramPacket(dataBytes, dataBytes.length,
					InetAddress.getByName("localhost"),port1);
			ds.send(dp);
			dp = new DatagramPacket(dataBytes, dataBytes.length,
					InetAddress.getByName("localhost"),port2);
			ds.send(dp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
