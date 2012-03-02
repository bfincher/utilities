package com.fincher.thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class Thumbnail {
	
	private static Pattern jpgPattern;
//	private static final String jpgAtEndOfStrPattern = "\\.(?i)(jpg)$";
	
	private static String basePath;
	private static File baseThumbsDir;
	
 	private static final class FileFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			if (new File(dir, name).isDirectory()) {
				return (!name.equals("thumbs"));
			} else {
				if (jpgPattern.matcher(name).matches()) {
					File thumbsDir = getThumbsDir(dir);
					if (thumbsDir.exists() && thumbsDir.isDirectory()) {
						return !(new File(thumbsDir, name).exists());
					} else {
						return true;
					}
				}
			}
			
			return false;
		}
	}
	
	private static final FileFilter filter = new FileFilter();

	public static void main(String[] args) {
		try {
						
			
			jpgPattern = Pattern.compile("(.+\\.(?i)(jpg)$)");
			// any char 1 or more times
			// a .
			// case insensitive "jpg" at end of line
			
			File baseDir = new File(args[0]);
			basePath = baseDir.getPath().replace(baseDir.getName(), "");
			baseThumbsDir = new File(args[1]);
			
			processDir(baseDir);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static File getThumbsDir(File dir) {
		String path = dir.getPath().replace(basePath, "");
		File thumbsDir;
		if (path.length() > 0) {
			thumbsDir = new File(baseThumbsDir, path);
		} else {
			thumbsDir = baseThumbsDir;
		}
		
		if (!thumbsDir.exists()) {
			thumbsDir.mkdir();
		}
		return thumbsDir;
	}
	
	private static void processDir(File dir) throws IOException {
		File[] files = dir.listFiles(filter);
		
		File thumbsDir = null;
		
		for (File file: files) {
			if (file.isDirectory()) {
				processDir(file);
			} else {
				System.out.println("Processing " + file.getPath());
				BufferedImage image = ImageIO.read(file);
				BufferedImage thumbnail = Scalr.resize(image, 150);
				
				if (thumbsDir == null) {
					thumbsDir = getThumbsDir(dir);
				}
				
				File destFile = new File(thumbsDir, file.getName());
				
				ImageIO.write(thumbnail, "jpg", destFile); 
			}
		}
	}

}
