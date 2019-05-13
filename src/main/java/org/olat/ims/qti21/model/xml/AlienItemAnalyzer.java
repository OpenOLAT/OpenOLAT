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
package org.olat.ims.qti21.model.xml;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.QTI21QuestionType;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.CustomOperator;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;

/**
 * 
 * Initial date: 30 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AlienItemAnalyzer {
	
	private static final Logger log = Tracing.createLoggerFor(AlienItemAnalyzer.class);
	
	private final AssessmentItem item;
	
	public AlienItemAnalyzer(AssessmentItem item) {
		this.item = item;
	}
	
	public Report analyze() {
		try {
			// We doesn't support item created by TaoTesting, the difference in response
			// processing are quiet large.
			if(item.getToolName() != null && "TAO".equalsIgnoreCase(item.getToolName())) {
				return new Report(QTI21QuestionType.unkown, true);
			}
			
			QTI21QuestionType type = QTI21QuestionType.getTypeRelax(item);
			Report report = new Report(type);
			checkTemplateProcessing(report);
			checkFeedback(report);
			checkItemBody(report);
			checkCustomOperator(report);
			checkKprim(report);
			return report;
		} catch (Exception e) {
			log.error("", e);
			return new Report(QTI21QuestionType.unkown, true);
		}
	}
	
	private void checkFeedback(Report report) {
		if(item.getModalFeedbacks() != null
				&& item.getModalFeedbacks().size() > 0) {

			Set<Identifier> outcomeIdentifiers = new HashSet<>();
			List<ModalFeedback> feedbacks = item.getModalFeedbacks();
			for(ModalFeedback feedback:feedbacks) {
				if(feedback.getOutcomeIdentifier() != null) {
					outcomeIdentifiers.add(feedback.getOutcomeIdentifier());
				}
			}
			
			if(outcomeIdentifiers.size() == 1) {
				if(outcomeIdentifiers.iterator().next().equals(QTI21Constants.FEEDBACKMODAL_IDENTIFIER)) {
					checkFeedbackModalResponseProcessing(report);
				} else {
					report.addWarning(ReportWarningEnum.alienFeedbacks);
				}
			} else {
				// Onyx and OpenOLAT only use one outcome identifier for the feedbacks
				// Taotesting use several different identifiers. And we don't
				// understand feedbacks without outcome identifiers
				report.addWarning(ReportWarningEnum.alienFeedbacks);
			}
		}
	}
	
	private void checkFeedbackModalResponseProcessing(Report report) {
		if(report.getType() != QTI21QuestionType.unkown) {
			boolean allOk = true;
			List<ModalFeedback> feedbacks = item.getModalFeedbacks();
			for(ModalFeedback feedback:feedbacks) {
				ModalFeedbackBuilder feedbackBuilder = new ModalFeedbackBuilder(item, feedback);
				if(feedbackBuilder.isCorrectRule()
						|| feedbackBuilder.isIncorrectRule()) {
					//ok
				} else if(feedbackBuilder.isCorrectSolutionRule()
						|| (feedback.getOutcomeIdentifier() != null
						&& QTI21Constants.CORRECT_SOLUTION_IDENTIFIER.equals(feedback.getOutcomeIdentifier()))) {
					//ok
				}  else if(feedbackBuilder.isEmptyRule() || feedbackBuilder.isAnsweredRule() || feedbackBuilder.isHint()) {
					//ok
				} else {
					List<ModalFeedbackCondition> conditions = feedbackBuilder.getFeedbackConditons();
					if(conditions == null || conditions.isEmpty()) {
						allOk &= false;
					}
				}
			}

			if(!allOk) {
				report.addWarning(ReportWarningEnum.unsupportedFeedbacks);
			}
		}
	}
	
	/**
	 * Check if there are text after the interaction
	 */
	private void checkItemBody(Report report) {
		if(report.getType() == QTI21QuestionType.sc
				|| report.getType() == QTI21QuestionType.mc
				|| report.getType() == QTI21QuestionType.kprim
				|| report.getType() == QTI21QuestionType.match
				|| report.getType() == QTI21QuestionType.matchdraganddrop
				|| report.getType() == QTI21QuestionType.hotspot
				|| report.getType() == QTI21QuestionType.essay
				|| report.getType() == QTI21QuestionType.upload) {

			ItemBody itemBody = item.getItemBody();
			List<Block> blocks = itemBody.getBlocks();
			Block lastBlock = blocks.get(blocks.size() - 1);
			if(!(lastBlock instanceof Interaction)) {
				report.addWarning(ReportWarningEnum.textAfterInteraction);
			}
		}
	}

	private void checkTemplateProcessing(Report report) {
		if(item.getTemplateDeclarations() != null
				&& item.getTemplateDeclarations().size()> 0) {
			report.addWarning(ReportWarningEnum.templates);
		} else if(item.getTemplateProcessing() != null
				&& item.getTemplateProcessing().getTemplateProcessingRules() != null
				&& item.getTemplateProcessing().getTemplateProcessingRules().size() > 0) {
			report.addWarning(ReportWarningEnum.templates);
		}
	}
	
	private void checkCustomOperator(Report report) {
		@SuppressWarnings("rawtypes")
		List<CustomOperator> customOperators = QueryUtils.search(CustomOperator.class, item);
		if(customOperators != null && customOperators.size() > 0) {
			report.addWarning(ReportWarningEnum.templates);
		}
	}
	
	private void checkKprim(Report report) {
		List<Interaction> interactions = item.getItemBody().findInteractions();
		if(interactions != null && interactions.size() == 1) {
			Interaction interaction = interactions.get(0);
			if(interaction instanceof MatchInteraction) {
				report.addAlternative(QTI21QuestionType.match);
				report.addAlternative(QTI21QuestionType.matchdraganddrop);
			}
		}
	}
	
	public enum ReportWarningEnum {
		
		templates("warning.t"
				+ "emplates", "o_icon_error"),
		alienFeedbacks("warning.alien.feedbacks", "o_icon_error"),
		unsupportedFeedbacks("warning.unsupported.feedbacks", "o_icon_error"),
		textAfterInteraction("warning.text.after.interaction", "o_icon_error"),
		customOperator("warning.custom.operator", "o_icon_error");

		private final String i18nKey;
		private final String cssClass;
		
		private ReportWarningEnum(String i18nKey, String cssClass) {
			this.i18nKey = i18nKey;
			this.cssClass = cssClass;
		}

		public String i18nKey() {
			return i18nKey;
		}
		
		public String cssClass() {
			return cssClass;
		}
	}

	public static class Report {
		
		private QTI21QuestionType type;
		
		private boolean blocker;
		private final List<ReportWarningEnum> warnings = new ArrayList<>();
		private final List<QTI21QuestionType> alternatives = new ArrayList<>();
		
		public Report(QTI21QuestionType type) {
			this(type, false);
		}
		
		public Report(QTI21QuestionType type, boolean blocker) {
			this.type = type;
			this.blocker = blocker;
		}
		
		public boolean isBlocker() {
			return blocker;
		}
		
		public void setBlocker(boolean blocker) {
			this.blocker = blocker;
		}
		
		public boolean hasWarnings() {
			return warnings.size() > 0;
		}
		
		protected void addWarning(ReportWarningEnum warning) {
			warnings.add(warning);
		}
		
		public List<ReportWarningEnum> getWarnings() {
			return warnings;
		}
		
		public List<QTI21QuestionType> getAlternatives() {
			return alternatives;
		}
		
		public void addAlternative(QTI21QuestionType alternative) {
			alternatives.add(alternative);
		}
		
		public QTI21QuestionType getType() {
			return type;
		}
		
		public void setType(QTI21QuestionType type) {
			this.type = type;
		}
	}
}
