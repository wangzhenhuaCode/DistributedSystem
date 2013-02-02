package lab1;

public class ProcessStatus {
	static final int WAITING=0;
	static final int RUNNING=2;
	static final int FINISHED=3;
	static final int MIGRATED=1;
	private String processId;
	private MigratableProcess process;
	private String nameAndArgs;
	private Integer status;
	private String addtionnal;
	


	public ProcessStatus(String processId, MigratableProcess process,
			String nameAndArgs, Integer status, String addtionnal) {
		super();
		this.processId = processId;
		this.process = process;
		this.nameAndArgs = nameAndArgs;
		this.status = status;
		this.addtionnal = addtionnal;
	}
	public MigratableProcess getProcess() {
		return process;
	}
	public void setProcess(MigratableProcess process) {
		this.process = process;
	}
	public String getNameAndArgs() {
		return nameAndArgs;
	}
	public void setNameAndArgs(String nameAndArgs) {
		this.nameAndArgs = nameAndArgs;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getAddtionnal() {
		return addtionnal;
	}
	public void setAddtionnal(String addtionnal) {
		this.addtionnal = addtionnal;
	}
	public String getInfo(){
		String info=nameAndArgs;
		switch(status){
		case WAITING:
			info=info+"is waiting ";
			break;
		case RUNNING:
			info=info+"is running ";
			break;
		case FINISHED:
			info=info+"has been finished ";
			break;
		case MIGRATED:
			info=info+"has been migrated ";
			break;
		}
		if(addtionnal!=null&&!addtionnal.equals(""))
			info=info+"("+addtionnal+")";
		return info;
	}
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	
	
}
