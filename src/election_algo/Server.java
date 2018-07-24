package election_algo;

import java.net.DatagramSocket;

public class Server {

	Sender sender;
	Receiver receiver;
	String name;
	int port1, port2;
	boolean isAlive;
	DatagramSocket ds = null;

	public Server(DatagramSocket ds, boolean isAlive, String name, int receivePort, int port1, int port2) {
		this.name = name;
		this.port1 = port1;
		this.port2 = port2;
		this.isAlive = isAlive;
		receiver = new Receiver(isAlive, name, receivePort);
		receiver.start();
		this.ds = ds;
	}

	public void send() {
		sender = new Sender(ds,name, port1, port2);
		sender.start();

	}
}
