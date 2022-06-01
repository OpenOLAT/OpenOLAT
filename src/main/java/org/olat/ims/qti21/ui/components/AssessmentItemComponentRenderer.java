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

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.ui.CandidateSessionContext;
import org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent.Event;

import uk.ac.ed.ph.jqtiplus.node.content.variable.PrintedVariable;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.template.declaration.TemplateDeclaration;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.result.SessionStatus;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 10.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemComponentRenderer extends AssessmentObjectComponentRenderer {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentItemComponentRenderer.class);

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		AssessmentItemComponent cmp = (AssessmentItemComponent)source;
		sb.append("<div class='qtiworks o_assessmentitem'>");

		ItemSessionController itemSessionController = cmp.getItemSessionController();
		
		CandidateSessionContext candidateSessionContext = cmp.getCandidateSessionContext();

        /* Create appropriate options that link back to this controller */
		final AssessmentTestSession candidateSession = candidateSessionContext.getCandidateSession();
        if (candidateSession != null && candidateSession.isExploded()) {
            renderExploded(sb, translator);
        } else if (candidateSessionContext.isTerminated()) {
            renderTerminated(sb, translator);
        } else {
            /* Look up most recent event */
            final CandidateEvent latestEvent = candidateSessionContext.getLastEvent();

            /* Load the ItemSessionState */
            final ItemSessionState itemSessionState = cmp.getItemSessionController().getItemSessionState();

            /* Touch the session's duration state if appropriate */
            if (itemSessionState.isEntered() && !itemSessionState.isEnded() && !itemSessionState.isSuspended()) {
                final Date timestamp = candidateSessionContext.getCurrentRequestTimestamp();
                itemSessionController.touchDuration(timestamp);
            }

            /* Render event */
            AssessmentRenderer renderHints = new AssessmentRenderer(renderer);
            renderItemEvent(renderHints, sb, cmp, latestEvent, itemSessionState, ubu, translator);
            
            if(renderHints.isMathJax()
            		|| (WebappHelper.isMathJaxMarkers() && (sb.contains("\\(") || sb.contains("\\[") || sb.contains("$$")))) {
				sb.append(Formatter.elementLatexFormattingScript("o_c".concat(cmp.getDispatchID())));
			}
        }
		
		sb.append("</div>");
	}
	
    private void renderItemEvent(AssessmentRenderer renderer, StringOutput sb, AssessmentItemComponent component,
    		CandidateEvent candidateEvent, ItemSessionState itemSessionState, URLBuilder ubu, Translator translator) {
        
    	final CandidateItemEventType itemEventType = candidateEvent.getItemEventType();

        /* Create and partially configure rendering request */
        //renderingRequest.setPrompt("" /* itemDeliverySettings.getPrompt() */);

        /* If session has terminated, render appropriate state and exit */
        if (itemSessionState.isExited()) {
        	renderTerminated(sb, translator);
            return;
        }

        /* Detect "modal" events. These will cause a particular rendering state to be
         * displayed, which candidate will then leave.
         */

        if (itemEventType==CandidateItemEventType.SOLUTION) {
        	renderer.setSolutionMode(true);
        }

        /* Now set candidate action permissions depending on state of session */
        if (itemEventType==CandidateItemEventType.SOLUTION || itemSessionState.isEnded()) {
            /* Item session is ended (closed) */
        	renderer.setEndAllowed(false);
        	renderer.setHardResetAllowed(false /* itemDeliverySettings.isAllowHardResetWhenEnded() */);
        	renderer.setSoftResetAllowed(false /* itemDeliverySettings.isAllowSoftResetWhenEnded() */);
        	renderer.setSolutionAllowed(true /* itemDeliverySettings.isAllowSolutionWhenEnded() */);
        	renderer.setCandidateCommentAllowed(false);
        } else if (itemSessionState.isOpen()) {
            /* Item session is open (interacting) */
        	renderer.setEndAllowed(true /* itemDeliverySettings.isAllowEnd() */);
        	renderer.setHardResetAllowed(false /* itemDeliverySettings.isAllowHardResetWhenOpen() */);
        	renderer.setSoftResetAllowed(false /* itemDeliverySettings.isAllowSoftResetWhenOpen() */);
        	renderer.setSolutionAllowed(true /* itemDeliverySettings.isAllowSolutionWhenOpen() */);
        	renderer.setCandidateCommentAllowed(false /* itemDeliverySettings.isAllowCandidateComment() */);
        } else {
            throw new OLATRuntimeException("Item has not been entered yet. We do not currently support rendering of this state.", null);
        }

        /* Finally pass to rendering layer */
       // candidateAuditLogger.logItemRendering(candidateEvent);
        //final List<CandidateEventNotification> notifications = candidateEvent.getNotifications();
        try {
        	renderTestItemBody(renderer, sb, component, itemSessionState, ubu, translator);
        } catch (final RuntimeException e) {
            /* Rendering is complex and may trigger an unexpected Exception (due to a bug in the XSLT).
             * In this case, the best we can do for the candidate is to 'explode' the session.
             * See bug #49.
             */
        	log.error("", e);
            renderExploded(sb, translator);
        }
    }
    
	private void renderTestItemBody(AssessmentRenderer renderer, StringOutput sb, AssessmentItemComponent component, ItemSessionState itemSessionState,
			URLBuilder ubu, Translator translator) {
		
		final AssessmentItem assessmentItem = component.getAssessmentItem();
		final ResolvedAssessmentItem resolvedAssessmentItem = component.getResolvedAssessmentItem();

		sb.append("<div class='o_assessmentitem_wrapper'>");
		//title + status
		sb.append("<h4 class='itemTitle'>");
		sb.append("<span class='o_qti_item_meta'>");
		if(component.isShowStatus()) {
			renderItemStatus(renderer, sb, itemSessionState, translator);
		}
		if(component.isShowQuestionLevel()) {
			renderQuestionLevels(component.getQuestionLevel(), component.getMaxQuestionLevel(), sb, translator);
		}
		sb.append("</span>");
		sb.append(StringHelper.escapeHtml(assessmentItem.getTitle())).append("</h4>")
		  .append("<div id='itemBody' class='o_qti_item_body clearfix'>");
		
		//TODO prompt
		
		//render itemBody
		assessmentItem.getItemBody().getBlocks().forEach((block)
				-> renderBlock(renderer, sb, component, resolvedAssessmentItem, itemSessionState, block, ubu, translator));

		//comment
		renderComment(renderer, sb, component, itemSessionState, translator);
		
		//end body
		sb.append("</div>");
		
		// Display active modal feedback (only after responseProcessing)
		if(itemSessionState.getSessionStatus() == SessionStatus.FINAL) {
			renderTestItemModalFeedback(renderer, sb, component, resolvedAssessmentItem, itemSessionState, ubu, translator);
		}

		//controls
		sb.append("<div class='o_button_group o_assessmentitem_controls'>");
		//submit button
		AssessmentItemFormItem itemEl = component.getQtiItem();
		if(component.isItemSessionOpen(itemSessionState, renderer.isSolutionMode())) {
			Component submit = itemEl.getSubmitButton().getComponent();
			submit.getHTMLRendererSingleton().render(renderer.getRenderer(), sb, submit, ubu, translator, new RenderResult(), null);
			submit.setDirty(false);
		}
		
		boolean enableAdditionalButtons = component.isEnableBack() || component.isEnableSkip()
				|| component.isEnableResetHard() || component.isEnableResetSoft();
		if(enableAdditionalButtons && (itemSessionState.isResponded() || itemSessionState.getEndTime() != null)) {
			boolean lobOnly = isLobOnly(assessmentItem);
			if(lobOnly && itemSessionState.isResponded()) {
				String title = translator.translate("next.item");
				renderControl(sb, component, title, false, "o_sel_next_question", new NameValuePair("cid", Event.next.name()));
			} else if(isIncorrectlyAnswered(itemSessionState) || (!itemSessionState.isResponded() && itemSessionState.getEndTime() != null)) {
				if(!willShowFeedback(component, assessmentItem)) {
					sb.append("<span class='o_sel_additional_feedback'><i class='o_icon o_icon-lg o_icon_failed'> </i></span>");
				}
				
				if(component.isEnableBack()) {
					String title = translator.translate("back.item");
					renderControl(sb, component, title, false, "o_sel_back_question", new NameValuePair("cid", Event.back.name()));
				}
				if(component.isEnableResetHard()) {
					String title = translator.translate("retry.item");
					renderControl(sb, component, title, false, "o_sel_reset_question", new NameValuePair("cid", Event.resethard.name()));
				} else if(component.isEnableResetSoft()) {
					String title = translator.translate("retry.item");
					renderControl(sb, component, title, false, "o_sel_reset_question", new NameValuePair("cid", Event.resetsoft.name()));
				}
				if(component.isEnableSkip()) {
					String title = translator.translate("skip.item");
					renderControl(sb, component, title, false, "o_sel_skip_question", new NameValuePair("cid", Event.skip.name()));
				}
			} else {
				if(isCorrectlyAnswered(itemSessionState) && !willShowFeedback(component, assessmentItem)) {
					sb.append("<span class='o_sel_additional_feedback'><i class='o_icon o_icon-lg o_icon_passed'> </i></span>");
				}
				String title = translator.translate("next.item");
				renderControl(sb, component, title, false, "o_sel_next_question", new NameValuePair("cid", Event.next.name()));
			}
		}
		
		sb.append("</div>");
		
		sb.append("</div>"); // end wrapper
	}
	
	private boolean willShowFeedback(AssessmentItemComponent component, AssessmentItem assessmentItem) {
		if(component.isHideFeedbacks()) return false;
		
		for(ModalFeedback modalFeedback:assessmentItem.getModalFeedbacks()) {
			if(component.isFeedback(modalFeedback, component.getItemSessionController().getItemSessionState())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isLobOnly(AssessmentItem assessmentItem) {
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		for(Interaction interaction:interactions) {
			if(!(interaction instanceof UploadInteraction)
					&& !(interaction instanceof DrawingInteraction)
					&& !(interaction instanceof ExtendedTextInteraction)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isCorrectlyAnswered(ItemSessionState itemSessionState) {
		if(itemSessionState.isResponded()) {
			Value maxScore = itemSessionState.getOutcomeValue(QTI21Constants.MAXSCORE_IDENTIFIER);
			Value score = itemSessionState.getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			if(maxScore != null && maxScore.equals(score)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isIncorrectlyAnswered(ItemSessionState itemSessionState) {
		if(itemSessionState.isResponded()) {
			Value maxScore = itemSessionState.getOutcomeValue(QTI21Constants.MAXSCORE_IDENTIFIER);
			Value score = itemSessionState.getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			if(maxScore != null && !maxScore.equals(score)) {
				return true;
			}
		}
		return false;
	}
    
	private void renderItemStatus(AssessmentRenderer renderer, StringOutput sb, ItemSessionState itemSessionState, Translator translator) {
		if(renderer.isSolutionMode()) {
			sb.append("<span class='o_assessmentitem_status review'>").append(translator.translate("assessment.item.status.modelSolution")).append("</span>");
		} else {
			super.renderItemStatus(sb, itemSessionState, null, translator);
		}
	}
	
	private void renderQuestionLevels(int level, int maxQuestionLevels, StringOutput sb, Translator translator) {
		sb.append("<span class='o_assessmentitem_level'>").append(translator.translate("assessment.item.level")).append("");
		for(int i=1; i<=maxQuestionLevels; i++) {
			if(i <= level) {
				sb.append(" <i class='o_icon o_icon_circle_color'> </i>");
			} else {
				sb.append(" <i class='o_icon o_icon_disabled'> </i>");
			}
		}
		sb.append("</span>");
	}
	
	@Override
	protected void renderPrintedVariable(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem,
			ItemSessionState itemSessionState, PrintedVariable printedVar) {

		Identifier identifier = printedVar.getIdentifier();
		Value templateValue = itemSessionState.getTemplateValues().get(identifier);
		Value outcomeValue = itemSessionState.getOutcomeValues().get(identifier);
		
		sb.append("<span class='printedVariable'>");
		if(outcomeValue != null) {
			OutcomeDeclaration outcomeDeclaration = resolvedAssessmentItem.getRootNodeLookup()
					.extractIfSuccessful().getOutcomeDeclaration(identifier);
			renderPrintedVariable(renderer, sb, printedVar, outcomeDeclaration, outcomeValue);
		} else if(templateValue != null) {
			TemplateDeclaration templateDeclaration = resolvedAssessmentItem.getRootNodeLookup()
					.extractIfSuccessful().getTemplateDeclaration(identifier);
			renderPrintedVariable(renderer, sb, printedVar, templateDeclaration, templateValue);
		} else {
			sb.append("(variable ").append(identifier.toString()).append(" was not found)");
		}
		sb.append("</span>");
	}
}