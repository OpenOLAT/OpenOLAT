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

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 12.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumUserStatistics {
	
	private final boolean guest;
	private final Identity identity;
	private final String pseudonym;
	
	private int numOfThreads = 0;
	private int numOfReplies = 0;
	private int numOfWords = 0;
	private int numOfCharacters = 0;
	private Date lastModified;
	
	public ForumUserStatistics(Identity identity, String pseudonym, boolean guest) {
		this.identity = identity;
		this.guest = guest;
		this.pseudonym = pseudonym;
	}
	
	public boolean isGuest() {
		return guest;
	}

	public String getPseudonym() {
		return pseudonym;
	}

	public Identity getIdentity() {
		return identity;
	}

	public int getNumOfThreads() {
		return numOfThreads;
	}

	public void addNumOfThreads(int threads) {
		this.numOfThreads += threads;
	}

	public int getNumOfReplies() {
		return numOfReplies;
	}

	public void addNumOfReplies(int replies) {
		this.numOfReplies += replies;
	}

	public int getNumOfWords() {
		return numOfWords;
	}

	public void addNumOfWords(int words) {
		this.numOfWords += words;
	}

	public int getNumOfCharacters() {
		return numOfCharacters;
	}

	public void addNumOfCharacters(int characters) {
		this.numOfCharacters += characters;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}


	
	

}
