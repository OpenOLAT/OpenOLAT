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
package org.olat.ims.qti21.model.statistics;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 03.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextEntryInteractionStatistics {
	
	private final Identifier responseIdentifier;
	
	private final boolean caseSensitive;
	private final String correctResponse;
	private final List<String> alternatives;
	private final List<String> wrongAnswers = new ArrayList<>();
	
	private long numOfCorrect = 0l;
	private long numOfIncorrect = 0l;
	private final Double points;
	
	public TextEntryInteractionStatistics(Identifier responseIdentifier,
			boolean caseSensitive, String correctResponse, List<String> alternatives, Double points) {
		this.responseIdentifier = responseIdentifier;
		this.caseSensitive = caseSensitive;
		this.correctResponse = correctResponse;
		this.alternatives = alternatives;
		this.points = points;
	}

	public Identifier getResponseIdentifier() {
		return responseIdentifier;
	}
	
	public String getCorrectResponse() {
		return correctResponse;
	}
	
	public List<String> getAlternatives() {
		return alternatives;
	}

	public long getNumOfCorrect() {
		return numOfCorrect;
	}
	
	public void addCorrect(long correct) {
		this.numOfCorrect += correct;
	}
	
	public long getNumOfIncorrect() {
		return numOfIncorrect;
	}
	
	public void addIncorrect(long incorrect) {
		this.numOfIncorrect += incorrect;
	}
	
	public Double getPoints() {
		return points;
	}

	public List<String> getWrongAnswers() {
		return wrongAnswers;
	}
	
	public void addWrongResponses(String response) {
		wrongAnswers.add(response);
	}

	public boolean matchResponse(String value) {
		if(matchString(correctResponse, value)) {
			return true;
		}
		for(String alternative:alternatives) {
			if(matchString(alternative, value)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean matchString(String value1, String value2) {
		if(value1 == value2) return true;
		if(value1 == null && (value2 == null || value2.isEmpty())) return true;
		if((value1 != null && value1.isEmpty()) && (value2 == null || value2.isEmpty())) return true;
		
		if(caseSensitive) {
			if(value1.equals(value2)) {
				return true;
			}
		} else if(value1.equalsIgnoreCase(value2)) {
			return true;
		}
		return false;
	}
}
