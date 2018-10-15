package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;

public class Monitor extends Observable implements SharingSystem, Runnable {
	
	private static Monitor monitor_instance = null;
	private final String PATH = "D:\\Dokumenty\\College\\Year 3\\Java - Distributed Systems Programming\\Server\\";
	private String[] fileNames = null;
	private Thread t;
	
	private Monitor()
	{
		getNames();
		t = new Thread(this, "Check Change Thread");
		t.start();
	}

	public static synchronized Monitor getInstance()
	{
		if(monitor_instance == null)
		{
			monitor_instance = new Monitor();
		}
		return monitor_instance;
	}
	
	@Override
	public String[] getNames() 
	{
		System.out.println("Getting names...");
		File dir = new File(PATH);
		if(dir.isDirectory())
		{
			fileNames = dir.list(new FilenameFilter() {
			    @Override
			    public boolean accept(File dir, String name) 
			    {
			        return name.endsWith(".mp3");
			    } 
			});
		}
		System.out.println("Names got...");
		return fileNames;
	}

	@Override
	public boolean copyFile(String fileName, File dest) throws IOException {
		System.out.println("\nCopying file from: " + PATH + fileName);
		File file = new File(PATH + fileName);
		dest = new File(dest + "\\" + fileName);
		System.out.println("To: " + dest.getAbsolutePath());
		InputStream input = null;
		OutputStream output = null;
		if(!dest.exists())
		{
			dest.createNewFile();
			try 
			{
				try 
				{
					input = new FileInputStream(file);
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}

				try 
				{
					output = new FileOutputStream(dest);
				} 
				catch (FileNotFoundException e) 
				{
					e.printStackTrace();
				}
				byte[] buf = new byte[1024];
				int bytesRead;

				while ((bytesRead = input.read(buf)) > 0) 
				{
					output.write(buf, 0, bytesRead);
				}
			} 
			finally 
			{
				input.close();
				output.close();
				System.out.println("SUCCESS");
			}
			return true;
		}
		else
		{
			System.out.println("FILE ALREADY EXISTS");
			return false;
		}
	}

	@Override
	public synchronized boolean checkForChange() {
		System.out.println("CHECKING...");
		String[] oldNames = new String[fileNames.length];
		for(int i = 0; i < fileNames.length; i++)
		{
			oldNames[i] = fileNames[i];
		}
		getNames();
		boolean diff = false;
		int j = 0;
		while(!diff && j < fileNames.length)
		{
			if(!oldNames[j].equals(fileNames[j]))
			{
				diff = true;
			}
			j++;
		}
		System.out.println("...CHECKED\n");
		return diff;
	}
	
	public void addObserver(Observer o)
	{
		super.addObserver(o);
	}

	@Override
	public void run()
	{
		while(true)
		{
			if(checkForChange())
			{
				System.out.println("Notifying observers...");
				setChanged();
				System.out.println("Setting changed...");
				notifyObservers();
				System.out.println("Notified");
			}
			try 
			{
				Thread.yield();
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
