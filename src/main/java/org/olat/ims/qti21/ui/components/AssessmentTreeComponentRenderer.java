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

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateTestEventType;
import org.olat.ims.qti21.ui.CandidateSessionContext;
import org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent.Event;

import uk.ac.ed.ph.jqtiplus.node.content.variable.PrintedVariable;
import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemProcessingContext;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPartSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * 
 * Initial date: 01.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTreeComponentRenderer extends AssessmentObjectComponentRenderer {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentTreeComponentRenderer.class);

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		AssessmentTreeComponent component = (AssessmentTreeComponent)source;
		TestSessionController testSessionController = component.getTestSessionController();
		if(!testSessionController.getTestSessionState().isEnded()) {
			CandidateSessionContext candidateSessionContext = component.getCandidateSessionContext();
			final AssessmentTestSession candidateSession = candidateSessionContext.getCandidateSession();
	        if (candidateSession != null && !candidateSession.isExploded() && !candidateSessionContext.isTerminated()) {

	    		CandidateEvent candidateEvent = candidateSessionContext.getLastEvent();
	    		CandidateTestEventType testEventType = candidateEvent.getTestEventType();
	    		
	    		RenderingRequest options;
	        	if (testEventType == CandidateTestEventType.REVIEW_ITEM) {
	            	options = RenderingRequest.getItemReview();
	            }  else if (testEventType == CandidateTestEventType.SOLUTION_ITEM) {
	            	options = RenderingRequest.getItemSolution();
	            } else {
	            	options = RenderingRequest.getItem(testSessionController);
	            }
	        		AssessmentRenderer renderHints = new AssessmentRenderer(renderer);
	        		renderTestEvent(testSessionController, renderHints, sb, component, ubu, translator, options);

			}
		}
	}
	
	private void renderTestEvent(TestSessionController testSessionController, AssessmentRenderer renderer, StringOutput target,
			AssessmentTreeComponent component, URLBuilder ubu, Translator translator, RenderingRequest options) {

		CandidateSessionContext candidateSessionContext = component.getCandidateSessionContext();
		CandidateEvent candidateEvent = candidateSessionContext.getLastEvent();
		CandidateTestEventType testEventType = candidateEvent.getTestEventType();

        final TestSessionState testSessionState = testSessionController.getTestSessionState();
        if(!candidateSessionContext.isTerminated() && !testSessionState.isExited()) {
        	if (testEventType == CandidateTestEventType.REVIEW_ITEM) {
        		renderer.setReviewMode(true);
        	} else if (testEventType == CandidateTestEventType.SOLUTION_ITEM) {
        		renderer.setSolutionMode(true);
        	}
            renderNavigation(renderer, target, component, ubu, translator, options);
        }
    }
	
	private void renderNavigation(AssessmentRenderer renderer, StringOutput sb,
			AssessmentTreeComponent component, URLBuilder ubu, Translator translator, RenderingRequest options) {
		sb.append("<div id='o_qti_menu' class='qtiworks o_assessmenttest o_testpartnavigation o_qti_menu_menustyle' role='navigation'>");
		//part, sections and item refs
		TestPlanNode currentTestPartNode = component.getCurrentTestPartNode();
		if(currentTestPartNode != null) {
			sb.append("<ul class='o_testpartnavigation list-unstyled'>");
			currentTestPartNode.getChildren().forEach((node)
				-> renderNavigation(renderer, sb, component, node, ubu, translator, options));
			sb.append("</ul>");
		}
		sb.append("</div>");
	}
	
	private void renderNavigation(AssessmentRenderer renderer, StringOutput sb, AssessmentTreeComponent component, TestPlanNode node,
			URLBuilder ubu, Translator translator, RenderingRequest options) {
		switch(node.getTestNodeType()) {
			case ASSESSMENT_SECTION:
				renderNavigationAssessmentSection(renderer, sb, component, node, ubu, translator, options);
				break;
			case ASSESSMENT_ITEM_REF:
				renderNavigationAssessmentItem(sb, component, node, translator, options);
				break;
			default: break;
		}
	}
	
	private void renderNavigationAssessmentSection(AssessmentRenderer renderer, StringOutput sb, AssessmentTreeComponent component, TestPlanNode sectionNode,
			URLBuilder ubu, Translator translator, RenderingRequest options) {
		String title = breakTitle(sectionNode.getSectionPartTitle());
		sb.append("<li class='o_assessmentsection o_qti_menu_item'>")
		  .append("<header><h4>").append(StringHelper.escapeHtml(title)).append("</h4>");

		sb.append("</header><ul class='o_testpartnavigation_inner list-unstyled'>");
		sectionNode.getChildren().forEach(child
				-> renderNavigation(renderer, sb, component, child, ubu, translator, options));
		sb.append("</ul></li>");
	}
	
	private void renderNavigationAssessmentItem(StringOutput sb, AssessmentTreeComponent component, TestPlanNode itemNode,
			Translator translator, RenderingRequest options) {
		
		// check if currently rendered item is the active item
		boolean active = false;
		TestSessionController sessionCtr = component.getTestSessionController();
		if (sessionCtr != null && itemNode != null) {
			TestSessionState sessionState = sessionCtr.getTestSessionState();
			if (sessionState != null && sessionState.getCurrentItemKey() != null) {
				TestPlanNodeKey testPlanNodeKey = sessionState.getCurrentItemKey();
				active = (testPlanNodeKey.getIdentifier().equals(itemNode.getIdentifier()));				
			}
		}
		
		sb.append("<li class='o_assessmentitem").append(" active", active).append("'>");
		try {
			renderAssessmentItemMark(sb, component, itemNode, translator);
			renderAssessmentItemAttempts(sb, component, itemNode, translator);
			renderItemStatus(sb, component, itemNode, translator, options);
			renderAssessmentItemLink(sb, component, itemNode, translator);
		} catch(IllegalStateException ex) {
			log.error("", ex);
			sb.append("<span class='o_danger'>ERROR</span>");
		}
		sb.append("</li>");
	}
	
	/**
	 * 
	 * @param sb
	 * @param component
	 * @param itemNode
	 * @return The event used or null
	 */
	private Event renderAssessmentItemLink(StringOutput sb, AssessmentTreeComponent component, TestPlanNode itemNode, Translator translator) {
		String key = itemNode.getKey().toString();
		Form form = component.getQtiItem().getRootForm();
		String dispatchId = component.getQtiItem().getFormDispatchId();
		TestSessionController testSessionController = component.getTestSessionController();
		
		TestSessionState testSessionState = testSessionController.getTestSessionState();
		TestPart testPart = testSessionController.getCurrentTestPart();
		TestPlanNodeKey currentTestPartKey = testSessionState.getCurrentTestPartKey();
		TestPartSessionState currentTestPartSessionState = testSessionState.getTestPartSessionStates().get(currentTestPartKey);

		Event event;
		if(testPart == null || testPart.getNavigationMode() == NavigationMode.NONLINEAR) {
			if(testSessionState.isEnded() || currentTestPartSessionState.isEnded()) {
				if(itemNode.getEffectiveItemSessionControl().isAllowReview() || itemNode.getEffectiveItemSessionControl().isShowFeedback()) {
					event = Event.reviewItem;
				} else {
					event = null;
				}
			} else {
				event = Event.selectItem;
			}
		} else {
			event = null;
		}
	
		if(event == null) {
			sb.append("<span class='o_assessmentitem_nav_disabled'>");
		} else {
			sb.append("<a href='javascript:;' ")
			  .onClickKeyEnter(FormJSHelper.getXHRFnCallFor(form, dispatchId, 1, true, true,
					new NameValuePair("cid", event.name()), new NameValuePair("item", key)))
			  .append(" class='o_sel_assessmentitem'>");
		}
		String title = getTitle(component, itemNode, translator);
		sb.append("<span class='questionTitle'>").append(title).append("</span>");

		if(event == null) {
			sb.append("</span>");
		} else {
			sb.append("</a>");
		}
		return event;
	}
	
	private String getTitle(AssessmentTreeComponent component, TestPlanNode itemNode, Translator translator) {
		String title;
		if(component.isShowTitles()) {
			title = StringHelper.escapeHtml(itemNode.getSectionPartTitle());
		} else {
			int num = component.getCandidateSessionContext().getNumber(itemNode);
			title = translator.translate("question.title", Integer.toString(num));
		}
		return breakTitle(title);
	}
	
	private String breakTitle(String title) {
		if(title != null) {
			title = title.replace("_", " ");
		}
		return title;
	}
	
	private void renderItemStatus(StringOutput sb, AssessmentTreeComponent component, TestPlanNode itemNode,
			Translator translator, RenderingRequest options) {
		ItemProcessingContext itemProcessingContext = component.getItemSessionState(itemNode);
		ItemSessionState itemSessionState = itemProcessingContext.getItemSessionState();
		renderItemStatus(sb, itemSessionState, options, translator);
	}
	
	private void renderAssessmentItemMark(StringOutput sb, AssessmentTreeComponent component, TestPlanNode itemNode, Translator translator) {	
		String key = itemNode.getKey().toString();
		Form form = component.getQtiItem().getRootForm();
		String dispatchId = component.getQtiItem().getFormDispatchId();
		boolean mark = component.getCandidateSessionContext().isMarked(key);

		String title = getTitle(component, itemNode, translator);
		String ariaLabelI18n = mark ? "assessment.item.mark.remove" : "assessment.item.mark.add";
		String ariaLabel = StringHelper.escapeHtml(translator.translate(ariaLabelI18n, title));
		
		sb.append("<a href='javascript:;' onclick=\"")
		  .append(FormJSHelper.getXHRNFFnCallFor(form, dispatchId, 1,
				new NameValuePair("cid", Event.mark.name()),
				new NameValuePair("item", key),
				new NameValuePair(Window.NO_RESPONSE_PARAMETER_MARKER, Window.NO_RESPONSE_VALUE_MARKER)))
		  .append("; o_toggleMark(this); return false;\" onkeydown=\"if(event.which == 13 || event.keyCode == 13) {")
		  .append(FormJSHelper.getXHRNFFnCallFor(form, dispatchId, 1,
					new NameValuePair("cid", Event.mark.name()),
					new NameValuePair("item", key),
					new NameValuePair(Window.NO_RESPONSE_PARAMETER_MARKER, Window.NO_RESPONSE_VALUE_MARKER)))
		  .append("; o_toggleMark(this); return false; }\" ")
		  .append(" class='o_assessmentitem_marks'><i class='o_icon ")
		  .append("o_icon_bookmark", "o_icon_bookmark_add", mark)
		  .append("' aria-label='").appendHtmlAttributeEscaped(ariaLabel)
		  .append("' title='").append(StringHelper.escapeHtml(translator.translate("assessment.item.mark"))).append("'>&nbsp;</i></a>");
	}
	
	private void renderAssessmentItemAttempts(StringOutput sb, AssessmentTreeComponent component, TestPlanNode itemNode,
			Translator translator) {
		ItemProcessingContext itemProcessingContext = component.getItemSessionState(itemNode);
		ItemSessionState itemSessionState = itemProcessingContext.getItemSessionState();
		
		//attempts
		int numOfAttempts = itemSessionState.getNumAttempts();
		int maxAttempts = 0;
		if(itemProcessingContext instanceof ItemSessionController itemSessionController) {
			maxAttempts = itemSessionController.getItemSessionControllerSettings().getMaxAttempts();
		}		
		sb.append("<span class='o_assessmentitem_attempts ");
		if(maxAttempts > 0) {
			if (maxAttempts - numOfAttempts > 0) {
				sb.append("o_assessmentitem_attempts_limited");								
			} else {
				sb.append("o_assessmentitem_attempts_nomore");				
			}
			String title = translator.translate("attemptsleft", Integer.toString((maxAttempts - numOfAttempts)));
			sb.append("' title=\"").append(StringHelper.escapeHtml(title)).append("\">");
			sb.append(numOfAttempts).append(" / ").append(Integer.toString(maxAttempts));
		} else {
			sb.append("'>").append(numOfAttempts);			
		}
		sb.append("</span>");
	}
	
	@Override
	protected void renderPrintedVariable(AssessmentRenderer renderer, StringOutput sb,
			AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState,
			PrintedVariable printedVar) {
		//
	}
}