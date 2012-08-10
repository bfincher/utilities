package com.fincher.htpasswd;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class HtpasswdServlet extends HttpServlet {
	
	private static final String TITLE = "Htpasswd Editor";		
	
	private enum FormEnum {
		none,
		selectFile,
		performDelete,
		performChangePassword,
		userAction,
		addUser,
		login,
		logout,
	}
	
	private enum OkCancelEnum {
		Ok,
		Cancel
	}		
	
	private static final String FILE_CONTENTS_ATTR_NAME = "fileContents";
	private static final String FILE_NAME_ATTR_NAME = "fileName";
	private static final String LOGGED_IN_AS_USER = "loggedInAsUser";
	
	private static AtomicInteger nextInstanceNumber = new AtomicInteger(1);
	
	private final Map<String, AbstractHtpasswd> htpasswdMap = new HashMap<String, AbstractHtpasswd>();
	
	private List<String> fileNames;
//	private String fileName = null;
//	private Map<String, String> fileContents = null;
	private boolean debugMode = false;
	private int instanceNumber = nextInstanceNumber.getAndIncrement();		
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		debugMode = new Boolean(config.getInitParameter("debugMode"));
		
		fileNames = new LinkedList<String>();
		fileNames.add(config.getInitParameter("mainHtpasswdFile"));
		
		String passwdFilesStr = config.getInitParameter("passwdFiles");
		if (passwdFilesStr != null) {
			StringTokenizer st = new StringTokenizer(passwdFilesStr, ",");
			while (st.hasMoreTokens()) {
				fileNames.add(st.nextToken());
			}
		}
	}

	@Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
		response.setContentType("text/html");

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<head>");
        
        out.println("<title>" + TITLE + "</title>");
        out.println("</head>");
        out.println("<body bgcolor=\"white\">");                                
        
        out.println("<center>");
        out.println("<h3>" + TITLE + "</h3>");
        out.println("</center>");
        
        HttpSession session = request.getSession(true);
        
        if (debugMode) {
        	out.println("instance number = " + instanceNumber + "<br>");
        	out.println("session is new " + session.isNew() + "<br>");
        	out.println("File contents = " + getFileContents(session) + "<br>");
        	@SuppressWarnings("unchecked")
			Enumeration<String> e = request.getParameterNames();
        	while (e.hasMoreElements()) {
        		String name = e.nextElement();
        		out.println(name + " " + request.getParameter(name) + "<br>");
        	}
        	out.println("<br>");
        }        
        
        String formName = request.getParameter("formName");
        FormEnum form;
        if (formName == null) {
        	form = FormEnum.none;
        } else {
        	form = FormEnum.valueOf(formName);
        }
                
        if (!isLoggedIn(session)) {
        	if (form == FormEnum.login) {
        		login(request, out, session);
        	} else {
        		showLogin(out);
        	}
        	return;
        }
        
		Map<String, String> fileContents = getFileContents(session);
		
		out.println("<center>");
        out.println("Logged in as user " + getLoggedInAsUser(session) + "<br>");
		out.println("<H1 align=center>");
        out.println("<form action=\"HtpasswdServlet\" method=POST>");
		out.println("<input type=hidden name=formName value=\"" + FormEnum.logout + "\">");
		out.println("<input type=submit value=\"Log Out\">");
		out.println("</form>"); 
        out.println("</H1>");                
        
        if (debugMode) {
        	out.println("<P>");      
        	out.println("form = " + form + "<br>");
        }
        
        switch (form) {       
        case none:
        	if (fileContents == null) {
        		showFileSelection(request, out);
        	} else {
        		showFileContents(request, out);
        	}
        	break;
        	
        case selectFile:
        	String fileName = request.getParameter(FILE_NAME_ATTR_NAME);
            if (fileName == null) {
            	out.println("<P> You must select a file name");
            } else {
            	session.setAttribute(FILE_NAME_ATTR_NAME, fileName);
            	fileContents = getHtpasswd(fileName).readPasswordFile(fileName);
            	setFileContents(session, fileContents);
            	if (debugMode) {
            		out.println("fileContents = " + fileContents + "<br>");
            	}
            	showFileContents(request, out);
            }        	 
            break;            
        	
        case performDelete:
        	performDelete(request, out);
        	break;
        	
        case performChangePassword:
        	performChangePassword(request, out);
        	break;        	
        	
        case userAction:
        	userAction(request, out);
        	break;
        	
        case addUser:
        	performAddUser(request, out);
        	break;
        	
        case login:
        	login(request, out, session);
        	break;
        	
        case logout:
        	request.getSession().invalidate();
        	showLogin(out);
        	break;
        }                
        
        out.println("</center>");
    	out.println("</body>");
    	out.println("</html>");
    }        
    

    @Override
    public void doPost(HttpServletRequest request,
                      HttpServletResponse response)
        throws IOException, ServletException
    {
        doGet(request, response);
    }
    
    @SuppressWarnings("unchecked")
	private final Map<String, String> getFileContents(HttpSession session) {
    	return (Map<String, String>)session.getAttribute(FILE_CONTENTS_ATTR_NAME);
    }
    
    private final void setFileContents(HttpSession session, Map<String, String> fileContents) {
    	session.setAttribute(FILE_CONTENTS_ATTR_NAME, fileContents);
    }
    
    private void showFileContents(HttpServletRequest request, PrintWriter out) throws IOException {
    	showFileSelection(request, out);
    	
    	Map<String, String> fileContents = getFileContents(request.getSession());
    	
//    	out.println("<table width=60% border=1 cellpadding=10>");
    	out.println("<table border=1>");
    	out.println("<th> User Name </th>");
    	out.println("<th> Password </th>");
    	out.println("<th> Actions </th>");
    
    	for (String userName: fileContents.keySet()) {
    		out.println("<tr>");
    		out.println("<td> " + userName  + "</td>");
    		out.println("<td> " + fileContents.get(userName) + "</td>");
    		out.println();
    		out.println("<td>");
    		out.println("<form action=\"HtpasswdServlet\" method=POST>");
    		out.println("<input type=hidden name=formName value=\"" + FormEnum.userAction + "\">");
    		out.println("<input type=hidden name=userName value=\"" + userName + "\">");
    		out.println("<input type=submit name=submit value=\"Delete\">");
    		out.println("<input type=submit name=submit value=\"Change Password\">");
    		out.println("</form>");    		
    		out.println();
    		out.println("</td>");
    		out.println("</tr>");
    		out.println();
    	}
    	out.println("</table>");    	
    	out.println("<p> <br>");
    	out.println("Add User <br>");
    	out.println("<form action=\"HtpasswdServlet\" method=POST>");
    	out.println("<table>");
    	out.println("<th> User Name </th>");
    	out.println("<th> New Password  </th>");
    	out.println("<th> Veirfy Password </th>");
    	out.println("<tr>");
    	out.println("<td> <input type=text name=userName> </td>");
    	out.println("<td> <input type=password name=password1> </td>");
    	out.println("<td> <input type=password name=password2> </td>");
    	out.println("</tr>");
    	out.println("</table>");
    	out.println("<input type=hidden name=formName value=\"" + FormEnum.addUser + "\">");
    	out.println("<input type=submit value=\"Add User\">");
    	out.println("</form>");    	    	    	
    }
    
    private void showFileSelection(HttpServletRequest request, PrintWriter out) throws IOException {    	    	
    	
    	String selectedFileName = (String)request.getSession().getAttribute(FILE_NAME_ATTR_NAME);
    	
    	out.print("<form action=\"HtpasswdServlet\" ");
        out.println("method=POST>");
        out.println("<SELECT NAME=fileName>");
        
        if (selectedFileName == null) {
        	out.println("<OPTION SELECTED VALUE=\"\"></OPTION>");
        }
        
        for (String fileName: fileNames) {
        	if (selectedFileName != null && selectedFileName.equals(fileName)) {
        		out.println("<OPTION SELECTED>" + fileName);
        	} else {
        		out.println("<OPTION>" + fileName);
        	}
        }        
        out.println("</SELECT>");
                                
        out.println("<input type=hidden name=formName value=\"" + FormEnum.selectFile + "\">");
        out.println("<input type=submit value=Select>");
        out.println("</form>");
        out.println("<p> <br>");                
    }
    
    private void showDeleteUser(PrintWriter out, String userName) throws IOException {
    	out.println("Are you sure you Want to delete the user " + userName);
    	out.println("<form action=\"HtpasswdServlet\" method=POST>");
    	out.println("<input type=hidden name=formName value=\"" + FormEnum.performDelete + "\">");
    	out.println("<input type=hidden name=userName value=\"" + userName + "\">");    	
    	out.println("<input type=submit name=submit value=" + OkCancelEnum.Ok + ">");
    	out.println("<input type=submit name=submit value=" + OkCancelEnum.Cancel + ">");
    	out.println("</form>");
    }
    
    private void showChangePassword(PrintWriter out, String userName) throws IOException {
    	out.println("<form action=\"HtpasswdServlet\" method=POST>");
    	out.println("<table>");
    	out.println("<th> User Name </th>");
    	out.println("<th> New Password  </th>");
    	out.println("<th> Veirfy Password </th>");
    	out.println("<tr>");
    	out.println("<td> " + userName + "</td>");
    	out.println("<td> <input type=password name=password1> </td>");
    	out.println("<td> <input type=password name=password2> </td>");
    	out.println("</tr>");
    	out.println("</table>");
    	out.println("<input type=hidden name=formName value=\"" + FormEnum.performChangePassword + "\">");
    	out.println("<input type=hidden name=userName value=\"" + userName + "\">");    	
    	out.println("<input type=submit name=submit value=" + OkCancelEnum.Ok + ">");
    	out.println("<input type=submit name=submit value=" + OkCancelEnum.Cancel + ">");
    	out.println("</form>");
    }
    
    private void performDelete(HttpServletRequest request, PrintWriter out) throws IOException {
    	String submit = request.getParameter("submit");
    	OkCancelEnum okCancel = OkCancelEnum.valueOf(submit);
    	
    	switch (okCancel) {
    	case Ok:
    		String userName = request.getParameter("userName");
    		HttpSession session = request.getSession();
    		Map<String, String> fileContents = getFileContents(session);
    		fileContents.remove(userName);
    		String fileName = (String)session.getAttribute(FILE_NAME_ATTR_NAME);
    		getHtpasswd(fileName).writePasswordFile(fileName, fileContents);
    		
    		showConfirmation(out, "User " + userName + " deleted");    		
    		break;
    		
    	case Cancel:
    		showConfirmation(out, "Action canceled");
    		break;
    	}    	    	
    }
    
    private void performChangePassword(HttpServletRequest request, PrintWriter out) throws IOException {
    	String submit = request.getParameter("submit");
    	OkCancelEnum okCancel = OkCancelEnum.valueOf(submit);
    	
    	HttpSession session = request.getSession();
    	String fileName = (String)session.getAttribute(FILE_NAME_ATTR_NAME);
    	
    	if (debugMode) {
    		out.println("inside performChangePassword.  okCancel = " + okCancel + "<br>");
    	}
    	
    	switch (okCancel) {
    	case Ok:
    		
    		String userName = request.getParameter("userName");
    		String password1 = request.getParameter("password1");
    		String password2 = request.getParameter("password2");
    		AbstractHtpasswd htpasswd = getHtpasswd(fileName);
    		
    		if (password1.equals(password2)) {    			
    			String crypted = htpasswd.cryptPassword(userName, password1);
    			    			
        		Map<String, String> fileContents = getFileContents(session);
        		    			
    			fileContents.put(userName, crypted);
    			htpasswd.writePasswordFile(fileName, fileContents);
    			
    			showConfirmation(out, "User " + userName + " password changed");    			
    		} else {
    			showConfirmation(out, "WARNING, the passwords did not match.  No action taken");    			
    		}
    		break;
    		
    	case Cancel:
    		showConfirmation(out, "Action canceled");
    		break;
    	}    	    	    
    }
    
    private void performAddUser(HttpServletRequest request, PrintWriter out) throws IOException {
    	String userName = request.getParameter("userName");    	
    	
    	HttpSession session = request.getSession();
    	String fileName = (String)session.getAttribute(FILE_NAME_ATTR_NAME);
    	AbstractHtpasswd htpasswd = getHtpasswd(fileName);
		Map<String, String> fileContents = getFileContents(session);
    	
    	if (fileContents.containsKey(userName)) {
    		out.println("<font size=5 color=RED>");
			out.println("User " + userName + " already exists.  No action taken");
			out.println("</font>");
			out.println("<p>");
    	} else {    	    	
    		String password1 = request.getParameter("password1");
    		String password2 = request.getParameter("password2");
		
    		if (password1.equals(password2)) {    			
    			String crypted = htpasswd.cryptPassword(userName, password1);
			
    			fileContents.put(userName, crypted);
    			htpasswd.writePasswordFile(fileName, fileContents);
			
    			showConfirmation(out, "User " + userName + " added successfully");    			
    		} else {
    			showConfirmation(out, "WARNING, the passwords did not match.  No action taken");    			
    		}
    	}		
    }
    
    private void userAction(HttpServletRequest request, PrintWriter out) throws IOException {
    	String submit = request.getParameter("submit");
    	String userName = request.getParameter("userName");
    	
    	if (submit.equals("Delete")) {
    		showDeleteUser(out, userName);
    	} else if (submit.equals("Change Password")) {
    		showChangePassword(out, userName);
    	} else {
    		throw new Error("Unknown submit command: " + submit);
    	}    	
    }
    
    private void showConfirmation(PrintWriter out, String displayString) {
        out.println("<font size=5 color=RED>");
		out.println(displayString);
		out.println("</font>");
		out.println("<p>");
		out.println("<form action=\"HtpasswdServlet\" method=POST>");
		out.println("<input type=hidden name=formName value=" + FormEnum.none + ">");
		out.println("<input type=submit value=Ok>");
		out.println("</form>");
        out.println("</center>");
        out.println("</body>");
        out.println("</html>");
    }
    
    private void showLogin(PrintWriter out) {
    	out.println("<h3>Please Login</h3>");
    	out.println("<form action=\"HtpasswdServlet\" method=POST>");
		out.println("<input type=hidden name=formName value=\"" + FormEnum.login + "\">");
		out.println("User Name <input type=text name=userName>");
		out.println("Password <input type=password name=password>");
		out.println("<input type=submit value=\"Ok\">");
		out.println("</form>"); 
    }
    
    private void login(HttpServletRequest request, PrintWriter out, HttpSession session) throws IOException {    	
    	String userName = request.getParameter("userName");
    	String password = request.getParameter("password");
    	String fileName = fileNames.get(0);
    	AbstractHtpasswd htpasswd = getHtpasswd(fileName);
    	Map<String, String> pwFile = htpasswd.readPasswordFile(fileName);
    	
    	boolean error = false;
    	
    	String encodedPw = pwFile.get(userName);
    	if (encodedPw == null) { 
    		error = true;
    	} else {
    		error = !htpasswd.verifyPassword(userName, password, encodedPw);
    	}
    	
    	if (error) {
    		session.invalidate();
    		out.println("<font size=5 color=RED>");
    		out.println("Invalid username / password");
    		out.println("</font>");
    		out.println("<br>");
    		showLogin(out);
    	} else {
    		setLoggedInAsUser(session, userName);
    		showConfirmation(out, "Login successfull");
    	}
    }
    
    private static void setLoggedInAsUser(HttpSession session, String userName) {
    	session.setAttribute(LOGGED_IN_AS_USER, String.valueOf(userName));
    }
    
    private static boolean isLoggedIn(HttpSession session) {
    	return session.getAttribute(LOGGED_IN_AS_USER) != null;    	
    }
    
    private static String getLoggedInAsUser(HttpSession session) {
    	return (String)session.getAttribute(LOGGED_IN_AS_USER);
    }
    
    private AbstractHtpasswd getHtpasswd(String fileName) throws IOException {
    	AbstractHtpasswd htpasswd = htpasswdMap.get(fileName);
    	if (htpasswd == null) {
    		BufferedReader input = null;
    		try {
    			input = new BufferedReader(new FileReader(fileName));
    			String str = input.readLine();
    			if (str.indexOf("{SHA}") == -1) {
    				StringTokenizer st = new StringTokenizer(str, ":");
    				st.nextToken();
    				String realm = st.nextToken();
    				htpasswd = new HtpasswdMd5(realm);
    			} else {
    				htpasswd = new HtpasswdSha();
    			}
    			
    			htpasswdMap.put(fileName, htpasswd);
    		} finally {
    			if (input != null) {
    				input.close();
    			}
    		}
    	}
    	
    	return htpasswd;
    }
    
}
