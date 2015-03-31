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
*/

package org.olat.course.auditing;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Description:<BR>
 * One-line-per-entry formatter for the course log. The log is formatted on line
 * line per entry using the tabulator caracter as a separator.
 * e.g.
 * 2004-12-24 12:05:24:123	NORMAL	ACTIVITY gnaegi	User entered course
 * 
 * <P>
 * Initial Date:  Dec 1, 2004
 *
 * @author gnaegi 
 */
public class CourseLogFormatter extends Formatter {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

	/**
	 * Create a formatter for a course log
	 */
	public CourseLogFormatter() {
		super();
	}

	/** 
	 * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
	 */
	public String format(LogRecord logRecord) {
		StringBuilder sb = new StringBuilder();
		Date date = new Date(logRecord.getMillis());
		synchronized(dateFormat) {
			sb.append(dateFormat.format(date));
		}
		sb.append("\t")
		  .append(logRecord.getLevel())
		  .append("\t")
		  .append(logRecord.getMessage())
		  .append("\n");
		return sb.toString();
	}

}
