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
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.isVisible;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.renderMultipleCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.renderOrderedCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.renderRecordCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.renderSingleCardinalityValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.renderValue;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.valueContains;

import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.StringOutputPool;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.render.velocity.VelocityHelper;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.ims.qti21.ui.rendering.QtiWorksRenderingException;
import org.olat.ims.qti21.ui.rendering.XmlUtilities;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import uk.ac.ed.ph.jqtiplus.attribute.Attribute;
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
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.image.Img;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Br;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.Div;
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
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.Value;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ClassPathResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.SimpleXsltStylesheetCache;
import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetManager;
import uk.ac.ed.ph.qtiworks.mathassess.MathEntryInteraction;

/**
 * 
 * Initial date: 21.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AssessmentObjectComponentRenderer extends DefaultComponentRenderer {
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentObjectComponentRenderer.class);
	private static final String velocity_root = Util.getPackageVelocityRoot(AssessmentObjectComponentRenderer.class);
	
	protected void renderItemStatus(StringOutput sb, ItemSessionState itemSessionState) {
		if(itemSessionState.getEndTime() != null) {
			sb.append("<span class='itemStatus ended'>Finished</span>");
		} else if(!(itemSessionState.getUnboundResponseIdentifiers().isEmpty() && itemSessionState.getInvalidResponseIdentifiers().isEmpty())) {
			sb.append("<span class='itemStatus invalid'>Needs Attention</span>");
		} else if(itemSessionState.isResponded() || itemSessionState.getUncommittedResponseValues().size() > 0) {
			sb.append("<span class='itemStatus answered'>Answered</span>");
		} else if(itemSessionState.getEntryTime() != null) {
			sb.append("<span class='itemStatus notAnswered'>Not Answered</span>");
		} else {
			sb.append("<span class='itemStatus notPresented'>Not Seen</span>");
		}
	}
	
	protected void renderTestItemModalFeedback(Renderer renderer, StringOutput sb, AssessmentObjectComponent component,
			AssessmentItem assessmentItem, ItemSessionState itemSessionState, URLBuilder ubu, Translator translator) {
		List<ModalFeedback> modalFeedbacks = new ArrayList<>();
		for(ModalFeedback modalFeedback:assessmentItem.getModalFeedbacks()) {
			if(component.isFeedback(modalFeedback, itemSessionState)) {
				modalFeedbacks.add(modalFeedback);
			}
		}
		
		if(modalFeedbacks.size() > 0) {
			sb.append("<div class='modalFeedback'>")
			  .append("<h2>").append(translator.translate("assessment.item.modal.feedback")).append("</h2>");
			for(ModalFeedback modalFeedback:modalFeedbacks) {
				sb.append("<div class='modalFeedback o_info'>");
				Attribute<?> title = modalFeedback.getAttributes().get("title");
				if(title != null && title.getValue() != null) {
					sb.append(title.getValue().toString());
				}
				
				modalFeedback.getFlowStatics().forEach((flow)
					-> renderFlow(renderer, sb, component, assessmentItem, itemSessionState, flow, ubu, translator));

				sb.append("</div>");
			}
			
			sb.append("</div>");
		}
	}
	
	public void renderFlow(Renderer renderer, StringOutput sb, AssessmentObjectComponent component,
			AssessmentItem assessmentItem, ItemSessionState itemSessionState, Flow flow, URLBuilder ubu, Translator translator) {
		
		if(flow instanceof Block) {
			renderBlock(renderer, sb, component, assessmentItem, itemSessionState, (Block)flow, ubu, translator);
		} else if(flow instanceof Inline) {
			renderInline(renderer, sb, component, assessmentItem, itemSessionState, (Inline)flow, ubu, translator);
		} else {
			log.error("What is it for a flow static object: " + flow);
		}
	}
	
	public void renderTextOrVariable(StringOutput sb, AssessmentObjectComponent component,
			AssessmentItem assessmentItem, ItemSessionState itemSessionState, TextOrVariable textOrVariable) {
		if(textOrVariable instanceof PrintedVariable) {
			renderPrintedVariable(sb, component, assessmentItem, itemSessionState, (PrintedVariable)textOrVariable);
		} else if(textOrVariable instanceof TextRun) {
			sb.append(((TextRun)textOrVariable).getTextContent());
		} else {
			log.error("What is it for a textOrVariable object: " + textOrVariable);
		}
	}
	
	public void renderBlock(Renderer renderer, StringOutput sb, AssessmentObjectComponent component,
			AssessmentItem assessmentItem, ItemSessionState itemSessionState, Block block, URLBuilder ubu, Translator translator) {
		
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
				renderInteraction(renderer, sb, (FlowInteraction)block, assessmentItem, itemSessionState, component, ubu, translator);
				break;
			}
			case CustomInteraction.QTI_CLASS_NAME: {
				renderCustomInteraction(renderer, sb, (CustomInteraction<?>)block, assessmentItem, itemSessionState, component, ubu, translator);
				break;
			}
			case PositionObjectStage.QTI_CLASS_NAME: {
				renderPositionObjectStage(renderer, sb, (PositionObjectStage)block, assessmentItem, itemSessionState, component, ubu, translator);
				break;
			}
			case TemplateBlock.QTI_CLASS_NAME: break;//never rendered
			case InfoControl.QTI_CLASS_NAME: {
				//TODO popup some infos
				break;
			}
			case FeedbackBlock.QTI_CLASS_NAME: {
				FeedbackBlock feedbackBlock = (FeedbackBlock)block;
				if(component.isFeedback(feedbackBlock, itemSessionState)) {
					sb.append("<div class='o_info feedbackBlock '").append(getAtClass(feedbackBlock)).append(">");
					feedbackBlock.getBlocks().forEach((child)
							-> renderBlock(renderer, sb, component, assessmentItem, itemSessionState, child, ubu, translator));
					sb.append("</div>");
				}
				break;
			}
			case RubricBlock.QTI_CLASS_NAME: break; //never rendered automatically
			case Math.QTI_CLASS_NAME: {
				renderMath(sb, component, itemSessionState, (Math)block);
				break;
			}
			case Div.QTI_CLASS_NAME:
				renderStartHtmlTag(sb, component, block, null);
				((Div)block).getFlows().forEach((flow)
						-> renderFlow(renderer, sb, component, assessmentItem, itemSessionState, flow, ubu, translator));
				renderEndTag(sb, block);
				break;
			default: {
				renderStartHtmlTag(sb, component, block, null);
				if(block instanceof AtomicBlock) {
					AtomicBlock atomicBlock = (AtomicBlock)block;
					atomicBlock.getInlines().forEach((child)
							-> renderInline(renderer, sb, component, assessmentItem, itemSessionState, child, ubu, translator));
					
				} else if(block instanceof SimpleBlock) {
					SimpleBlock simpleBlock = (SimpleBlock)block;
					simpleBlock.getBlocks().forEach((child)
							-> renderBlock(renderer, sb, component, assessmentItem, itemSessionState, child, ubu, translator));
				}
				renderEndTag(sb, block);
			}
		}
	}
	
	public void renderInline(Renderer renderer, StringOutput sb, AssessmentObjectComponent component, AssessmentItem assessmentItem,
			ItemSessionState itemSessionState, Inline inline, URLBuilder ubu, Translator translator) {
		
		switch(inline.getQtiClassName()) {
			case EndAttemptInteraction.QTI_CLASS_NAME:
			case InlineChoiceInteraction.QTI_CLASS_NAME:
			case TextEntryInteraction.QTI_CLASS_NAME: {
				renderInteraction(renderer, sb, (FlowInteraction)inline, assessmentItem, itemSessionState, component, ubu, translator);
				break;
			}
			case Hottext.QTI_CLASS_NAME: {
				renderHottext(renderer, sb, assessmentItem, itemSessionState, (Hottext)inline, component, ubu, translator);
				break;
			}
			case Gap.QTI_CLASS_NAME: {
				renderGap(sb, (Gap)inline);
				break;
			}
			case PrintedVariable.QTI_CLASS_NAME: {
				renderPrintedVariable(sb, component, assessmentItem, itemSessionState, (PrintedVariable)inline);
				break;
			}
			case TemplateInline.QTI_CLASS_NAME: break; //not part of the item body
			case FeedbackInline.QTI_CLASS_NAME: {
				FeedbackInline feedbackInline = (FeedbackInline)inline;
				if(component.isFeedback(feedbackInline, itemSessionState)) {
					sb.append("<span class='feedbackBlock ").append(getAtClass(feedbackInline)).append("'>");
					feedbackInline.getInlines().forEach((child)
							-> renderInline(renderer, sb, component, assessmentItem, itemSessionState, child, ubu, translator));
					sb.append("</span>");
				}
				break;
			}
			case TextRun.DISPLAY_NAME: {
				sb.append(((TextRun)inline).getTextContent());
				break;
			}
			case Math.QTI_CLASS_NAME: {
				renderMath(sb, component, itemSessionState, (Math)inline);
				break;
			}
			case Img.QTI_CLASS_NAME: {
				renderHtmlTag(sb, component, inline, null);
				break;
			}
			case Br.QTI_CLASS_NAME: {
				sb.append("<br/>");
				break;
			}
			default: {
				renderStartHtmlTag(sb, component, inline, null);
				if(inline instanceof SimpleInline) {
					SimpleInline simpleInline = (SimpleInline)inline;
					simpleInline.getInlines().forEach((child)
							-> renderInline(renderer, sb, component, assessmentItem, itemSessionState, child, ubu, translator));
				}
				renderEndTag(sb, inline);
			}
		}
	}
	
	private void renderHtmlTag(StringOutput sb, AssessmentObjectComponent component, QtiNode node, String cssClass) {
		sb.append("<").append(node.getQtiClassName());
		for(Attribute<?> attribute:node.getAttributes()) {
			String value = getHtmlAttributeValue(component, attribute);
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
	
	private void renderStartHtmlTag(StringOutput sb, AssessmentObjectComponent component, QtiNode node, String cssClass) {
		sb.append("<").append(node.getQtiClassName());
		for(Attribute<?> attribute:node.getAttributes()) {
			String value = getHtmlAttributeValue(component, attribute);
			if(StringHelper.containsNonWhitespace(value)) {
				String name = attribute.getLocalName();
				sb.append(" ").append(name).append("=\"").append(value);
				if(cssClass != null && name.equals("class")) {
					sb.append(" ").append(cssClass);
				}
				sb.append("\"");
			}
		}
		sb.append(">");
	}
	
	private void renderEndTag(StringOutput sb, QtiNode node) {
		sb.append("</").append(node.getQtiClassName()).append(">");
	}
	
	private void renderPositionObjectStage(Renderer renderer, StringOutput sb, PositionObjectStage positionObjectStage,
			AssessmentItem assessmentItem, ItemSessionState itemSessionState, AssessmentObjectComponent component,
			URLBuilder ubu, Translator translator) {
		Context ctx = new VelocityContext();
		ctx.put("positionObjectStage", positionObjectStage);
		String page = getInteractionTemplate(positionObjectStage);
		renderVelocity(renderer, sb, positionObjectStage, ctx, page, assessmentItem, itemSessionState, component, ubu, translator);
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
	private void renderCustomInteraction(Renderer renderer, StringOutput sb, CustomInteraction<?> interaction,
			AssessmentItem assessmentItem, ItemSessionState itemSessionState, AssessmentObjectComponent component,
			URLBuilder ubu, Translator translator) {
		Context ctx = new VelocityContext();
		ctx.put("interaction", interaction);

		String page;
		if(interaction instanceof MathEntryInteraction) {
			page = getInteractionTemplate(interaction);
		} else {
			page = velocity_root + "/unsupportedCustomInteraction.html";
		}
		renderVelocity(renderer, sb, interaction, ctx, page, assessmentItem, itemSessionState, component, ubu, translator);
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
	private void renderInteraction(Renderer renderer, StringOutput sb, FlowInteraction interaction,
			AssessmentItem assessmentItem, ItemSessionState itemSessionState, AssessmentObjectComponent component,
			URLBuilder ubu, Translator translator) {
		Context ctx = new VelocityContext();
		ctx.put("interaction", interaction);
		String page = getInteractionTemplate(interaction);
		renderVelocity(renderer, sb, interaction, ctx, page, assessmentItem, itemSessionState, component, ubu, translator);
	}
		
	private void renderVelocity(Renderer renderer, StringOutput sb, QtiNode interaction, Context ctx, String page,
			AssessmentItem assessmentItem, ItemSessionState itemSessionState, AssessmentObjectComponent component,
			URLBuilder ubu, Translator translator) {

		ctx.put("localName", interaction.getQtiClassName());
		ctx.put("assessmentItem", assessmentItem);
		ctx.put("itemSessionState", itemSessionState);
		ctx.put("isItemSessionOpen", component.isItemSessionOpen(itemSessionState, false));//TODO qti $solutionMode
		ctx.put("isItemSessionEnded", component.isItemSessionEnded(itemSessionState, false));//TODO qti $solutionMode

		log.audit("Render: " + page);
		
		Renderer fr = Renderer.getInstance(component, translator, ubu, new RenderResult(), renderer.getGlobalSettings());
		AssessmentObjectVelocityRenderDecorator vrdec = new AssessmentObjectVelocityRenderDecorator(fr, sb, component, assessmentItem, itemSessionState, ubu, translator);			
		ctx.put("r", vrdec);
		VelocityHelper vh = VelocityHelper.getInstance();
		vh.mergeContent(page, ctx, sb, null);
		ctx.remove("r");
		IOUtils.closeQuietly(vrdec);
	}
	
	private String getInteractionTemplate(QtiNode interaction) {
		String interactionName = interaction.getClass().getSimpleName();
		String templateName = interactionName.substring(0, 1).toLowerCase().concat(interactionName.substring(1));
		String page = velocity_root + "/" + templateName + ".html";
		return page;
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
	private void renderGap(StringOutput sb, Gap gap) {
		
		GapMatchInteraction interaction = null;
		for(QtiNode parentNode=gap.getParent(); parentNode.getParent() != null; parentNode = parentNode.getParent()) {
			if(parentNode instanceof GapMatchInteraction) {
				interaction = (GapMatchInteraction)parentNode;
				break;
			}
		}
		
		if(interaction != null) {
			List<Gap> gaps = QueryUtils.search(Gap.class, interaction.getBlockStatics());
			sb.append("<span class='gap' id=\"qtiworks_id_").append(interaction.getResponseIdentifier().toString())
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
	private void renderHottext(Renderer renderer, StringOutput sb, AssessmentItem assessmentItem, ItemSessionState itemSessionState, Hottext hottext,
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
			sb.append("' name='qtiworks_response_").append(interaction.getResponseIdentifier().toString()).append("'")
			  .append(" value='").append(hottext.getIdentifier().toString()).append("'");
			if(component.isItemSessionEnded(itemSessionState, false)) {//TODO qti solutionMode
				sb.append(" disabled");
			}
			Value responseValue = getResponseValue(assessmentItem, itemSessionState, interaction.getResponseIdentifier(), false);//TODO qti solutionMode
			if(valueContains(responseValue, hottext.getIdentifier())) {
				sb.append(" checked");
			}
			sb.append(" />");
			hottext.getInlineStatics().forEach((inline)
					-> renderInline(renderer, sb, component, assessmentItem, itemSessionState, inline, ubu, translator));
			sb.append("</span>");
		}
	}
	
	protected void renderExtendedTextBox(StringOutput sb, AssessmentObjectComponent component, AssessmentItem assessmentItem,
			ItemSessionState itemSessionState, ExtendedTextInteraction interaction) {
		
		ResponseData responseInput = getResponseInput(itemSessionState, interaction.getResponseIdentifier());
		ResponseDeclaration responseDeclaration = getResponseDeclaration(assessmentItem, interaction.getResponseIdentifier());
		Cardinality cardinality = responseDeclaration.getCardinality();
		if(cardinality.isRecord() || cardinality.isSingle()) {
			String responseInputString = extractSingleCardinalityResponseInput(responseInput);
			renderExtendedTextBox(sb, component, assessmentItem, itemSessionState, interaction, responseInputString, false);
		} else {
			if(interaction.getMaxStrings() != null) {
				int maxStrings = interaction.getMaxStrings().intValue();
				for(int i=0; i<maxStrings; i++) {
					String responseInputString = extractResponseInputAt(responseInput, i);
					renderExtendedTextBox(sb, component, assessmentItem, itemSessionState, interaction, responseInputString, false);
				}	
			} else {
				// <xsl:with-param name="stringsCount" select="if (exists($responseValue)) then max(($minStrings, qw:get-cardinality-size($responseValue))) else $minStrings"/>
				int stringCounts = interaction.getMinStrings();
				Value responseValue = AssessmentRenderFunctions
						.getResponseValue(assessmentItem, itemSessionState, interaction.getResponseIdentifier(), false);//TODO qti solutionMode
				if(exists(responseValue)) {
					stringCounts = java.lang.Math.max(interaction.getMinStrings(), getCardinalitySize(responseValue));	
				}
				
				for(int i=0; i<stringCounts; i++) {
					String responseInputString = extractResponseInputAt(responseInput, i);
					renderExtendedTextBox(sb, component, assessmentItem, itemSessionState, interaction,
							responseInputString, i == (stringCounts - 1));
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
	protected void renderExtendedTextBox(StringOutput sb, AssessmentObjectComponent component, AssessmentItem assessmentItem,
			ItemSessionState itemSessionState, ExtendedTextInteraction interaction, String responseInputString, boolean allowCreate) {
		
		sb.append("<textarea name='qtiworks_response_").append(interaction.getResponseIdentifier().toString()).append("'");
		if(component.isItemSessionEnded(itemSessionState, false)) {//TODO qti solutionMode
			sb.append(" disabled");
		}
		if(isBadResponse(itemSessionState, interaction.getResponseIdentifier())
				|| isInvalidResponse(itemSessionState, interaction.getResponseIdentifier())) {
			sb.append(" class='badResponse'");
		}
		
		int expectedLines = interaction.getExpectedLength() == null ? 6 : interaction.getExpectedLines().intValue();
		sb.append(" rows='").append(expectedLines).append("'");
		if(interaction.getExpectedLength() == null) {
			sb.append(" cols='72'");
		} else {
			int cols = interaction.getExpectedLength().intValue() / expectedLines;
			sb.append(" cols='").append(cols).append("'");
		}
		
		if(allowCreate) {
			sb.append(" onkeyup='QtiWorksRendering.addNewTextBox(this)'");
		}
		
		ResponseDeclaration responseDeclaration = getResponseDeclaration(assessmentItem, interaction.getResponseIdentifier());
		String checkJavascript = checkJavaScript(responseDeclaration, interaction.getPatternMask());
		if(StringHelper.containsNonWhitespace(checkJavascript)) {
			sb.append(" onchange=\"").append(checkJavascript).append("\">");
		}
		if(StringHelper.containsNonWhitespace(responseInputString)) {
			sb.append(responseInputString);
		}
		sb.append("</textarea>");
	}
	
	protected abstract void renderPrintedVariable(StringOutput sb,
			AssessmentObjectComponent component, AssessmentItem assessmentItem, ItemSessionState itemSessionState,
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
	protected void renderPrintedVariable(StringOutput sb, PrintedVariable source, VariableDeclaration valueDeclaration, Value valueHolder) {
		if(isNullValue(valueHolder)) {
			//(Spec says to output nothing in this case)
		} else if(isSingleCardinalityValue(valueHolder)) {
			if(valueDeclaration.hasBaseType(BaseType.INTEGER) || valueDeclaration.hasBaseType(BaseType.FLOAT)) {
				//TODO qti format
				renderSingleCardinalityValue(sb, valueHolder);
			} else {
				renderSingleCardinalityValue(sb, valueHolder);
			}
		// math content is a record with special markers
		} else if (isMathsContentValue(valueHolder)) {
			//<!-- MathAssess math variable -->
			//<xsl:copy-of select="qw:extract-maths-content-pmathml($valueHolder)"/>
			//TODO qti renderMathmlAsString(sb, extractMathsContentPmathml(valueHolder));
			sb.append(extractMathsContentPmathml(valueHolder));
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
	
	protected void renderMath(StringOutput sb, AssessmentObjectComponent component, ItemSessionState itemSessionState, Math math) {
		StringOutput mathOutput = StringOutputPool.allocStringBuilder(2048);
		math.getContent().forEach((foreignElement) -> renderMath(mathOutput, component, itemSessionState, foreignElement));
		String enrichedMathML = StringOutputPool.freePop(mathOutput);
		//renderMathmlAsString(sb, enrichedMathML);
		sb.append(enrichedMathML);
	}
	
	protected void renderMath(StringOutput out, AssessmentObjectComponent component, ItemSessionState itemSessionState, QtiNode mathElement) {
		//List<ForeignElement> content = math.hasChildNodes();
		if(mathElement instanceof ForeignElement) {
			ForeignElement fElement = (ForeignElement)mathElement;
			if(fElement.getQtiClassName().equals("annotation")) {
				//do nothing
			} else if(fElement.getQtiClassName().equals("mi") || fElement.getQtiClassName().equals("ci")) {
				String text = contentAsString(fElement);
				Value templateValue = getTemplateValue(itemSessionState, text);
				Value outcomeValue = getOutcomeValue(itemSessionState, text);
				if(templateValue != null) {
					renderValue(out, templateValue);
				} else if(outcomeValue != null) {
					renderValue(out, outcomeValue);
				} else {
					out.append("<").append(mathElement.getQtiClassName()).append(">");
					fElement.getChildren().forEach((child) -> renderMath(out, component, itemSessionState, child));
					out.append("</").append(mathElement.getQtiClassName()).append(">");
				}
			} else {
				out.append("<").append(mathElement.getQtiClassName()).append(">");
				fElement.getChildren().forEach((child) -> renderMath(out, component, itemSessionState, child));
				out.append("</").append(mathElement.getQtiClassName()).append(">");
			}
		} else if(mathElement instanceof TextRun) {
			out.append(((TextRun)mathElement).getTextContent());
		}
	}
	
	protected void renderMathmlAsString(StringOutput sb, String mathmlAsString) {
		if(!StringHelper.containsNonWhitespace(mathmlAsString)) return;

		XsltStylesheetManager stylesheetManager = new XsltStylesheetManager(new ClassPathResourceLocator(), new SimpleXsltStylesheetCache());
    	URI ctopXsltUri = URI.create("classpath:/org/olat/ims/qti21/ui/components/_content/ctop.xsl");
    	final TransformerHandler mathmlTransformerHandler = stylesheetManager.getCompiledStylesheetHandler(ctopXsltUri, null);

        try {
        	StringOutput out = new StringOutput(255);
            mathmlTransformerHandler.setResult(new StreamResult(out));
            final XMLReader xmlReader = XmlUtilities.createNsAwareSaxReader(false);
            xmlReader.setContentHandler(mathmlTransformerHandler);
        	
            Reader mathStream = new StringReader(mathmlAsString);
            InputSource assessmentSaxSource = new InputSource(mathStream);
            xmlReader.parse(assessmentSaxSource);
            sb.append(out);
        } catch (final Exception e) {
            log.error("Rendering XSLT pipeline failed for request {}", e);
            throw new QtiWorksRenderingException("Unexpected Exception running rendering XML pipeline", e);
        }
	}
	
	public static class RenderHints {
		
		private FlowInteraction currentInteraction;
		private boolean disableMathXslt;
		
		public FlowInteraction getCurrentInteraction() {
			return currentInteraction;
		}
		
		public void setCurrentInteraction(FlowInteraction currentInteraction) {
			this.currentInteraction = currentInteraction;
		}
		
		public boolean isDisableMathXslt() {
			return disableMathXslt;
		}
		
		public void setDisableMathXslt(boolean disableMathXslt) {
			this.disableMathXslt = disableMathXslt;
		}
	}
}
