package controller;

import java.net.*;
import java.io.*;

public class ServerThread extends Thread {
	
	private Socket socket = null;

	public ServerThread(Socket socket) 
	{
		super("ServerThread");
		this.socket = socket;
	}

	public void run() {

		//DO SOMETHING
		
		try
		{
			socket.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}