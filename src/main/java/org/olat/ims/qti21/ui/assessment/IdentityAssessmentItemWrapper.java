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
package org.olat.ims.qti21.ui.assessment;

import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.ui.assessment.model.AssessmentItemCorrection;
import org.olat.ims.qti21.ui.components.InteractionResultFormItem;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;

/**
 * 
 * Initial date: 15.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityAssessmentItemWrapper {
	
	private final String fullName;
	private String minScore, maxScore;
	private Double minScoreVal, maxScoreVal;
	private AssessmentItemCorrection itemInfos;
	private final AssessmentItem assessmentItem;
	private final List<InteractionResultFormItem> responseFormItems;

	private StaticTextElement statusEl;
	private TextElement scoreEl, commentEl;
	
	public IdentityAssessmentItemWrapper(String fullName, AssessmentItem assessmentItem,
			AssessmentItemCorrection itemInfos, List<InteractionResultFormItem> responseFormItems,
			TextElement scoreEl, TextElement commentEl, StaticTextElement statusEl) {
		this.scoreEl = scoreEl;
		this.fullName = fullName;
		this.commentEl = commentEl;
		this.assessmentItem = assessmentItem;
		this.responseFormItems = responseFormItems;
		this.itemInfos = itemInfos;
		this.statusEl = statusEl;
	}
	
	public AssessmentItemCorrection getCorrection() {
		return itemInfos;
	}
	
	public String getFullName() {
		return fullName;
	}
	
	public String getAssessmentItemTitle() {
		return assessmentItem.getTitle();
	}
	
	public boolean isResponded() {
		return itemInfos.isResponded();
	}
	
	public AssessmentTestSession getTestSession() {
		return itemInfos.getTestSession();
	}
	
	public ItemSessionState getItemSessionState() {
		return itemInfos.getItemSessionState();
	}
	
	public boolean isItemSessionStateFinal() {
		return itemInfos.isItemSessionStatusFinal();
	}
	
	public String getItemSessionStatus() {
		return itemInfos.getItemSessionStatus();
	}
	
	public TestPlanNodeKey getTestPlanNodeKey() {
		return itemInfos.getItemNode().getKey();
	}

	public TextElement getScoreEl() {
		return scoreEl;
	}
	
	public TextElement getCommentEl() {
		return commentEl;
	}
	
	public StaticTextElement getStatusEl() {
		return statusEl;
	}

	public List<InteractionResultFormItem> getResponseFormItems() {
		return responseFormItems;
	}

	public String getMinScore() {
		return minScore;
	}

	public void setMinScore(String minScore) {
		this.minScore = minScore;
	}

	public String getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(String maxScore) {
		this.maxScore = maxScore;
	}

	public Double getMinScoreVal() {
		return minScoreVal;
	}

	public void setMinScoreVal(Double minScoreVal) {
		this.minScoreVal = minScoreVal;
	}

	public Double getMaxScoreVal() {
		return maxScoreVal;
	}

	public void setMaxScoreVal(Double maxScoreVal) {
		this.maxScoreVal = maxScoreVal;
	}
}
