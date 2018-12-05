package controller;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;

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
	private static int portNumber;
	private static Socket socket;
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	
	private final String GET_FILE_LIST = "1";
	private final String DOWNLOAD_FILE = "2";
	private final String UPLOAD_FILE = "3";
	private final String CHECK_FOR_CHANGE = "4";
	private final String EXIT = "EXIT";
	private final String WAITING_FOR_FILE_NAME = "2_1";
	private final String WAITING_FOR_FILE = "3_1";
	
	private Monitor mon = Monitor.getInstance();
	private static Thread t;
	
	private static boolean execute;
	
	private String[] serverFiles;
	
	public static void main(String[] args) throws IOException {

		/**
		if (args.length != 2) 
		{
			System.err.println("Usage: java Client <host name> <port number>");
			System.exit(1);
		}
		*/

		hostName = "localhost";
		portNumber = 1234;
		
		openConnection();

		launch(args);

	}

	@Override
	public void start(Stage primaryStage) {
		
		try 
		{
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
			socket = new Socket(hostName, portNumber);
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());	

			execute = true;
			t = new Thread("Check Server Change Thread");
			t.start();
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
		}
	}
	
	public boolean sendCommand(String command, Object data)
	{		
		try 
		{
			out.writeObject(command);
			out.flush();
			System.out.println(command);
			if(command.equals(GET_FILE_LIST))
			{
				receiveFileList();
			}
			else if(command.equals(DOWNLOAD_FILE))
			{
				downloadFile(data);
			}
			else if(command.equals(UPLOAD_FILE))
			{
				uploadFile(data);
			}
			else if(command.equals(CHECK_FOR_CHANGE))
			{
				receiveChange();
			}
			else if(command.equals(EXIT))
			{
				exit();
			}
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
			boolean change = (boolean) in.readObject();
			if(change)
			{
				mon.setChange();
			}
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
					mon.copyFile((File) in.readObject());
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
					out.writeObject(data);
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
			serverFiles = new String[data.size()];
			serverFiles = data.toArray(serverFiles);
			System.out.println(Arrays.toString(serverFiles));
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
			out.writeObject(EXIT);
			System.out.println("Object Written");
			out.close();
			System.out.println("Out closed");
		    in.close();
		    System.out.println("In closed");
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
		return serverFiles;
	}

	@Override
	public void run() {
		while(execute)
		{
			try 
			{
				System.out.println("EXECUTING");
				Object input = in.readObject();
				System.out.println(input);
				if(input instanceof Boolean)
				{
					if((boolean) input)
					{
						mon.setChange();
					}
				}
			} 
			catch (ClassNotFoundException | IOException e1) 
			{
				e1.printStackTrace();
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
		execute = false;
	}
	
}