package controller;

import java.io.File;
import java.io.IOException;

public interface SharingSystem {
	
	public String[] getNames();
	
	public boolean copyFile(File source) throws IOException;
	
	public boolean checkForChange();

}
