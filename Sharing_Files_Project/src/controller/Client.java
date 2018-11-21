package controller;

import java.io.*;
import java.net.*;

public class Client {
	
	public static void main(String[] args) throws IOException {

		if (args.length != 2) 
		{
			System.err.println(
					"Usage: java Client <host name> <port number>");
			System.exit(1);
		}

		String hostName = args[0];
		int portNumber = Integer.parseInt(args[1]);

		try (Socket kkSocket = new Socket(hostName, portNumber);) 
		{
			
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
}