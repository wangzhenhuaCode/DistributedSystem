package lab1;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.event.ListSelectionEvent;





public class Main {

	/**
	 * @param args
	 */
	static ThreadPool threadPool;
	static SocketConnection connection;
	static MasterSlaveControl masterslavecontrol;
	public static void main(String[] args) {

		// TODO Auto-generated method stub
		threadPool=new ThreadPool(5);
		connection=new SocketConnection();
		Thread t=new Thread(connection);
		t.start();
		masterslavecontrol=new MasterSlaveControl();
		byte[] buffer=new byte[1024];
		int n=0;
		
		System.out.println("Welcome to Process Monitor");
		System.out.println("Your address : "+SocketConnection.LOCAL_HOSTNAME+":"+SocketConnection.SOCKET_PORT);
		while(true){
			try{
			n=System.in.read(buffer);
			String command=new String(buffer,0,n-1);
			if(command.equals("quit")){
				System.out.println("Bye!");
				break;
			}else{
				parseComand(command);
			}
				
			}catch(Exception e){
				
			}
		}
	}

	public static void parseComand(String command) {
		String args[]=command.split("\\s+");
		if(args[0].equals("ps")){
			threadPool.showStatus();
		}else if(args[0].equals("-c")){
			String args2[]=args[1].split(":");
			if(args2.length==2)
				masterslavecontrol.beSlave(args2[0], Integer.valueOf(args2[1]));
			else{
				System.out.println("Incorrect address!");
				System.out.println("Format:-c masterHostname:portNum");
			}
		}else {
			
			launchProcess(args);
			
		}
		
	}
	public static void launchProcess(String[] args){
		String className=args[0];
		Class<?> processClass;
		try {
			processClass=Class.forName(className)
;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Process not found!");
			return;
		}
		Class<?> interfaces[]=processClass.getInterfaces();
		boolean impl=false;
		for(int i=0;i<interfaces.length;i++){
			if(interfaces[i].getName().contains("MigratableProcess")){
				impl=true;
				break;
			}
		}
		if(!impl){
			System.out.println("Process is not migratable!");
			return;
		}
		Constructor<?> constructors[]=processClass.getConstructors();
		Class<?> paramaters[];
		boolean findConstructor=false;
		Object instance = null;
		for(int i=0;i<constructors.length;i++){
			paramaters=constructors[i].getParameterTypes();
			if(paramaters.length==args.length-1){
				ArrayList<Object> argslist=new ArrayList<Object>();
				findConstructor=true;
				for(int j=0;j<paramaters.length;j++){
					String c=paramaters[j].toString();
					try{
						if(c.equals(Integer.class.toString())){
							argslist.add(Integer.valueOf(args[j+1]));
						}else if(c.equals(Double.class.toString())){
							argslist.add(Double.valueOf(args[j+1]));
						}else if(c.equals(Boolean.class.toString())){
							argslist.add(Boolean.valueOf(args[j+1]));
						}else if(c.equals(String.class.toString())){
							argslist.add(args[j+1]);
						}else if(c.equals(int.class.toString())){
							argslist.add(Integer.valueOf(args[j+1]));
						}else if(c.equals(double.class.toString())){
							argslist.add(Double.valueOf(args[j+1]));
						}else if(c.equals(boolean.class.toString())){
							argslist.add(Boolean.valueOf(args[j+1]));
						}else{
							findConstructor=false;
							break;
						}
					}catch(Exception e){
						findConstructor=false;
						break;
					}
				}
				if(findConstructor){
			
						try {
							instance=constructors[i].newInstance(argslist.toArray());
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							findConstructor=false;
							break;
						} catch (InstantiationException e) {
							// TODO Auto-generated catch block
							findConstructor=false;
							break;
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							findConstructor=false;
							break;
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							findConstructor=false;
							break;
						}
					
					
					break;
				}
			}
		}
		if(!findConstructor||instance==null){
			System.out.println("Invalid arguments!");
			System.out.println();
			return;
		}
		threadPool.addTask((MigratableProcess) instance);
		
		
		}
	
}
