package lab1;

public class test implements MigratableProcess{
	private String name;
	private Integer i=0;
	private volatile boolean stop=false;
	public test(String arg0){
		this.name=arg0;
	}
	public test(String arg0, String arg1){
		System.out.print("2");
	}
	public test(String arg0, int arg1){
		System.out.print("3");
	}
	public test(String arg0, String arg1, int arg2){
		System.out.print("4");
	}
	public test(int arg0, String arg1, String arg3){
		System.out.print("5");
	}
	public test(String arg0, int arg1, String arg2){
		System.out.print("6");
	}
	
	
	@Override
	public void run() {
		System.out.println(name+":  start");
			while(!stop){
				if(i<10){
				try {
					System.out.println(name+" :"+i);
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				i++;
				}else{
					break;
				}
			}
			System.out.println(name+":  end");
			stop=false;
		
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		stop=true;
	}
	
}
