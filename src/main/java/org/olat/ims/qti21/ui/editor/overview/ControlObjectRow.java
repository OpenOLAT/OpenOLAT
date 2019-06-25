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
package org.olat.ims.qti21.ui.editor.overview;

import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.modules.qpool.manager.MetadataConverterHelper;
import org.olat.modules.qpool.model.LOMDuration;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.ControlObject;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;

/**
 * 
 * Initial date: 15 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ControlObjectRow {
	
	private final String title;
	private final String iconCssClass;
	private final ControlObject<?> part;
	
	private Double maxScore;
	private Boolean feedbacks;
	private QTI21QuestionType type;

	private OptionAndInheritance review;
	private OptionAndInheritance comment;
	private OptionAndInheritance skipping;
	private OptionAndInheritance solution;
	private MaxAttemptOption attemptOption;
	
	private Long typicalLearningTime;
	
	public ControlObjectRow(String title, ControlObject<?> part, String iconCssClass) {
		this.part = part;
		this.title = title;
		this.iconCssClass = iconCssClass;
	}
	
	public static ControlObjectRow valueOf(AssessmentTest assessmentTest) {
		ControlObjectRow row = new ControlObjectRow(assessmentTest.getTitle(), assessmentTest, "o_qtiassessment_icon");
		row.maxScore = QtiNodesExtractor.extractMaxScore(assessmentTest);

		boolean hasFeedbacks = !assessmentTest.getTestFeedbacks().isEmpty();
		row.feedbacks = Boolean.valueOf(hasFeedbacks);
		
		List<TestPart> parts = assessmentTest.getTestParts();
		if(parts.size() == 1) {
			TestPart part = parts.get(0);
			configuration(row, part);
		}
		return row;
	}
	
	public static ControlObjectRow valueOf(TestPart part, int pos) {
		ControlObjectRow row = new ControlObjectRow(pos + ". Test part", part, "o_icon-lg o_qtiassessment_icon");
		configuration(row, part);
		return row;
	}

	public static ControlObjectRow valueOf(AssessmentSection section) {
		ControlObjectRow row = new ControlObjectRow(section.getTitle(), section, "o_mi_qtisection");
		configuration(row, section);
		return row;
	}
	
	public static ControlObjectRow errorOf(AssessmentItemRef itemRef) {
		return new ControlObjectRow("ERROR", itemRef, "o_icon_error");
	}
	
	public static ControlObjectRow valueOf(AssessmentItemRef itemRef, AssessmentItem assessmentItem, ManifestBuilder manifestBuilder) {
		String itemCssClass;
		QTI21QuestionType type = QTI21QuestionType.getType(assessmentItem);
		if(type != null) {
			itemCssClass = type.getCssClass();
		} else {
			itemCssClass = "o_mi_qtiunkown";
		}
		ControlObjectRow row = new ControlObjectRow(assessmentItem.getTitle(), itemRef, itemCssClass);
		row.maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
		row.type = type;
		boolean hasFeedbacks = !assessmentItem.getModalFeedbacks().isEmpty();
		row.feedbacks = Boolean.valueOf(hasFeedbacks);
		configuration(row, itemRef);

		ManifestMetadataBuilder metadata = manifestBuilder.getResourceBuilderByHref(itemRef.getHref().toString());
		if(metadata != null && metadata.getLom(false) != null) {
			String learningTime = metadata.getEducationalLearningTime();
			if(StringHelper.containsNonWhitespace(learningTime)) {
				LOMDuration duration = MetadataConverterHelper.convertDuration(learningTime);
				row.typicalLearningTime = Long.valueOf(MetadataConverterHelper.convertToSeconds(duration));
			}
		}
		
		return row;
	}
	
	private static void configuration(ControlObjectRow row, AbstractPart part) {
		attempts(row, part);
		skipping(row, part);
		comment(row, part);
		review(row, part);
		solution(row, part);
	}
	
	private static void attempts(ControlObjectRow row, AbstractPart part) {
		Integer maxAttempts = null;
		ItemSessionControl itemSessionControl = part.getItemSessionControl();
		if(itemSessionControl != null) {
			maxAttempts = itemSessionControl.getMaxAttempts();
		}
		
		if(maxAttempts != null) {
			OptionEnum option = (maxAttempts.intValue() == 0 ? OptionEnum.no : OptionEnum.yes);
			row.attemptOption = MaxAttemptOption.valueOf(option, maxAttempts);	
		} else if(part instanceof TestPart) {
			row.attemptOption = MaxAttemptOption.valueOf(OptionEnum.no, Integer.valueOf(1));
		} else {
			Integer inheritedMaxAttempts = inheritedAttempts(part);
			row.attemptOption = MaxAttemptOption.valueOf(OptionEnum.inherited, inheritedMaxAttempts);
		}
	}
	
	private static Integer inheritedAttempts(AbstractPart part) {
		for(QtiNode parent=part.getParent(); parent != null; parent=parent.getParent()) {
			if(parent instanceof AbstractPart) {
				ItemSessionControl itemSessionControl = ((AbstractPart)parent).getItemSessionControl();
				if(itemSessionControl != null && itemSessionControl.getMaxAttempts() != null) {
					return itemSessionControl.getMaxAttempts();
				}
			}
		}
		return Integer.valueOf(1);
	}
	
	private static void skipping(ControlObjectRow row, AbstractPart part) {
		ItemSessionControl itemSessionControl = part.getItemSessionControl();
		if(itemSessionControl != null && itemSessionControl.getAllowSkipping() != null) {
			row.skipping = itemSessionControl.getAllowSkipping().booleanValue() ? OptionAndInheritance.yes() : OptionAndInheritance.no();
		} else {
			row.skipping = (part instanceof TestPart) ? OptionAndInheritance.yes() : OptionAndInheritance.inherited(inheritedSkipping(part));
		}
	}
	
	private static OptionEnum inheritedSkipping(AbstractPart part) {
		for(QtiNode parent=part.getParent(); parent != null; parent=parent.getParent()) {
			if(parent instanceof AbstractPart) {
				ItemSessionControl itemSessionControl = ((AbstractPart)parent).getItemSessionControl();
				if(itemSessionControl != null && itemSessionControl.getAllowSkipping() != null) {
					return itemSessionControl.getAllowSkipping().booleanValue() ? OptionEnum.yes : OptionEnum.no;
				}
			}
		}
		return OptionEnum.yes;
	}
	
	private static void comment(ControlObjectRow row, AbstractPart part) {
		ItemSessionControl itemSessionControl = part.getItemSessionControl();
		if(itemSessionControl != null && itemSessionControl.getAllowComment() != null) {
			row.comment = itemSessionControl.getAllowComment().booleanValue() ? OptionAndInheritance.yes() : OptionAndInheritance.no();
		} else {
			row.comment = (part instanceof TestPart) ? OptionAndInheritance.yes() : OptionAndInheritance.inherited(inheritedComment(part));
		}
	}
	
	private static OptionEnum inheritedComment(AbstractPart part) {
		for(QtiNode parent=part.getParent(); parent != null; parent=parent.getParent()) {
			if(parent instanceof AbstractPart) {
				ItemSessionControl itemSessionControl = ((AbstractPart)parent).getItemSessionControl();
				if(itemSessionControl != null && itemSessionControl.getAllowComment() != null) {
					return itemSessionControl.getAllowComment().booleanValue() ? OptionEnum.yes : OptionEnum.no;
				}
			}
		}
		return OptionEnum.yes;
	}
	
	private static void review(ControlObjectRow row, AbstractPart part) {
		ItemSessionControl itemSessionControl = part.getItemSessionControl();
		if(itemSessionControl != null && itemSessionControl.getAllowReview() != null) {
			row.review = itemSessionControl.getAllowReview().booleanValue() ? OptionAndInheritance.yes() : OptionAndInheritance.no();
		} else {
			row.review = (part instanceof TestPart) ? OptionAndInheritance.no() : OptionAndInheritance.inherited(inheritedReview(part));
		}
	}
	
	private static OptionEnum inheritedReview(AbstractPart part) {
		for(QtiNode parent=part.getParent(); parent != null; parent=parent.getParent()) {
			if(parent instanceof AbstractPart) {
				ItemSessionControl itemSessionControl = ((AbstractPart)parent).getItemSessionControl();
				if(itemSessionControl != null && itemSessionControl.getAllowReview() != null) {
					return itemSessionControl.getAllowReview().booleanValue() ? OptionEnum.yes : OptionEnum.no;
				}
			}
		}
		return OptionEnum.no;
	}
	
	private static void solution(ControlObjectRow row, AbstractPart part) {
		ItemSessionControl itemSessionControl = part.getItemSessionControl();
		if(itemSessionControl != null && itemSessionControl.getShowSolution() != null) {
			row.solution = itemSessionControl.getShowSolution().booleanValue() ? OptionAndInheritance.yes() : OptionAndInheritance.no();
		} else {
			row.solution = (part instanceof TestPart) ? OptionAndInheritance.no() : OptionAndInheritance.inherited(inheritedSolution(part));
		}
	}
	
	private static OptionEnum inheritedSolution(AbstractPart part) {
		for(QtiNode parent=part.getParent(); parent != null; parent=parent.getParent()) {
			if(parent instanceof AbstractPart) {
				ItemSessionControl itemSessionControl = ((AbstractPart)parent).getItemSessionControl();
				if(itemSessionControl != null && itemSessionControl.getShowSolution() != null) {
					return itemSessionControl.getShowSolution().booleanValue() ? OptionEnum.yes : OptionEnum.no;
				}
			}
		}
		return OptionEnum.no;
	}
	
	
	
	
	public String getTitle() {
		return title;
	}

	public String getIdentifier() {
		return part.getIdentifier().toString();
	}
	
	public ControlObject<?> getControlObject() {
		return part;
	}
	
	public String getIconCssClass() {
		return iconCssClass;
	}
	
	public Double getMaxScore() {
		return maxScore;
	}
	
	public void setMaxScore(Double maxScore) {
		this.maxScore = maxScore;
	}
	
	public QTI21QuestionType getType() {
		return type;
	}
	
	public Boolean getFeedbacks() {
		return feedbacks;
	}
	
	public OptionAndInheritance getSkipping() {
		return skipping;
	}
	
	public OptionAndInheritance getComment() {
		return comment;
	}
	
	public OptionAndInheritance getReview() {
		return review;
	}
	
	public OptionAndInheritance getSolution() {
		return solution;
	}
	
	public MaxAttemptOption getAttemptOption() {
		return attemptOption;
	}
	
	public Long getLearningTime() {
		return typicalLearningTime;
	}
	
	public void setLearningTime(Long learningTime) {
		this.typicalLearningTime = learningTime;
	}
	
	public int getDepth() {
		int depth = 0;
		for(ControlObject<?> parent=part.getParent(); parent != null; parent = parent.getParent()) {
			depth++;
		}
		return depth;
	}
}
