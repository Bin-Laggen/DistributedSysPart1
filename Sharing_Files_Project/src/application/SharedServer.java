package application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import controller.ServerMonitor;
import controller.ServerThread;

public class SharedServer {
	
	private static int portNumber;
	private static boolean listening;
	private static ServerMonitor mon;
	
	public static void main(String[] args) throws IOException {

		int counter = 0;
		if (args.length != 1) {
			System.err.println("Usage: java SharedServer <port number>");
			System.exit(1);
		}
		
		mon = ServerMonitor.getInstance();
		mon.setPath("D:\\Dokumenty\\College\\Year 3\\Java - Distributed Systems Programming\\Server");

		portNumber = Integer.parseInt(args[0]);
		listening = true;

		try (ServerSocket serverSocket = new ServerSocket(portNumber)) 
		{ 
			while (listening) 
			{
				Socket socket = serverSocket.accept();
				counter++;
				System.out.println("Connecting new client...");
				new ServerThread("Server Thread" + counter, socket).start();
			}
		} 
		catch (IOException e) 
		{
			System.err.println("Could not listen on port " + portNumber);
			System.exit(-1);
		}
	}
}