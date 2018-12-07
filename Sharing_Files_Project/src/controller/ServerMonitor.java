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

public class ServerMonitor extends Observable implements SharingSystem {
	
	private static ServerMonitor monitor_instance = null;
	private String path;
	private String[] fileNames = null;
	
	private ServerMonitor()
	{
		path = null;
	}

	public static synchronized ServerMonitor getInstance()
	{
		if(monitor_instance == null)
		{
			monitor_instance = new ServerMonitor();
		}
		return monitor_instance;
	}
	
	@Override
	public synchronized String[] getNames() 
	{
		File dir = new File(path);
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
		return fileNames;
	}

	@Override
	public synchronized boolean copyFile(File source) throws IOException 
	{
		System.out.println("\nCopying file from: " + source.getAbsolutePath());
		File dest = new File(path + "\\" + source.getName());
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
					input = new FileInputStream(source);
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
	public synchronized boolean checkForChange() 
	{
		String[] oldNames = new String[fileNames.length];
		for(int i = 0; i < fileNames.length; i++)
		{
			oldNames[i] = fileNames[i];
		}
		getNames();
		if(oldNames.length != fileNames.length)
		{
			System.out.println("Notifying observers...");
			setChanged();
			System.out.println("Setting changed...");
			notifyObservers();
			System.out.println("Notified");
			return true;
			
		}
		else
		{
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
			if(diff)
			{
				System.out.println("Notifying observers...");
				setChanged();
				System.out.println("Setting changed...");
				notifyObservers();
				System.out.println("Notified");
			}
			return diff;
		}
	}
	
	public String getPath() {
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
		getNames();
	}
	
	public void addObserver(Observer o)
	{
		super.addObserver(o);
		System.out.println("Observers:" + super.countObservers());
	}
}
