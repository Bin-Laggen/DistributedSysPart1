package controller;

import java.io.IOException;
import java.net.ServerSocket;

public class SharedServer {
	
	private static int portNumber;
	private static boolean listening;
	private static Monitor mon;
	
	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			System.err.println("Usage: java SharedServer <port number>");
			System.exit(1);
		}
		
		mon = Monitor.getInstance();
		mon.setPath("D:\\Dokumenty\\College\\Year 3\\Java - Distributed Systems Programming\\Server");
		mon.threadStart();

		portNumber = Integer.parseInt(args[0]);
		listening = true;

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) 
		{ 
			while (listening) 
			{
				new ServerThread(serverSocket.accept()).start();
			}
		} 
		catch (IOException e) 
		{
			System.err.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}
	}
}