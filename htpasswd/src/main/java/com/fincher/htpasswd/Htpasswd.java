package com.fincher.htpasswd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;

import com.fincher.NaturalOrderMap;

public class Htpasswd {
	
	@SuppressWarnings("restriction")
	public static String cryptPassword( String password ) throws IOException {
		try {
			return "{SHA}" + new sun.misc.BASE64Encoder().encode(java.security.MessageDigest.getInstance("SHA1").digest(password.getBytes()));
		} catch (NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
	
	public static boolean verifyPassword(String password, String encodedPassword) throws IOException {
		if (password == null || password.length() == 0) {
            if (encodedPassword == null || encodedPassword.length() == 0) {
                return true;
            }            
            return false;
        }
            
        if (encodedPassword == null || encodedPassword.length() == 0) {
            return false;
        }
        
        return encodedPassword.equals(cryptPassword(password));
	}
	
	public static Map<String, String> readPasswordFile(String fileName) throws IOException {
		NaturalOrderMap<String, String> map = new NaturalOrderMap<String, String>();
		
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(fileName));
			String str;
		
			while ((str = input.readLine()) != null) {
				int idx = str.indexOf(':');
				String userName = str.substring(0, idx);
				String pw = str.substring(idx + 1);
				map.put(userName, pw);
			}
		
			return map;
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}
	
	public static void writePasswordFile (String fileName, Map<String, String> passwords) throws IOException {
		BufferedWriter output = null;
		
		try {
			output = new BufferedWriter(new FileWriter(fileName));
		
			for (Iterator<String> iterator = passwords.keySet().iterator(); iterator.hasNext() ; ) {
				String userId = iterator.next();
				output.write(userId + ":" + passwords.get(userId));
				if (iterator.hasNext()) {
					output.newLine();
				}
			}
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

}
