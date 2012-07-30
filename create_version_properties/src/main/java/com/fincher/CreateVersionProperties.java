package com.fincher;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/** Maven plugin to create version.properties
 * 
 * @goal versionprop
 *
 */
public class CreateVersionProperties extends AbstractMojo {
	
	/** The version number
	 * @parameter expression="${versionprop.version}"
	 */
	private String version;
	
	/** The destination version file
	 * @parameter expression="${versionprop.destFile}" default-value="${pom.version}/classes/version.properties"
	 */
	private String destFile;
	
	@Override
	public void execute() throws MojoExecutionException {
		if (version.endsWith("-SNAPSHOT")) {
			version = version.replace("-SNAPSHOT", "");
		}
		
		
		File f = new File(destFile);
		try {
			PrintStream out = new PrintStream(f);
			out.println("version=" + version);
			out.close();
		} catch (IOException ioe) {
			throw new MojoExecutionException(ioe.getMessage(), ioe);
		}
	}

}
