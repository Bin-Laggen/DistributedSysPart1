package controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientThread extends Thread {
	
	private boolean execute;
	private Monitor mon;
	private Socket socket;
	private ObjectInputStream in;
	
	public ClientThread(Socket clientSocket, String name)
	{
		super(name);
		this.socket = clientSocket;
		try 
		{
			in = new ObjectInputStream(socket.getInputStream());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		execute = true;
		mon = Monitor.getInstance();
		super.start();
	}
	
	public void run()
	{
		while(execute)
		{
			try 
			{
				System.out.println("EXECUTING");
				if(in.available() > 0)
				{
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
	
	public void stopThread()
	{
		execute = false;
	}

}
