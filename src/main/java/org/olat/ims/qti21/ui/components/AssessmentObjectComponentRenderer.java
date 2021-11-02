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

import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.checkJavaScript;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.contentAsString;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.exists;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.extractIterableElement;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.extractMathsContentPmathml;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.extractRecordFieldValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.extractResponseInputAt;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.extractSingleCardinalityResponseInput;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.getAtClass;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.getCardinalitySize;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.getHtmlAttributeValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.getOutcomeValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.getResponseDeclaration;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.getResponseInput;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.getResponseValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.getTemplateValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isBadResponse;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isInvalidResponse;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isMathsContentValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isMultipleCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isNullValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isOrderedCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isRecordCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isSingleCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isTemplateDeclarationAMathVariable;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isVisible;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.renderMultipleCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.renderOrderedCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.renderRecordCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.renderSingleCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.valueContains;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.StringOutputPool;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.render.velocity.VelocityHelper;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.QTI21Module;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.XmlUtilities;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import uk.ac.ed.ph.jqtiplus.attribute.Attribute;
import uk.ac.ed.ph.jqtiplus.attribute.AttributeList;
import uk.ac.ed.ph.jqtiplus.attribute.ForeignAttribute;
import uk.ac.ed.ph.jqtiplus.attribute.value.IntegerAttribute;
import uk.ac.ed.ph.jqtiplus.attribute.value.StringAttribute;
import uk.ac.ed.ph.jqtiplus.attribute.value.StringMultipleAttribute;
import uk.ac.ed.ph.jqtiplus.node.ForeignElement;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.InfoControl;
import uk.ac.ed.ph.jqtiplus.node.content.basic.AtomicBlock;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Flow;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Inline;
import uk.ac.ed.ph.jqtiplus.node.content.basic.SimpleBlock;
import uk.ac.ed.ph.jqtiplus.node.content.basic.SimpleInline;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.mathml.Math;
import uk.ac.ed.ph.jqtiplus.node.content.template.TemplateBlock;
import uk.ac.ed.ph.jqtiplus.node.content.template.TemplateInline;
import uk.ac.ed.ph.jqtiplus.node.content.variable.FeedbackBlock;
import uk.ac.ed.ph.jqtiplus.node.content.variable.FeedbackInline;
import uk.ac.ed.ph.jqtiplus.node.content.variable.PrintedVariable;
import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.content.variable.TextOrVariable;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.hypertext.A;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.image.Img;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Dd;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Dl;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.DlElement;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Dt;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Li;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Ol;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.list.Ul;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.object.Object;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Col;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Colgroup;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Table;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.TableCell;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tbody;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tfoot;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Thead;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Tr;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Br;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Div;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Span;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.AssociateInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.CustomInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.FlowInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GapMatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicAssociateInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicGapMatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicOrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MediaInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.PositionObjectInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.SelectPointInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.SliderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Gap;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.PositionObjectStage;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.VariableDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.TestSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetManager;
import uk.ac.ed.ph.qtiworks.mathassess.MathEntryInteraction;

/**
 * 
 * Initial date: 21.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AssessmentObjectComponentRenderer extends DefaultComponentRenderer {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentObjectComponentRenderer.class);
	private static final String velocity_root = Util.getPackageVelocityRoot(AssessmentObjectComponentRenderer.class);
	private static final URI ctopXsltUri = URI.create("classpath:/org/olat/ims/qti21/ui/components/_content/ctop.xsl");
	
	protected void renderExploded(StringOutput sb, Translator translator) {
		sb.append("<div class='o_error'>").append(translator.translate("exploded.msg")).append("</div>");
    }
	
	protected void renderMissingItem(StringOutput sb, Translator translator) {
		sb.append("<div class='o_error'>").append(translator.translate("error.assessment.item.missing")).append("</div>");
    }

    protected void renderTerminated(StringOutput sb, Translator translator) {
		sb.append("<div class='o_info o_sel_assessment_test_terminated'>").append(translator.translate("terminated.msg")).append("</div>");
    }
	
	protected void renderItemStatus(StringOutput sb, ItemSessionState itemSessionState, RenderingRequest options, Translator translator) {
		if(options != null && options.isSolutionMode()) {
			renderItemStatusMessage("review", "assessment.item.status.modelSolution", sb, translator);
		} else if(options != null && options.isReviewMode()) {
			renderItemReviewStatus(sb, itemSessionState, translator);
		} else if(itemSessionState.getEndTime() != null) {
			renderItemStatusMessage("ended", "assessment.item.status.finished", sb, translator);
		} else if(!(itemSessionState.getUnboundResponseIdentifiers().isEmpty() && itemSessionState.getInvalidResponseIdentifiers().isEmpty())) {
			renderItemStatusMessage("invalid", "assessment.item.status.needsAttention", sb, translator);
		} else if(itemSessionState.isResponded()) {
			if(itemSessionState.getUncommittedResponseValues().size() > 0) {
				renderItemStatusMessage("notAnswered", "assessment.item.status.notAnswered", sb, translator);
			} else {
				renderItemStatusMessage("answered", "assessment.item.status.answered", sb, translator);
			}
		} else if(itemSessionState.getEntryTime() != null) {
			renderItemStatusMessage("notAnswered", "assessment.item.status.notAnswered", sb, translator);
		} else {
			renderItemStatusMessage("notPresented", "assessment.item.status.notSeen", sb, translator);
		}
	}
	
	protected void renderItemReviewStatus(StringOutput sb, ItemSessionState itemSessionState, Translator translator) {
		if(!(itemSessionState.getUnboundResponseIdentifiers().isEmpty() && itemSessionState.getInvalidResponseIdentifiers().isEmpty())) {
			renderItemStatusMessage("reviewInvalid", "assessment.item.status.reviewInvalidAnswer", sb, translator);
		} else if(itemSessionState.isResponded()) {
			renderItemStatusMessage("review", "assessment.item.status.review", sb, translator);
		} else if(itemSessionState.getEntryTime() != null) {
			renderItemStatusMessage("reviewNotAnswered", "assessment.item.status.reviewNotAnswered", sb, translator);
		} else {
			renderItemStatusMessage("reviewNotSeen", "assessment.item.status.reviewNotSeen", sb, translator);
		}
		// missing? see AssessmentTestComponentRenderer
		// buildRenderStatus("reviewNotAllowed", "assessment.item.status.reviewNot", sb, translator);

	}
	
	private void renderItemStatusMessage(String status, String i18nKey, StringOutput sb, Translator translator) {
		String title = translator.translate(i18nKey);
		sb.append("<span class='o_assessmentitem_status ").append(status).append(" ' title=\"").append(StringHelper.escapeHtml(title))
		.append("\"><i class='o_icon o_icon-fw o_icon_qti_").append(status).append("'> </i><span>").append(title).append("</span></span>");
	}
	
	protected void renderControl(StringOutput sb, AssessmentObjectComponent component, String title, boolean primary, String cssClass, NameValuePair... pairs) {
		Form form = component.getQtiItem().getRootForm();
		String dispatchId = component.getQtiItem().getFormDispatchId();
		sb.append("<button type='button' ")
		  .onClickKeyEnter(FormJSHelper.getXHRFnCallFor(form, dispatchId, 1, true, true, pairs))
		  .append(" class='btn ").append("btn-primary ", "btn-default ", primary).append(cssClass).append("'").append("><span>").append(title).append("</span></button>");
	}
	
	protected void renderTestItemModalFeedback(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, URLBuilder ubu, Translator translator) {
		if(component.isHideFeedbacks()) return;
		
		List<ModalFeedback> modalFeedbacks = new ArrayList<>();
		AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
		for(ModalFeedback modalFeedback:assessmentItem.getModalFeedbacks()) {
			if(component.isFeedback(modalFeedback, itemSessionState)) {
				modalFeedbacks.add(modalFeedback);
			}
		}
		
		if(modalFeedbacks.size() > 0) {
			sb.append("<div class='modalFeedback'>");
			for(ModalFeedback modalFeedback:modalFeedbacks) {
				Identifier outcomeIdentifier = modalFeedback.getOutcomeIdentifier();
				if(QTI21Constants.FEEDBACKMODAL_IDENTIFIER.equals(outcomeIdentifier)) {
					renderTestItemModalFeedback_feedbackModal(renderer, sb, modalFeedback,
							component, resolvedAssessmentItem, itemSessionState, ubu, translator);
				} else if(QTI21Constants.CORRECT_SOLUTION_IDENTIFIER.equals(outcomeIdentifier)) {
					renderTestItemModalFeedback_correctSolution(renderer, sb, modalFeedback,
							component, resolvedAssessmentItem, itemSessionState, ubu, translator);
				} else {
					renderTestItemModalFeedback_standard(renderer, sb, modalFeedback,
							component, resolvedAssessmentItem, itemSessionState, ubu, translator);
				}
			}
			sb.append("</div>");
		}
	}
	
	protected void renderTestItemModalFeedback_correctSolution(AssessmentRenderer renderer, StringOutput sb, ModalFeedback modalFeedback,
			AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState,
			URLBuilder ubu, Translator translator) {
		
		sb.append("<div class='modalFeedback o_togglebox_wrapper o_block clearfix'>");
		Attribute<?> title = modalFeedback.getAttributes().get("title");
		String feedbackTitle = null;
		if(title != null && title.getValue() != null) {
			feedbackTitle = title.getValue().toString();
		}
		if(!StringHelper.containsNonWhitespace(feedbackTitle)) {
			feedbackTitle = translator.translate("correct.solution");
		}

		sb.append("<h4><a href='#modal-correct-solution' data-toggle='collapse' data-target='#modal-correct-solution' class=\"o_opener\" onclick=\"jQuery(this).toggleClass('o_in'); return false;\"><i class='o_icon o_icon-fw o_icon-lg'> </i> ").append(StringHelper.escapeHtml(feedbackTitle)).append("</a></h4>");
		sb.append("<div id='modal-correct-solution' class='collapse'><div class='o_togglebox_content clearfix'>");
		
		modalFeedback.getFlowStatics().forEach((flow)
			-> renderFlow(renderer, sb, component, resolvedAssessmentItem, itemSessionState, flow, ubu, translator));

		sb.append("</div></div></div>");
		
		/*
		<div class="o_togglebox_wrapper o_block">
		<a href="#$uid" data-toggle="collapse" data-target="#${uid}" class="o_opener" onclick="jQuery(this).toggleClass('o_in'); return false;">
			<i class="o_icon o_icon-fw o_icon-lg"></i> 
			$title
		</a>
		<div id="$uid" class="collapse">
			<div class="o_togglebox_content clearfix">	
		*/
	}
	
	/**
	 * Render the feedback modal generated by OpenOLAT editor.
	 * 
	 * @param renderer
	 * @param sb
	 * @param modalFeedback
	 * @param component
	 * @param resolvedAssessmentItem
	 * @param itemSessionState
	 * @param ubu
	 * @param translator
	 */
	private void renderTestItemModalFeedback_feedbackModal(AssessmentRenderer renderer, StringOutput sb, ModalFeedback modalFeedback,
			AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, URLBuilder ubu, Translator translator) {
		sb.append("<div class='modalFeedback o_info clearfix");
		
		Value feedbackBasic = itemSessionState.getOutcomeValue(QTI21Constants.FEEDBACKBASIC_IDENTIFIER);
		if(feedbackBasic != null && feedbackBasic.hasBaseType(BaseType.IDENTIFIER) && feedbackBasic instanceof IdentifierValue) {
			IdentifierValue identifierValue = (IdentifierValue)feedbackBasic;
			if(AssessmentItemFactory.matchAdditionalFeedback(resolvedAssessmentItem.getRootNodeLookup().extractAssumingSuccessful(), modalFeedback)) {
				sb.append(" o_additional_modal_feedback");
			} else if(QTI21Constants.CORRECT_IDENTIFIER_VALUE.equals(identifierValue)) {
				sb.append(" o_correct_modal_feedback");
			} else if(QTI21Constants.INCORRECT_IDENTIFIER_VALUE.equals(identifierValue)) {
				sb.append(" o_incorrect_modal_feedback");
			} else if(QTI21Constants.EMPTY_IDENTIFIER_VALUE.equals(identifierValue)) {
				sb.append(" o_empty_modal_feedback");
			}
		}

		sb.append("'>");
		Attribute<?> title = modalFeedback.getAttributes().get("title");
		if(title != null && title.getValue() != null) {
			String feedbackTitle = title.getValue().toString();
			if(StringHelper.containsNonWhitespace(feedbackTitle)) {
				sb.append("<h4>");
				if(modalFeedback.getIdentifier() != null && QTI21Constants.HINT_IDENTIFIER.equals(modalFeedback.getIdentifier())) {
					sb.append("<i class='o_icon o_icon_help'> </i> ");
				}
				sb.append(StringHelper.escapeHtml(feedbackTitle)).append("</h4>");
			}
		}
		
		modalFeedback.getFlowStatics().forEach((flow)
			-> renderFlow(renderer, sb, component, resolvedAssessmentItem, itemSessionState, flow, ubu, translator));

		sb.append("</div>");
	}

	/**
	 * Render the standard feedback in an info box.
	 * 
	 * @param renderer
	 * @param sb
	 * @param modalFeedback
	 * @param component
	 * @param resolvedAssessmentItem
	 * @param itemSessionState
	 * @param ubu
	 * @param translator
	 */
	private void renderTestItemModalFeedback_standard(AssessmentRenderer renderer, StringOutput sb, ModalFeedback modalFeedback,
			AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, URLBuilder ubu, Translator translator) {
		sb.append("<div class='modalFeedback o_info clearfix'>");
		Attribute<?> title = modalFeedback.getAttributes().get("title");
		if(title != null && title.getValue() != null) {
			String feedbackTitle = title.getValue().toString();
			if(StringHelper.containsNonWhitespace(feedbackTitle)) {
				sb.append("<h4>");
				if(modalFeedback.getIdentifier() != null && QTI21Constants.HINT_IDENTIFIER.equals(modalFeedback.getIdentifier())) {
					sb.append("<i class='o_icon o_icon_help'> </i> ");
				}
				sb.append(StringHelper.escapeHtml(feedbackTitle)).append("</h4>");
			}
		}
		
		modalFeedback.getFlowStatics().forEach((flow)
			-> renderFlow(renderer, sb, component, resolvedAssessmentItem, itemSessionState, flow, ubu, translator));

		sb.append("</div>");
	}
	
	public void renderFlow(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, Flow flow, URLBuilder ubu, Translator translator) {

		if(flow instanceof Block) {
			renderBlock(renderer, sb, component, resolvedAssessmentItem, itemSessionState, (Block)flow, ubu, translator);
		} else if(flow instanceof Inline) {
			renderInline(renderer, sb, component, resolvedAssessmentItem, itemSessionState, (Inline)flow, ubu, translator);
		} else {
			log.error("What is it for a flow static object: " + flow);
		}
	}
	
	public void renderTextOrVariable(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, TextOrVariable textOrVariable) {
		if(textOrVariable instanceof PrintedVariable) {
			renderPrintedVariable(renderer, sb, component, resolvedAssessmentItem, itemSessionState, (PrintedVariable)textOrVariable);
		} else if(textOrVariable instanceof TextRun) {
			sb.append(((TextRun)textOrVariable).getTextContent());
		} else {
			log.error("What is it for a textOrVariable object: " + textOrVariable);
		}
	}
	
	public void renderBlock(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, Block block, URLBuilder ubu, Translator translator) {
		
		switch(block.getQtiClassName()) {
			case AssociateInteraction.QTI_CLASS_NAME:
			case ChoiceInteraction.QTI_CLASS_NAME:
			case DrawingInteraction.QTI_CLASS_NAME:
			case ExtendedTextInteraction.QTI_CLASS_NAME:
			case GapMatchInteraction.QTI_CLASS_NAME:
			case GraphicAssociateInteraction.QTI_CLASS_NAME:
			case GraphicGapMatchInteraction.QTI_CLASS_NAME:
			case GraphicOrderInteraction.QTI_CLASS_NAME:
			case HotspotInteraction.QTI_CLASS_NAME:
			case SelectPointInteraction.QTI_CLASS_NAME:
			case HottextInteraction.QTI_CLASS_NAME:
			case MatchInteraction.QTI_CLASS_NAME:
			case MediaInteraction.QTI_CLASS_NAME:
			case OrderInteraction.QTI_CLASS_NAME:
			case PositionObjectInteraction.QTI_CLASS_NAME:
			case SliderInteraction.QTI_CLASS_NAME:
			case UploadInteraction.QTI_CLASS_NAME: {
				renderInteraction(renderer, sb, (FlowInteraction)block, resolvedAssessmentItem, itemSessionState, component, ubu, translator);
				break;
			}
			case CustomInteraction.QTI_CLASS_NAME: {
				renderCustomInteraction(renderer, sb, (CustomInteraction<?>)block, resolvedAssessmentItem, itemSessionState, component, ubu, translator);
				break;
			}
			case PositionObjectStage.QTI_CLASS_NAME: {
				renderPositionObjectStage(renderer, sb, (PositionObjectStage)block, resolvedAssessmentItem, itemSessionState, component, ubu, translator);
				break;
			}
			case TemplateBlock.QTI_CLASS_NAME: break;//never rendered
			case InfoControl.QTI_CLASS_NAME: {
				renderInfoControl(renderer, sb, component, resolvedAssessmentItem, itemSessionState, (InfoControl)block, ubu, translator);
				break;
			}
			case FeedbackBlock.QTI_CLASS_NAME: {
				FeedbackBlock feedbackBlock = (FeedbackBlock)block;
				if(component.isFeedback(feedbackBlock, itemSessionState)) {
					sb.append("<div class='o_info feedbackBlock '").append(getAtClass(feedbackBlock)).append(">");
					feedbackBlock.getBlocks().forEach((child)
							-> renderBlock(renderer, sb, component, resolvedAssessmentItem, itemSessionState, child, ubu, translator));
					sb.append("</div>");
				}
				break;
			}
			case RubricBlock.QTI_CLASS_NAME: break; //never rendered automatically
			case Math.QTI_CLASS_NAME: {
				sb.append("<div>");
				renderMath(renderer, sb, component, resolvedAssessmentItem, itemSessionState, (Math)block);
				sb.append("</div>");
				renderer.setMathJax(true);
				break;
			}
			case Div.QTI_CLASS_NAME: {
				if (containsClass(block, "math")) {
					renderer.setMathJax(true);
				}
				renderStartHtmlTag(sb, component, resolvedAssessmentItem, block, null);
				((Div)block).getFlows().forEach((flow)
						-> renderFlow(renderer, sb, component, resolvedAssessmentItem, itemSessionState, flow, ubu, translator));
				renderEndTag(sb, block);
				break;
			}
			case Ul.QTI_CLASS_NAME:
				renderStartHtmlTag(sb, component, resolvedAssessmentItem, block, null);
				((Ul)block).getLis().forEach((li)
						-> renderLi(renderer, sb, component, resolvedAssessmentItem, itemSessionState, li, ubu, translator));
				renderEndTag(sb, block);
				break;
			case Ol.QTI_CLASS_NAME:
				renderStartHtmlTag(sb, component, resolvedAssessmentItem, block, null);
				((Ol)block).getLis().forEach((li)
						-> renderLi(renderer, sb, component, resolvedAssessmentItem, itemSessionState, li, ubu, translator));
				renderEndTag(sb, block);
				break;
			case Dl.QTI_CLASS_NAME:
				renderStartHtmlTag(sb, component, resolvedAssessmentItem, block, null);
				((Dl)block).getDlElements().forEach((dlElement)
						-> renderDlElement(renderer, sb, component, resolvedAssessmentItem, itemSessionState, dlElement, ubu, translator));
				renderEndTag(sb, block);
				break;
				
			case Table.QTI_CLASS_NAME:
				renderTable(renderer, sb, component, resolvedAssessmentItem, itemSessionState, (Table)block, ubu, translator);
				break;
			case Object.QTI_CLASS_NAME:
				break;
			default: {
				renderStartHtmlTag(sb, component, resolvedAssessmentItem, block, null);
				if(block instanceof AtomicBlock) {
					AtomicBlock atomicBlock = (AtomicBlock)block;
					atomicBlock.getInlines().forEach((child)
							-> renderInline(renderer, sb, component, resolvedAssessmentItem, itemSessionState, child, ubu, translator));
					
				} else if(block instanceof SimpleBlock) {
					SimpleBlock simpleBlock = (SimpleBlock)block;
					simpleBlock.getBlocks().forEach((child)
							-> renderBlock(renderer, sb, component, resolvedAssessmentItem, itemSessionState, child, ubu, translator));
				}
				renderEndTag(sb, block);
			}
		}
	}
	
	public void renderTable(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, Table table, URLBuilder ubu, Translator translator) {
		renderStartHtmlTag(sb, component, resolvedAssessmentItem, table, null);
		
		table.getColgroups().forEach(colgroup
				-> renderColgroup(sb, component, resolvedAssessmentItem, colgroup));
		
		Thead thead = table.getThead();
		if(thead != null) {
			renderStartHtmlTag(sb, component, resolvedAssessmentItem, thead, null);
			thead.getTrs().forEach(tr
					-> renderTr(renderer, sb, component, resolvedAssessmentItem, itemSessionState, tr, ubu, translator));
			renderEndTag(sb, thead);
		}
		
		List<Tbody> tbodies = table.getTbodys();
		for(Tbody tbody:tbodies) {
			renderStartHtmlTag(sb, component, resolvedAssessmentItem, tbody, null);
			tbody.getTrs().forEach(tr
					-> renderTr(renderer, sb, component, resolvedAssessmentItem, itemSessionState, tr, ubu, translator));
			renderEndTag(sb, tbody);
		}

		Tfoot tfoot = table.getTfoot();
		if(tfoot != null) {
			renderStartHtmlTag(sb, component, resolvedAssessmentItem, tfoot, null);
			tfoot.getTrs().forEach(tr
					-> renderTr(renderer, sb, component, resolvedAssessmentItem, itemSessionState, tr, ubu, translator));
			renderEndTag(sb, tfoot);
		}
		
		renderEndTag(sb, table);
	}
	
	public void renderColgroup(StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, Colgroup colGroup) {
		renderStartHtmlTag(sb, component, resolvedAssessmentItem, colGroup, null);
		colGroup.getCols().forEach(col
				-> renderCol(sb, component, resolvedAssessmentItem, col));
		renderEndTag(sb, colGroup);
	}
	
	public void renderCol(StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, Col col) {
		sb.append("<").append(col.getQtiClassName());
		renderHtmlTagAttributes(sb, component, resolvedAssessmentItem, col, null);
		
		IntegerAttribute spanAttr = col.getAttributes().getIntegerAttribute(Col.ATTR_SPAN_NAME);
		if(spanAttr.getComputedValue() != null) {
			sb.append(" span=\"").append(spanAttr.getComputedNonNullValue()).append("\"");
		}
		sb.append(">");
		renderEndTag(sb, col);
	}
	
	public void renderTr(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, Tr tr, URLBuilder ubu, Translator translator) {
		renderStartHtmlTag(sb, component, resolvedAssessmentItem, tr, null);
		tr.getTableCells().forEach(cell
				-> renderTableCell(renderer, sb, component, resolvedAssessmentItem, itemSessionState, cell, ubu, translator));
		renderEndTag(sb, tr);
	}
	
	public void renderTableCell(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, TableCell cell, URLBuilder ubu, Translator translator) {
		sb.append("<").append(cell.getQtiClassName());
		renderHtmlTagAttributes(sb, component, resolvedAssessmentItem, cell, null);
		
		IntegerAttribute colSpanAttr = cell.getAttributes().getIntegerAttribute(TableCell.ATTR_COLSPAN_NAME);
		if(colSpanAttr.getComputedValue() != null) {
			sb.append(" colspan=\"").append(colSpanAttr.getComputedNonNullValue()).append("\"");
		}
		IntegerAttribute rowSpanAttr = cell.getAttributes().getIntegerAttribute(TableCell.ATTR_ROWSPAN_NAME);
		if(rowSpanAttr.getComputedValue() != null) {
			sb.append(" rowspan=\"").append(rowSpanAttr.getComputedNonNullValue()).append("\"");
		}
		sb.append(">");
		
		cell.getChildren().forEach(child
				-> renderFlow(renderer, sb, component, resolvedAssessmentItem, itemSessionState, child, ubu, translator));
		renderEndTag(sb, cell);
	}
	
	public void renderLi(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, Li li, URLBuilder ubu, Translator translator) {
		renderStartHtmlTag(sb, component, resolvedAssessmentItem, li, null);
		li.getFlows().forEach((flow)
				-> renderFlow(renderer, sb, component, resolvedAssessmentItem, itemSessionState, flow, ubu, translator));
		renderEndTag(sb, li);
	}
	
	public void renderDlElement(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, DlElement dlElement, URLBuilder ubu, Translator translator) {
		renderStartHtmlTag(sb, component, resolvedAssessmentItem, dlElement, null);
		switch(dlElement.getQtiClassName()) {
			case Dt.QTI_CLASS_NAME:
				((Dt)dlElement).getInlines().forEach((inline)
						-> renderInline(renderer, sb, component, resolvedAssessmentItem, itemSessionState, inline, ubu, translator));
				break;
			case Dd.QTI_CLASS_NAME:
				((Dd)dlElement).getFlows().forEach((flow)
						-> renderFlow(renderer, sb, component, resolvedAssessmentItem, itemSessionState, flow, ubu, translator));
				break;
			default:
				//ignore other type
				break;
		}
		renderEndTag(sb, dlElement);
	}
	
	public void renderInline(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem,
			ItemSessionState itemSessionState, Inline inline, URLBuilder ubu, Translator translator) {
		
		switch(inline.getQtiClassName()) {
			case EndAttemptInteraction.QTI_CLASS_NAME: {
				renderEndAttemptInteraction(renderer, sb, (EndAttemptInteraction)inline, itemSessionState, component, ubu, translator);
				break;
			}
			case InlineChoiceInteraction.QTI_CLASS_NAME:
			case TextEntryInteraction.QTI_CLASS_NAME: {
				renderInteraction(renderer, sb, (FlowInteraction)inline, resolvedAssessmentItem, itemSessionState, component, ubu, translator);
				break;
			}
			case Hottext.QTI_CLASS_NAME: {
				renderHottext(renderer, sb, resolvedAssessmentItem, itemSessionState, (Hottext)inline, component, ubu, translator);
				break;
			}
			case Gap.QTI_CLASS_NAME: {
				renderGap(sb, (Gap)inline, itemSessionState, component);
				break;
			}
			case PrintedVariable.QTI_CLASS_NAME: {
				renderPrintedVariable(renderer, sb, component, resolvedAssessmentItem, itemSessionState, (PrintedVariable)inline);
				break;
			}
			case TemplateInline.QTI_CLASS_NAME: break; //not part of the item body
			case FeedbackInline.QTI_CLASS_NAME: {
				FeedbackInline feedbackInline = (FeedbackInline)inline;
				if(component.isFeedback(feedbackInline, itemSessionState)) {
					sb.append("<span class='feedbackInline ").append(getAtClass(feedbackInline)).append("'>");
					feedbackInline.getInlines().forEach((child)
							-> renderInline(renderer, sb, component, resolvedAssessmentItem, itemSessionState, child, ubu, translator));
					sb.append("</span>");
				}
				break;
			}
			case TextRun.DISPLAY_NAME: {
				String content = ((TextRun)inline).getTextContent();
				if(content != null) {
					sb.append(content.replace("<", "&lt;"));
				}
				break;
			}
			case Math.QTI_CLASS_NAME: {
				sb.append("<span>");
				renderMath(renderer, sb, component, resolvedAssessmentItem, itemSessionState, (Math)inline);
				sb.append("</span>");
				renderer.setMathJax(true);
				break;
			}
			case Img.QTI_CLASS_NAME: {
				renderHtmlTag(sb, component, resolvedAssessmentItem, inline, null);
				break;
			}
			case Br.QTI_CLASS_NAME: {
				sb.append("<br/>");
				break;
			}
			case Span.QTI_CLASS_NAME: {
				renderSpan(renderer, sb, (Span)inline, component, resolvedAssessmentItem, itemSessionState, ubu, translator);
				break;
			}
			case A.QTI_CLASS_NAME: {
				renderA(renderer, sb, (A)inline, component, resolvedAssessmentItem, itemSessionState, ubu, translator);
				break;
			}
			case Object.QTI_CLASS_NAME: {
				renderObject(sb, (Object)inline, component, resolvedAssessmentItem);
				break;
			}
			default: {
				renderStartHtmlTag(sb, component, resolvedAssessmentItem, inline, null);
				if(inline instanceof SimpleInline) {
					SimpleInline simpleInline = (SimpleInline)inline;
					simpleInline.getInlines().forEach((child)
							-> renderInline(renderer, sb, component, resolvedAssessmentItem, itemSessionState, child, ubu, translator));
				}
				renderEndTag(sb, inline);
			}
		}
	}
	
	protected final void renderSpan(AssessmentRenderer renderer, StringOutput sb, Span span, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, URLBuilder ubu, Translator translator) {
		if (containsClass(span,"math")) {
			renderer.setMathJax(true);
		}
		renderStartHtmlTag(sb, component, resolvedAssessmentItem, span, null);
		span.getInlines().forEach((child)
			-> renderInline(renderer, sb, component, resolvedAssessmentItem, itemSessionState, child, ubu, translator));
		renderEndTag(sb, span);
	}
	
	protected final void renderA(AssessmentRenderer renderer, StringOutput sb, A a, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, URLBuilder ubu, Translator translator) {
	
		sb.append("<a");
		boolean target = false;
		for(Attribute<?> attribute:a.getAttributes()) {
			String value = getHtmlAttributeValue(component, resolvedAssessmentItem, attribute);
			if(StringHelper.containsNonWhitespace(value)) {
				String name = attribute.getLocalName();
				if("target".equals(name)) {
					target = true;
				}
				sb.append(" ").append(name).append("=\"").append(value).append("\"");
			}
		}
		if(!target) {
			sb.append(" target=\"_blank\"");
		}
		sb.append(">");

		a.getInlines().forEach((child)
				-> renderInline(renderer, sb, component, resolvedAssessmentItem, itemSessionState, child, ubu, translator));
		renderEndTag(sb, a);
	}
	
	protected final void renderObject(StringOutput sb, Object object, AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem) {
		Attribute<?> attrId = object.getAttributes().get("id");
		if(attrId != null && attrId.getValue() != null && attrId.getValue().toString().startsWith("olatFlashMovieViewer")) {
			//this is a OpenOLAT movie and need to be converted
			renderObjectVideo(sb, attrId, object, component, resolvedAssessmentItem);
		} else {
			renderStartHtmlTag(sb, component, resolvedAssessmentItem, object, null);
			renderEndTag(sb, object);
		}
	}
	
	private final void renderObjectVideo(StringOutput sb, Attribute<?> attrId, Object object, AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem) {
		/*
		<span id="olatFlashMovieViewer213060" class="olatFlashMovieViewer" style="display:block;border:solid 1px #000; width:320px; height:240px;">
		  <script src="/raw/fx-111111x11/movie/player.js"></script>
		  <script defer="defer">// <![CDATA[
		    BPlayer.insertPlayer("demo-video.mp4","olatFlashMovieViewer213060",320,240,0,0,"video",undefined,false,false,true,undefined);
		   // ]]></script>
		</span>
		*/
		String id = attrId.getValue().toString();
		String uniqueId = id + CodeHelper.getForeverUniqueID();
		Attribute<?> dataAttr = object.getAttributes().get("data");
		String data = dataAttr.getValue().toString();
		Attribute<?> attrDataMovie = object.getAttributes().get("data-oo-movie");
		String dataMovie = attrDataMovie.getValue().toString();
		
		if(data != null && !data.startsWith("http://") && !data.startsWith("https://")) {
			String relativePath = component.relativePathTo(resolvedAssessmentItem);
			String src = Settings.createServerURI() + component.getMapperUri();
			if(!src.endsWith("/") && !relativePath.startsWith("/")) {
				src += "/";
			}
			src += relativePath;
			if(!src.endsWith("/") && !data.startsWith("/")) {
				src += "/";
			}
			src += data;
			dataMovie = dataMovie.replace(data, src);
		}
		
		String height = "240";
		String width = "320";
		//try to guess the height and width
		if(dataMovie != null) {
			String[] dataMovieParts = dataMovie.split(",");
			if(dataMovieParts.length > 3) {
				width = dataMovieParts[2];
				height = dataMovieParts[3];
			}
			dataMovie = dataMovie.replace(id, uniqueId);
		}

		sb.append("<span id=\"").append(uniqueId).append("\" class=\"olatFlashMovieViewer\" style=\"display:block;border:solid 1px #000; width:").append(width).append("px; height:").append(height).append("px;\">\n")
		  .append(" <script src=\"");
		Renderer.renderStaticURI(sb, "movie/player.js");
		sb.append("\"></script>\n")
		  .append(" <script defer=\"defer\">")
		  .append("  BPlayer.insertPlayer(").append(dataMovie).append(");\n")
		  .append(" </script>\n")
		  .append("</span>\n");
		
	}
	
	protected final void renderInfoControl(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, InfoControl infoControl, URLBuilder ubu, Translator translator) {
		sb.append("<div class=\"infoControl\">")
		  .append("<button type='button' onclick=\"return QtiWorksRendering.showInfoControlContent(this)\" class='btn btn-default'>")
		  .append("<span>").append(StringHelper.escapeHtml(infoControl.getTitle())).append("</span></button>")
		  .append("<div class='infoControlContent o_info'>");
		infoControl.getChildren().forEach((flow)
				-> renderFlow(renderer, sb, component, resolvedAssessmentItem, itemSessionState, flow, ubu, translator));
		sb.append("</div></div>");
	}
	
	protected final void renderHtmlTag(StringOutput sb, AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem, QtiNode node, String cssClass) {
		sb.append("<").append(node.getQtiClassName());
		for(Attribute<?> attribute:node.getAttributes()) {
			String value = getHtmlAttributeValue(component, resolvedAssessmentItem, attribute);
			if(StringHelper.containsNonWhitespace(value)) {
				String name = attribute.getLocalName();
				sb.append(" ").append(name).append("=\"").append(value);
				if(cssClass != null && name.equals("class")) {
					sb.append(" ").append(cssClass);
				}
				sb.append("\"");
			}
		}
		sb.append(" />");
	}
	
	protected final void renderStartHtmlTag(StringOutput sb, AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem, QtiNode node, String cssClass) {
		sb.append("<").append(node.getQtiClassName());
		renderHtmlTagAttributes(sb, component, resolvedAssessmentItem, node, cssClass);
		sb.append(">");
	}
	
	protected final void renderHtmlTagAttributes(StringOutput sb, AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem, QtiNode node, String cssClass) {
		for(Attribute<?> attribute:node.getAttributes()) {
			String value = getHtmlAttributeValue(component, resolvedAssessmentItem, attribute);
			if(StringHelper.containsNonWhitespace(value)) {
				String name = attribute.getLocalName();
				sb.append(" ").append(name).append("=\"").append(value);
				if(cssClass != null && name.equals("class")) {
					sb.append(" ").append(cssClass);
				}
				sb.append("\"");
			}
		}
	}
	
	protected void renderEndTag(StringOutput sb, QtiNode node) {
		sb.append("</").append(node.getQtiClassName()).append(">");
	}
	
  /*
  <xsl:choose>
    <xsl:when test="$allowComment and $isItemSessionOpen">
      <fieldset class="candidateComment">
        <legend>Please use the following text box if you need to provide any additional information, comments or feedback during this test:</legend>
        <input name="qtiworks_comment_presented" type="hidden" value="true"/>
        <textarea name="qtiworks_comment"><xsl:value-of select="$itemSessionState/qw:candidateComment"/></textarea>
      </fieldset>
    </xsl:when>
    <xsl:when test="$allowComment and $isItemSessionEnded and exists($itemSessionState/qw:candidateComment)">
      <fieldset class="candidateComment">
        <legend>You submitted the following comment with this item:</legend>
        <input name="qtiworks_comment_presented" type="hidden" value="true"/>
        <textarea name="qtiworks_comments" disabled="disabled"><xsl:value-of select="$itemSessionState/qw:candidateComment"/></textarea>
      </fieldset>
    </xsl:when>
  </xsl:choose>
  */
	protected void renderComment(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component, ItemSessionState itemSessionState, Translator translator) {
		if(renderer.isCandidateCommentAllowed()) {
			if(component.isItemSessionOpen(itemSessionState, renderer.isSolutionMode())) {
				String comment = itemSessionState.getCandidateComment();
				renderComment(sb, comment, false, translator);
			} else if(component.isItemSessionEnded(itemSessionState, renderer.isSolutionMode())
					&& StringHelper.containsNonWhitespace(itemSessionState.getCandidateComment())) {
				String comment = itemSessionState.getCandidateComment();
				renderComment(sb, comment, true, translator);
			}
		}
		
	}
	
	private void renderComment(StringOutput sb, String comment, boolean disabled, Translator translator) {
		sb.append("<fieldset class='o_candidatecomment'>")
		  .append("<legend>").append(translator.translate("assessment.comment.legend")).append("</legend>")
		  .append("<div class='o_item_container_help'><p><i class='o_icon o_icon_help'> </i> ").append(translator.translate("assessment.comment.legend.help")).append("</p></div>")
		  .append("<input name='qtiworks_comment_presented' type='hidden' value='true' />")
		  .append("<textarea name='qtiworks_comment'").append(" disabled=\"disabled\"", disabled).append(" rows='4' class='form-control'>");
		if(StringHelper.containsNonWhitespace(comment)) {
			sb.append(comment);
		}
		sb.append("</textarea></fieldset>");
	}
	
	private void renderEndAttemptInteraction(AssessmentRenderer renderer, StringOutput sb, EndAttemptInteraction interaction,
			ItemSessionState itemSessionState, AssessmentObjectComponent component, URLBuilder ubu, Translator translator) {
		if(QTI21Constants.HINT_REQUEST_IDENTIFIER.equals(interaction.getResponseIdentifier())
				&& component.isHideFeedbacks()) {
			return;//don't show our hint's, they trigger feedbacks
		}

		boolean ended =  component.isItemSessionEnded(itemSessionState, renderer.isSolutionMode());
		AssessmentObjectFormItem item = component.getQtiItem();
		String responseUniqueId = component.getResponseUniqueIdentifier(itemSessionState, interaction);
		String id = "qtiworks_response_".concat(responseUniqueId);
		if(!ended) {
			sb.append("<input name=\"qtiworks_presented_").append(responseUniqueId).append("\" type=\"hidden\" value=\"1\"/>");
		}

		FormItem endAttemptButton = item.getFormComponent(id);
		if(endAttemptButton == null) {
			String title = StringHelper.escapeHtml(interaction.getTitle());
			FormLink button = FormUIFactory.getInstance().addFormLink(id, id, title, null, null, Link.BUTTON | Link.NONTRANSLATED);
			// use specific icon for known types
			if (interaction.getResponseIdentifier().equals(QTI21Constants.HINT_REQUEST_IDENTIFIER)) {
				button.setIconLeftCSS("o_icon o_icon-fw o_icon_qti_hint");
				button.setElementCssClass("o_sel_assessment_item_hint");
			}
			endAttemptButton = button;
			endAttemptButton.setTranslator(translator);
			endAttemptButton.setUserObject(interaction);
			
			
			if(item.getRootForm() != endAttemptButton.getRootForm()) {
				endAttemptButton.setRootForm(item.getRootForm());
			}
			item.addFormItem(endAttemptButton);
		}
		endAttemptButton.setEnabled(!ended);
		Component endAttemptCmp = endAttemptButton.getComponent();
		endAttemptCmp.getHTMLRendererSingleton()
				.render(renderer.getRenderer(), sb, endAttemptCmp, ubu, translator, new RenderResult(), null);
		endAttemptCmp.setDirty(false);

	}
	
	private void renderPositionObjectStage(AssessmentRenderer renderer, StringOutput sb, PositionObjectStage positionObjectStage,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, AssessmentObjectComponent component,
			URLBuilder ubu, Translator translator) {
		Context ctx = new VelocityContext();
		ctx.put("positionObjectStage", positionObjectStage);
		String page = getInteractionTemplate(positionObjectStage);
		renderVelocity(renderer, sb, positionObjectStage, ctx, page, resolvedAssessmentItem, itemSessionState, component, ubu, translator);
	}
	
	/**
	 * Render the interaction or the PositionStageObject
	 * @param renderer
	 * @param sb
	 * @param interaction
	 * @param assessmentItem
	 * @param itemSessionState
	 * @param component
	 * @param ubu
	 * @param translator
	 */
	private void renderCustomInteraction(AssessmentRenderer renderer, StringOutput sb, CustomInteraction<?> interaction,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, AssessmentObjectComponent component,
			URLBuilder ubu, Translator translator) {
		Context ctx = new VelocityContext();
		ctx.put("interaction", interaction);

		String page;
		if(interaction instanceof MathEntryInteraction) {
			page = velocity_root.concat("/mathEntryInteraction.html");
		} else {
			page = velocity_root.concat("/unsupportedCustomInteraction.html");
		}
		renderVelocity(renderer, sb, interaction, ctx, page, resolvedAssessmentItem, itemSessionState, component, ubu, translator);
	}
	
	/**
	 * Render the interaction or the PositionStageObject
	 * @param renderer
	 * @param sb
	 * @param interaction
	 * @param assessmentItem
	 * @param itemSessionState
	 * @param component
	 * @param ubu
	 * @param translator
	 */
	private void renderInteraction(AssessmentRenderer renderer, StringOutput sb, FlowInteraction interaction,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, AssessmentObjectComponent component,
			URLBuilder ubu, Translator translator) {
		Context ctx = new VelocityContext();
		ctx.put("interaction", interaction);
		String page = getInteractionTemplate(interaction);
		renderVelocity(renderer, sb, interaction, ctx, page, resolvedAssessmentItem, itemSessionState, component, ubu, translator);
	}
		
	private void renderVelocity(AssessmentRenderer renderer, StringOutput sb, QtiNode interaction, Context ctx, String page,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, AssessmentObjectComponent component,
			URLBuilder ubu, Translator translator) {

		ctx.put("localName", interaction.getQtiClassName());
		ctx.put("assessmentItem", resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful());
		ctx.put("itemSessionState", itemSessionState);
		ctx.put("isItemSessionOpen", component.isItemSessionOpen(itemSessionState, renderer.isSolutionMode()));
		ctx.put("isItemSessionEnded", component.isItemSessionEnded(itemSessionState, renderer.isSolutionMode()));
		ctx.put("isCorrectionHelp", component.isCorrectionHelp());
		ctx.put("isCorrectionSolution", component.isCorrectionSolution());
		ctx.put("isSolutionMode", renderer.isSolutionMode());

		Renderer fr = Renderer.getInstance(component, translator, ubu, new RenderResult(), renderer.getGlobalSettings(), renderer.getRenderer().getCsrfToken());
		AssessmentRenderer fHints = renderer.newHints(fr);
		try(AssessmentObjectVelocityRenderDecorator vrdec
			= new AssessmentObjectVelocityRenderDecorator(fHints, sb, component, resolvedAssessmentItem, itemSessionState, ubu, translator)) {
			ctx.put("r", vrdec);
			VelocityHelper vh = VelocityHelper.getInstance();
			vh.mergeContent(page, ctx, sb, null);
			ctx.remove("r");
			if(!renderer.isMathJax()) {
				renderer.setMathJax(fHints.isMathJax());
			}
		} catch(IOException e) {
			log.error("", e);
		}
		
	}
	
	private String getInteractionTemplate(QtiNode interaction) {
		String interactionName;
		switch(interaction.getQtiClassName()) {
			case "matchInteraction": {
				MatchInteraction matchInteraction = (MatchInteraction)interaction;
				interactionName = interaction.getQtiClassName();
				if(matchInteraction.getResponseIdentifier().toString().startsWith("KPRIM_")) {
					interactionName += "_kprim";
				} else if(hasClass(matchInteraction, QTI21Constants.CSS_MATCH_DRAG_AND_DROP)) {
					interactionName += "_dnd";
				} else if(hasClass(matchInteraction, QTI21Constants.CSS_MATCH_TRUE_FALSE)) {
					interactionName += "_truefalse";
				} else if(hasClass(matchInteraction, QTI21Constants.CSS_MATCH_KPRIM)) {
					interactionName += "_kprim";
				}
				break;
			}
			case "mathEntryInteraction": {
				if(!CoreSpringFactory.getImpl(QTI21Module.class).isMathAssessExtensionEnabled()) {
					interactionName = "mathEntryInteractionNotEnabled";
					break;
				}
			}
			default: interactionName = interaction.getQtiClassName(); break;
		}

		String templateName = interactionName.substring(0, 1).toLowerCase().concat(interactionName.substring(1));
		return velocity_root + "/" + templateName + ".html";
	}
	
	private final boolean hasClass(Interaction interaction, String cssClass) {
		if(interaction == null || cssClass == null) return false;
		
		List<String> cssClasses = interaction.getClassAttr();
		return cssClasses != null && cssClasses.size() > 0 && cssClasses.contains(cssClass);
	}
	
	/*
	  <xsl:template match="qti:gap">
	    <xsl:variable name="gmi" select="ancestor::qti:gapMatchInteraction" as="element(qti:gapMatchInteraction)"/>
	    <xsl:variable name="gaps" select="$gmi//qti:gap" as="element(qti:gap)+"/>
	    <xsl:variable name="thisGap" select="." as="element(qti:gap)"/>
	    <span class="gap" id="qtiworks_id_{$gmi/@responseIdentifier}_{@identifier}">
	      <!-- (Print index of this gap wrt all gaps in the interaction) -->
	      GAP <xsl:value-of select="for $i in 1 to count($gaps) return
	        if ($gaps[$i]/@identifier = $thisGap/@identifier) then $i else ()"/>
	    </span>
	  </xsl:template>
	 */
	private void renderGap(StringOutput sb, Gap gap, ItemSessionState itemSessionState, AssessmentObjectComponent component) {
		GapMatchInteraction interaction = null;
		for(QtiNode parentNode=gap.getParent(); parentNode.getParent() != null; parentNode = parentNode.getParent()) {
			if(parentNode instanceof GapMatchInteraction) {
				interaction = (GapMatchInteraction)parentNode;
				break;
			}
		}
		
		if(interaction != null) {
			List<Gap> gaps = QueryUtils.search(Gap.class, interaction.getBlockStatics());
			String responseUniqueId = component.getResponseUniqueIdentifier(itemSessionState, interaction);
			sb.append("<span class='gap' id=\"qtiworks_id_").append(responseUniqueId)
			  .append("_").append(gap.getIdentifier().toString()).append("\">");
			sb.append("GAP ").append(gaps.indexOf(gap));
			sb.append("</span>");
		}
	}
	
	/*
	  <xsl:template match="qti:hottext">
	    <xsl:if test="qw:is-visible(.)">
	      <xsl:variable name="hottextInteraction" select="ancestor::qti:hottextInteraction" as="element(qti:hottextInteraction)"/>
	      <xsl:variable name="responseIdentifier" select="$hottextInteraction/@responseIdentifier" as="xs:string"/>
	      <span class="hottext">
	        <input type="{if ($hottextInteraction/@maxChoices=1) then 'radio' else 'checkbox'}"
	             name="qtiworks_response_{$responseIdentifier}"
	             value="{@identifier}">
	          <xsl:if test="$isItemSessionEnded">
	            <xsl:attribute name="disabled">disabled</xsl:attribute>
	          </xsl:if>
	          <xsl:if test="qw:value-contains(qw:get-response-value(/, $responseIdentifier), @identifier)">
	            <xsl:attribute name="checked" select="'checked'"/>
	          </xsl:if>
	        </input>
	        <xsl:apply-templates/>
	      </span>
	    </xsl:if>
	  </xsl:template>
	 */
	private void renderHottext(AssessmentRenderer renderer, StringOutput sb, ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, Hottext hottext,
			AssessmentObjectComponent component, URLBuilder ubu, Translator translator) {
		if(!isVisible(hottext, itemSessionState)) return;
		
		HottextInteraction interaction = null;
		for(QtiNode parentNode=hottext.getParent(); parentNode.getParent() != null; parentNode = parentNode.getParent()) {
			if(parentNode instanceof HottextInteraction) {
				interaction = (HottextInteraction)parentNode;
				break;
			}
		}
		
		if(interaction != null) {
			sb.append("<span class='hottext'><input type='");
			if(interaction.getMaxChoices() == 1) {
				sb.append("radio");
			} else {
				sb.append("checkbox");
			}
			String guid = "oo_" + CodeHelper.getRAMUniqueID();
			String responseUniqueId = component.getResponseUniqueIdentifier(itemSessionState, interaction);
			sb.append("' id='").append(guid).append("' name='qtiworks_response_").append(responseUniqueId).append("'")
			  .append(" value='").append(hottext.getIdentifier().toString()).append("'");
			if(component.isItemSessionEnded(itemSessionState, renderer.isSolutionMode())) {
				sb.append(" disabled");
			}
			AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
			Value responseValue = getResponseValue(assessmentItem, itemSessionState, interaction.getResponseIdentifier(), renderer.isSolutionMode());
			if(valueContains(responseValue, hottext.getIdentifier())) {
				sb.append(" checked");
			}
			sb.append(" />");
			sb.append("<label for='").append(guid).append("'>");
			hottext.getInlineStatics().forEach((inline)
					-> renderInline(renderer, sb, component, resolvedAssessmentItem, itemSessionState, inline, ubu, translator));
			FormJSHelper.appendFlexiFormDirtyOn(sb, component.getQtiItem().getRootForm(), "change click", guid);
			sb.append("</label></span>");
		}
	}
	
	protected void renderExtendedTextBox(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component, AssessmentItem assessmentItem,
			ItemSessionState itemSessionState, ExtendedTextInteraction interaction, Translator translator) {
		
		ResponseData responseInput = getResponseInput(itemSessionState, interaction.getResponseIdentifier());
		ResponseDeclaration responseDeclaration = getResponseDeclaration(assessmentItem, interaction.getResponseIdentifier());
		Cardinality cardinality = responseDeclaration == null ? null : responseDeclaration.getCardinality();
		if(cardinality != null && (cardinality.isRecord() || cardinality.isSingle())) {
			String responseInputString = extractSingleCardinalityResponseInput(responseInput);
			renderExtendedTextBox(renderer, sb, component, assessmentItem, itemSessionState, interaction, responseInputString, translator);
		} else {
			if(interaction.getMaxStrings() != null) {
				int maxStrings = interaction.getMaxStrings().intValue();
				for(int i=0; i<maxStrings; i++) {
					String responseInputString = extractResponseInputAt(responseInput, i);
					renderExtendedTextBox(renderer, sb, component, assessmentItem, itemSessionState, interaction, responseInputString, translator);
				}	
			} else {
				// <xsl:with-param name="stringsCount" select="if (exists($responseValue)) then max(($minStrings, qw:get-cardinality-size($responseValue))) else $minStrings"/>
				int stringCounts = interaction.getMinStrings();
				Value responseValue = AssessmentRenderFunctions
						.getResponseValue(assessmentItem, itemSessionState, interaction.getResponseIdentifier(), renderer.isSolutionMode());
				if(exists(responseValue)) {
					stringCounts = java.lang.Math.max(interaction.getMinStrings(), getCardinalitySize(responseValue));	
				}
				
				for(int i=0; i<stringCounts; i++) {
					String responseInputString = extractResponseInputAt(responseInput, i);
					renderExtendedTextBox(renderer, sb, component, assessmentItem, itemSessionState, interaction,
							responseInputString, translator);
				}
			}
		}
	}
	
	
	/*
	  <xsl:template match="qti:extendedTextInteraction" mode="multibox">
	    <xsl:param name="responseInput" as="element(qw:responseInput)?"/>
	    <xsl:param name="checkJavaScript" as="xs:string?"/>
	    <xsl:param name="stringsCount" as="xs:integer"/>
	    <xsl:param name="allowCreate" select="false()" as="xs:boolean"/>
	    <xsl:variable name="interaction" select="." as="element(qti:extendedTextInteraction)"/>
	    <xsl:for-each select="1 to $stringsCount">
	      <xsl:variable name="i" select="." as="xs:integer"/>
	      <xsl:apply-templates select="$interaction" mode="singlebox">
	        <xsl:with-param name="responseInputString" select="$responseInput/qw:string[position()=$i]"/>
	        <xsl:with-param name="checkJavaScript" select="$checkJavaScript"/>
	        <xsl:with-param name="allowCreate" select="$allowCreate and $i=$stringsCount"/>
	      </xsl:apply-templates>
	      <br />
	    </xsl:for-each>
	  </xsl:template>
	  
	  <xsl:template match="qti:extendedTextInteraction" mode="singlebox">
	    <xsl:param name="responseInputString" as="xs:string?"/>
	    <xsl:param name="checkJavaScript" as="xs:string?"/>
	    <xsl:param name="allowCreate" select="false()" as="xs:boolean"/>
	    <xsl:variable name="is-bad-response" select="qw:is-bad-response(@responseIdentifier)" as="xs:boolean"/>
	    <xsl:variable name="is-invalid-response" select="qw:is-invalid-response(@responseIdentifier)" as="xs:boolean"/>
	    <textarea cols="72" rows="6" name="qtiworks_response_{@responseIdentifier}">
	      <xsl:if test="$isItemSessionEnded">
	        <xsl:attribute name="disabled">disabled</xsl:attribute>
	      </xsl:if>
	      <xsl:if test="$is-bad-response or $is-invalid-response">
	        <xsl:attribute name="class" select="'badResponse'"/>
	      </xsl:if>
	      <xsl:if test="@expectedLines">
	        <xsl:attribute name="rows" select="@expectedLines"/>
	      </xsl:if>
	      <xsl:if test="@expectedLines and @expectedLength">
	        <xsl:attribute name="cols" select="ceiling(@expectedLength div @expectedLines)"/>
	      </xsl:if>
	      <xsl:if test="$checkJavaScript">
	        <xsl:attribute name="onchange" select="$checkJavaScript"/>
	      </xsl:if>
	      <xsl:if test="$allowCreate">
	        <xsl:attribute name="onkeyup" select="'QtiWorksRendering.addNewTextBox(this)'"/>
	      </xsl:if>
	      <xsl:value-of select="$responseInputString"/>
	    </textarea>
	  </xsl:template>
	*/
	protected final void renderExtendedTextBox(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component, AssessmentItem assessmentItem,
			ItemSessionState itemSessionState, ExtendedTextInteraction interaction, String responseInputString, Translator translator) {
		
		List<String> cssClasses = interaction.getClassAttr();
		boolean copyPasteDisabled = cssClasses != null && cssClasses.contains(QTI21Constants.CSS_ESSAY_DISABLE_COPYPASTE);
		boolean hasPlaceholder = StringHelper.containsNonWhitespace(interaction.getPlaceholderText());
		
		String responseUniqueId = component.getResponseUniqueIdentifier(itemSessionState, interaction);
		boolean ended = component.isItemSessionEnded(itemSessionState, renderer.isSolutionMode());
		int expectedLines = interaction.getExpectedLines() == null ? 6 : interaction.getExpectedLines().intValue();
		if(ended) {
			if(renderer.isSolutionMode() && renderer.isReport()) {
				expectedLines = 1;// resized textarea for solution in reports
			}
			sb.append("<div id='oo_").append(responseUniqueId).append("' style='min-height:").append(expectedLines * 1.5).append("em;' class='form-control textarea_disabled o_disabled o_form_element_disabled");
		} else {
			sb.append("<textarea id='oo_").append(responseUniqueId).append("' name='qtiworks_response_").append(responseUniqueId).append("'");
	
			if(hasPlaceholder || copyPasteDisabled) {
				sb.append(" placeholder=\"");
				if(hasPlaceholder) {
					sb.append(StringHelper.escapeHtml(interaction.getPlaceholderText()));
				}
				if(copyPasteDisabled) {
					if(hasPlaceholder) {
						sb.append(" &#10;");
					}
					sb.append(translator.translate("essay.copypaste.disabled"));
				}
				sb.append("\"");
			}
			
			sb.append(" rows='").append(expectedLines).append("'");
			if(interaction.getExpectedLength() == null) {
				sb.append(" cols='72'");
			} else {
				int cols = interaction.getExpectedLength().intValue() / expectedLines;
				sb.append(" cols='").append(cols).append("'");
			}
			
			ResponseDeclaration responseDeclaration = getResponseDeclaration(assessmentItem, interaction.getResponseIdentifier());
			String checkJavascript = checkJavaScript(responseDeclaration, interaction.getPatternMask());
			if(StringHelper.containsNonWhitespace(checkJavascript)) {
				sb.append(" onchange=\"").append(checkJavascript).append("\"");
			}
			sb.append(" class='form-control");
		}
		
		if(isBadResponse(itemSessionState, interaction.getResponseIdentifier())
				|| isInvalidResponse(itemSessionState, interaction.getResponseIdentifier())) {
			sb.append(" badResponse");
		}
		sb.append("'>");
		
		if(renderer.isSolutionMode()) {
			String placeholder = interaction.getPlaceholderText();
			if(StringHelper.containsNonWhitespace(placeholder)) {
				sb.append(placeholder);
			}
		} else if(StringHelper.containsNonWhitespace(responseInputString)) {
			if(ended) {
				sb.append(StringHelper.escapeHtml(responseInputString));
			} else {
				sb.append(responseInputString);
			}
		}
		
		if(ended) {
			sb.append("</div>");
		} else {
			sb.append("</textarea>");
			
			FormJSHelper.appendFlexiFormDirty(sb, component.getQtiItem().getRootForm(), "oo_" + responseUniqueId);
			sb.append(FormJSHelper.getJSStartWithVarDeclaration("oo_" + responseUniqueId))
			//plain textAreas should not propagate the keypress "enter" (keynum = 13) as this would submit the form
			  .append("oo_").append(responseUniqueId).append(".on('keypress', function(event, target){if (13 == event.keyCode) {event.stopPropagation()} })")
			  .append(FormJSHelper.getJSEnd());
			
			String[] wordPlaceholder = new String[] { "xxx" };
			Form form = component.getQtiItem().getRootForm();
			sb.append(FormJSHelper.getJSStart())
			  .append("jQuery(function() {\n")
			  .append(" jQuery('#").append("oo_").append(responseUniqueId).append("').qtiAutosave({\n")
			  .append("  responseUniqueId:'").append(responseUniqueId).append("',\n")
			  .append("  formName:'").append(form.getFormName()).append("',\n")
			  .append("  dispIdField:'").append(form.getDispatchFieldId()).append("',\n")
			  .append("  dispId:'").append(component.getQtiItem().getFormDispatchId()).append("',\n")
			  .append("  eventIdField:'").append(form.getEventFieldId()).append("',\n")
			  .append("  csrf:'").append(renderer.getRenderer().getCsrfToken()).append("',\n")
			  .append(" })");
			if(copyPasteDisabled) {
				String errorHeader = translator.translate("essay.copypaste.disabled.header");
				String errorMessage = translator.translate("essay.copypaste.disabled");
				sb.append(".qtiCopyPaste({\n")
				  .append("  errorHeader:'").append(StringHelper.escapeJavaScript(errorHeader)).append("',")
				  .append("  errorMessage:'").append(StringHelper.escapeJavaScript(errorMessage)).append("'")
				  .append("})\n");
			}
			sb.append(".qtiCountWord({\n")
			  .append("  responseUniqueId:'").append(responseUniqueId).append("',\n")
			  .append("  labelSingular:'").append(translator.translate("word.count", wordPlaceholder)).append("',\n")
			  .append("  labelPlural:'").append(translator.translate("word.count.plural", wordPlaceholder)).append("'\n")
			  .append(" }).tabOverride();\n")
			  .append("})\n")
			  .append(FormJSHelper.getJSEnd());
		}
	}
	
	protected abstract void renderPrintedVariable(AssessmentRenderer renderer, StringOutput sb,
			AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState,
			PrintedVariable printedVar);
	
	/**
	 * The QTI spec says that this variable must have single cardinality.
	 * 
	 * For convenience, we also accept multiple, ordered and record cardinality variables here,
	 * printing them out in a hard-coded form that probably won't make sense to test
	 * candidates but might be useful for debugging.
	 * 
	 * Our implementation additionally adds support for "printing" MathsContent variables
	 * used in MathAssess, outputting an inline Presentation MathML element, as documented
	 * in the MathAssses spec.
	 */
	protected void renderPrintedVariable(AssessmentRenderer renderer, StringOutput sb, PrintedVariable source, VariableDeclaration valueDeclaration, Value valueHolder) {
		if(isNullValue(valueHolder)) {
			//(Spec says to output nothing in this case)
		} else if(isSingleCardinalityValue(valueHolder)) {
			if(valueDeclaration.hasBaseType(BaseType.INTEGER) || valueDeclaration.hasBaseType(BaseType.FLOAT)) {
				renderSingleCardinalityValue(sb, valueHolder);
			} else {
				renderSingleCardinalityValue(sb, valueHolder);
			}
		// math content is a record with special markers
		} else if (isMathsContentValue(valueHolder)) {
			//<xsl:copy-of select="qw:extract-maths-content-pmathml($valueHolder)"/>
			String mathMlContent = extractMathsContentPmathml(valueHolder);
			if(renderer.isMathXsltDisabled()) {
				sb.append(mathMlContent);
			} else {
				transformMathmlAsString(sb, mathMlContent);
			}
		} else if(isMultipleCardinalityValue(valueHolder)) {
			String delimiter = source.getDelimiter();
			if(!StringHelper.containsNonWhitespace(delimiter)) {
				delimiter = ";";
			}
			renderMultipleCardinalityValue(sb, valueHolder, delimiter);
		} else if(isOrderedCardinalityValue(valueHolder)) {
			if(source.getIndex() != null) {
				int index = -1;
				if(source.getIndex().isConstantInteger()) {
					index = source.getIndex().getConstantIntegerValue().intValue();
				} else if(source.getIndex().isVariableRef()) {
					//TODO qti what to do???
				}
				SingleValue indexedValue = extractIterableElement(valueHolder, index);
				renderSingleCardinalityValue(sb, indexedValue);
			} else {
				String delimiter = source.getDelimiter();
				if(!StringHelper.containsNonWhitespace(delimiter)) {
					delimiter = ";";
				}
				renderOrderedCardinalityValue(sb, valueHolder, delimiter);
			}
		} else if(isRecordCardinalityValue(valueHolder)) {
			String field = source.getField();
			if(StringHelper.containsNonWhitespace(field)) {
				Identifier fieldIdentifier = Identifier.assumedLegal(field);
				SingleValue mappedValue = extractRecordFieldValue(valueHolder, fieldIdentifier);
				renderSingleCardinalityValue(sb, mappedValue);
			} else {
				String delimiter = source.getDelimiter();
				String mappingIndicator = source.getMappingIndicator();
				if(!StringHelper.containsNonWhitespace(delimiter)) {
					delimiter = ";";
				}
				if(!StringHelper.containsNonWhitespace(mappingIndicator)) {
					mappingIndicator = "=";
				}
				renderRecordCardinalityValue(sb, valueHolder, delimiter, mappingIndicator);
			}
		} else {
			sb.append("printedVariable may not be applied to value ").append(valueHolder.toString());
		}
	}
	
	protected void renderMath(AssessmentRenderer renderer, StringOutput sb, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, Math math) {
		
		renderer.setMathXsltDisabled(true);
		try(StringOutput mathOutput = StringOutputPool.allocStringBuilder(2048)) {
			if (!math.getAttributes().contains("xmlns")) {
				StringAttribute xmlnsAttribute = new StringAttribute(math, "xmlns", false);
				xmlnsAttribute.setValue("http://www.w3.org/1998/Math/MathML");
				math.getAttributes().add(xmlnsAttribute);
			}
			renderStartHtmlTag(mathOutput, component, resolvedAssessmentItem, math, null);
			math.getContent().forEach(foreignElement
					-> renderMath(renderer, mathOutput, component, resolvedAssessmentItem, itemSessionState, foreignElement));
			renderEndTag(mathOutput, math);
			String enrichedMathML = StringOutputPool.freePop(mathOutput);
			renderer.setMathXsltDisabled(false);
			transformMathmlAsString(sb, enrichedMathML);
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	protected void renderMath(AssessmentRenderer renderer, StringOutput out, AssessmentObjectComponent component,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, QtiNode mathElement) {
		if(mathElement instanceof ForeignElement) {
			ForeignElement fElement = (ForeignElement)mathElement;
			boolean mi = fElement.getQtiClassName().equals("mi");
			boolean ci = fElement.getQtiClassName().equals("ci");
			
			if(ci || mi) {
				AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
				
				String text = contentAsString(fElement);
				Identifier identifier = Identifier.assumedLegal(text);
				Value templateValue = getTemplateValue(itemSessionState, text);
				Value outcomeValue = getOutcomeValue(itemSessionState, text);
				Value responseValue = getResponseValue(assessmentItem, itemSessionState, identifier, renderer.isSolutionMode());
				if(templateValue != null && isTemplateDeclarationAMathVariable(assessmentItem, text)) {
					if(ci) {
						substituteCi(out, templateValue);
					} else if(mi) {
						substituteMi(out, templateValue);
					}
				} else if(outcomeValue != null) {
					if(ci) {
						substituteCi(out, outcomeValue);
					} else if(mi) {
						substituteMi(out, outcomeValue);
					}
				}  else if(responseValue != null) {
					if(ci) {
						substituteCi(out, responseValue);
					} else if(mi) {
						substituteMi(out, responseValue);
					}
				} else {
					renderStartHtmlTag(out, component, resolvedAssessmentItem, fElement, null);
					fElement.getChildren().forEach((child)
							-> renderMath(renderer, out, component, resolvedAssessmentItem, itemSessionState, child));
					renderEndTag(out, fElement);
				}
			} else {
				renderStartHtmlTag(out, component, resolvedAssessmentItem, fElement, null);
				fElement.getChildren().forEach((child)
						-> renderMath(renderer, out, component, resolvedAssessmentItem, itemSessionState, child));
				renderEndTag(out, fElement);
			}
		} else if(mathElement instanceof TextRun) {
			out.append(StringHelper.escapeXml(((TextRun)mathElement).getTextContent()));
		}
	}
	
	/*
	  <xsl:template name="substitute-mi" as="element()">
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:param name="value" as="element()"/>
    <xsl:choose>
      <xsl:when test="qw:is-null-value($value)">
        <!-- We shall represent null as an empty mrow -->
        <xsl:element name="mrow" namespace="http://www.w3.org/1998/Math/MathML"/>
      </xsl:when>
      <xsl:when test="qw:is-single-cardinality-value($value)">
        <!-- Single cardinality template variables are substituted according to Section 6.3.1 of the
        spec. Note that it does not define what should be done with multiple and ordered
        cardinality variables. -->
        <xsl:element name="mn" namespace="http://www.w3.org/1998/Math/MathML">
          <xsl:copy-of select="@*"/>
          <xsl:value-of select="qw:extract-single-cardinality-value($value)"/>
        </xsl:element>
      </xsl:when>
      <xsl:when test="qw:is-maths-content-value($value)">
        <!-- This is a MathAssess MathsContent variable. What we do here is
        replace the matched MathML element with the child(ren) of the <math/> PMathML field
        in this record, wrapping in an <mrow/> if required so as to ensure that we have a
        single replacement element -->
        <xsl:variable name="pmathml" select="qw:extract-maths-content-pmathml($value)" as="element(m:math)"/>
        <xsl:choose>
          <xsl:when test="count($pmathml/*)=1">
            <xsl:copy-of select="$pmathml/*"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="mrow" namespace="http://www.w3.org/1998/Math/MathML">
              <xsl:copy-of select="$pmathml/*"/>
            </xsl:element>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- Unsupported substitution -->
        <xsl:message>
          Substituting the variable <xsl:value-of select="$identifier"/> with value
          <xsl:copy-of select="$value"/>
          within MathML is not currently supported.
        </xsl:message>
        <xsl:element name="mtext" namespace="http://www.w3.org/1998/Math/MathML">(Unsupported variable substitution)</xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
	
	 */
	protected void substituteMi(StringOutput sb, Value value) {
		if(value == null || value.isNull()) {
			sb.append("<mrow />");
		} else if(isSingleCardinalityValue(value)) {
			sb.append("<mn>");//<xsl:copy-of select="@*"/>
			renderSingleCardinalityValue(sb, value);
			sb.append("</mn>");
		} else if(isMathsContentValue(value)) {
			String mathMlContent = extractMathsContentPmathml(value);
			sb.append(mathMlContent);
		} else {
			//not supported
		}
	}
	/*
  <xsl:template name="substitute-ci" as="element()*">
    <xsl:param name="identifier" as="xs:string"/>
    <xsl:param name="value" as="element()"/>
    <xsl:choose>
      <xsl:when test="qw:is-null-value($value)">
        <!-- We shall omit nulls -->
      </xsl:when>
      <xsl:when test="qw:is-single-cardinality-value($value)">
        <!-- Single cardinality template variables are substituted according to Section 6.3.1 of the
        spec. Note that it does not define what should be done with multiple and ordered
        cardinality variables. -->
        <xsl:element name="cn" namespace="http://www.w3.org/1998/Math/MathML">
          <xsl:copy-of select="@*"/>
          <xsl:value-of select="qw:extract-single-cardinality-value($value)"/>
        </xsl:element>
      </xsl:when>
      <xsl:when test="qw:is-maths-content-value($value)">
        <!-- This is a MathAssess MathsContent variable. What we do here is
        replace the matched MathML element with the child(ren) of the <math/> PMathML field
        in this record, wrapping in an <mrow/> if required so as to ensure that we have a
        single replacement element -->
        <xsl:variable name="cmathml" select="qw:extract-maths-content-cmathml($value)" as="element(m:math)"/>
        <xsl:copy-of select="$cmathml/*"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Unsupported substitution -->
        <xsl:message>
          Substituting the variable <xsl:value-of select="$identifier"/> with value
          <xsl:copy-of select="$value"/>
          within MathML is not currently supported.
        </xsl:message>
        <xsl:element name="mtext" namespace="http://www.w3.org/1998/Math/MathML">(Unsupported variable substitution)</xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
	 */
	protected void substituteCi(StringOutput sb, Value value) {
		if(value == null || value.isNull()) {
			//we omit null
		} else if(isSingleCardinalityValue(value)) {
			sb.append("<cn>");//<xsl:copy-of select="@*"/>
			renderSingleCardinalityValue(sb, value);
			sb.append("</cn>");
		} else if(isMathsContentValue(value)) {
			String mathMlContent = extractMathsContentPmathml(value);
			sb.append(mathMlContent);
		} else {
			//not supported
		}
	}
	
	protected void transformMathmlAsString(StringOutput sb, String mathmlAsString) {
		if(!StringHelper.containsNonWhitespace(mathmlAsString)) {
			return;
		}

		XsltStylesheetManager stylesheetManager = CoreSpringFactory.getImpl(QTI21Service.class).getXsltStylesheetManager();
		final TransformerHandler mathmlTransformerHandler = stylesheetManager.getCompiledStylesheetHandler(ctopXsltUri, null);

        try {
            mathmlTransformerHandler.setResult(new StreamResult(sb));
            final XMLReader xmlReader = XmlUtilities.createNsAwareSaxReader();
            xmlReader.setContentHandler(mathmlTransformerHandler);
        	
            Reader mathStream = new StringReader(mathmlAsString);
            InputSource assessmentSaxSource = new InputSource(mathStream);
            xmlReader.parse(assessmentSaxSource);
        } catch (final Exception e) {
            log.error("Rendering XSLT pipeline failed for request", e);
            sb.append("<span class='o_error'>ERROR MATHML</span>");
        }
	}

	protected boolean containsClass(QtiNode element, String marker) {
		AttributeList attributes = element.getAttributes();
		for(int i=attributes.size(); i-->0; ) {
			Attribute<?> attr = attributes.get(i);
			if("class".equals(attr.getLocalName())) {
				if(attr instanceof ForeignAttribute) {
					String css = ((ForeignAttribute)attr).getValue();
					return css != null && css.contains(marker);
				} else if(attr instanceof StringMultipleAttribute) {
					List<String> css = ((StringMultipleAttribute)attr).getValue();
					return css != null && css.contains(marker);
				}
			}
		}
		return false;
	}
	
    public static class RenderingRequest {
    	
    	private boolean reviewMode;
    	private boolean solutionMode;
    	private boolean testPartNavigationAllowed;
    	private boolean advanceTestItemAllowed;
    	private boolean nextItemAllowed;
    	private boolean endTestPartAllowed;

    	
    	private RenderingRequest(boolean reviewMode, boolean solutionMode, boolean testPartNavigationAllowed,
    			boolean advanceTestItemAllowed, boolean nextItemAllowed, boolean endTestPartAllowed) {
    		this.reviewMode = reviewMode;
    		this.solutionMode = solutionMode;
    		this.testPartNavigationAllowed = testPartNavigationAllowed;
    		this.advanceTestItemAllowed = advanceTestItemAllowed;
    		this.nextItemAllowed = nextItemAllowed;
    		this.endTestPartAllowed = endTestPartAllowed;
    	}

		public boolean isReviewMode() {
			return reviewMode;
		}

		public boolean isSolutionMode() {
			return solutionMode;
		}

		public boolean isTestPartNavigationAllowed() {
			return testPartNavigationAllowed;
		}

		public boolean isAdvanceTestItemAllowed() {
			return advanceTestItemAllowed;
		}
		
		public boolean isNextItemAllowed() {
			return nextItemAllowed;
		}

		public boolean isEndTestPartAllowed() {
			return endTestPartAllowed;
		}
    	
    	public static RenderingRequest getItemSolution() {
    		return new RenderingRequest(true, true, false, false, false, false);
    	}
    	
    	public static RenderingRequest getItemReview() {
    		return new RenderingRequest(true, false, false, false, false, false);
    	}
    	
    	public static RenderingRequest getItem(TestSessionController testSessionController) {
    		final TestPart currentTestPart = testSessionController.getCurrentTestPart();
            final NavigationMode navigationMode = currentTestPart == null ? null : currentTestPart.getNavigationMode();
          
            boolean nextItemAllowed = navigationMode == NavigationMode.NONLINEAR;
            boolean advanceTestItemAllowed = navigationMode == NavigationMode.LINEAR
            		&& testSessionController.getTestSessionState().getCurrentItemKey() != null//mayAdvanceItemLinear assert on the current selected item
            		&& testSessionController.mayAdvanceItemLinear();
            boolean testPartNavigationAllowed = navigationMode == NavigationMode.NONLINEAR;
            boolean endTestPartAllowed = navigationMode == NavigationMode.LINEAR
            		&& testSessionController.getCurrentTestPart() != null
            		&& testSessionController.mayEndCurrentTestPart();

            return new RenderingRequest(false, false, testPartNavigationAllowed, advanceTestItemAllowed, nextItemAllowed, endTestPartAllowed);
    	}
    }
}
