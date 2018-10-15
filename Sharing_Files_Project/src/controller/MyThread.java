package controller;

public class MyThread extends Thread {
	
	public MyThread()
	{
		this.setDaemon(true);
	}

	public void run(Monitor mon)
	{
		while(true)
		{
			try
			{
				mon.checkForChange();
				sleep(1000);
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
}
