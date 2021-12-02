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

import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.exists;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.extractResponseInputAt;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.getCardinalitySize;
import static org.olat.ims.qti21.ui.components.AssessmentRenderFunctions.renderValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.manager.CorrectResponsesUtil;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.AbstractEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.HotspotAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.HottextAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.ui.CandidateSessionContext;
import org.olat.ims.qti21.ui.QTIWorksAssessmentTestEvent;

import uk.ac.ed.ph.jqtiplus.attribute.value.StringMultipleAttribute;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.BodyElement;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.basic.BlockStatic;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Flow;
import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.node.content.variable.TextOrVariable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Shape;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.AssociateInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.EndAttemptInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GapMatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicAssociateInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicGapMatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.GraphicOrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Prompt;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.SliderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.StringInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.GapChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.GapImg;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Gap;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.AssociableHotspot;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.graphic.HotspotChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.MapEntry;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.Mapping;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.DirectedPairValue;
import uk.ac.ed.ph.jqtiplus.value.FileValue;
import uk.ac.ed.ph.jqtiplus.value.ListValue;
import uk.ac.ed.ph.jqtiplus.value.NullValue;
import uk.ac.ed.ph.jqtiplus.value.Orientation;
import uk.ac.ed.ph.jqtiplus.value.RecordValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.StringValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 14.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentObjectVelocityRenderDecorator extends VelocityRenderDecorator {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentObjectVelocityRenderDecorator.class);

	private final URLBuilder ubu;
	private final AssessmentRenderer renderer;
	private final StringOutput target;
	private final Translator translator;
	
	
	private final AssessmentItem assessmentItem;
	private final ItemSessionState itemSessionState;
	private final ResolvedAssessmentItem resolvedAssessmentItem;
	
	private final AssessmentObjectComponent avc;
	
	
	public AssessmentObjectVelocityRenderDecorator(AssessmentRenderer renderer, StringOutput target, AssessmentObjectComponent vc,
			ResolvedAssessmentItem resolvedAssessmentItem, ItemSessionState itemSessionState, URLBuilder ubu, Translator translator) {
		super(renderer.getRenderer(), vc, target);
		this.avc = vc;
		this.ubu = ubu;
		this.target = target;
		this.renderer = renderer;
		this.translator = translator;
		this.itemSessionState = itemSessionState;
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		this.assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
	}

	public boolean isSolutionMode() {
		return renderer.isSolutionMode();
	}
	
	public boolean isScorePerAnswers() {
		return avc.isScorePerAnswers();
	}
	
	public boolean isItemSessionOpen() {
		return avc.isItemSessionOpen(itemSessionState, isSolutionMode());
	}
	
	//isItemSessionEnded" as="xs:boolean" select="$itemSessionState/@endTime!='' or $solutionMode
	public boolean isItemSessionEnded() {
		return avc.isItemSessionEnded(itemSessionState, isSolutionMode());
	}
	
	public String getAssessmentTestSessionKey() {
		CandidateSessionContext ctx = avc.getCandidateSessionContext();
		if(ctx != null) {
			AssessmentTestSession candidateSession = ctx.getCandidateSession();
			return candidateSession == null ? "" : candidateSession.getKey().toString();
		}
		return "";
	}
	
	public String ffXHREvent(String key, String value) {
		Form theForm = avc.getQtiItem().getRootForm();
		String elementId = "o_fi".concat(avc.getDispatchID());
		return FormJSHelper.getXHRFnCallFor(theForm, elementId, 1, true, true, new NameValuePair(key, value));
	}
	
	public String ffXHREvent(String key1, String value1, String key2, String value2) {
		Form theForm = avc.getQtiItem().getRootForm();
		String elementId = "o_fi".concat(avc.getDispatchID());
		return FormJSHelper.getXHRFnCallFor(theForm, elementId, 1, true, true,
				new NameValuePair(key1, value1), new NameValuePair(key2, value2));
	}
	
	public String appendFlexiFormDirty(String id) {
		FormJSHelper.appendFlexiFormDirty(target, avc.getQtiItem().getRootForm(), id);
		return "";
	}
	
	public String appendFlexiFormDirtyForCheckbox(String id) {
		FormJSHelper.appendFlexiFormDirtyForCheckbox(target, avc.getQtiItem().getRootForm(), id);
		return "";
	}
	
	public String appendFlexiFormDirtyForClick(String id) {
		FormJSHelper.appendFlexiFormDirtyForClick(target, avc.getQtiItem().getRootForm(), id);
		return "";
	}
	
	public String appendFlexiFormDirtyOn(String id, String events) {
		FormJSHelper.appendFlexiFormDirtyOn(target, avc.getQtiItem().getRootForm(), events, id);
		return "";
	}
	
	public String convertLink(String uri) {
		return AssessmentRenderFunctions.convertLink(avc, resolvedAssessmentItem, "href", uri);
	}
	
	public String convertLinkFull(String uri) {
		return AssessmentRenderFunctions.convertLink(avc, resolvedAssessmentItem, "data", uri);
	}
	
	public String convertLinkAbsolut(String uri) {
		String url = AssessmentRenderFunctions.convertLink(avc, resolvedAssessmentItem, "data", uri);
		String path = Settings.getServerContextPathURI();
		return path.concat(url);
	}
	
	public String convertSubmissionLinkFull(String uri) {
		return AssessmentRenderFunctions.convertSubmissionLink(avc, uri);
	}
	
	public String getFormDispatchFieldId() {
		return avc.getQtiItem().getRootForm().getDispatchFieldId();
	}
	
	public boolean isNullValue(Value value) {
		return value == null || value.isNull();
	}
	
	public boolean isNotNullValue(Value value) {
		return value != null && !value.isNull();
	}
	
	/**
	 * Generate a unique ID
	 * @return
	 */
	public String responseUniqueId(Interaction interaction) {
		return avc.getResponseUniqueIdentifier(itemSessionState, interaction);
	}
	
	/**
	 * For upload interaction
	 * 
	 * @param value
	 * @return
	 */
	public boolean notEmpty(Value value) {
		if(value instanceof FileValue) {
			FileValue fValue = (FileValue)value;
			return fValue.getFile() != null;
		}
		return value != null && !value.isNull();
	}
	
	//<xsl:if test="qw:is-invalid-response(@responseIdentifier)">
	public boolean isInvalidResponse(Identifier identifier) {
		//$itemSessionState/@invalidResponseIdentifiers
		return AssessmentRenderFunctions.isInvalidResponse(itemSessionState, identifier);
	}
	
	//<xsl:sequence select="$unboundResponseIdentifiers=$identifier"/>
	public boolean isBadResponse(Identifier identifier) {
		return AssessmentRenderFunctions.isBadResponse(itemSessionState, identifier);
	}
	
	/**
	 * Check the maxChoices and the cardinality
	 * @param interaction
	 * @return
	 */
	public boolean isSingleChoice(Interaction interaction) {
		if(interaction instanceof ChoiceInteraction) {
			ChoiceInteraction choiceInteraction = (ChoiceInteraction)interaction;
			boolean sc = choiceInteraction.getMaxChoices() == 1;
			ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(choiceInteraction.getResponseIdentifier());
			if(responseDeclaration != null && responseDeclaration.hasCardinality(Cardinality.MULTIPLE)) {
				return false;
			}
			return sc;
		} else if(interaction instanceof HottextInteraction) {
			HottextInteraction hottextInteraction = (HottextInteraction)interaction;
			boolean sc = hottextInteraction.getMaxChoices() == 1;
			ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(hottextInteraction.getResponseIdentifier());
			if(responseDeclaration != null && responseDeclaration.hasCardinality(Cardinality.MULTIPLE)) {
				return false;
			}
			return sc;
		} else if(interaction instanceof HotspotInteraction) {
			HotspotInteraction hotspotInteraction = (HotspotInteraction)interaction;
			ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(hotspotInteraction.getResponseIdentifier());
			if(responseDeclaration != null && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	public String getOrientation(Orientation orientation) {
		if(orientation == null) {
			return Orientation.VERTICAL.toQtiString();
		}
		return orientation.toQtiString();
	}
	
	//<xsl:variable name="minStrings" select="if (@minStrings) then xs:integer(@minStrings) else 1" as="xs:integer"/>
	public String getMinStrings(ExtendedTextInteraction interaction) {
		int minStrings = interaction.getMinStrings();
		return Integer.toString(minStrings);
	}
	
	public String getMaxStrings(ExtendedTextInteraction interaction) {
		Integer maxStrings = interaction.getMaxStrings();
		return maxStrings == null ? "()" : Integer.toString(maxStrings);
	}
	
	public Integer getWordCount(ExtendedTextInteraction interaction) {
		int numOfWords = 0;
		try {
			ResponseData responseInput = AssessmentRenderFunctions.getResponseInput(itemSessionState, interaction.getResponseIdentifier());
			ResponseDeclaration responseDeclaration = AssessmentRenderFunctions.getResponseDeclaration(assessmentItem, interaction.getResponseIdentifier());
			Cardinality cardinality = responseDeclaration == null ? null : responseDeclaration.getCardinality();
			
			if(cardinality != null && (cardinality.isRecord() || cardinality.isSingle())) {
				String responseInputString = extractSingleCardinalityResponseInput(responseInput);
				numOfWords = ExtendedTextInteraction.countWords(responseInputString);
			} else {
				if(interaction.getMaxStrings() != null) {
					int maxStrings = interaction.getMaxStrings().intValue();
					for(int i=0; i<maxStrings; i++) {
						String responseInputString = extractResponseInputAt(responseInput, i);
						numOfWords += ExtendedTextInteraction.countWords(responseInputString);
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
						numOfWords += ExtendedTextInteraction.countWords(responseInputString);
					}
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		
		return numOfWords;
	}
	
	public List<SimpleAssociableChoice> getVisibleAssociableChoices(AssociateInteraction interaction) {
		return interaction.getSimpleAssociableChoices().stream()
			.filter(choice -> isVisible(choice, itemSessionState))
			.collect(Collectors.toList());
	}
	
	public List<HotspotChoice> getVisibleHotspotChoices(GraphicOrderInteraction interaction) {
		return interaction.getHotspotChoices().stream()
				.filter(hotspot -> isVisible(hotspot, itemSessionState))
				.collect(Collectors.toList());
	}
	
	public List<AssociableHotspot> getVisibleAssociableHotspots(GraphicGapMatchInteraction interaction) {
		return interaction.getAssociableHotspots().stream()
				.filter(hotspot -> isVisible(hotspot, itemSessionState))
				.collect(Collectors.toList());
	}
	
	public List<AssociableHotspot> getVisibleAssociableHotspots(GraphicAssociateInteraction interaction) {
		return interaction.getAssociableHotspots().stream()
				.filter(hotspot -> isVisible(hotspot, itemSessionState))
				.collect(Collectors.toList());
	}
	
	public List<GapImg> getVisibleGapImgs(GraphicGapMatchInteraction interaction) {
		return interaction.getGapImgs().stream()
				.filter(gapImg -> isVisible(gapImg, itemSessionState))
				.collect(Collectors.toList());
	}
	
	public List<SimpleAssociableChoice> getVisibleOrderedChoices(MatchInteraction interaction, int pos) {
		try {
			List<SimpleAssociableChoice> choices;
			if(interaction.getShuffle()) {
				List<Identifier> choiceOrders = itemSessionState.getShuffledInteractionChoiceOrder(interaction.getResponseIdentifier());
				Map<Identifier,SimpleAssociableChoice> idTochoice = new HashMap<>();
				
				List<SimpleAssociableChoice> allChoices = interaction.getSimpleMatchSets().get(pos).getSimpleAssociableChoices();
				for(SimpleAssociableChoice allChoice:allChoices) {
					idTochoice.put(allChoice.getIdentifier(), allChoice);
				}
				
				choices = new ArrayList<>();
				for(Identifier choiceOrder:choiceOrders) {
					SimpleAssociableChoice choice = idTochoice.get(choiceOrder);
					if(choice != null) {
						choices.add(choice);
					}
				}
			} else {
				choices = interaction.getSimpleMatchSets().get(pos).getSimpleAssociableChoices();
			}
			
			return choices.stream()
					.filter(choice -> isVisible(choice, itemSessionState))
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	//<xsl:apply-templates select="qw:get-visible-ordered-choices(., qti:simpleChoice)"/>
	public List<SimpleChoice> getVisibleOrderedSimpleChoices(ChoiceInteraction interaction) {
		List<SimpleChoice> choices;
		if(interaction.getShuffle()) {
			// <xsl:variable name="shuffledChoiceOrders" select="$itemSessionState/qw:shuffledInteractionChoiceOrder"
		    // as="element(qw:shuffledInteractionChoiceOrder)*"/>
			//<xsl:variable name="choiceSequence" as="xs:string?"
		    //  select="$shuffledChoiceOrders[@responseIdentifier=$interaction/@responseIdentifier]/@choiceSequence"/>
			List<Identifier> choiceOrders = itemSessionState.getShuffledInteractionChoiceOrder(interaction.getResponseIdentifier());
			
			choices = new ArrayList<>();
			choiceOrders.forEach(choiceIdentifier
					-> choices.add(interaction.getSimpleChoice(choiceIdentifier)));
		} else {
			choices = interaction.getSimpleChoices();
		}
		
		return choices.stream()
			.filter(choice -> isVisible(choice, itemSessionState))
			.collect(Collectors.toList());
	}
	
	public List<SimpleChoice> getVisibleOrderedSimpleChoices(OrderInteraction interaction) {
		List<SimpleChoice> choices;
		if(interaction.getShuffle()) {
			// <xsl:variable name="shuffledChoiceOrders" select="$itemSessionState/qw:shuffledInteractionChoiceOrder"
		    // as="element(qw:shuffledInteractionChoiceOrder)*"/>
			//<xsl:variable name="choiceSequence" as="xs:string?"
		    //  select="$shuffledChoiceOrders[@responseIdentifier=$interaction/@responseIdentifier]/@choiceSequence"/>
			List<Identifier> choiceOrders = itemSessionState.getShuffledInteractionChoiceOrder(interaction.getResponseIdentifier());

			choices = new ArrayList<>();
			if(choiceOrders != null) {
				for(Identifier choiceOrder:choiceOrders) {
					choices.add(interaction.getSimpleChoice(choiceOrder));
				}
			}
		} else {
			choices = interaction.getSimpleChoices();
		}
		return choices.stream()
				.filter(choice -> isVisible(choice, itemSessionState))
				.collect(Collectors.toList());
	}
	/*
	  <xsl:function name="qw:get-visible-ordered-choices" as="element()*">
	    <xsl:param name="interaction" as="element()"/>
	    <xsl:param name="choices" as="element()*"/>
	    <xsl:variable name="orderedChoices" as="element()*">
	      <xsl:choose>
	        <xsl:when test="$interaction/@shuffle='true'">
	          <xsl:for-each select="qw:get-shuffled-choice-order($interaction)">
	            <xsl:sequence select="$choices[@identifier=current()]"/>
	          </xsl:for-each>
	        </xsl:when>
	        <xsl:otherwise>
	          <xsl:sequence select="$choices"/>
	        </xsl:otherwise>
	      </xsl:choose>
	    </xsl:variable>
	    <xsl:sequence select="qw:filter-visible($orderedChoices)"/>
	  </xsl:function>
	  */
	public List<InlineChoice> getVisibleOrderedChoices(InlineChoiceInteraction interaction) {
		List<InlineChoice> choices;
		if(interaction.getShuffle()) {
			// <xsl:variable name="shuffledChoiceOrders" select="$itemSessionState/qw:shuffledInteractionChoiceOrder"
		    // as="element(qw:shuffledInteractionChoiceOrder)*"/>
			//<xsl:variable name="choiceSequence" as="xs:string?"
		    //  select="$shuffledChoiceOrders[@responseIdentifier=$interaction/@responseIdentifier]/@choiceSequence"/>
			List<Identifier> choiceOrders = itemSessionState.getShuffledInteractionChoiceOrder(interaction.getResponseIdentifier());

			choices = new ArrayList<>();
			for(Identifier choiceOrder:choiceOrders) {
				choices.add(interaction.getInlineChoice(choiceOrder));
			}
		} else {
			choices = interaction.getInlineChoices();
		}
		
		return choices.stream()
				.filter(choice -> isVisible(choice, itemSessionState))
				.collect(Collectors.toList());
	}
	
	public List<GapChoice> getVisibleOrderedChoices(GapMatchInteraction interaction) {
		List<GapChoice> choices;
		if(interaction.getShuffle()) {
			// <xsl:variable name="shuffledChoiceOrders" select="$itemSessionState/qw:shuffledInteractionChoiceOrder"
		    // as="element(qw:shuffledInteractionChoiceOrder)*"/>
			//<xsl:variable name="choiceSequence" as="xs:string?"
		    //  select="$shuffledChoiceOrders[@responseIdentifier=$interaction/@responseIdentifier]/@choiceSequence"/>
			List<Identifier> choiceOrders = itemSessionState.getShuffledInteractionChoiceOrder(interaction.getResponseIdentifier());

			choices = new ArrayList<>();
			for(Identifier choiceOrder:choiceOrders) {
				choices.add(interaction.getGapChoice(choiceOrder));
			}
		} else {
			choices = new ArrayList<>(interaction.getGapChoices());
		}
		
		return choices.stream()
				.filter(choice -> isVisible(choice, itemSessionState))
				.collect(Collectors.toList());
	}
	
	public List<Gap> findGaps(GapMatchInteraction interaction) {
		return QueryUtils.search(Gap.class, interaction.getBlockStatics());
	}
	
	public List<Gap> filterVisible(List<Gap> gaps) {
		if(gaps == null) return new ArrayList<>(0);
		
		return gaps.stream()
				.filter(gap -> isVisible(gap, itemSessionState))
				.collect(Collectors.toList());
	}
	
	/*
		<xsl:variable name="respondedChoiceIdentifiers" select="qw:extract-iterable-elements(qw:get-response-value(/, @responseIdentifier))" as="xs:string*"/>
        <xsl:variable name="unselectedVisibleChoices" select="$visibleOrderedChoices[not(@identifier = $respondedChoiceIdentifiers)]" as="element(qti:simpleChoice)*"/>
        
        <xsl:variable name="respondedVisibleChoices" as="element(qti:simpleChoice)*">
          <xsl:for-each select="$respondedChoiceIdentifiers">
            <xsl:sequence select="$thisInteraction/qti:simpleChoice[@identifier=current() and qw:is-visible(.)]"/>
          </xsl:for-each>
        </xsl:variable>
	 */
	public OrderChoices getRespondedVisibleChoices(OrderInteraction interaction) {
		List<SimpleChoice> visibleChoices = getVisibleOrderedSimpleChoices(interaction);

		Value responseValue = getResponseValue(interaction.getResponseIdentifier());
		List<String> responseIdentifiers = new ArrayList<>();
		if(responseValue instanceof ListValue) {
			for(SingleValue singleValue: (ListValue)responseValue) {
				responseIdentifiers.add(singleValue.toQtiString());
			}
		}

		List<SimpleChoice> unselectedVisibleChoices = new ArrayList<>(visibleChoices);
		for(Iterator<SimpleChoice> it=unselectedVisibleChoices.iterator(); it.hasNext(); ) {
			SimpleChoice choice = it.next();
			if(responseIdentifiers.contains(choice.getIdentifier().toString())) {
				it.remove();
			}
		}

		List<SimpleChoice> respondedVisibleChoices = new ArrayList<>();
		for(String responseIdentifier:responseIdentifiers) {
			for(SimpleChoice visibleChoice:visibleChoices) {
				if(responseIdentifier.equals(visibleChoice.getIdentifier().toString())) {
					respondedVisibleChoices.add(visibleChoice);
				}
			}
		}
		return new OrderChoices(respondedVisibleChoices, unselectedVisibleChoices);
	}
	
	/*
	<xsl:variable name="is-discrete" select="qw:get-response-declaration(/, @responseIdentifier)/@baseType='integer'" as="xs:boolean"/>
    <xsl:variable name="min" select="if ($is-discrete) then string(floor(@lowerBound)) else string(@lowerBound)" as="xs:string"/>
    <xsl:variable name="max" select="if ($is-discrete) then string(ceiling(@upperBound)) else string(@upperBound)" as="xs:string"/>
    <xsl:variable name="step" select="if (@step) then @step else if ($is-discrete) then '1' else '0.01'" as="xs:string"/>
    <xsl:value-of select="if (@reverse) then @reverse else 'false'"/>
    */
	public SliderOptions getSliderOptions(SliderInteraction interaction) {
		ResponseDeclaration responseDeclaration = getResponseDeclaration(interaction.getResponseIdentifier());
		boolean discrete = responseDeclaration.hasBaseType(BaseType.INTEGER);
		boolean reverse = interaction.getReverse() == null ? false : interaction.getReverse().booleanValue();
		
		String step;
		if(interaction.getStep() != null) {
			step = Integer.toString(interaction.getStep().intValue());
		} else {
			step = discrete ? "1" : "0.01";
		}
		String min;
		String max;
		if(discrete) {
			min = Long.toString(java.lang.Math.round(java.lang.Math.floor(interaction.getLowerBound())));
			max = Long.toString(java.lang.Math.round(java.lang.Math.ceil(interaction.getUpperBound())));
		} else {
			min = Long.toString(java.lang.Math.round(interaction.getLowerBound()));
			max = Long.toString(java.lang.Math.round(interaction.getUpperBound()));
		}
		return new SliderOptions(discrete, reverse, min, max, step);
	}
	
	public boolean hasCssClass(Interaction interaction, String cssClass) {
		if(StringHelper.containsNonWhitespace(cssClass)) {
			List<String> cssClasses = interaction.getClassAttr();
			return cssClasses != null && cssClasses.contains(cssClass);
		}
		return false;
	}
	
	public boolean isVisible(Choice choice, ItemSessionState iSessionState) {
		return AssessmentRenderFunctions.isVisible(choice, iSessionState);
	}
	
	public boolean valueContains(Value value, Identifier identifier) {
		return AssessmentRenderFunctions.valueContains(value, identifier);
	}
	
	public boolean valueContains(Value value, String string) {
		return AssessmentRenderFunctions.valueContains(value, string);
	}
	
	public boolean trueFalseDefault(Value response, String targetIdentifier, MatchInteraction interaction) {
		return AssessmentRenderFunctions.trueFalseDefault(response, targetIdentifier, interaction);
	}
	
	public ResponseData getResponseInput(Identifier identifier) {
		return AssessmentRenderFunctions.getResponseInput(itemSessionState, identifier);
	}
	
	public String extractSingleCardinalityResponseInput(ResponseData data) {
		return AssessmentRenderFunctions.extractSingleCardinalityResponseInput(data);
	}
	
	public String getResponseValueForField(Value value, String field) {
		String responseValue;
		//for math entry interaction
		if(value instanceof RecordValue) {
			Identifier fieldIdentifier = Identifier.assumedLegal(field);
			RecordValue recordValue = (RecordValue)value;
			SingleValue sValue = recordValue.get(fieldIdentifier);
			responseValue = sValue == null ? null : sValue.toQtiString();
		} else {
			responseValue = null;
		}
		return responseValue;
	}

	public Value getResponseValue(Identifier identifier) {
		return AssessmentRenderFunctions.getResponseValue(assessmentItem, itemSessionState, identifier, isSolutionMode());
	}
	
	public String getResponseValueAsBase64(Identifier identifier) {
		AssessmentTestSession assessmentTestSession = avc.getCandidateSessionContext().getCandidateSession();
		return AssessmentRenderFunctions.getResponseValueAsBase64(assessmentItem, assessmentTestSession, itemSessionState, identifier, isSolutionMode());
	}
	
	public String getCompanionResponseValue(Identifier identifier) {
		AssessmentTestSession assessmentTestSession = avc.getCandidateSessionContext().getCandidateSession();
		return AssessmentRenderFunctions.getCompanionResponseValue(assessmentItem, assessmentTestSession, itemSessionState, identifier, isSolutionMode());
	}
	
	public ResponseDeclaration getResponseDeclaration(Identifier identifier) {
		return AssessmentRenderFunctions.getResponseDeclaration(assessmentItem, identifier);
	}
	
	public Boolean isCorrectTextEntry(TextEntryInteraction textEntry) {
		if(textEntry == null) {
			return null;
		}
		
		Value val = getResponseValue(textEntry.getResponseIdentifier());
		if(val == null) {
			val = NullValue.INSTANCE;
		}
		
		String stringuifiedResponses = toString(val);
		AbstractEntry correctAnswers = CorrectResponsesUtil.getCorrectTextResponses(assessmentItem, textEntry);
		if(correctAnswers == null) {
			return null;
		}
		stringuifiedResponses = CorrectResponsesUtil.stripResponse(stringuifiedResponses);
		boolean correct = correctAnswers.match(stringuifiedResponses);
		return Boolean.valueOf(correct);
	}
	
	public String renderTextEntryAlternatives(TextEntryInteraction textEntry) {

		TextEntry entryForScore = null;
		LinkedHashSet<String> alternatives = new LinkedHashSet<>();
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(textEntry.getResponseIdentifier());
		if(responseDeclaration != null &&responseDeclaration.hasBaseType(BaseType.STRING) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
			CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
			if(correctResponse != null && correctResponse.getFieldValues() != null) {
				for(FieldValue fValue:correctResponse.getFieldValues()) {
					SingleValue aValue = fValue.getSingleValue();
					if(aValue instanceof StringValue) {
						alternatives.add(((StringValue)aValue).stringValue());
					}
				}
			}

			Mapping mapping = responseDeclaration.getMapping();
			if(mapping != null) {
				for(MapEntry mapEntry:mapping.getMapEntries()) {
					SingleValue sValue = mapEntry.getMapKey();
					if(sValue instanceof StringValue) {
						alternatives.add(((StringValue)sValue).stringValue());
					}
				}
			}
			
			// if there is a correct answer, remove the first one
			if(correctResponse != null && correctResponse.getFieldValues() != null
					&& !correctResponse.getFieldValues().isEmpty() && !alternatives.isEmpty()) {
				alternatives.remove(alternatives.iterator().next());
			}
			
			if(avc.isScorePerAnswers()) {
				entryForScore = new TextEntry(textEntry);
				FIBAssessmentItemBuilder.extractTextEntrySettingsFromResponseDeclaration(entryForScore, responseDeclaration, new AtomicInteger(), new DoubleAdder());
			}
		}
		
		String separator = ", ";
		// Don't use , as a separator on punctuation exercise
		if(alternatives.contains(",") || alternatives.contains(" ") || alternatives.contains(".")) {
			separator = " \u007C ";
		}
		
		StringBuilder sb = new StringBuilder();
		for(String alternative:alternatives) {
			if(sb.length() > 0) sb.append(separator);
			sb.append(alternative);
			
			if(entryForScore != null) {
				String val = renderScorePerAnswer(entryForScore.getAlternativeScore(alternative), translator);
				sb.append(" ").append(val);
			}
		}
		return sb.toString();
	}
	
	public String renderClassAttr(BodyElement block) {
		List<String> classAttr = block.getClassAttr();
		if(classAttr != null && !classAttr.isEmpty()) {
			for(String attr:classAttr) {
				if(target.getLastChar() != ' ') target.append(" ");
				target.append(attr);
			}
		}
		return "";
	}
	
	public String renderPrompt(Prompt prompt) {
		if(prompt != null) {
			prompt.getInlineStatics().forEach(inline
					->	avc.getHTMLRendererSingleton().renderInline(renderer, target, avc, resolvedAssessmentItem, itemSessionState, inline, ubu, translator));
		}
		return "";
	}
	
	public String renderBlock(Block block) {
		if(block != null) {
			avc.getHTMLRendererSingleton().renderBlock(renderer, target, avc, resolvedAssessmentItem, itemSessionState, block, ubu, translator);
		}
		return "";
	}
	
	public String renderBlockStatics(List<BlockStatic> blockStaticList) {
		if(blockStaticList != null && !blockStaticList.isEmpty()) {
			blockStaticList.forEach(block
					-> avc.getHTMLRendererSingleton().renderBlock(renderer, target, avc, resolvedAssessmentItem, itemSessionState, block, ubu, translator));
		}
		return "";
	}
	
	public String renderFlowStatics(List<FlowStatic> flowStaticList) {
		if(flowStaticList != null && !flowStaticList.isEmpty()) {
			flowStaticList.forEach(flow
					-> avc.getHTMLRendererSingleton().renderFlow(renderer, target, avc, resolvedAssessmentItem, itemSessionState, flow, ubu, translator));
		}
		return "";
	}
	
	public String renderKprimSpecialFlowStatics(List<FlowStatic> flowStaticList) {
		try(StringOutput sb = new StringOutput()) {
			if(flowStaticList != null && !flowStaticList.isEmpty()) {
				flowStaticList.forEach(flow
						-> avc.getHTMLRendererSingleton().renderFlow(renderer, sb, avc, resolvedAssessmentItem, itemSessionState, flow, ubu, translator));
			}
			String specialKprim = sb.toString();
			if("+".equals(specialKprim)) {
				if(translator != null) {
					specialKprim = translator.translate("kprim.plus");
				} else {
					specialKprim = "True";
				}
			} else if("-".equals(specialKprim)) {
				if(translator != null) {
					specialKprim = translator.translate("kprim.minus");
				} else {
					specialKprim = "False";
				}
			}
			return specialKprim;
		} catch(IOException e) {
			log.error("", e);
			return "";
		}
	}
	
	public String renderTextOrVariables(List<TextOrVariable> textOrVariables) {
		if(textOrVariables != null && !textOrVariables.isEmpty()) {
			textOrVariables.forEach(textOrVariable
					-> avc.getHTMLRendererSingleton().renderTextOrVariable(renderer, target, avc, resolvedAssessmentItem, itemSessionState, textOrVariable));
		}
		return "";
	}
	
	public String renderFlow(Flow flow) {
		if(flow != null) {
			avc.getHTMLRendererSingleton().renderFlow(renderer, target, avc, resolvedAssessmentItem, itemSessionState, flow, ubu, translator);
		}
		return "";
	}
	
	public String renderExtendedTextBox(ExtendedTextInteraction interaction) {
		avc.getHTMLRendererSingleton()
			.renderExtendedTextBox(renderer, target, avc, assessmentItem, itemSessionState, interaction, translator);
		return "";
	}
	
	/**
	 * The method returns the score of specific interaction.
	 * 
	 * @param interaction the score
	 * @return A formatted <span> tag
	 */
	public String renderScorePerInteraction(Interaction interaction) {
		if(interaction == null || interaction.getResponseIdentifier() == null || !avc.isScorePerAnswers()) return "";
		
		Double score = null;
		ResponseDeclaration responseDeclaration = assessmentItem.getResponseDeclaration(interaction.getResponseIdentifier());
		if(interaction instanceof TextEntryInteraction && responseDeclaration != null) {
			if(responseDeclaration.hasBaseType(BaseType.STRING) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
				TextEntry textEntry = new TextEntry((TextEntryInteraction)interaction);
				FIBAssessmentItemBuilder.extractTextEntrySettingsFromResponseDeclaration(textEntry, responseDeclaration, new AtomicInteger(), new DoubleAdder());
				score = textEntry.getScore();
			} else if(responseDeclaration.hasBaseType(BaseType.FLOAT) && responseDeclaration.hasCardinality(Cardinality.SINGLE)) {
				NumericalEntry numericalEntry = new NumericalEntry((TextEntryInteraction)interaction);
				FIBAssessmentItemBuilder.extractNumericalEntrySettings(assessmentItem, numericalEntry, responseDeclaration, new AtomicInteger(), new DoubleAdder());
				score = numericalEntry.getScore();
			} 
		} else if(interaction instanceof OrderInteraction) {
			long numOfInteractions = getNumOfInteractions(assessmentItem);
			if(numOfInteractions == 1) {
				score = QtiNodesExtractor.extractMaxScore(assessmentItem);
			}
		}
		
		return renderScorePerAnswer(score, translator);
	}
	
	/**
	 * 
	 * @param interaction The interaction
	 * @param choice The choice
	 * @return
	 */
	public String renderScorePerChoice(Interaction interaction, Choice choice) {
		if(interaction == null || interaction.getResponseIdentifier() == null || !avc.isScorePerAnswers()) return "";
		return renderScorePerChoice(assessmentItem, interaction, choice, translator);
	}
	
	/**
	 * Return the value as (1 pt).
	 * 
	 * @param interaction The interaction
	 * @param choice The choice
	 * @return Decorated score with html tag.
	 */
	public String renderScoreValuePerChoice(Interaction interaction, Choice choice) {
		if(interaction == null || choice == null || !avc.isScorePerAnswers()) return "";

		Double score = getScorePerChoice(assessmentItem, interaction, choice);
		return translatedScorePerAnswer( score, translator);
	}
	
	protected static String renderScorePerChoice(AssessmentItem item, Interaction interaction, Choice choice, Translator translator) {
		if(interaction == null) return "";
		
		Double score = getScorePerChoice(item, interaction, choice);
		return renderScorePerAnswer(score, translator);
	}
	
	/**
	 * @param value The score value
	 * @param translator The translator
	 * @return Returns the value formatted and decorated in a span with a short translated add on for points.
	 */
	public static String renderScorePerAnswer(Double value, Translator translator) {
		if(value == null) {
			return "";
		}
		String stringVal = translatedScorePerAnswer(value, translator);
		return "<span class='o_qti_score_infos'>" + stringVal + "</span>";
	}
	
	public static String translatedScorePerAnswer(Double value, Translator translator) {
		String stringVal;
		if(value == null) {
			stringVal = "";
		} else if(value.doubleValue() < 1.1d) {
			stringVal = translator.translate("point.answer.singular", AssessmentHelper.getRoundedScore(value));
		} else {
			stringVal = translator.translate("point.answer.plural", AssessmentHelper.getRoundedScore(value));
		}
		return stringVal;
	}
	
	/**
	 * 
	 * @param item The assessment item
	 * @param interaction The interaction
	 * @param choice The choice
	 * @return The score of single/multiple choices, hottext or hotspot interactions
	 */
	private static final Double getScorePerChoice(AssessmentItem item, Interaction interaction, Choice choice) {
		Double score = null;
		if(interaction instanceof ChoiceInteraction) {
			ChoiceInteraction choiceInteraction = (ChoiceInteraction)interaction;
			Map<Identifier,Double> mapping = SimpleChoiceAssessmentItemBuilder.getMapping(item, choiceInteraction);
			if(mapping != null) {
				score = mapping.get(choice.getIdentifier());
			} else {
				long numOfInteractions = getNumOfInteractions(item);
				List<Identifier> correctResponses = CorrectResponsesUtil.getCorrectIdentifierResponses(item, choiceInteraction);
				if(numOfInteractions == 1l
						&& correctResponses != null && correctResponses.size() == 1
						&& choice != null && choice.getIdentifier() != null && choice.getIdentifier().equals(correctResponses.get(0))) {
					score = QtiNodesExtractor.extractMaxScore(item);
				}
			}
		} else if(interaction instanceof HottextInteraction) {
			Map<Identifier,Double> mapping = HottextAssessmentItemBuilder.getMapping(item, (HottextInteraction)interaction);
			if(mapping != null) {
				score = mapping.get(choice.getIdentifier());
			}
		} else if(interaction instanceof HotspotInteraction) {
			Map<Identifier,Double> mapping = HotspotAssessmentItemBuilder.getMapping(item, (HotspotInteraction)interaction);
			if(mapping != null) {
				score = mapping.get(choice.getIdentifier());
			}
		}
		return score;
	}
	
	/**
	 * Very specialized method which pack the scores of the specified source identifier in the
	 * form of target1.identifier=score1;target2.identifier=score2 suited for the Javascript
	 * library which rendered the D&D version of the match interaction.
	 * 
	 * @param interaction The interaction
	 * @param source The source choice identifier
	 * @return A string
	 */
	public String renderDirectedScoreAttribute(Interaction interaction, Identifier source) {
		if(interaction == null || source == null || !avc.isScorePerAnswers()) return "";
		
		StringBuilder sb = new StringBuilder();
		
		if(interaction instanceof MatchInteraction) {
			ResponseDeclaration responseDeclaration = getResponseDeclaration(interaction.getResponseIdentifier());
			if(responseDeclaration != null) {
				Mapping mapping = responseDeclaration.getMapping();
				if(mapping != null) {
					for(MapEntry entry:mapping.getMapEntries()) {
						SingleValue sValue = entry.getMapKey();
						if(sValue instanceof DirectedPairValue) {
							Identifier sourceIdentifier = ((DirectedPairValue)sValue).sourceValue();
							if(source.equals(sourceIdentifier)) {
								Identifier destIdentifier = ((DirectedPairValue)sValue).destValue();
								String val = translatedScorePerAnswer(entry.getMappedValue(), translator);
								sb.append(destIdentifier.toString()).append("=").append(val).append(";");
							}
						}
					}
				} else {
					long numOfInteractions = getNumOfInteractions(assessmentItem);
					Set<String> correctResponses = CorrectResponsesUtil.getCorrectDirectPairResponses(assessmentItem, interaction, false);
					if(numOfInteractions == 1l && correctResponses != null && correctResponses.size() == 1) {
						String correctResponse = correctResponses.iterator().next();
						if(correctResponse.startsWith(source.toString())) {
							String[] pairs = correctResponse.split("[ ]");
							Double score = QtiNodesExtractor.extractMaxScore(assessmentItem);
							if(score != null && pairs != null && pairs.length == 2) {
								String val = translatedScorePerAnswer(score, translator);
								sb.append(pairs[1]).append("=").append(val).append(";");
							}
						}
					}
				}
			}
		}
		
		return sb.toString();
	}
	

	public boolean renderDirectedScore(Interaction interaction) {
		if(interaction == null || !avc.isScorePerAnswers()) return false;
		
		return AssessmentRenderFunctions.getResponseMapping(assessmentItem, interaction.getResponseIdentifier()) != null;
	}
	
	/**
	 * Specialized methods for match. Returns the score of a particular directed pair.
	 * 
	 * @param interaction The interaction
	 * @param source The source choice identifier
	 * @param destination The target choice identifier
	 * @return Mapped score of a match choice
	 */
	public String renderDirectedScoreAttribute(Interaction interaction, Identifier source, Identifier destination) {
		if(interaction == null || source == null || !avc.isScorePerAnswers()) return "";
		
		Double score = null;
		if(interaction instanceof MatchInteraction) {
			score = CorrectResponsesUtil.getMappedValue(assessmentItem, interaction, source, destination);
		}
		return renderScorePerAnswer(score, translator);
	}
	
	/**
	 * Specialized methods for match. check if a particular directed pair is correct or not.
	 * 
	 * @param interaction The interaction
	 * @param source The source choice identifier
	 * @param destination The target choice identifier
	 * @return Correctness of a match choice
	 */
	public boolean isDirectedCorrect(Interaction interaction, Identifier source, Identifier destination) {
		if(interaction == null || source == null || !avc.isScorePerAnswers()) return false;
		
		boolean correct = false;
		if(interaction instanceof MatchInteraction) {
			Set<String> correctResponses = CorrectResponsesUtil.getCorrectDirectPairResponses(assessmentItem, interaction, false);
			String pair = source.toString() + " " + destination.toString();
			correct = correctResponses != null && correctResponses.contains(pair);
		}
		return correct;
	}
	
	public boolean hasMapping() {
		return true;
	}
	
	public String placeholder(Interaction interaction) {
		if(interaction instanceof StringInteraction) {
			StringInteraction tei = (StringInteraction)interaction;
			if(StringHelper.containsNonWhitespace(tei.getPlaceholderText())) {
				target.append(" placeholder=\"").append(StringHelper.escapeHtml(tei.getPlaceholderText())).append("\"");
			}
		}
		return "";
	}
	
	public String getFullTemporaryResponseEvent() {
		String id = FormBaseComponentIdProvider.DISPPREFIX.concat(avc.getDispatchID()); 
		return FormJSHelper.getJSFnCallFor(avc.getQtiItem().getRootForm(), id, 2,
				false, QTIWorksAssessmentTestEvent.Event.fullTmpResponse.name());
	}
	
	/**
	 * @return The number of interactions but without the end attempts.
	 */
	private static long getNumOfInteractions(AssessmentItem item) {
		return item.getItemBody().findInteractions().stream()
				.filter(interact -> !(interact instanceof EndAttemptInteraction))
				.count();
	}
	
	public int getNumOfUploadInteractions() {
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		int numOfUploads = 0;
		for(Interaction interaction:interactions) {
			if(interaction instanceof UploadInteraction) {
				numOfUploads++;
			}
		}
		return numOfUploads;
	}
	
	public int getIndexOfUploadInteraction(Object responseIdentifier) {
		List<Interaction> interactions = assessmentItem.getItemBody().findInteractions();
		int index = 0;
		for(Interaction interaction:interactions) {
			if(responseIdentifier.equals(interaction.getResponseIdentifier())) {
				break;
			}
			if(interaction instanceof UploadInteraction) {
				index++;
			}
		}
		return index + 1;
	}
	
	public String getUploadDate(Identifier responseIdentifier) {
		File file = getUploadFile(responseIdentifier);
		if(file != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(file.lastModified());
			return formatDateAndTime(cal.getTime());
		}
		return "";
	}
	
	public String getUploadExtension(Identifier responseIdentifier) {
		File file = getUploadFile(responseIdentifier);
		if(file != null) {
			return FileUtils.getFileSuffix(file.getName());
		}
		return "";
	}
	
	public String getUploadSize(Identifier responseIdentifier) {
		File file = getUploadFile(responseIdentifier);
		if(file != null) {
			return Formatter.formatBytes(file.length());
		}
		return "";
	}
	
	private File getUploadFile(Identifier responseIdentifier) {
		if(responseIdentifier == null) return null;
		
		Value val = getResponseValue(responseIdentifier);
		if(val instanceof FileValue) {
			FileValue fileValue = (FileValue)val;
			return fileValue.getFile();
		}
		return null;
	}
	
	public String escapeForJavascriptString(String text) {
		return escapeJavaScript(text);
	}
	
	public String toString(Identifier identifier) {
		return identifier == null ? "" : identifier.toString();
	}
	
	public String toString(Value value) {
		return toString(value, ",", " ");
	}
	
	public String toString(Value value, String delimiter) {
		return toString(value, delimiter, " ");
	}
	
	public String toString(NullValue value, String delimiter) {
		return toString(value, delimiter, " ");
	}

	public String toString(Value value, String delimiter, String mappingIndicator) {
		try(StringOutput out = new StringOutput(32)) {
			renderValue(out, value, delimiter, mappingIndicator);
			return out.toString();
		} catch(IOException e) {
			log.error("", e);
			return "";
		}
	}
	
	public String toJavascriptArguments(List<? extends Choice> choices) {
		if(choices == null || choices.isEmpty()) return "";
		
		StringBuilder out = new StringBuilder(32);
		for(Choice choice:choices) {
			if(out.length() > 0) out.append(",");
			out.append("'").append(choice.getIdentifier().toString()).append("'");
		}
		return out.toString();
	}
	
	public String checkJavaScript(ResponseDeclaration declaration, String patternMask) {
		return AssessmentRenderFunctions.checkJavaScript(declaration, patternMask);
	}
	
	public String shapeToString(Shape value) {
		return value.name().toLowerCase();
	}
	
	public String coordsToString(List<Integer> coords) {
		StringBuilder out = new StringBuilder();
		for(Integer coord:coords) {
			if(out.length() > 0) out.append(",");
			out.append(coord.intValue());
		}
		return out.toString();
	}
	
	public List<Integer> maxToList(int max) {
		List<Integer> list = new ArrayList<>(max);
		for(int i=1; i<=max; i++) {
			list.add(i);
		}
		return list;
	}
	
	public String subStringBefore(String text, String separator) {
		if(StringHelper.containsNonWhitespace(text)) {
			int index = text.indexOf(separator);
			if(index > 0) {
				target.append(text.substring(0, index));
			}
		}
		return "";
	}
	
	public String subStringAfter(String text, String separator) {
		if(StringHelper.containsNonWhitespace(text)) {
			int index = text.indexOf(separator);
			if(index == 0) {
				target.append(text);
			} else if(index > 0 && index < text.length()) {
				target.append(text.substring(index, text.length()));
			}
		}
		return "";
	}
	
	public boolean classContains(QtiNode element, String marker) {
		StringMultipleAttribute css = element.getAttributes().getStringMultipleAttribute("class");
		return css != null && css.getValue() != null && css.getValue().contains(marker);
	}
	
	public static class SliderOptions {
		
		private final boolean isDiscrete;
		private final boolean reverse;
		private final String min;
		private final String max;
		private final String step;
		
		public SliderOptions(boolean isDiscrete, boolean reverse, String min, String max, String step) {
			this.isDiscrete = isDiscrete;
			this.reverse = reverse;
			this.max = max;
			this.min = min;
			this.step = step;
		}

		public boolean isDiscrete() {
			return isDiscrete;
		}

		public boolean isReverse() {
			return reverse;
		}

		public String getMin() {
			return min;
		}

		public String getMax() {
			return max;
		}

		public String getStep() {
			return step;
		}
	}
	
	public static class OrderChoices {
		
		private final List<SimpleChoice> respondedVisibleChoices;
		private final List<SimpleChoice> unselectedVisibleChoices;
		
		public OrderChoices(List<SimpleChoice> respondedVisibleChoices, List<SimpleChoice> unselectedVisibleChoices) {
			this.respondedVisibleChoices = respondedVisibleChoices;
			this.unselectedVisibleChoices = unselectedVisibleChoices;
		}

		public List<SimpleChoice> getRespondedVisibleChoices() {
			return respondedVisibleChoices;
		}

		public List<SimpleChoice> getUnselectedVisibleChoices() {
			return unselectedVisibleChoices;
		}
	}
}
