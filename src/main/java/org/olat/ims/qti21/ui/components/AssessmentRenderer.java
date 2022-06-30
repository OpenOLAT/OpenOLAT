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
package org.olat.ims.qti21.ui.components;

import org.olat.core.gui.GlobalSettings;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.URLBuilder;

/**
 * 
 * Initial date: 23.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentRenderer {

	private Renderer renderer;
	private boolean mathJax;
	private boolean mathXsltDisabled;
	private boolean solutionMode;
	private boolean reviewMode;
	private boolean endAllowed;
	private boolean hardResetAllowed;
	private boolean softResetAllowed;
	private boolean solutionAllowed;
	private boolean candidateCommentAllowed;
	private boolean showTitles;
	private boolean report;
	private boolean offline;
	
	public AssessmentRenderer(Renderer renderer) {
		this.renderer = renderer;
	}
	
	public AssessmentRenderer newHints(Renderer subRenderer) {
		AssessmentRenderer clone = new AssessmentRenderer(subRenderer);
		clone.setMathXsltDisabled(mathXsltDisabled);
		clone.setSolutionMode(solutionMode);
		clone.setSolutionAllowed(solutionAllowed);
		clone.setReviewMode(reviewMode);
		clone.setEndAllowed(endAllowed);
		clone.setHardResetAllowed(hardResetAllowed);
		clone.setSoftResetAllowed(softResetAllowed);
		clone.setSolutionAllowed(solutionAllowed);
		clone.setCandidateCommentAllowed(candidateCommentAllowed);
		clone.setShowTitles(showTitles);
		clone.setReport(report);
		clone.setOffline(offline);
		return clone;
	}
	
	public Renderer getRenderer() {
		return renderer;
	}
	
	public URLBuilder getUrlBuilder() {
		return renderer.getUrlBuilder();
	}

	public GlobalSettings getGlobalSettings() {
		return renderer.getGlobalSettings();
	}
	
	public boolean isSolutionMode() {
		return solutionMode;
	}

	public void setSolutionMode(boolean solutionMode) {
		this.solutionMode = solutionMode;
	}

	public boolean isReviewMode() {
		return reviewMode;
	}

	public void setReviewMode(boolean reviewMode) {
		this.reviewMode = reviewMode;
	}

	public boolean isMathXsltDisabled() {
		return mathXsltDisabled;
	}
	
	public void setMathXsltDisabled(boolean disable) {
		this.mathXsltDisabled = disable;
	}

	public boolean isEndAllowed() {
		return endAllowed;
	}

	public void setEndAllowed(boolean endAllowed) {
		this.endAllowed = endAllowed;
	}

	public boolean isHardResetAllowed() {
		return hardResetAllowed;
	}

	public void setHardResetAllowed(boolean hardResetAllowed) {
		this.hardResetAllowed = hardResetAllowed;
	}

	public boolean isSoftResetAllowed() {
		return softResetAllowed;
	}

	public void setSoftResetAllowed(boolean softResetAllowed) {
		this.softResetAllowed = softResetAllowed;
	}

	public boolean isSolutionAllowed() {
		return solutionAllowed;
	}

	public void setSolutionAllowed(boolean solutionAllowed) {
		this.solutionAllowed = solutionAllowed;
	}

	public boolean isCandidateCommentAllowed() {
		return candidateCommentAllowed;
	}

	public void setCandidateCommentAllowed(boolean candidateCommentAllowed) {
		this.candidateCommentAllowed = candidateCommentAllowed;
	}

	public boolean isShowTitles() {
		return showTitles;
	}

	public void setShowTitles(boolean showTitles) {
		this.showTitles = showTitles;
	}

	public boolean isMathJax() {
		return mathJax;
	}

	public void setMathJax(boolean mathJax) {
		this.mathJax = mathJax;
	}

	public boolean isReport() {
		return report;
	}

	public void setReport(boolean report) {
		this.report = report;
	}

	public boolean isOffline() {
		return offline;
	}

	public void setOffline(boolean offline) {
		this.offline = offline;
	}

	public void setRenderer(Renderer renderer) {
		this.renderer = renderer;
	}
}
