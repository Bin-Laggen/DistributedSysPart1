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
	private final String EXIT = "EXIT";
	private final String WAITING_FOR_FILE_NAME = "2_1";
	private final String WAITING_FOR_FILE = "3_1";
	
	private Monitor mon;

	public ServerThread(Socket socket) 
	{
		super("ServerThread");
		this.socket = socket;
		mon = Monitor.getInstance();
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
		
		while(true)
		{
			try 
			{
				Object fromClient;
				try 
				{
					fromClient = in.readObject();
					System.out.println(fromClient);
					if(fromClient.equals(GET_FILE_LIST))
					{
						sendFileList();
						System.out.println("RECEIVE FILE LIST");
					}
					else if(fromClient.equals(DOWNLOAD_FILE))
					{
						sendFile();
						System.out.println("DOWNLOAD FILE");
					}
					else if(fromClient.equals(UPLOAD_FILE))
					{
						receiveFile();
						System.out.println("UPLOAD FILE");
					}
					else if(fromClient.equals(CHECK_FOR_CHANGE))
					{
						checkForChange();
						System.out.println("CHECK FOR CHANGE");
					}
					else if(fromClient.equals(EXIT))
					{
						System.out.println("EXIT");
						out.close();
						in.close();
						socket.close();
						break;
					}
				} 
				catch (ClassNotFoundException e) 
				{
					//e.printStackTrace();
				}
			} 
			catch (IOException e) 
			{
				//e.printStackTrace();
			}
		}
	}
	
	private void checkForChange() {
		try 
		{
			out.writeObject(mon.checkForChange());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}

	private void receiveFile() {
		try 
		{
			out.writeObject(WAITING_FOR_FILE);
			File file = (File) in.readObject();
			boolean success = mon.copyFile(file);
			out.writeObject(success);
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}

	private void sendFile() {
		try 
		{
			out.writeObject(WAITING_FOR_FILE_NAME);
			String fileName = (String) in.readObject();
			File file = new File(mon.getPath() + "\\" + fileName);
			out.writeObject(file);
		} 
		catch (IOException | ClassNotFoundException e) 
		{
			e.printStackTrace();
		}
	}

	private void sendFileList()
	{
		try 
		{
			List<String> data = Arrays.asList(mon.getNames());
			out.writeObject(data);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void update(Observable o, Object arg) {
		try 
		{
			System.out.println("UPDATING");
			out.writeObject(true);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}