package controller;

import java.util.HashMap;

public class Protocol {
	
	private static Protocol protocol_instance = null;
	private HashMap<String, Runnable> actions;
	
	private Protocol()
	{
		actions = new HashMap<String, Runnable>();
		actions.put("METHOD 1", this::method);
	}
	
	public static Protocol getInstance()
	{
		if(protocol_instance == null)
		{
			protocol_instance = new Protocol();
		}
		return protocol_instance;
	}
	
	public void processCommand(String i)
	{
		actions.get(i).run();
	}

	private void method()
	{
		System.out.println("Input == 1: Method called");
	}
}
