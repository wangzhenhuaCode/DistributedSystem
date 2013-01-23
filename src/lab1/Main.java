package lab1;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;




public class Main {

	/**
	 * @param args
	 */
	static ArrayList<MigratableProcess> processList;
	public static void main(String[] args) {

		// TODO Auto-generated method stub
		byte[] buffer=new byte[1024];
		int n=0;
		processList=new ArrayList<MigratableProcess>();
		System.out.println("Welcome to Process Monitor");
		while(true){
			try{
			n=System.in.read(buffer);
			String command=new String(buffer,0,n-1);
			if(command.equals("quit")){
				System.out.println("Bye!");
				break;
			}else{
				interaction(command);
			}
				
			}catch(Exception e){
				
			}
		}
	}

	public static void interaction(String command) {
		if(command.equals("ps")){
			//count process
		}else {
			ArrayList<String> str = new ArrayList<String>();
			for(String s : command.split(" ")){
				if(s.length() > 0){
					str.add(s);
				}else{
					System.out.print("invalid args");
					return;
				}
			}
			launchProcess(str);
			
		}
		
	}
	public static void launchProcess(ArrayList<String> args){
		String className=args.get(0);
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
			if(paramaters.length==args.size()-1){
				ArrayList<Object> argslist=new ArrayList<Object>();
				findConstructor=true;
				for(int j=0;j<paramaters.length;j++){
					String c=paramaters[j].toString();
					try{
						if(c.equals(Integer.class.toString())){
							argslist.add(Integer.valueOf(args.get(j+1)));
						}else if(c.equals(Double.class.toString())){
							argslist.add(Double.valueOf(args.get(j+1)));
						}else if(c.equals(Boolean.class.toString())){
							argslist.add(Boolean.valueOf(args.get(j+1)));
						}else if(c.equals(String.class.toString())){
							argslist.add(args.get(j+1));
						}else if(c.equals(int.class.toString())){
							argslist.add(Integer.valueOf(args.get(j+1)));
						}else if(c.equals(double.class.toString())){
							argslist.add(Double.valueOf(args.get(j+1)));
						}else if(c.equals(boolean.class.toString())){
							argslist.add(Boolean.valueOf(args.get(j+1)));
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
			return;
		}
		Thread thread=new Thread((Runnable)instance);
		processList.add((MigratableProcess) instance);
		thread.start();
		
		
		}
	
}
