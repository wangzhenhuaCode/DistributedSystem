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
	private AtomicBoolean isSlave=new AtomicBoolean();
	private AtomicBoolean isMaster=new AtomicBoolean();
	private String masterHostname;
	private int masterPort;
	
	private HashMap<String,Slave> slavemap;
	class Slave{
		String hostname;
		int port;
		boolean updated;
		int processNum;
		public Slave(String hostname, int port, boolean updated, int processNum) {
			super();
			this.hostname = hostname;
			this.port = port;
			this.updated = updated;
			this.processNum = processNum;
		}
		
	}
	public MasterSlaveControl(){
		isSlave.set(false);
		isMaster.set(false);
		slavemap=new HashMap<String,Slave>();
		
		Thread salveUpdate=new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(true){
					synchronized(isSlave){
						while(!isSlave.get()){
							try {
								isSlave.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					Message m=new Message();
					String content=SocketConnection.LOCAL_HOSTNAME+" "+SocketConnection.SOCKET_PORT+" "+Main.threadPool.getProcessNum();
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
		salveUpdate.start();
		Thread masterCheck=new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				synchronized(isMaster){
					while(!isMaster.get()){
						try {
							isMaster.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
				synchronized(slavemap){
					while(slavemap.isEmpty()){
						try {
							slavemap.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
				
				List<Slave> list=new ArrayList<Slave>();
				list.addAll(slavemap.values());
				Collections.sort(list, new Comparator<Slave>(){

					@Override
					public int compare(Slave arg0, Slave arg1) {
						// TODO Auto-generated method stub
						if(arg0.processNum<arg1.processNum)return -1;
						else if(arg0.processNum>arg1.processNum)return 1;
						else return 0;
						
					}
					
				});
				for(int i=0;i<list.size();i++){
					Slave s=list.get(i);
					if(s.updated==false){
						list.remove(i);
						i--;
					}else{
						s.updated=false;
					}
				}
				Slave s1=list.get(0),s2=list.get(list.size()-1);
				int myprocessNum=Main.threadPool.getProcessNum();
				int diff=s2.processNum-s1.processNum;
				String largeHostname=s2.hostname,smallHostname=s1.hostname;
				int largePort=s2.port,smallPort=s1.port;
				if(myprocessNum>s2.processNum){
					diff=myprocessNum-s1.processNum;
					largePort=SocketConnection.SOCKET_PORT;
					largeHostname=SocketConnection.LOCAL_HOSTNAME;
				}else if(myprocessNum<s1.processNum){
					diff=s2.processNum-myprocessNum;
					smallPort=SocketConnection.SOCKET_PORT;
					smallHostname=SocketConnection.LOCAL_HOSTNAME;
				}
					
					if(diff>1){
						int migrateNum=diff/2;
						Message m=new Message();
						m.setRequestType(Message.REQUESTTYPE_NOTIFY_MIGRATION);
						m.setDestinationHostName(largeHostname);
						m.setDestinationPort(largePort);
						m.setContent(smallHostname+" "+smallPort+" "+migrateNum);
						Main.connection.getMessageHandler().sendMessage(m);
					
				}
				try {
					Thread.sleep(6000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}
			
		});
		masterCheck.start();
	}
	public void beSlave(String hostName,int port){
		synchronized(isSlave){
			this.masterHostname=hostName;
			this.masterPort=port;
			isSlave.set(true);
			Message m=new Message();
			m.setRequestType(Message.REQUESTTYPE_SLAVE_UPDATE);
			m.setContent(SocketConnection.LOCAL_HOSTNAME+" "+SocketConnection.SOCKET_PORT+" "+Main.threadPool.getProcessNum());
			m.setDestinationHostName(masterHostname);
			m.setDestinationPort(masterPort);
			Main.connection.getMessageHandler().sendMessage(m);
			isSlave.notify();
		}
	}
	public void addSlave(String hostname, int port,int processNum){
		synchronized(isMaster){
			synchronized(slavemap){			
				slavemap.put(hostname+":"+port,new Slave(hostname,port,true,processNum) );
				slavemap.notify();
			}
			isMaster.set(true);
			isMaster.notify();
		}
	}
}
