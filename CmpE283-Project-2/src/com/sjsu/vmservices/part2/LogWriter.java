package com.sjsu.vmservices.part2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class is utilized by VMPerformanceCollector Class to write performance metrics into log file.
 */
public class LogWriter {
	public static LogWriter obj;
	public static LogWriter archiveobj;
	BufferedWriter writer = null;
	BufferedWriter archivewriter = null;
	File logFile = new File("vmStatistics.log");
	File logArchiveFile = new File("archivestats.log");

	private LogWriter() throws IOException {
		writer = new BufferedWriter(new FileWriter(logFile, false));
		archivewriter = new BufferedWriter(new FileWriter(logArchiveFile, true));
	}

	public static LogWriter getInstance() throws IOException {
		if (obj == null) {
			obj = new LogWriter();
		}
		return obj;
	}

	public static LogWriter getArchiveInstance() throws IOException {
		if (archiveobj == null) {
			archiveobj = new LogWriter();
		}
		return archiveobj;
	}

	/**
	 * Write to file.
	 * @param s
	 * @param objname
	 */
	public void write(String s, String objname) {
		try {
			if (objname.equals("writer")) {
				writer.flush();
				writer.write(s);
			} else if (objname.equals("archivewriter")) {
				archivewriter.flush();
				archivewriter.write(s);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
