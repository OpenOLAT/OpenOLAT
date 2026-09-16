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
package org.olat.ims.qti21.ui;

import java.math.BigDecimal;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.ims.qti21.AssessmentTestSession;

/**
 * 
 * Initial date: 26 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentTestSessionDetails {
	
	private final int run;
	private final boolean error;
	private final int numOfItems;
	private final int numOfItemsResponded;
	private final int numOfItemsToCorrect;
	private final int numOfItemsToReview;

	private final BigDecimal manualScore;
	private final BigDecimal automaticScore;
	private final BigDecimal adjustmentPlusScore;
	private final BigDecimal adjustmentMinusScore;
	private final AssessmentTestSession testSession;
	private final SessionStatus sessionStatus;
	
	private FormLink toolsLink;
	private FormLink toCorrectLink;
	private FormLink toReviewLink;
	
	public QTI21AssessmentTestSessionDetails(AssessmentTestSession testSession,
			int numOfItems, int numOfItemsResponded, int numOfItemsToCorrect,
			int numOfItemsToReview, BigDecimal automaticScore,
			BigDecimal manualScore, BigDecimal adjustmentPlusScore, BigDecimal adjustmentMinusScore,
			SessionStatus sessionStatus, boolean error, int run) {
		this.run = run;
		this.error = error;
		this.testSession = testSession;
		this.numOfItems = numOfItems;
		this.numOfItemsResponded = numOfItemsResponded;
		this.numOfItemsToCorrect = numOfItemsToCorrect;
		this.numOfItemsToReview = numOfItemsToReview;
		this.manualScore = manualScore;
		this.automaticScore = automaticScore;
		this.adjustmentPlusScore = adjustmentPlusScore;
		this.adjustmentMinusScore = adjustmentMinusScore;
		this.sessionStatus = sessionStatus;
	}
	
	public int getRun() {
		return run;
	}

	public int getNumOfItems() {
		return numOfItems;
	}
	
	public int getNumOfItemsResponded() {
		return numOfItemsResponded;
	}
	
	public int getNumOfItemsToCorrect() {
		return numOfItemsToCorrect;
	}

	public int getNumOfItemsToReview() {
		return numOfItemsToReview;
	}

	public BigDecimal getAutomaticScore() {
		return automaticScore;
	}
	
	public BigDecimal getAdjustmentPlusScore() {
		return adjustmentPlusScore;
	}

	public BigDecimal getAdjustmentMinusScore() {
		return adjustmentMinusScore;
	}

	public BigDecimal getScore() {
		return testSession.getScore();
	}
	
	public BigDecimal getManualScore() {
		return manualScore;
	}
	
	public AssessmentTestSession getTestSession() {
		return testSession;
	}
	
	public boolean isError() {
		return error;
	}

	public SessionStatus getSessionStatus() {
		return sessionStatus;
	}

	public FormLink getToCorrectLink() {
		return toCorrectLink;
	}

	public void setToCorrectLink(FormLink toCorrectLink) {
		this.toCorrectLink = toCorrectLink;
	}

	public FormLink getToReviewLink() {
		return toReviewLink;
	}

	public void setToReviewLink(FormLink toReviewLink) {
		this.toReviewLink = toReviewLink;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
	
	public enum SessionStatus {
		RUNNING,
		REVIEWING,
		SUSPENDED,
		TERMINATED,
		CANCELLED,
		ERROR
	}
}
