package controller;

import java.io.File;
import java.io.IOException;

public interface SharingSystem {
	
	public String[] getNames();
	
	public boolean copyFile(String fileName, File dest) throws IOException;
	
	public boolean checkForChange();

}
