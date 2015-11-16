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
package org.olat.modules.fo.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 12.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumUser extends UserPropertiesRow {
	
	private final boolean guest;
	private final String pseudonym;
	
	private final int numOfThreads;
	private final int numOfReplies;
	private final int numOfWords;
	private final int numOfCharacters;
	private final Date lastModified;
	
	public ForumUser(ForumUserStatistics statistics, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(statistics.getIdentity(), userPropertyHandlers, locale);
		guest = statistics.isGuest();
		pseudonym = statistics.getPseudonym();
		numOfCharacters = statistics.getNumOfCharacters();
		numOfWords = statistics.getNumOfWords();
		numOfReplies = statistics.getNumOfReplies();
		numOfThreads = statistics.getNumOfThreads();
		lastModified = statistics.getLastModified();
	}

	public boolean isGuest() {
		return guest;
	}

	public String getPseudonym() {
		return pseudonym;
	}

	public int getNumOfThreads() {
		return numOfThreads;
	}

	public int getNumOfReplies() {
		return numOfReplies;
	}

	public int getNumOfWords() {
		return numOfWords;
	}

	public int getNumOfCharacters() {
		return numOfCharacters;
	}

	public Date getLastModified() {
		return lastModified;
	}
}
