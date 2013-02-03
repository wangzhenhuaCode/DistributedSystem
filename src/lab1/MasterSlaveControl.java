package lab1;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MasterSlaveControl {
	
	private AtomicBoolean isMaster = new AtomicBoolean();
	private String masterHostname;
	private int masterPort;

	private HashMap<String, Slave> slavemap;

	class Slave {
		String hostname;
		int port;
		boolean updated;
		int processNum;

		public Slave() {
		}

		public Slave(String hostname, int port, boolean updated, int processNum) {
			super();
			this.hostname = hostname;
			this.port = port;
			this.updated = updated;
			this.processNum = processNum;
		}

	}
	private Thread salveUpdate;
	public MasterSlaveControl() {

		isMaster.set(false);
		slavemap = new HashMap<String, Slave>();

		salveUpdate = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){
					Message m = new Message();
					String content = SocketConnection.LOCAL_HOSTNAME
							+ Message.devide1 + SocketConnection.SOCKET_PORT
							+ Message.devide1
							+ Main.threadPool.getTotalProcess()
							+ Message.devide1;
					int len = Main.processStatusList.values().size(), i = 0;
					
					synchronized(Main.processStatusList){
				
					for (ProcessStatus p : Main.processStatusList.values()) {

						content = content + p.getProcessId() + Message.devide3
								+ p.getNameAndArgs() + Message.devide3
								+ p.getStatus();
						if (i < len - 1) {
							content = content + Message.devide2;
						}
						i++;
					}
					}
					m.setContent(content);
					m.setRequestType(Message.REQUESTTYPE_SLAVE_UPDATE);
					m.setDestinationHostName(masterHostname);
					m.setDestinationPort(masterPort);
					Main.connection.getMessageHandler().sendMessage(m);
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
			}

		});
		salveUpdate.setPriority(Thread.MAX_PRIORITY);
		
		Thread masterCheck = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					synchronized (isMaster) {
						while (!isMaster.get()) {
							try {
								isMaster.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}
					synchronized (slavemap) {
						while (slavemap.isEmpty()) {
							try {
								slavemap.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}

					List<Slave> list = new ArrayList<Slave>();
					list.addAll(slavemap.values());
					Collections.sort(list, new Comparator<Slave>() {

						@Override
						public int compare(Slave arg0, Slave arg1) {
							// TODO Auto-generated method stub
							if (arg0.processNum < arg1.processNum)
								return -1;
							else if (arg0.processNum > arg1.processNum)
								return 1;
							else
								return 0;

						}

					});
					List<ProcessStatus> recoveryList = new ArrayList<ProcessStatus>();
					for (int i = 0; i < list.size(); i++) {
						Slave s = list.get(i);
						if (s.updated == false) {
							System.out.println(s.hostname+":"+s.port+" disconnect!");
							for (ProcessStatus ps : Main.processStatusList
									.values()) {
								if (ps.getRunningMachine().equals(
										s.hostname + ":" + s.port)
										&& ps.getStatus() != ProcessStatus.MIGRATED
										&& ps.getStatus() != ProcessStatus.FINISHED) {
									recoveryList.add(ps);
								}
							}
							list.remove(i);
							slavemap.remove(s.hostname + ":" + s.port);
							i--;
						} else {
							s.updated = false;
						}
					}
					if (list.size() == 0) {
						if (recoveryList.size() > 0) {
							for (ProcessStatus ps : recoveryList) {
								Message newMessage = new Message();
								newMessage
										.setRequestType(Message.REQUESTTYPE_RECOVERY);
								newMessage
										.setDestinationHostName(SocketConnection.LOCAL_HOSTNAME);
								newMessage.setDestinationPort(Integer
										.valueOf(SocketConnection.SOCKET_PORT));
								newMessage.setContent(ps.getProcessId()
										+ Message.devide1 + ps.getNameAndArgs());
								Main.connection.getMessageHandler()
										.sendMessage(newMessage);
							}
						}
						continue;
					}

					Slave s1 = list.get(0), s2 = list.get(list.size() - 1);
					int myprocessNum = Main.threadPool.getTotalProcess();
					int diff = s2.processNum - s1.processNum;
					String largeHostname = s2.hostname, smallHostname = s1.hostname;
					int largePort = s2.port, smallPort = s1.port;
					if (myprocessNum > s2.processNum) {
						diff = myprocessNum - s1.processNum;
						largePort = SocketConnection.SOCKET_PORT;
						largeHostname = SocketConnection.LOCAL_HOSTNAME;
					} else if (myprocessNum < s1.processNum) {
						diff = s2.processNum - myprocessNum;
						smallPort = SocketConnection.SOCKET_PORT;
						smallHostname = SocketConnection.LOCAL_HOSTNAME;
					}
					for (ProcessStatus ps : recoveryList) {
						Message newMessage = new Message();
						newMessage.setRequestType(Message.REQUESTTYPE_RECOVERY);
						newMessage.setDestinationHostName(smallHostname);
						newMessage.setDestinationPort(Integer
								.valueOf(smallPort));
						newMessage.setContent(ps.getProcessId()
								+ Message.devide1 + ps.getNameAndArgs());
						Main.connection.getMessageHandler().sendMessage(
								newMessage);
						diff--;
					}

					if (diff > 1) {
						int migrateNum = diff / 2;
						Message m = new Message();
						m.setRequestType(Message.REQUESTTYPE_NOTIFY_MIGRATION);
						m.setDestinationHostName(largeHostname);
						m.setDestinationPort(largePort);
						m.setContent(smallHostname + Message.devide1
								+ smallPort + Message.devide1 + migrateNum);
						Main.connection.getMessageHandler().sendMessage(m);

					}
					try {
						Thread.sleep(7000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});
		masterCheck.start();
	}

	public void beSlave(String hostName, int port) {
		
			this.masterHostname = hostName;
			this.masterPort = port;
			
			salveUpdate.start();
			
		
	}

	public void addSlave(Message message) {
		String arg1[] = message.getContent().split(Message.devide1);
		Slave slave = new Slave();
		slave.hostname = arg1[0];
		slave.port = Integer.valueOf(arg1[1]);
		slave.processNum = Integer.valueOf(arg1[2]);
		slave.updated=true;
		String arg2[] = null;
		System.out.println("slave update:"+arg1[0]);
		if (arg1.length == 4)
			arg2 = arg1[3].split(Message.devide2);

		synchronized (isMaster) {
			synchronized (slavemap) {
				slavemap.put(slave.hostname + ":" + slave.port, slave);
				slavemap.notify();
			}
			if (arg2 != null) {
				synchronized (Main.processStatusList) {
					for (int i = 0; i < arg2.length; i++) {
						String arg3[] = arg2[i].split(Message.devide3);
						if (!arg3[2].equals(ProcessStatus.MIGRATED)) {

							ProcessStatus ps = new ProcessStatus();
							ps.setProcessId(arg3[0]);
							ps.setNameAndArgs(arg3[1]);
							ps.setStatus(Integer.valueOf(arg3[2]));
							ps.setRunningMachine(slave.hostname + ":"
									+ slave.port);
							Main.processStatusList.put(arg3[0], ps);

						}
					}
				}
			}
			isMaster.set(true);
			isMaster.notify();
		}
		System.out.println("slave update finish:"+arg1[0]);
	}

	public boolean getIsMaster() {
		return isMaster.get();
	}
}
