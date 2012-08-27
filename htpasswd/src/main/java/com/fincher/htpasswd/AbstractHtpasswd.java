package com.fincher.htpasswd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import com.fincher.NaturalOrderMap;

public abstract class AbstractHtpasswd {

	private final AlgorithmEnum algorithm;	
	private final MessageDigest messageDigest;

	public AbstractHtpasswd(AlgorithmEnum algorithm) {
		this.algorithm = algorithm;

		try {
			messageDigest = MessageDigest.getInstance(algorithm.toString());
		} catch (NoSuchAlgorithmException e) {
			throw new Error(e);
		}
	}

	public abstract String cryptPassword(String username, String password);
	
	protected abstract String buildPasswordFileEntry(String userName, String password);		
	
	protected abstract String parsePasswordFileEntryPassword(String passwordFileEntry);
	
	protected String parsePasswordFileEntryUserName(String passwordFileEntry) {
		StringTokenizer st = new StringTokenizer(passwordFileEntry, ":");
		return st.nextToken();
	}

	public boolean verifyPassword(String username, String password, String encodedPassword) {
		if (password == null || password.length() == 0) {
            if (encodedPassword == null || encodedPassword.length() == 0) {
                return true;
            }            
            return false;
        }
            
        if (encodedPassword == null || encodedPassword.length() == 0) {
            return false;
        }
        
        return encodedPassword.equals(cryptPassword(username, password));
	}	

	public Map<String, String> readPasswordFile(String fileName) throws IOException {
		return readPasswordFile(new File(fileName));
	}
	
	public Map<String, String> readPasswordFile(File file) throws IOException {
		NaturalOrderMap<String, String> map = new NaturalOrderMap<String, String>();

		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader(file));
			String str;

			while ((str = input.readLine()) != null) {
				String userName = parsePasswordFileEntryUserName(str);
				String pw = parsePasswordFileEntryPassword(str);
				map.put(userName, pw);
			}

			return map;
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}
	
	public void writePasswordFile (String fileName, Map<String, String> passwords) throws IOException {
		writePasswordFile(new File(fileName), passwords);
	}

	public void writePasswordFile (File file, Map<String, String> passwords) throws IOException {
		BufferedWriter output = null;
		
		try {
			output = new BufferedWriter(new FileWriter(file));
		
			for (Iterator<String> iterator = passwords.keySet().iterator(); iterator.hasNext() ; ) {
				String userId = iterator.next();
				output.write(buildPasswordFileEntry(userId, passwords.get(userId)));
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
	
	public AlgorithmEnum getAlgorithm() {
		return algorithm;
	}

	public MessageDigest getMessageDigest() {
		return messageDigest;
	}

}
