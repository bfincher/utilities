package com.fincher;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.util.List;

/**
 * Converts text to HTML
 * @goal genhtml
 */
public class TextToHtml extends AbstractMojo
{
	/**
	 * The files to convert to html
	 * @parameter expression="${genhtml.fileset}"
	 */
	private FileSet[] filesets;

	/**
	 * The output directory
	 * @parameter expression="${genhtml.output.dir}" default-value="target/html"
	 */
	private String outputDir;

	public void execute() throws MojoExecutionException {
		try {
			for (FileSet fs: filesets) {
				File directory = new File(fs.getDirectory());
				@SuppressWarnings("unchecked")
				String includes = toString(fs.getIncludes()); 
				@SuppressWarnings("unchecked")
				String excludes = toString(fs.getExcludes()); 
				for (Object o: FileUtils.getFiles(directory, includes, excludes)) {
					process((File)o);
				}
			}
		} catch (IOException ioe) {
			throw new MojoExecutionException(ioe.getMessage(), ioe);
		}
	}

	public void process(File file) throws IOException {

		getLog().info("Processing " + file);
		BufferedReader input = null;
		BufferedWriter output = null;

		try {
			input = new BufferedReader(new FileReader(file));
			new File(outputDir).mkdirs();
			output = new BufferedWriter(new FileWriter(new File(outputDir, file.getName() + ".html")));

			output.write("<html><body><pre>\n");

			String str;
			while ((str = input.readLine()) != null) {
				str = str.replace("<", "&lt");
				output.write(str + "\n");
			}

			output.write("</pre></body></html>");
		} finally {
			if (input != null) {
				input.close();
			}

			if (output != null) {
				output.close();
			}
		}
	}

	private String toString (List<String> list) {
		StringBuilder sb = new StringBuilder();
		for (String str: list) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(str);
		}
		return sb.toString();
	}
}

