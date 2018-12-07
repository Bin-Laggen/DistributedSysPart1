package controller;

import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.io.*;

public class ServerThread extends Thread implements Observer {
	
	private Socket socket = null;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
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
	
	private ServerMonitor mon;
	private boolean running;
	private boolean actionInProgress;
	private boolean change;

	public ServerThread(String name, Socket socket) 
	{
		super(name);
		this.socket = socket;
		running = true;
		actionInProgress = false;
		change = false;
		mon = ServerMonitor.getInstance();
		mon.addObserver(this);
		System.out.println(mon.getPath());
		
		try
		{
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void run() {
		
		while(running)
		{
			while(actionInProgress)
			{
				//System.out.println("Something in progress");
			}
			try 
			{
				Object fromClient;
				try 
				{
					fromClient = in.readObject();
					System.out.println(this.getName() + " received message: " + fromClient);
					if(fromClient.equals(GET_FILE_LIST))
					{
						System.out.println("File list requested");
						sendFileList();
						//System.out.println("SEND FILE LIST");
					}
					else if(fromClient.equals(DOWNLOAD_FILE))
					{
						System.out.println("File requested");
						sendFile();
						//System.out.println("SEND FILE");
					}
					else if(fromClient.equals(UPLOAD_FILE))
					{
						System.out.println("File incoming");
						receiveFile();
						//System.out.println("RECEIVE FILE");
					}
					else if(fromClient.equals(CHECK_FOR_CHANGE))
					{
						System.out.println("Change requested");
						System.out.println(mon.getPath());
						checkForChange();
						//System.out.println("CHECK FOR CHANGE");
					}
					/**
					else if(fromClient.equals(COMPLETE))
					{
						actionInProgress = false;
					}
					*/
					else if(fromClient.equals(EXIT))
					{
						System.out.println("EXIT");
						out.close();
						in.close();
						running = false;
						socket.close();
						break;
					}
				} 
				catch (ClassNotFoundException e) 
				{
					e.printStackTrace();
				}
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	private synchronized void checkForChange() 
	{
		try 
		{
			actionInProgress = true;
			mon.checkForChange();
			System.out.println(change);
			out.writeObject(change);
			Object response = in.readObject();
			while(!((!change && response.equals(NOT_CHANGED)) || (change && response.equals(CHANGED))))
			{
				out.writeObject(change);
				response = in.readObject();
			}
			out.writeObject(CONFIRMED);
			if(in.readObject().equals(COMPLETE))
			{
				actionInProgress = false;
				change = false;
			}
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
		
	}

	private synchronized void receiveFile() {
		try 
		{
			actionInProgress = true;
			out.writeObject(WAITING_FOR_FILE);
			File file = (File) in.readObject();
			mon.copyFile(file);
			out.writeObject(RECEIVED);
			if(in.readObject().equals(COMPLETE))
			{
				//out.reset();
				actionInProgress = false;
			}
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}

	private synchronized void sendFile() {
		try 
		{
			actionInProgress = true;
			out.writeObject(WAITING_FOR_FILE_NAME);
			String fileName = (String) in.readObject();
			File file = new File(mon.getPath() + "\\" + fileName);
			System.out.println(file.getName());
			out.writeObject(file);
			System.out.println(file + " sent");
			if(in.readObject().equals(COMPLETE))
			{
				//out.reset();
				actionInProgress = false;
			}
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}

	private synchronized void sendFileList()
	{
		try 
		{
			actionInProgress = true;
			List<String> data = Arrays.asList(mon.getNames());
			out.writeObject(data);
			if(in.readObject().equals(COMPLETE))
			{
				//out.reset();
				actionInProgress = false;
			}
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {
		change = true;
	}
}