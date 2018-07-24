package election_algo;

import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;

public class Test {

	static boolean s1_sender_isAlive = true;
	static boolean s2_sender_isAlive = true;
	static boolean s3_sender_isAlive = true;

	static int TIME_OUT = 6000;
	static int LEADER_ID = 100;
	static int S1_ID = 1;
	static int S2_ID = 2;
	static int S3_ID = 3 ;
	static Object mapAccessor = new Object();
	static HashMap<String, Integer> currentIds = new HashMap<>();
	public static HashMap<String, Boolean> server_leader_status = new HashMap<>();
	public static HashMap<String, Long> server_last_updated_time = new HashMap<>();

	public static void main(String[] args) {
		int s1_receive_port = 5433;
		int s2_receive_port = 6543;
		int s3_receive_port = 3332;
		String serverName1 = "s1";
		String serverName2 = "s2";
		String serverName3 = "s3";

		server_leader_status.put(serverName1, true);

		currentIds.put(serverName1, LEADER_ID);
		currentIds.put(serverName2, S2_ID);
		currentIds.put(serverName3, S3_ID);

		server_last_updated_time.put(serverName1, System.nanoTime() / 1000000);
		server_last_updated_time.put(serverName2, System.nanoTime() / 1000000);
		server_last_updated_time.put(serverName3, System.nanoTime() / 1000000);
		Server s1,s2,s3;
		try {
			DatagramSocket socket1 = new DatagramSocket();
			s1 = new Server(socket1, s1_sender_isAlive, serverName1, s1_receive_port, s2_receive_port,
					s3_receive_port);
			DatagramSocket socket2 = new DatagramSocket();
			s2 = new Server(socket2, s2_sender_isAlive, serverName2, s2_receive_port, s1_receive_port,
					s3_receive_port);
			DatagramSocket socket3 = new DatagramSocket();
			s3 = new Server(socket3, s3_sender_isAlive, serverName3, s3_receive_port, s1_receive_port,
					s2_receive_port);
			
			Thread thread1 = new Thread() {
				public void run() {
					while (getStatus(serverName1)) {
						s1.send();
					}
				}
			};
			Thread thread2 = new Thread() {
				public void run() {
					while (getStatus(serverName2)) {
						s2.send();
					}
				}
			};
			Thread thread3 = new Thread() {
				public void run() {
					while (getStatus(serverName3)) {
						s3.send();
					}
				}
			};
			thread1.start();
			thread2.start();
			thread3.start();

			Thread statusChecker = new Thread() {
				public void run() {
					while (true) {
						checkServerStatus("s1");
						checkServerStatus("s2");
						checkServerStatus("s3");
					}
				}
			};

			statusChecker.start();

			try {
				Thread.sleep(10000);
				setStatus();
				s1.receiver.setStatus(false);
				s3.receiver.setStatus(false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (Exception e) {

		}
	}

	private static void setStatus() {
		s1_sender_isAlive = false;
		s3_sender_isAlive = false;
	}

	private static synchronized void checkServerStatus(String serverName) {
		synchronized (mapAccessor) {
			long currentTime = System.nanoTime() / 1000000;
			if(server_last_updated_time.containsKey(serverName)) {
				if (currentTime - server_last_updated_time.get(serverName) > TIME_OUT) {
					System.out.println(serverName + "Failed");
					if(server_leader_status.get(serverName)) {
						System.out.println(serverName+ " Leader Failed Found!!!");
						electNewLeader(serverName);
					}
				}
			}
		}
	}

	private static void electNewLeader(String oldLeader) {
		server_leader_status.remove(oldLeader);
		server_last_updated_time.remove(oldLeader);
		currentIds.remove(oldLeader);
		Map.Entry<String, Integer> maxEntry = null;
		for (Map.Entry<String, Integer> entry : currentIds.entrySet())
		{
		    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
		    {
		        maxEntry = entry;
		    }
		}
		server_leader_status.put(maxEntry.getKey(),true);
		currentIds.put(maxEntry.getKey(), LEADER_ID);
		System.out.println("Elected new leader :: "+maxEntry.getKey());
	}
	private static boolean getStatus(String name) {
		if (name.equals("s1")) {
			return s1_sender_isAlive;
		} else if (name.equals("s2")) {
			return s2_sender_isAlive;
		} else if (name.equals("s3")) {
			return s3_sender_isAlive;
		}
		return false;
	}

}
