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
package org.olat.ims.qti21.ui.assessment.model;

/**
 * 
 * Initial date: 1 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class CorrectionRow {
	
	private int numCorrected = 0;
	private int numAutoCorrected = 0;
	private int numToReview = 0;
	private int numNotCorrected = 0;
	private int numOfSessions = 0;
	private boolean manualCorrection;
	private int numAnswered = 0;
	private int numNotAnswered = 0;
	
	public int getNumAutoCorrected() {
		return numAutoCorrected;
	}
	
	public int addAutoCorrected() {
		numAutoCorrected++;
		return numAutoCorrected;
	}
	
	public int getNumCorrected() {
		return numCorrected;
	}
	
	public int addCorrected() {
		numCorrected++;
		return numCorrected;
	}

	public int getNumToReview() {
		return numToReview;
	}
	
	public int addToReview() {
		numToReview++;
		return numToReview;
	}

	public int getNumNotCorrected() {
		return numNotCorrected;
	}
	
	public int addNotCorrected() {
		numNotCorrected++;
		return numNotCorrected;
	}
	
	public int getNumOfSessions() {
		return numOfSessions;
	}
	
	public int addSession() {
		numOfSessions++;
		return numOfSessions;
	}

	public int getNumAnswered() {
		return numAnswered;
	}

	public int addAnswered() {
		numAnswered++;
		return numAnswered;
	}

	public int getNumNotAnswered() {
		return numNotAnswered;
	}

	public int addNotAnswered() {
		numNotAnswered++;
		return numNotAnswered;
	}

	public boolean isManualCorrection() {
		return manualCorrection;
	}

	public void setManualCorrection(boolean manualCorrection) {
		this.manualCorrection = manualCorrection;
	}

}
