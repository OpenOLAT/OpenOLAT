/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.core.commons.modules.glossary;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Description:<br>
 * Revision for DocBook
 * 
 * <P>
 * Initial Date:  15 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class Revision {
	
	private static final Logger log = Tracing.createLoggerFor(Revision.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy", Locale.ENGLISH);
	
	private Author author;
	private String revisionflag;
	private String date;
	
	public Revision() {
		//
	}
	
	
	public Author getAuthor() {
		return author;
	}
	
	public void setAuthor(Author author) {
		this.author = author;
	}
	
	public String getRevisionflag() {
		return revisionflag;
	}
	
	public void setRevisionflag(String revisionflag) {
		this.revisionflag = revisionflag;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	
	public Date getJavaDate() {
		if(StringHelper.containsNonWhitespace(date)) {
			synchronized(dateFormat) {
				try {
					return dateFormat.parse(date);
				} catch (ParseException e) {
					log.warn("Cannot parse a date: " + date, e);
					return null;
				}
			}
		}
		return null;
	}
	
	public void setJavaDate(Date date) {
		synchronized(dateFormat) {
			this.date = dateFormat.format(date);
		}
	}
}
