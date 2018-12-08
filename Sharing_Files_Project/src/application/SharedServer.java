package application;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import controller.RemoteInterface;
import controller.ServerMonitor;

public class SharedServer extends UnicastRemoteObject implements RemoteInterface {

	private static final long serialVersionUID = 1L;
	private static int portNumber;
	private static ServerMonitor mon;
	
	protected SharedServer() throws RemoteException {
		super();
	}
	
	public static void main(String[] args) throws IOException {

		if (args.length != 1) {
			System.err.println("Usage: java SharedServer <port number>");
			System.exit(1);
		}
		
		portNumber = Integer.parseInt(args[0]);

		mon = ServerMonitor.getInstance();
		mon.setPath("D:\\Dokumenty\\College\\Year 3\\Java - Distributed Systems Programming\\Server");
		
		Registry reg = LocateRegistry.createRegistry(portNumber);
		
		try 
		{
			reg.bind("Client", new SharedServer());
		} 
		catch (AlreadyBoundException e1) 
		{
			e1.printStackTrace();
		}
	}

	@Override
	public void connectClient() {
		
	}
}