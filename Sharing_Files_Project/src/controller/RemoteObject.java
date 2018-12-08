package controller;

public class RemoteObject implements RemoteInterface {

	@Override
	public void connectClient() {
		System.out.println("New client connected");
	}

}
