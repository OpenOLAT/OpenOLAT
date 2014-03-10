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
package org.olat.ims.qti.statistics.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 10.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatisticFIBOption {
	
	private long numOfCorrect = 0l;
	private long numOfIncorrect = 0l;
	private float points;
	
	private boolean caseSensitive;
	private String correctBlank;
	private List<String> alternatives;
	private List<String> wrongAnswers = new ArrayList<>();

	public long getNumOfCorrect() {
		return numOfCorrect;
	}

	public void setNumOfCorrect(long numOfCorrect) {
		this.numOfCorrect = numOfCorrect;
	}

	public long getNumOfIncorrect() {
		return numOfIncorrect;
	}

	public void setNumOfIncorrect(long numOfIncorrect) {
		this.numOfIncorrect = numOfIncorrect;
	}

	public float getPoints() {
		return points;
	}

	public void setPoints(float points) {
		this.points = points;
	}

	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public String getCorrectBlank() {
		return correctBlank;
	}

	public void setCorrectBlank(String correctBlank) {
		this.correctBlank = correctBlank;
	}

	public List<String> getAlternatives() {
		return alternatives;
	}

	public void setAlternatives(List<String> alternatives) {
		this.alternatives = alternatives;
	}

	public List<String> getWrongAnswers() {
		return wrongAnswers;
	}

	public void setWrongAnswers(List<String> wrongAnswers) {
		this.wrongAnswers = wrongAnswers;
	}
}
