package lab1;

public class test implements MigratableProcess{
	public test(String arg0){
		System.out.print("1");
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
		
			for(int i=0; i<10; i++){
				try {
					Thread.currentThread();
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.print(i);
			}
			
		
		
	}

	@Override
	public void suspend() {
		// TODO Auto-generated method stub
		
	}
	
}
