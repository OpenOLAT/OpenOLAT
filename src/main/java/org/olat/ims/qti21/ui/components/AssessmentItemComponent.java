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

import java.util.HashMap;
import java.util.Map;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;

/**
 * 
 * Initial date: 10.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemComponent extends AssessmentObjectComponent {
	
	private static final AssessmentItemComponentRenderer RENDERER = new AssessmentItemComponentRenderer();
	
	private ItemSessionController itemSessionController;
	private ResolvedAssessmentItem resolvedAssessmentItem;
	
	private final AssessmentItemFormItem qtiItem;
	private final Map<String,Interaction> responseIdentifiersMap = new HashMap<>();
	
	private boolean enableBack;
	private boolean enableResetHard;
	private boolean enableResetSoft;
	private boolean enableSkip;
	
	private int questionLevel;
	private boolean showQuestionLevel = false;
	private boolean showStatus = true;
	
	public AssessmentItemComponent(String name, AssessmentItemFormItem qtiItem) {
		super(name);
		this.qtiItem = qtiItem;
	}
	
	public boolean isEnableBack() {
		return enableBack;
	}

	public void setEnableBack(boolean enableBack) {
		this.enableBack = enableBack;
	}

	public boolean isEnableResetHard() {
		return enableResetHard;
	}

	public void setEnableResetHard(boolean enableResetHard) {
		this.enableResetHard = enableResetHard;
	}

	public boolean isEnableResetSoft() {
		return enableResetSoft;
	}

	public void setEnableResetSoft(boolean enableResetSoft) {
		this.enableResetSoft = enableResetSoft;
	}

	public boolean isEnableSkip() {
		return enableSkip;
	}

	public void setEnableSkip(boolean enableSkip) {
		this.enableSkip = enableSkip;
	}
	
	public int getQuestionLevel() {
		return questionLevel;
	}

	public void setQuestionLevel(int questionLevel) {
		this.questionLevel = questionLevel;
	}

	public boolean isShowQuestionLevel() {
		return showQuestionLevel;
	}

	public void setShowQuestionLevel(boolean showQuestionLevel) {
		this.showQuestionLevel = showQuestionLevel;
	}

	public boolean isShowStatus() {
		return showStatus;
	}

	public void setShowStatus(boolean showStatus) {
		this.showStatus = showStatus;
	}

	@Override
	public boolean isSilentlyDynamicalCmp() {
		return true;
	}

	@Override
	public String relativePathTo(ResolvedAssessmentItem item) {
		return "";
	}
	
	public ResolvedAssessmentItem getResolvedAssessmentItem() {
		return resolvedAssessmentItem;
	}
	
	public void setResolvedAssessmentItem(ResolvedAssessmentItem resolvedAssessmentItem) {
		this.resolvedAssessmentItem = resolvedAssessmentItem;
	}

	@Override
	public AssessmentItemFormItem getQtiItem() {
		return qtiItem;
	}
	
	public AssessmentItem getAssessmentItem() {
		return resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
	}

	@Override
	public String getResponseUniqueIdentifier(ItemSessionState itemSessionState, Interaction interaction) {
		String id = "oo" + interaction.getResponseIdentifier().toString();
		responseIdentifiersMap.put(id, interaction);
		return id;
	}

	@Override
	public Interaction getInteractionOfResponseUniqueIdentifier(String responseUniqueId) {
		return responseIdentifiersMap.get(responseUniqueId);
	}

	public ItemSessionController getItemSessionController() {
		return itemSessionController;
	}

	public void setItemSessionController(ItemSessionController itemSessionController) {
		this.itemSessionController = itemSessionController;
	}

	@Override
	public AssessmentItemComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
