/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.logging;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * Description: logfile handling
 * 
 * @author Sabina Jeger
 */
public class LogFileParser {

	private static final Logger log = Tracing.createLoggerFor(LogFileParser.class);
	
	private static String logfilepathBase;
	private static String filename;
	private static final int linecount = 3; // we always get 4 lines
	private static final String matchError = ".*" + Level.ERROR + ".*";
	private static final String matchWarn = ".*" + Level.WARN + ".*" ;

	/**
	 * [spring]
	 */
	private LogFileParser(String logdir, String file) {
		if (StringHelper.containsNonWhitespace(file)) {
			filename = file;
		} else {
			filename = "olat.log";
		}
		if (StringHelper.containsNonWhitespace(logdir)) {
			if (logdir.endsWith(File.separator)) {
				logfilepathBase = logdir + filename;
			} else {
				logfilepathBase = logdir + File.separator + filename;
			}
		} else {
			logfilepathBase = System.getProperty("java.io.tmpdir") + File.separator + "logs" + File.separator + filename;
		}
	}
	
	/**
	 * @param date the date of the log to retrieve, or null when no date suffix should be appended (= take today's log)
	 * @return the VFSLeaf of the Logfile given the Date, or null if no such file could be found
	 */
	public static VFSLeaf getLogfilePath(Date date) {
		String tmpFileName = logfilepathBase;
		if (date != null) {
			SimpleDateFormat sdb = new SimpleDateFormat("yyyy-MM-dd");
			String suffix = sdb.format(date);
			String today = sdb.format(new Date());
			if(suffix.equals(today))
				tmpFileName = logfilepathBase;
			else
				tmpFileName = logfilepathBase + "."+suffix;
		}
		
		File logf = new File(tmpFileName);
		if (!logf.exists()) return null;
		return new LocalFileImpl(logf);
	}

	/**
	 * Extracts the error message from a line in the log file and formats it for the
	 * html.
	 * 
	 * @param s The splited error
	 * @return The error message as a string
	 */
	private static String extractErrorAsHTML(String[] s) {
		StringBuilder sb = new StringBuilder(2048);

		sb.append("<table class='table'><tbody>");
		if (s.length == 6) {
			// before refactoring of logging.Tracing
			sb.append("<tr><th>Date</th><td>").append(Formatter.truncate(s[0].trim(), 20)).append("</td></tr>");
			sb.append("<tr><th>Error</th><td class='danger'>").append(s[1].trim()).append("</td></tr>");
			sb.append("<tr><th>Identity</th><td>").append(s[3].trim()).append("</td></tr>");
			sb.append("<tr><th>Category/Class</th><td>").append(s[2].trim()).append("</td></tr>");
			sb.append("<tr><th>Log msg</th><td>").append(s[4].trim()).append("</td></tr>");
			sb.append("<tr><th>Cause</th><td>").append(s[5].trim().replace(" at ", "<br>at ").replace(">>>", "<br><br>&gt;&gt;&gt;")).append("</td></tr>");
		} else if (s.length == 9) {
			// the new Tracing
			sb.append("<tr><th>Date</th><td>").append(Formatter.truncate(s[0].trim(), 20)).append("</td></tr>");
			sb.append("<tr><th>Error</th><td class='danger'>").append( s[1].trim()).append("</td></tr>");
			sb.append("<tr><th>Identity</th><td>").append(s[3].trim()).append("</td></tr>");
			sb.append("<tr><th>Category/Class</th><td>").append(s[2].trim()).append("</td></tr>");
			sb.append("<tr><th>Remote IP</th><td>").append(s[4].trim()).append("</td></tr>");
			sb.append("<tr><th>Referer</th><td>").append(s[5].trim()).append("</td></tr>");
			sb.append("<tr><th>User-Agent</th><td>").append(s[6].trim()).append("</td></tr>");
			sb.append("<tr><th>Log msg</th><td>").append( s[7].trim()).append("</td></tr>");
			sb.append("<tr><th>Cause</th><td>").append(s[8].trim().replace(" at ", "<br>at ").replace(">>>", "<br><br>&gt;&gt;&gt;")).append("</td></tr>");
		} else {
			sb.append("<tr><th>Log</th><td colspan='8'>");
			for(String t:s) {
				String text = t == null ? "" : t.trim();
				if(StringHelper.containsNonWhitespace(text)) {
					sb.append(text).append("<br>");
				}
			}
			sb.append("</td></tr>");
		}
		sb.append("</tbody></table>");
		
		return sb.toString();
	}

	/**
	 * Extracts the error message from a line in the log file and formats it as text
	 * 
	 * @param s
	 * @return errormsg
	 */
	private static String extractError(String[] s) {
		StringBuilder sb = new StringBuilder(2048);
		if (s.length == 8) {
			sb.append("Date: " + Formatter.truncate(s[0].trim(), 20) + "\n");
			sb.append("Error#: " + s[1].trim() + "\n");
			sb.append("Identity: " + s[3].trim() + "\n");
			sb.append("Category/Class: " + s[2].trim() + "\n");
			sb.append("Remote IP: " + s[4].trim() + "\n");
			sb.append("Referer: " + s[5].trim() + "\n");
			sb.append("User-Agent: " + s[6].trim() + "\n");
			sb.append("Exception: " + s[7].trim().replace(" at ", "\nat ").replace(">>>", "\n\n"));
		} else {
			for(String st:s) {
				sb.append("Raw msg: " + st + "\n");
			}
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param errorNumber
	 * @param dd
	 * @param mm
	 * @param yyyy
	 * @param asHTML
	 * @return
	 */
	public static Collection<String> getErrorToday(String errorNumber, boolean asHTML) {
		return getError(errorNumber, null, asHTML);
	}

	/**
	 * Looks through the log file.
	 * 
	 * @param errorNumber The error number to search for
	 * @param date The date (optional)
	 * @param asHTML Result as HTML or not
	 * @return A list of error messages
	 */
	public static List<String> getError(String errorNumber, Date date, boolean asHTML) {
		if (logfilepathBase == null) {
			//this is null when olat is setup with an empty olat.local.properties file and no log.dir path is set. 
			return Collections.emptyList();
		}

		List<String> errormsg = new ArrayList<>();
		
		File logFile = getLogFile(date, new SimpleDateFormat("yyyy-MM-dd"));
		if(logFile == null) {
			logFile = getLogFile(date, new SimpleDateFormat("dd MM yyyy"));
		}
		
		if(logFile == null || !logFile.exists() || !logFile.canRead()) {
			String logFilePath = logFile == null ? "???" : logFile.getAbsolutePath();
			errormsg.add("logfile <strong>" + logFilePath + "</strong> does not exist or unable to read from");
			return errormsg;
		}
		
		String line;
		String line2;
		String memoryline = "empty";
		String[] em = new String[10];

		int founderror = 0;
		int counter = linecount;
		String logFilePath = logFile.getAbsolutePath();
	
		try(BufferedReader br = new BufferedReader(new FileReader(logFile))) {
			while ((line = br.readLine()) != null) {
				if (counter == 0) {
					errormsg.add(line);
					counter = linecount;
				} else if (counter == 1) {
					errormsg.add(line);
					counter--;
				} else if ( line.matches(matchError) || line.matches(matchWarn) ) {
					line2 = line.replaceAll("[/^]", "/");
					em = line2.split("/%/");
					if (em[1].trim().contains(errorNumber)) {
						founderror++;
						if (asHTML) {
							line2 = extractErrorAsHTML(em);
						} else {
							line2 = extractError(em);
						}
						errormsg.add(memoryline);
						counter--;
						errormsg.add(line2);
						counter--;
					}
				}
				memoryline = line;
			}

			if (founderror > 0 && counter < linecount) {
				while (counter > 0) {
					errormsg.add("empty");
					counter--;
				}
			}
			
			if (founderror == 0){
				errormsg.add("no error with number " + errorNumber + " found in " + logFilePath);
			}
			return errormsg;
		} catch (IOException e) {
			throw new OLATRuntimeException("error reading OLAT error log at " + logFilePath, e);
		}
	}
	
	private static File getLogFile(Date date, SimpleDateFormat sdb) {
		String logfilepath;
		if(date == null) {
			logfilepath = logfilepathBase;
		} else {
			String today = sdb.format(new Date());
			String reqdate = sdb.format(date);
			if (today.equals(reqdate)) {
				logfilepath = logfilepathBase;
			} else {
				logfilepath  = logfilepathBase + "." + reqdate;
			}
			log.info("logfilepath changed to {}  (today: {},  requested date:{})", logfilepath, today, reqdate);
		}

		return new File(logfilepath);
	}
}
