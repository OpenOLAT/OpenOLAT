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
	private static final String matchError = ".*" + Tracing.PREFIX + Tracing.ERROR + ".*";
	private static final String matchWarn = ".*" + Tracing.PREFIX + Tracing.WARN + ".*" ;

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
	 * extracts the errormessage from a line in the logfile and formats it for the
	 * html
	 * 
	 * @param s
	 * @return errormsg
	 */
	private static String extractErrorAsHTML(String s[]) {
		StringBuilder sb = new StringBuilder();

		if (s.length == 6) {
			// before refactoring of logging.Tracing
			sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
			sb.append("<tr><td valign=\"top\"><b>Date&nbsp;</b></td><td>" + Formatter.truncate(s[0].trim(), 20) + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Error#&nbsp;</td><td><font color=\"red\"><b>" + s[1].trim() + "</font></td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Identity&nbsp;</b></td><td>" + s[3].trim() + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Category/Class&nbsp;</b></td><td>" + s[2].trim() + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Log msg&nbsp;</b></td><td>" + s[4].trim() + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Cause&nbsp;</b></td><td>"
					+ s[5].trim().replaceAll(" at ", "<br />at ").replaceAll(">>>", "<br /><br />&gt;&gt;&gt;") + "</td></tr>");
			sb.append("</table>");
		} else if (s.length == 9) {
			// the new Tracing
			sb.append("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
			sb.append("<tr><td valign=\"top\"><b>Date&nbsp;</b></td><td>" + Formatter.truncate(s[0].trim(), 20) + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Error#&nbsp;</td><td><font color=\"red\"><b>" + s[1].trim() + "</font></td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Identity&nbsp;</b></td><td>" + s[3].trim() + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Category/Class&nbsp;</b></td><td>" + s[2].trim() + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Remote IP&nbsp;</b></td><td>" + s[4].trim() + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Referer&nbsp;</b></td><td>" + s[5].trim() + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>User-Agent&nbsp;</b></td><td>" + s[6].trim() + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Log msg&nbsp;</b></td><td>" + s[7].trim() + "</td></tr>");
			sb.append("<tr><td valign=\"top\"><b>Cause&nbsp;</b></td><td>"
					+ s[8].trim().replaceAll(" at ", "<br />at ").replaceAll(">>>", "<br /><br />&gt;&gt;&gt;") + "</td></tr>");
			sb.append("</table>");
		} else {
			throw new AssertException("Unknown Logfile format");
		}
		return sb.toString();
	}

	/**
	 * extracts the errormessage from a line in the logfile and formats it as text
	 * 
	 * @param s
	 * @return errormsg
	 */
	private static String extractError(String s[]) {
		StringBuilder sb = new StringBuilder();

		if (s.length == 6) {
			// before refactoring of logging.Tracing
			sb.append("Date: " + Formatter.truncate(s[0].trim(), 20) + "\n");
			sb.append("Error#: " + s[1].trim() + "\n");
			sb.append("Identity: " + s[3].trim() + "\n");
			sb.append("Category/Class: " + s[2].trim() + "\n");
			sb.append("Log msg: " + s[4].trim() + "\n");
			sb.append("Cause: "
					+ s[5].trim().replaceAll(" at ", "\nat ").replaceAll(">>>", "\n>>>") + "\n");
		} else if (s.length == 9) {
			// the new Tracing
			sb.append("Date: " + Formatter.truncate(s[0].trim(), 20) + "\n");
			sb.append("Error#: " + s[1].trim() + "\n");
			sb.append("Identity: " + s[3].trim() + "\n");
			sb.append("Category/Class: " + s[2].trim() + "\n");
			sb.append("Remote IP: " + s[4].trim() + "\n");
			sb.append("Referer: " + s[5].trim() + "\n");
			sb.append("User-Agent: " + s[6].trim() + "\n");
			sb.append("Log msg: " + s[7].trim() + "\n");
			sb.append("Cause: "
					+ s[8].trim().replaceAll(" at ", "\nat ").replaceAll(">>>", "\n\n"));
		} else {
			throw new AssertException("Unknown Logfile format");
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
	 * looks through the logfile
	 * 
	 * @param s
	 * @param dd requested day
	 * @param mm requested month
	 * @param yyyy requested yyyy
	 * @return the first found errormessage
	 */
	public static Collection<String> getError(String errorNumber, Date date, boolean asHTML) {
		
		if (logfilepathBase == null) {
			//this is null when olat is setup with an empty olat.local.properties file and no log.dir path is set. 
			return Collections.emptyList();
		}
		
		String line;
		String line2;
		String memoryline = "empty";
		String em[] = new String[10];
		Collection<String> errormsg = new ArrayList<String>();

		SimpleDateFormat sdb = new SimpleDateFormat("yyyy-MM-dd");
		
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
			log.info("logfilepath changed to " + logfilepath + "  (today: " + today + ",  requested date:" + reqdate + ")");
		}

		int counter = linecount;
		int founderror = 0;
		final File logFile = new File(logfilepath);
		if(!logFile.exists() || !logFile.canRead()){
			errormsg.add("logfile <b>"+logfilepath+"</b> does not exist or unable to read from");
			return errormsg;
		}
			
		try {
			BufferedReader br = new BufferedReader(new FileReader(logFile));
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
					if (errorNumber.equals(em[1].trim())) {
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
			br.close();

			if (founderror > 0) {
				if (counter < linecount) {
					while (counter > 0) {
						errormsg.add("empty");
						counter--;
					}
				}
			}
			
			if (founderror == 0){
				errormsg.add("no error with number "+errorNumber+" found in "+logfilepath);
			}
			
			return errormsg;
		} catch (IOException e) {
			throw new OLATRuntimeException("error reading OLAT error log at " + logfilepath, e);
		}
	}
}
