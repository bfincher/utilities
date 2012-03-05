package com.fincher.thumbnail;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public class Thumbnail {
	
	// any char 1 or more times
	// a .
	// case insensitive "jpg" at end of line
	private static Pattern imagePattern = Pattern.compile("(.+(\\.(?i)(jpg|png|gif|bmp|tif))$)");
	
	private static Pattern imageExtensionPattern = Pattern.compile("(\\.(?i)(jpg|png|gif|bmp|tif))$");
	
	private static String basePath;
	private static File baseThumbsDir;
	
 	private static final class FileFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
			if (new File(dir, name).isDirectory()) {
				return (!name.equals("thumbs"));
			} else {
				if (imagePattern.matcher(name).matches()) {
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
			
//			String[] strs = {
//				"file1.jpg",
//				"file2.tif",
//				"filejpg",
//				"filetif",
//				"file3.dat"
//			};
//			
//			for (String str: strs) {
//				boolean match = imagePattern.matcher(str).matches();
//				if (match) {
//					System.out.println(str + " " + match + " " + getFileExtension(str));
//				} else {
//					System.out.println(str + " " + match);
//				}
//			}
//			
//			System.exit(0);
			
			File baseDir = new File(args[0]);
			basePath = baseDir.getPath().replace(baseDir.getName(), "");
			baseThumbsDir = new File(args[1]);
			
			renameLargeFileNames(baseDir);
			processDir(baseDir);
			deleteObsoleteThumbs(new File(basePath), baseThumbsDir); 
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void renameLargeFileNames(File dir) {
	    int count = 1;
	    for (File file: dir.listFiles(filter)) {
	    	if (file.isDirectory()) {
			renameLargeFileNames(file);
		} else if (file.getName().length() > 15) {
			File newFile;
			do {
				StringBuilder newFileName = new StringBuilder("IMG_");
				int zeroes = 4 - (int) (Math.log(count) / Math.log(10)) - 1;
				for (int i = 0; i < zeroes; i++) {
					newFileName.append(0);
				}
				newFileName.append(count++);
				
				String extension = getFileExtension(file.getName());
				
				newFileName.append(extension);
				newFile = new File(dir, newFileName.toString());
			} while (newFile.exists());
			
			System.out.println("Renaming " + file.getPath() + " to " + newFile.getPath());
			file.renameTo(newFile);
		}
	    }
	}
	
	private static void deleteObsoleteThumbs(File picDir, File thumbDir) {
	    for (File file: thumbDir.listFiles()) {
	    	File picFile = new File(picDir, file.getName());
		if (picFile.exists()) {
			if (file.isDirectory()) {
				deleteObsoleteThumbs(picFile, file);
			}
		} else {
			System.out.println("Deleting " + file.getPath());
		    if (file.isDirectory()) {
		    	deleteDir(file);
		    } else {
		      file.delete();
		    }
		}
	    }
	}

	private static void deleteDir(File dir) {
		for (File file: dir.listFiles()) {
		    if (file.isDirectory()) {
		        deleteDir(file);
		    } else {
		        file.delete();
		    }
		}
		dir.delete();
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
			thumbsDir.mkdirs();
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
				
				String extension = getFileExtension(file.getName()).substring(1);
				
				ImageIO.write(thumbnail, extension, destFile); 
			}
		}
	}
	
	private static String getFileExtension(String fileName) {
		Matcher matcher = imageExtensionPattern.matcher(fileName);
		matcher.find();
		return matcher.group();
	}

}
