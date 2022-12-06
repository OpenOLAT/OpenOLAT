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

import org.olat.core.gui.components.Component;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21Constants;

import uk.ac.ed.ph.jqtiplus.attribute.Attribute;
import uk.ac.ed.ph.jqtiplus.node.content.variable.PrintedVariable;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 24 mai 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FeedbackResultComponentRenderer extends AssessmentObjectComponentRenderer {
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		FeedbackResultComponent cmp = (FeedbackResultComponent)source;
		ResolvedAssessmentItem resolvedAssessmentItem = cmp.getResolvedAssessmentItem();
		
		ItemSessionState itemSessionState = cmp.getItemSessionState();
		AssessmentRenderer assessmentRenderer = new AssessmentRenderer(renderer);

		AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		for(ModalFeedback modalFeedback:assessmentItem.getModalFeedbacks()) {
			Identifier outcomeIdentifier = modalFeedback.getOutcomeIdentifier();
			if(QTI21Constants.CORRECT_SOLUTION_IDENTIFIER.equals(outcomeIdentifier)) {
				sb.append("<div class='modalFeedback'>");
				renderAssessmentItemCorrectSolutionModalFeedback(assessmentRenderer, sb, modalFeedback,
						cmp, resolvedAssessmentItem, itemSessionState, ubu, translator);
				sb.append("</div>");
			}
		}
	}
	
	/**
	 * A special rendering of "correct solution" feedback for the results report (without the open / close part).
	 * @param renderer
	 * @param sb
	 * @param modalFeedback
	 * @param component
	 * @param resolvedAssessmentItem
	 * @param itemSessionState
	 * @param ubu
	 * @param translator
	 */
	private void renderAssessmentItemCorrectSolutionModalFeedback(AssessmentRenderer renderer, StringOutput sb, ModalFeedback modalFeedback,
			AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState,
			URLBuilder ubu, Translator translator) {
		
		sb.append("<div class='modalFeedback o_togglebox_wrapper clearfix'>");
		Attribute<?> title = modalFeedback.getAttributes().get("title");
		String feedbackTitle = null;
		if(title != null && title.getValue() != null) {
			feedbackTitle = title.getValue().toString();
		}
		if(!StringHelper.containsNonWhitespace(feedbackTitle)) {
			feedbackTitle = translator.translate("correct.solution");
		}

		sb.append("<h5>").append(StringHelper.escapeHtml(feedbackTitle)).append("</h5>");
		sb.append("<div id='modal-correct-solution'><div class='o_togglebox_content clearfix'>");

		modalFeedback.getFlowStatics().forEach((flow)
			-> renderFlow(renderer, sb, component, resolvedAssessmentItem, itemSessionState, flow, ubu, translator));

		sb.append("</div></div></div>");
	}

	@Override
	protected void renderPrintedVariable(AssessmentRenderer renderer, StringOutput sb,
			AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem,
			ItemSessionState itemSessionState, PrintedVariable printedVar) {
		//
	}
}
