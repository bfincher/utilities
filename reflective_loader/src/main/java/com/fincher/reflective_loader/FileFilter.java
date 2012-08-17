package com.fincher.reflective_loader;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/** A java.io.FileFilter */
public class FileFilter implements java.io.FileFilter {
	
	private Set<String> fileExtensions = new HashSet<String>();
	
	private boolean acceptDirectories = false; 
	
	/** Adds a file extension that will be accepted by this filter
	 * 
	 * @param extension
	 */
	public void addFileExtension(String extension) {
		if (extension.startsWith("."))
			extension = extension.substring(1);
		
		fileExtensions.add(extension);
	}
	
	/** Should this filter accept directories
	 * 
	 * @param val
	 */
	public void setAcceptDirectories(boolean val) {
		acceptDirectories = val;
	}
	
	@Override
	public boolean accept(File f) {
		if (f.isDirectory() && acceptDirectories)
			return true;
		
		if (fileExtensions.isEmpty())
			return true;
		
		String fileExtension;
		
		StringTokenizer tokenizer = new StringTokenizer(f.getName(), ".");
		int numTokens = tokenizer.countTokens();
		
		for (int i = 0; i < numTokens - 1; i++)
			tokenizer.nextToken();
		
		fileExtension = tokenizer.nextToken();
		
		return fileExtensions.contains(fileExtension);
	}

}
