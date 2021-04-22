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
package org.olat.ims.qti21.ui.editor;

import java.util.List;

import org.olat.core.gui.components.tree.DnDTreeModel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.QtiMaxScoreEstimator;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestEditorAndComposerTreeModel extends GenericTreeModel implements DnDTreeModel {

	private static final long serialVersionUID = -7174220071296935121L;
	
	private final ResolvedAssessmentTest resolvedAssessmentTest;

	public AssessmentTestEditorAndComposerTreeModel(ResolvedAssessmentTest resolvedAssessmentTest) {
		this.resolvedAssessmentTest = resolvedAssessmentTest;
		AssessmentTest test = resolvedAssessmentTest.getTestLookup().getRootNodeHolder().getRootNode();
		
		GenericTreeNode node = new GenericTreeNode(test.getIdentifier());
		node.setTitle(test.getTitle());
		node.setIconCssClass("o_icon o_icon-lg o_qtiassessment_icon");
		node.setUserObject(test);
		setRootNode(node);
		
		//list all test parts
		List<TestPart> parts = test.getChildAbstractParts();
		if(parts.size() == 1) {
			List<AssessmentSection> sections = parts.get(0).getAssessmentSections();
			for(AssessmentSection section:sections) {
				buildRecursively(section, node);
			}
			
		} else {
			int counter = 0;
			for(TestPart part:parts) {
				buildRecursively(part, ++counter, node);
			}
		}
	}
	
	public TreeNode addItem(AssessmentItemRef itemRef, TreeNode section) {
		return buildRecursively(itemRef, section);
	}
	
	private void buildRecursively(TestPart part, int pos, TreeNode parentNode) {
		GenericTreeNode partNode = new GenericTreeNode(part.getIdentifier().toString());
		partNode.setTitle(pos + ". Test part");
		partNode.setIconCssClass("o_icon o_qtiassessment_icon");
		partNode.setUserObject(part);
		parentNode.addChild(partNode);

		List<AssessmentSection> sections = part.getAssessmentSections();
		for(AssessmentSection section:sections) {
			buildRecursively(section, partNode);
		}
	}
	
	private void buildRecursively(AssessmentSection section, TreeNode parentNode) {
		GenericTreeNode sectionNode = new GenericTreeNode(section.getIdentifier().toString());
		sectionNode.setTitle(section.getTitle());
		sectionNode.setIconCssClass("o_icon o_mi_qtisection");
		sectionNode.setUserObject(section);
		if(maxScoreWarning(section)) {
			sectionNode.setIconDecorator1CssClass("o_midwarn");
		}
		parentNode.addChild(sectionNode);
		
		for(SectionPart part: section.getSectionParts()) {
			if(part instanceof AssessmentItemRef) {
				buildRecursively((AssessmentItemRef)part, sectionNode);
			} else if(part instanceof AssessmentSection) {
				buildRecursively((AssessmentSection) part, sectionNode);
			}
		}
	}
	
	private boolean maxScoreWarning(AssessmentSection section) {
		int selectNum = section.getSelection() != null ? section.getSelection().getSelect() : 0;
		return selectNum > 0 && !QtiMaxScoreEstimator.sameMaxScore(section, resolvedAssessmentTest);
	}
	
	private TreeNode buildRecursively(AssessmentItemRef itemRef, TreeNode parentNode) {
		GenericTreeNode itemNode = new GenericTreeNode(itemRef.getIdentifier().toString());
		ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
		if(resolvedAssessmentItem == null || resolvedAssessmentItem.getItemLookup() == null
				|| resolvedAssessmentItem.getItemLookup().getRootNodeHolder() == null) {
			itemNode.setTitle("ERROR - Not found");
			itemNode.setIconCssClass("o_icon o_icon_error");
			itemNode.setUserObject(itemRef);
			parentNode.addChild(itemNode);
		} else {
			BadResourceException ex = resolvedAssessmentItem.getItemLookup().getBadResourceException();
			if(ex != null) {
				itemNode.setTitle("ERROR");
				itemNode.setIconCssClass("o_icon o_icon_error");
				itemNode.setUserObject(itemRef);
				parentNode.addChild(itemNode);
			} else {
				AssessmentItem assessmentItem = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
				itemNode.setTitle(assessmentItem.getTitle());
				
				QTI21QuestionType type = QTI21QuestionType.getType(assessmentItem);
				if(type != null) {
					itemNode.setIconCssClass("o_icon ".concat(type.getCssClass()));
				} else {
					itemNode.setIconCssClass("o_icon o_mi_qtiunkown");
				}
				itemNode.setUserObject(itemRef);
				parentNode.addChild(itemNode);
			}
		}
		return itemNode;
	}

	@Override
	public boolean isNodeDroppable(TreeNode node) {
		if(node == null) return false;
		Object uobject = node.getUserObject();
		return uobject instanceof AssessmentSection || uobject instanceof TestPart
				|| uobject instanceof AssessmentItemRef
				|| (uobject instanceof AssessmentTest && ((AssessmentTest)uobject).getTestParts().size() == 1);
	}

	@Override
	public boolean isNodeDraggable(TreeNode node) {
		if(node == null) return false;
		Object uobject = node.getUserObject();
		return uobject instanceof AssessmentSection || uobject instanceof AssessmentItemRef;
	}
}