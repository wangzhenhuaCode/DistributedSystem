package lab1;



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
				System.out.println(command);
			}
				
			}catch(Exception e){
				
			}
		}
	}

}
