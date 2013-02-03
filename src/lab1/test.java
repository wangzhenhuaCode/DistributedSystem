package lab1;

import java.io.IOException;
import java.io.PrintStream;

public class test implements MigratableProcess{
	private String name;
	private Integer i=0;
	private volatile boolean stop=false;
	private TransactionalFileOutputStream outFile;
	public test(String arg0){
		this.name=arg0;
		try {
			outFile=new TransactionalFileOutputStream(name+".test",false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public test(){
		
	}
	
	
	@Override
	public void run() {
		
		PrintStream out = new PrintStream(outFile);
		System.out.println(name+":  start");
			while(!stop){
				if(i<10){
				try {
					System.out.println(name+" :"+i);
					out.println(name+" :"+i);
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i++;
				}else{
					break;
				}
				out.flush();
			}
			System.out.println(name+":  end");
			stop=false;
			
			out.close();
			try {
				outFile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		stop=true;
	}
	
}
