package lab1;

import java.util.ArrayList;



public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO Auto-generated method stub
		byte[] buffer=new byte[1024];
		int n=0;
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

	private static void interaction(String command) {
		if(command.equals("ps")){
			//count process
		}else {
			ArrayList<String> str = new ArrayList<String>();
			for(String s : command.split(" ")){
				if(s.length() > 0){
					str.add(s);
				}else{
					System.out.print("invalid args");
				}
			}
			/*for(String x : str){
				System.out.print(x);
			}*/
		}
		
	}

}
