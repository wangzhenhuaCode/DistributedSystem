package lab1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Main {

	/*
	 * This class is the entry of the process manager. It checks the argument
	 * and command and initialize global instances like thread pool, socket
	 * connection, message handler, master slave controller and a hashmap which
	 * maintains process status. This class also implements the function for
	 * showing process and launch a new process. When launch a process, the
	 * function will match constructor's parameters of the target class. Those
	 * constructors with no array type parameters has higher priority.
	 */

	static ThreadPool threadPool;
	static SocketConnection connection;
	static MasterSlaveControl masterslavecontrol;
	static HashMap<String, ProcessStatus> processStatusList;
	static String workingDirectory = "";

	public static void main(String[] args) {

		// TODO Auto-generated method stub

		int poolSize = 4;
		boolean slave = false;
		Integer masterPort = null;
		String masterHostname = null;
		if (args.length > 0) {
			for (int i = 0; i < args.length - 1; i++) {
				if (args[i].equals("-c")) {
					String args2[] = args[i + 1].split(":");
					if (args2.length == 2) {
						try {
							masterPort = Integer.valueOf(args2[1]);
							masterHostname = args2[0];
							slave = true;
						} catch (Exception e) {
							System.out
									.println("Incorrect master address, please restart.");
							System.out
									.println("Format:-c masterHostname:portNum");
							System.exit(0);
						}

					} else {
						System.out
								.println("Incorrect master address, please restart.");
						System.out.println("Format:-c masterHostname:portNum");
						System.exit(0);
					}
				} else if (args[i].equals("-p")) {
					try {
						SocketConnection.SOCKET_PORT = Integer
								.valueOf(args[i + 1]);
					} catch (Exception e) {
						System.out
								.println("Invalid port arguement, please restart.");
						System.exit(0);
					}
				} else if (args[i].equals("-t")) {
					try {
						poolSize = Integer.valueOf(args[i + 1]);
					} catch (Exception e) {
						System.out
								.println("Invalid threadpool size arguement, please restart.");
						System.exit(0);
					}
				} else if (args[i].equals("-w")) {
					try {
						workingDirectory = args[i + 1];
						if (workingDirectory.lastIndexOf("/") != workingDirectory
								.length() - 1) {
							System.out
									.println("Working directory should end with /, please restart.");
							System.exit(0);
						}
						File f = new File(workingDirectory);
						if (!f.isDirectory() || !f.exists()) {
							System.out
									.println("Invalid working directory, please restart.");
							System.exit(0);
						}

					} catch (Exception e) {
						System.out
								.println("Invalid working directory, please restart.");
						System.exit(0);
					}
				}

			}

		}
		processStatusList = new HashMap<String, ProcessStatus>();
		threadPool = new ThreadPool(poolSize);

		try {
			connection = new SocketConnection();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			System.out
					.println("Port confliction, please restart and use '-p portNum' to change port");
			return;
		}
		Thread t = new Thread(connection);
		t.start();

		masterslavecontrol = new MasterSlaveControl();
		if (slave) {
			masterslavecontrol.beSlave(masterHostname, masterPort);
		}

		byte[] buffer = new byte[1024];
		int n = 0;

		System.out.println("Welcome to ProcessManager");
		System.out.println("Instruction:");
		System.out
				.println("  <processName> [arg1] [arg2] ... [argN] (lauch a process)");
		System.out
				.println("  ps (prints a list of local running processes and their arguments)");
		System.out.println("  quit (exits the ProcessManager)");
		System.out.println("Your address : " + SocketConnection.LOCAL_HOSTNAME
				+ ":" + SocketConnection.SOCKET_PORT);

		while (true) {
			try {
				n = System.in.read(buffer);
				String command = new String(buffer, 0, n - 1);
				if (command.equals("quit")) {
					System.out.println("Bye!");
					break;
				} else {
					parseComand(command);
				}

			} catch (Exception e) {

			}
		}
		System.exit(0);
	}

	public static void parseComand(String command) {
		String args[] = command.split("\\s+");
		if (args[0].equals("ps")) {
			showProcess();
		} else {

			launchProcess(args);

		}

	}

	public static void showProcess() {
		synchronized (Main.processStatusList) {
			boolean isMaster = masterslavecontrol.getIsMaster();
			if (isMaster) {
				System.out.println("Process Name & Args\tStatus\tMachine");
			} else {
				System.out.println("Process Name & Args\tStatus");
			}
			for (ProcessStatus ps : Main.processStatusList.values()) {
				if (isMaster) {
					System.out.println(ps.getNameAndArgs() + "\t"
							+ ps.getStatutsInfo() + "\t"
							+ ps.getRunningMachine());
				} else {
					System.out.println(ps.getNameAndArgs() + "\t"
							+ ps.getStatutsInfo());
				}
			}
		}
	}

	public static void launchProcess(String[] args) {
		String className = args[0];
		Class<?> processClass;
		try {
			processClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Process not found!");
			return;
		}
		Class<?> interfaces[] = processClass.getInterfaces();
		boolean impl = false;
		for (int i = 0; i < interfaces.length; i++) {
			if (interfaces[i].getName().contains("MigratableProcess")) {
				impl = true;
				break;
			}
		}
		if (!impl) {
			System.out.println("Process is not migratable!");
			return;
		}
		Constructor<?> constructors[] = processClass.getConstructors();
		Class<?> paramaters[];
		boolean findConstructor = false;
		Object instance = null;
		for (int i = 0; i < constructors.length; i++) {
			paramaters = constructors[i].getParameterTypes();
			if (paramaters.length == args.length - 1) {
				ArrayList<Object> argslist = new ArrayList<Object>();
				findConstructor = true;
				for (int j = 0; j < paramaters.length; j++) {
					String c = paramaters[j].toString();

					try {
						if (c.equals(Integer.class.toString())) {
							argslist.add(Integer.valueOf(args[j + 1]));
						} else if (c.equals(Double.class.toString())) {
							argslist.add(Double.valueOf(args[j + 1]));
						} else if (c.equals(Boolean.class.toString())) {
							argslist.add(Boolean.valueOf(args[j + 1]));
						} else if (c.equals(String.class.toString())) {
							argslist.add(args[j + 1]);
						} else if (c.equals(int.class.toString())) {
							argslist.add(Integer.valueOf(args[j + 1]));
						} else if (c.equals(double.class.toString())) {
							argslist.add(Double.valueOf(args[j + 1]));
						} else if (c.equals(boolean.class.toString())) {
							argslist.add(Boolean.valueOf(args[j + 1]));
						} else {
							findConstructor = false;
							break;
						}
					} catch (Exception e) {
						findConstructor = false;
						break;
					}
				}
				if (findConstructor) {

					try {
						instance = constructors[i].newInstance(argslist
								.toArray());
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						findConstructor = false;
						break;
					} catch (InstantiationException e) {
						// TODO Auto-generated catch block
						findConstructor = false;
						break;
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						findConstructor = false;
						break;
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						findConstructor = false;
						break;
					}

					break;
				}
			}
		}
		if (!findConstructor || instance == null) {
			try {
				Constructor<?> constructor = processClass
						.getConstructor(String[].class);
				Object[] argslist = { Arrays.copyOfRange(args, 1, args.length) };
				instance = constructor.newInstance(argslist);
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				System.out.println("Invalid arguments!");
				System.out.println();
				return;
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				System.out.println("Invalid arguments!");
				System.out.println();
				return;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				System.out.println("Invalid arguments!");
				System.out.println();
				return;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				System.out.println("Invalid arguments!");
				System.out.println();
				return;
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				System.out.println("Invalid arguments!");
				System.out.println();
				return;
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				System.out.println("Invalid arguments!");
				System.out.println();
				return;
			} catch (Exception e) {
				System.out.println(e.toString());
				return;
			}

		}
		MigratableProcess process = (MigratableProcess) instance;

		String nameAndArgs = "";
		for (String s : args) {
			nameAndArgs = nameAndArgs + s + " ";
		}
		final String processId = args[0] + "_" + new Date().getTime() + "_"
				+ SocketConnection.LOCAL_HOSTNAME;
		final ProcessStatus processstatus = new ProcessStatus(processId, process,
				nameAndArgs, ProcessStatus.WAITING, "Local", new Date());
		Thread t=new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				synchronized (processStatusList) {
					processStatusList.put(processId, processstatus);
				}
				threadPool.addTask(processstatus);
				String fileName = workingDirectory
						+ processstatus.getProcessId().replace(".", "_") + ".ser";
				try {

					FileOutputStream fileOut = new FileOutputStream(fileName);
					ObjectOutputStream out = new ObjectOutputStream(fileOut);
					out.writeObject(processstatus.getProcess());
					out.close();
					fileOut.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block

					e.printStackTrace();
					
				}
			}
			
			
		});
		t.start();
		
	}

}
