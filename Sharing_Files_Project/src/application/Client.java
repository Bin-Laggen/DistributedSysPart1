package application;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.Arrays;
import java.util.List;

import controller.Monitor;
import controller.RemoteInterface;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import view.ServerView;

public class Client extends Application implements Runnable {

	private ScrollPane root;
	private GridPane pane;
	private Scene scene;
	private VBox box;
	private static String hostName;
	private static String bindName;
	private static Socket socket;
	private static ObjectOutputStream out;
	private static ObjectInputStream in;

	private final String GET_FILE_LIST = "1";
	private final String DOWNLOAD_FILE = "2";
	private final String UPLOAD_FILE = "3";
	private final String CHECK_FOR_CHANGE = "4";
	private final String EXIT = "5";
	private final String COMPLETE = "6";
	private final String WAITING_FOR_FILE_NAME = "2_1";
	private final String RECEIVED = "2_2";
	private final String WAITING_FOR_FILE = "3_1";
	private final String CHANGED = "4_1";
	private final String NOT_CHANGED = "4_2";
	private final String CONFIRMED = "4_3";

	private Monitor mon = Monitor.getInstance();
	private Thread t;

	private boolean execute;
	private boolean checkForUpdate;
	
	private static RemoteInterface server;

	private String[] serverFiles;

	public static void main(String[] args) throws IOException {


		if (args.length != 2) 
		{
			System.err.println("Usage: java Client <host name> <bind name>");
			System.exit(1);
		}

		hostName = args[0];
		bindName = args[1];

		openConnection();

		launch(args);

	}

	@Override
	public void start(Stage primaryStage) {

		try 
		{
			System.out.println("THREAD STARTED");
			System.out.println("LAUNCHNG GUI");
			pane = new GridPane();
			box = new ServerView(primaryStage, this);
			root = new ScrollPane(pane);
			pane.add(box, 0, 0);
			pane.setPadding(new Insets(20));
			scene = new Scene(root,800, 600);
			primaryStage.setScene(scene);
			primaryStage.show();

		} 
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}

	private static void openConnection()
	{
		try
		{
			server = (RemoteInterface) Naming.lookup("rmi://" + hostName + "/" + bindName);
			//socket = new Socket(hostName, portNumber);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());	

			System.out.println("CONNECTION OPENED");

		} 
		catch (UnknownHostException e) 
		{
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} 
		catch (IOException e) 
		{
			System.err.println("Couldn't get I/O for the connection to " + hostName);
			System.exit(1);
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean sendCommand(String command, Object data)
	{		
		try 
		{
			//busy = true;
			out.writeObject(command);
			out.flush();
			System.out.println(command);
			if(command.equals(GET_FILE_LIST))
			{
				System.out.println("Requesting List");
				receiveFileList();
				System.out.println("List received");
			}
			else if(command.equals(DOWNLOAD_FILE))
			{
				System.out.println("Requesting file");
				downloadFile(data);
				System.out.println("File received");
			}
			else if(command.equals(UPLOAD_FILE))
			{
				System.out.println("Sending file");
				uploadFile(data);
				System.out.println("File sent");
			}
			else if(command.equals(CHECK_FOR_CHANGE))
			{
				System.out.println("Requesting change");
				checkForUpdate = false;
				receiveChange();
				checkForUpdate = true;
				System.out.println("Change received");
			}
			else if(command.equals(EXIT))
			{
				exit();
			}
			//busy = false;
			return true;
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
	}

	private void receiveChange() {
		try 
		{
			do
			{
				System.out.println("Reading object");
				Object change = in.readObject();
				System.out.println("Object" + change);
				if(change.equals(true))
				{
					mon.setChange();
					out.writeObject(CHANGED);
				}
				else
				{
					out.writeObject(NOT_CHANGED);
				}
			}
			while(!in.readObject().equals(CONFIRMED));
			out.writeObject(COMPLETE);
		} 
		catch (ClassNotFoundException | IOException e) 
		{
			e.printStackTrace();
		}

	}

	private void downloadFile(Object data) {
		try 
		{
			if(in.readObject().equals(WAITING_FOR_FILE_NAME))
			{
				try 
				{
					out.writeObject(data);
					Object file = in.readObject();
					System.out.println(file);
					mon.copyFile((File) file);
					out.writeObject(COMPLETE);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		} 
		catch (ClassNotFoundException | IOException e) 
		{
			e.printStackTrace();
		}
	}

	private void uploadFile(Object data) 
	{
		try 
		{
			if(in.readObject().equals(WAITING_FOR_FILE))
			{
				try 
				{
					System.out.println("Sending: " + data.toString());
					out.writeObject(data);
					if(in.readObject().equals(RECEIVED))
					{
						System.out.println("Sent");
						out.writeObject(COMPLETE);
					}
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		} 
		catch (ClassNotFoundException | IOException e) 
		{
			e.printStackTrace();
		}
	}

	private void receiveFileList()
	{
		try 
		{
			@SuppressWarnings("unchecked")
			List<String> data = (List<String>) in.readObject();
			serverFiles = new String[(data).size()];
			serverFiles = (data).toArray(serverFiles);
			System.out.println(Arrays.toString(serverFiles));
			out.writeObject(COMPLETE);
		} 
		catch (ClassNotFoundException | IOException e) 
		{
			e.printStackTrace();
		}
	}

	public void exit()
	{
		try 
		{
			System.out.println("Exiting");
			threadStop();
			System.out.println("Threads stopped");
			in.close();
			System.out.println("In closed");
			out.close();
			System.out.println("Out closed");
			socket.close();
			System.out.println("Socket closed");

		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	public String[] getServerFiles()
	{
		sendCommand(GET_FILE_LIST, null);
		return serverFiles;
	}

	@Override
	public void run() 
	{
		while(execute)
		{
			while(checkForUpdate) //stop this loop if a different operation is being carried out
			{
				sendCommand(CHECK_FOR_CHANGE, null);
				try 
				{
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			}
			try 
			{
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public void threadStop()
	{
		checkForUpdate = false;
		execute = false;
	}

	public void threadStart()
	{
		checkForUpdate = true;
		execute = true;
		//busy = false;
		t = new Thread(Client.this, "SERVER CHECK DATA THREAD");
		t.start();
	}

}