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
package org.olat.core.commons.modules.glossary.morphService;

import java.util.ArrayList;

/**
 * Description:<br>
 * Mainly used to map an xml-reply from the morphological-service to an object.
 * XStream de-serializes xml-output to a FlexionReply which can then be handled internally.
 * 
 * <P>
 * Initial Date:  29.12.2008 <br>
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class FlexionReply {
	
	private String word;
	private String pos;
	private String status;
	private ArrayList<String> stem;
	/**
	 * @return Returns the word.
	 */
	public String getWord() {
		return word;
	}
	/**
	 * @param word The word to set.
	 */
	public void setWord(String word) {
		this.word = word;
	}
	/**
	 * @return Returns the pos.
	 */
	public String getPos() {
		return pos;
	}
	/**
	 * @param pos The pos to set.
	 */
	public void setPos(String pos) {
		this.pos = pos;
	}
	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status The status to set.
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return Returns the stem.
	 */
	public ArrayList<String> getStem() {
		if (stem==null) return new ArrayList<String>();
		return stem;
	}
	/**
	 * @param stem The stem to set.
	 */
	public void setStem(ArrayList<String> stem) {
		this.stem = stem;
	}
	
	
	
}
