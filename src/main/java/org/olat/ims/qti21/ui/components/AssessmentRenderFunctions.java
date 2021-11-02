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


import static uk.ac.ed.ph.qtiworks.mathassess.MathAssessConstants.FIELD_PMATHML_IDENTIFIER;
import static uk.ac.ed.ph.qtiworks.mathassess.MathAssessConstants.MATHS_CONTENT_RECORD_VARIABLE_IDENTIFIER;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;

import uk.ac.ed.ph.jqtiplus.attribute.Attribute;
import uk.ac.ed.ph.jqtiplus.node.ForeignElement;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.CorrectResponse;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.node.item.template.declaration.TemplateDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedback;
import uk.ac.ed.ph.jqtiplus.node.test.VisibilityMode;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.DurationValue;
import uk.ac.ed.ph.jqtiplus.value.FileValue;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.IntegerValue;
import uk.ac.ed.ph.jqtiplus.value.ListValue;
import uk.ac.ed.ph.jqtiplus.value.MultipleValue;
import uk.ac.ed.ph.jqtiplus.value.NullValue;
import uk.ac.ed.ph.jqtiplus.value.OrderedValue;
import uk.ac.ed.ph.jqtiplus.value.RecordValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;
import uk.ac.ed.ph.jqtiplus.value.StringValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 18.09.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentRenderFunctions {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentRenderFunctions.class);
	
	public static boolean exists(Value value) {
		return value != null && !value.isNull();
	}
	
	public static boolean isNullValue(Value value) {
		return value == null || value.isNull();
	}
	
	public static boolean isSingleCardinalityValue(Value value) {
		return value != null && value.hasCardinality(Cardinality.SINGLE);
	}
	
	//<xsl:sequence select="boolean($valueHolder[@cardinality='multiple'])"/>
	public static boolean isMultipleCardinalityValue(Value value) {
		return value != null && value.hasCardinality(Cardinality.MULTIPLE);
	}
	
	// <xsl:sequence select="boolean($valueHolder[@cardinality='ordered'])"/>
	public static boolean isOrderedCardinalityValue(Value value) {
		return value != null && value.hasCardinality(Cardinality.ORDERED);
	}
	
	// <xsl:sequence select="boolean($valueHolder[@cardinality='record'])"/>
	public static boolean isRecordCardinalityValue(Value value) {
		return value != null && value.hasCardinality(Cardinality.RECORD);
	}
	
	//<xsl:sequence select="boolean($valueHolder[@cardinality='record'
    //	      and qw:value[@baseType='string' and @fieldIdentifier='MathsContentClass'
    //	        and string(qw:value)='org.qtitools.mathassess']])"/>
	public static boolean isMathsContentValue(Value value) {
		if(value.hasCardinality(Cardinality.RECORD)) {
			RecordValue recordValue = (RecordValue)value;
			for(Map.Entry<Identifier, SingleValue> entry:recordValue.entrySet()) {
				final Identifier itemIdentifier = entry.getKey();
                final SingleValue itemValue = entry.getValue();
                if(itemValue.hasBaseType(BaseType.STRING)
                		&& MATHS_CONTENT_RECORD_VARIABLE_IDENTIFIER.equals(itemIdentifier)) {
                	return true;
                }
			}
		}
		return false;
	}
	
//  <xsl:sequence select="boolean($element[$overrideTemplate
    // or not(@templateIdentifier)
    // or (qw:value-contains(qw:get-template-value(@templateIdentifier), @identifier) and not(@showHide='hide'))])"/>
	public static boolean isVisible(Choice choice, ItemSessionState iSessionState) {
		if(choice.getTemplateIdentifier() == null) return true;
		
		Value templateValue = iSessionState.getTemplateValue(choice.getTemplateIdentifier());
		boolean visible = templateValue instanceof IdentifierValue
				&& ((IdentifierValue)templateValue).identifierValue().equals(choice.getIdentifier())
				&& choice.getVisibilityMode() != VisibilityMode.HIDE_IF_MATCH;
		return visible;
	}
	
	//<xsl:if test="qw:is-invalid-response(@responseIdentifier)">
	public static boolean isInvalidResponse(ItemSessionState itemSessionState, Identifier identifier) {
		//$itemSessionState/@invalidResponseIdentifiers
		return itemSessionState.getInvalidResponseIdentifiers().contains(identifier);
	}
	
	//<xsl:sequence select="$unboundResponseIdentifiers=$identifier"/>
	public static boolean isBadResponse(ItemSessionState itemSessionState, Identifier identifier) {
		return itemSessionState.getUnboundResponseIdentifiers().contains(identifier);
	}
	
	public static final Value getTemplateValue(ItemSessionState itemSessionState, String identifierAsString) {
		Identifier identifier = Identifier.assumedLegal(identifierAsString);
		return itemSessionState.getTemplateValues().get(identifier);
	}
	
	public static final boolean isTemplateDeclarationAMathVariable(AssessmentItem assessmentItem, String identifierString) {
		Identifier identifier = Identifier.assumedLegal(identifierString);
		TemplateDeclaration templateDeclaration = assessmentItem.getTemplateDeclaration(identifier);
		return templateDeclaration == null ? false : templateDeclaration.getMathVariable();
	}
	
	public static final Value getOutcomeValue(ItemSessionState itemSessionState, String identifierAsString) {
		Identifier identifier = Identifier.assumedLegal(identifierAsString);
		return itemSessionState.getOutcomeValues().get(identifier);
	}

	public static String getResponseValueAsBase64(AssessmentItem assessmentItem, AssessmentTestSession candidateSession, ItemSessionState itemSessionState,
			Identifier identifier, boolean solutionMode) {
		Value val = getResponseValue(assessmentItem, itemSessionState, identifier, solutionMode);
		
		String encodedString = null;
		if(val instanceof FileValue) {
			FileValue fileValue = (FileValue)val;
			File myStore = CoreSpringFactory.getImpl(AssessmentTestSessionDAO.class).getSessionStorage(candidateSession);
	        File submissionDir = new File(myStore, "submissions");
	        File submittedFile = new File(submissionDir, fileValue.getFileName());
	        try(InputStream inStream = new FileInputStream(submittedFile)) {
	        	 byte[] binaryData = IOUtils.toByteArray(inStream);
	        	 encodedString = new String(Base64.encodeBase64(binaryData), StandardCharsets.UTF_8);
	        } catch(Exception e) {
	        	log.error("", e);
	        }
		}
		return encodedString;
	}
	
	public static String getCompanionResponseValue(AssessmentItem assessmentItem, AssessmentTestSession candidateSession, ItemSessionState itemSessionState,
			Identifier identifier, boolean solutionMode) {
		Value val = getResponseValue(assessmentItem, itemSessionState, identifier, solutionMode);
		
		String encodedString = null;
		if(val instanceof FileValue) {
			FileValue fileValue = (FileValue)val;
			File myStore = CoreSpringFactory.getImpl(AssessmentTestSessionDAO.class).getSessionStorage(candidateSession);
	        File submissionDir = new File(myStore, "submissions");
	        File submittedFile = new File(submissionDir, fileValue.getFileName() + ".json");
	        try(InputStream inStream = new FileInputStream(submittedFile)) {
	        	encodedString = IOUtils.toString(inStream, StandardCharsets.UTF_8);
	        } catch(Exception e) {
	        	log.error("", e);
	        }
		}
		return encodedString;
	}
	
	public static Value getResponseValue(AssessmentItem assessmentItem, ItemSessionState itemSessionState, Identifier identifier, boolean solutionMode) {
		Value responseValue = null;

		//<xsl:when test="$solutionMode and $overriddenCorrectResponses[@identifier=$identifier]">
		if(solutionMode && itemSessionState.getOverriddenCorrectResponseValue(identifier) != null) {
			responseValue = itemSessionState.getOverriddenCorrectResponseValue(identifier);
		}
		//<xsl:when test="$solutionMode and $responseDeclaration/qti:correctResponse">
		else if(solutionMode && getResponseDeclaration(assessmentItem, identifier) != null) {
			/* <!-- <correctResponse> has been set in the QTI -->
        <!-- (We need to convert QTI <qti:correctResponse/> to <qw:responseVariable/>) -->
			<xsl:for-each select="$responseDeclaration/qti:correctResponse">
          <qw:responseVariable>
            <xsl:copy-of select="../@cardinality, ../@baseType"/>
            <xsl:for-each select="qti:value">
              <qw:value>
                <xsl:copy-of select="@fieldIdentifier, @baseType"/>
                <xsl:copy-of select="text()"/>
              </qw:value>
            </xsl:for-each>
          </qw:responseVariable>
        </xsl:for-each>
			 */
			ResponseDeclaration responseDeclaration = getResponseDeclaration(assessmentItem, identifier);
			CorrectResponse correctResponse = responseDeclaration.getCorrectResponse();
			if(correctResponse != null) {
				responseValue = correctResponse.evaluate();
			}
		}
		//<xsl:when test="$uncommittedResponseValues[@identifier=$identifier]">
		else if(itemSessionState.getUncommittedResponseValue(identifier) != null) {
			responseValue = itemSessionState.getUncommittedResponseValue(identifier);
		} else {
			responseValue = itemSessionState.getResponseValue(identifier);
		}
		return responseValue;
	}
	
	//<xsl:sequence select="$document/qti:assessmentItem/qti:responseDeclaration[@identifier=$identifier]"/>
	public static ResponseDeclaration getResponseDeclaration(AssessmentItem assessmentItem, Identifier identifier) {
		return assessmentItem.getResponseDeclaration(identifier);
	}
	
	public static ResponseData getResponseInput(ItemSessionState itemSessionState, Identifier identifier) {
		ResponseData responseInput =  itemSessionState.getRawResponseDataMap().get(identifier);
		
		return responseInput;
	}
	
	public static String extractSingleCardinalityResponseInput(ResponseData data) {
		if(data instanceof StringResponseData) {
			StringResponseData stringData = (StringResponseData)data;
			List<String> dataList = stringData.getResponseData();
			if(dataList != null && dataList.size() == 1) {
				return dataList.get(0);
			}
		}
		return null;
	}
	
	public static int getCardinalitySize(Value data) {
		int size = 0;
		if(data.getCardinality() != null) {
			switch(data.getCardinality()) {
				case SINGLE:
				case RECORD:
					size = 1;
					break;
				case MULTIPLE:
				case ORDERED:
					size = ((ListValue)data).size();
					break;
			}
		}
		return size;
	}
	
	public static String extractResponseInputAt(ResponseData data, int index) {
		if(data instanceof StringResponseData) {
			StringResponseData stringData = (StringResponseData)data;
			List<String> dataList = stringData.getResponseData();
			if(dataList != null && index < 0 && dataList.size() > index) {
				return dataList.get(index);
			}
		}
		return null;
	}
	
	public static void renderValue(StringOutput sb, Value valueHolder, String delimiter, String mappingIndicator) {
		if(isNullValue(valueHolder)) {
			//
		} else if(isSingleCardinalityValue(valueHolder)) {
			renderSingleCardinalityValue(sb, valueHolder);
		} else if (isMathsContentValue(valueHolder)) {
			//TODO qti renderMathmlAsString(sb, extractMathsContentPmathml(valueHolder));
			sb.append(extractMathsContentPmathml(valueHolder));
		} else if(isMultipleCardinalityValue(valueHolder)) {
			renderMultipleCardinalityValue(sb, valueHolder, delimiter);
		} else if(isOrderedCardinalityValue(valueHolder)) {
			renderOrderedCardinalityValue(sb, valueHolder, delimiter);
		} else if(isRecordCardinalityValue(valueHolder)) {
			renderRecordCardinalityValue(sb, valueHolder, delimiter, mappingIndicator);
		} else {
			sb.append("printedVariable may not be applied to value ").append(valueHolder.toString());
		}
	}
	
	public static void renderSingleCardinalityValue(StringOutput sb, Value value) {
		if(value != null && !value.isNull() && value.hasCardinality(Cardinality.SINGLE)) {
			switch(value.getBaseType()) {
				case STRING: sb.append(((StringValue)value).stringValue()); break;
				case INTEGER: sb.append(((IntegerValue)value).intValue()); break;
				case FLOAT: sb.append(((FloatValue)value).doubleValue()); break;//TODO qti format
				case BOOLEAN: sb.append(((BooleanValue)value).booleanValue()); break;
				//TODO qti Duration in seconds
				case DURATION: sb.append(((DurationValue)value).doubleValue()); break;
				//TODO qti File value ???
				case FILE: sb.append(((FileValue)value).toQtiString()); break;
				case DIRECTED_PAIR:
				case PAIR:
				case IDENTIFIER:
				case POINT: 
				case URI: sb.append(value.toQtiString()); break;
			}
		}
	}
	
	public static void renderMultipleCardinalityValue(StringOutput sb, Value value, String delimiter) {
		if(value != null && value.hasCardinality(Cardinality.MULTIPLE)) {
			MultipleValue mValue = (MultipleValue)value;
			if(StringHelper.containsNonWhitespace(delimiter)) {
				int numOfValues = mValue.size();
				for(int i=0; i<numOfValues; i++) {
					if(i > 0) sb.append(delimiter);
					renderSingleCardinalityValue(sb, mValue.get(i));
				}
			} else {
				mValue.forEach((singleValue) -> renderSingleCardinalityValue(sb, singleValue));
			}
		}
	}
	
	public static void renderOrderedCardinalityValue(StringOutput sb, Value value, String delimiter) {
		if(value != null && value.hasCardinality(Cardinality.ORDERED)) {
			OrderedValue oValue = (OrderedValue)value;
			if(StringHelper.containsNonWhitespace(delimiter)) {
				int numOfValues = oValue.size();
				for(int i=0; i<numOfValues; i++) {
					if(i > 0) sb.append(delimiter);
					renderSingleCardinalityValue(sb, oValue.get(i));
				}
			} else {
				oValue.forEach((singleValue) -> renderSingleCardinalityValue(sb, singleValue));
			}
		}
	}
	
	public static void renderRecordCardinalityValue(StringOutput sb, Value value, String delimiter, String mappingIndicator) {
		if(value != null && value.hasCardinality(Cardinality.RECORD)) {
			RecordValue oValue = (RecordValue)value;
			boolean hasDelimiter = StringHelper.containsNonWhitespace(delimiter);
			boolean hasMappingIndicator = StringHelper.containsNonWhitespace(mappingIndicator);
			
			int count = 0;
			for(Map.Entry<Identifier, SingleValue>entry:oValue.entrySet()) {
				if(hasDelimiter && count++ > 0) sb.append(delimiter);
				
				String identifierString = entry.getKey().toString();
				sb.append(identifierString);
				if(hasMappingIndicator) {
					sb.append(mappingIndicator);
				}
				renderSingleCardinalityValue(sb, entry.getValue());
			}
		}
	}
	
	/*
  <xsl:function name="qw:extract-record-field-value" as="xs:string?">
    <xsl:param name="valueHolder" as="element()"/>
    <xsl:param name="fieldName" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="qw:is-record-cardinality-value($valueHolder)">
        <xsl:value-of select="$valueHolder/qw:value[@fieldIdentifier=$fieldName]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          Expected value <xsl:copy-of select="$valueHolder"/> to have record
          cardinalty.
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
	 */
	public static SingleValue extractRecordFieldValue(Value value, Identifier identifier) {
		SingleValue mappedValue = null;
		if(value != null && identifier != null && value.hasCardinality(Cardinality.RECORD) ) {
			RecordValue recordValue = (RecordValue)value;
			mappedValue = recordValue.get(identifier);
		}
		return mappedValue;
	}
	
	/*
	  <xsl:function name="qw:extract-maths-content-pmathml" as="element(m:math)">
	    <xsl:param name="valueHolder" as="element()"/>
	    <xsl:choose>
	      <xsl:when test="qw:is-maths-content-value($valueHolder)">
	        <xsl:variable name="pmathmlString" select="$valueHolder/qw:value[@fieldIdentifier='PMathML']" as="xs:string"/>
	        <xsl:variable name="pmathmlDocNode" select="saxon:parse($pmathmlString)" as="document-node()"/>
	        <xsl:copy-of select="$pmathmlDocNode/*"/>
	      </xsl:when>
	      <xsl:otherwise>
	        <xsl:message terminate="yes">
	          Expected value <xsl:copy-of select="$valueHolder"/> to be a MathsContent value
	        </xsl:message>
	      </xsl:otherwise>
	    </xsl:choose>
	  </xsl:function>
	*/
	public static String extractMathsContentPmathml(Value value) {
		if(value.hasCardinality(Cardinality.RECORD)) {
			RecordValue recordValue = (RecordValue)value;
			for(Map.Entry<Identifier, SingleValue> entry:recordValue.entrySet()) {
				final Identifier itemIdentifier = entry.getKey();
                final SingleValue itemValue = entry.getValue();
                if(itemValue.hasBaseType(BaseType.STRING) && FIELD_PMATHML_IDENTIFIER.equals(itemIdentifier)) {
                	return ((StringValue)itemValue).stringValue();
                }
			}
		}
		return "";
	}
	
	/*
  <xsl:function name="qw:extract-iterable-element" as="xs:string">
    <xsl:param name="valueHolder" as="element()"/>
    <xsl:param name="index" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="qw:is-null-value($valueHolder)">
        <xsl:sequence select="''"/>
      </xsl:when>
      <xsl:when test="qw:is-ordered-cardinality-value($valueHolder) or qw:is-multiple-cardinality-value($valueHolder)">
        <xsl:sequence select="string($valueHolder/qw:value[position()=$index])"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">
          Expected value <xsl:copy-of select="$valueHolder"/> to have ordered
          or multiple cardinality
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
	 */
	
	public static final SingleValue extractIterableElement(Value valueHolder, int index) {
		SingleValue indexedValue = null;
		if(valueHolder != null && !valueHolder.isNull()) {
			if(valueHolder.hasCardinality(Cardinality.ORDERED)) {
				OrderedValue oValue = (OrderedValue)valueHolder;
				if(index >= 0 && index < oValue.size()) {
					indexedValue = oValue.get(index);
				}
			} else if(valueHolder.hasCardinality(Cardinality.MULTIPLE)) {
				MultipleValue mValue = (MultipleValue)valueHolder;
				if(index >= 0 && index < mValue.size()) {
					indexedValue = mValue.get(index);
				}
			}
		}
		return indexedValue;
	}
	
	/**
	 * The method only collect text from ForeignElement, but
	 * recursively.
	 * 
	 * @param fElement
	 * @return
	 */
	public static final String contentAsString(ForeignElement fElement) {
		StringBuilder out = new StringBuilder(255);
		contentAsString(out, fElement);
		return out.toString();
	}
		
	private static final void contentAsString(StringBuilder out, ForeignElement fElement) {
		for(QtiNode child:fElement.getChildren()) {
			switch(child.getQtiClassName()) {
				case TextRun.DISPLAY_NAME:
					out.append(((TextRun)child).getTextContent());
					break;
	
				default: {
					if(child instanceof ForeignElement) {
						ForeignElement fChild = (ForeignElement)child;
						contentAsString(out, fChild);
					}
				}
			}
		}
	}
	
	public static String checkJavaScript(ResponseDeclaration declaration, String patternMask) {
		List<String> checks = new ArrayList<>(3);
        // NB: We don't presently do any JS checks for numeric values bound to records, as the JS isn't currently
        // clever enough to handle all numeric formats (e.g. 4e12)
		if(declaration != null) {
			if(declaration.getBaseType().isFloat()) {
				checks.add("float");
			}
			if(declaration.getBaseType().isInteger()) {
				checks.add("integer");
			}
		}
		if(StringHelper.containsNonWhitespace(patternMask)) {
			checks.add("regex");
			checks.add(patternMask);
		}

		if(checks == null || checks.isEmpty()) {
			return null;
		}
		
		StringBuilder out = new StringBuilder(128);
		out.append("QtiWorksRendering.validateInput(this");
		for(String check:checks) {
			out.append(",'").append(check).append("'");
		}
		out.append(");");
		return out.toString();
	}
	
	//value-contains
	public static final boolean valueContains(Value value, Identifier identifier) {
		if(value != null && !value.isNull()) {
			if(value.hasBaseType(BaseType.IDENTIFIER)) {
				if(value.getCardinality().isSingle()) {
					IdentifierValue identifierValue = (IdentifierValue)value;
					return identifierValue.identifierValue().equals(identifier);
				} else if(value.getCardinality().isList()) {
					boolean contains = false;
					for(IdentifierValue identifierValue : ((ListValue) value).values(IdentifierValue.class)) {
						if(identifierValue.identifierValue().equals(identifier)) {
							contains = true;
						}
					}
					return contains;
				}
			}
		}
		return false;
	}
	
	public static boolean valueContains(Value value, String string) {
		if(value == null || value.isNull() || value instanceof NullValue) {
			return false;
		}
		return value.toQtiString().contains(string);//TODO qti perhaps must match closer for MultipleValue
	}
	
	/**
	 * Return true if the answer is not answered
	 * @param response
	 * @param string
	 * @return
	 */
	public static boolean trueFalseDefault(Value response, String targetIdentifier, MatchInteraction interaction) {
		//first target is "unanswered"
		SimpleAssociableChoice unansweredChoice = interaction.getSimpleMatchSets().get(1).getSimpleAssociableChoices().get(0);
		return (targetIdentifier.equals(unansweredChoice.getIdentifier().toString())
				&& (response == null || response.isNull() || response instanceof NullValue));
	}
	
	/**
	 * Mimic the @class
	 * @param node
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static final String getAtClass(QtiNode node) {
		Attribute classAttribute = node.getAttributes().get("class");
		return classAttribute.toDomAttributeValue(classAttribute.getValue());
	}

	/**
	 * Return only value if the attribute is a legal html attribute. the list doesn't
	 * include some HTML 5 like contenteditable (content is never editable but by QTI runtime).
	 * 
	 * @param attribute
	 * @return
	 */
	@SuppressWarnings({ "rawtypes" })
	public static final String getHtmlAttributeValue(AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem, Attribute attribute) {
		String value;
		String name = attribute.getLocalName();
		switch(name) {
			case "accesskey":
			case "alt":
			case "class":
			case "contextmenu":
			case "dir":
			case "display":
			case "download":
			case "hidden":
			case "name":
			case "id":
			case "lang":
			case "encoding":
			case "tabindex":
			case "title":
			case "style":
			case "width":
			case "height":
			case "xmlns":
				value = getDomAttributeValue(attribute);
				break;
			case "href":
				String uri = getDomAttributeValue(attribute);
				value = convertLink(component, resolvedAssessmentItem, "href", uri);
				break;
			case "src":
			case "data":
				String data = getDomAttributeValue(attribute);
				value = convertLink(component, resolvedAssessmentItem, "data", data);
				break;
			default:
				value = null;
				
		}
		return value;
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static final String getDomAttributeValue(Attribute attribute) {
		String value;
		if(attribute.isSet()) {
			value = attribute.toDomAttributeValue(attribute.getValue());
		} else {
			value = null;
		}
		return value;
	}
	
	/*
  <xsl:function name="qw:convert-link" as="xs:string">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="starts-with($uri, 'http:') or starts-with($uri, 'https:') or starts-with($uri, 'mailto:') or starts-with($uri, 'data:')">
        <xsl:sequence select="$uri"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="resolved" as="xs:string" select="string(resolve-uri($uri, $systemId))"/>
        <xsl:sequence select="concat($webappContextPath, $serveFileUrl, '?href=', encode-for-uri($resolved))"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
	 */
	
	public static final String convertLink(AssessmentObjectComponent component, ResolvedAssessmentItem resolvedAssessmentItem, String type, String uri) {
		if(uri != null && (uri.startsWith("http:") || uri.startsWith("https:") || uri.startsWith("mailto:") || uri.startsWith("data:"))) {
			return uri;
		}
		
		if(!StringHelper.containsNonWhitespace(uri)) {
			uri = "file";
		}
		String path = uri;
		if(path != null && path.startsWith("../")) {
			for(int i=0; i<10 &&  path.startsWith("../"); i++) {
				path = path.substring(3, path	.length());
			}	
		}
		String relativePath = component.relativePathTo(resolvedAssessmentItem);
		// path is not used to deliver the file, the query parameters is relevant 
		if(path != null) {
			path = relativePath + path;
		}
		if(uri != null) {
			relativePath += uri;
		}
		return component.getMapperUri() + "/" + path + "?" + type + "=" + relativePath;
	}
	
	public static final String convertSubmissionLink(AssessmentObjectComponent component, String uri) {
		String filename = getLinkFilename(uri);
		return component.getSubmissionMapperUri() + "/submissions/" + filename + "?href=" + (uri == null ? "" : uri);
	}
	
	private static final String getLinkFilename(String uri) {
		String filename = "file";
		try {
			if(StringHelper.containsNonWhitespace(uri)) {
				int lastIndex = uri.lastIndexOf('/');
				if(lastIndex >= 0 && lastIndex + 1 < uri.length()) {
					filename = uri.substring(lastIndex + 1, uri.length());
				} else {
					filename = uri;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return filename;
	}
	
	public static final boolean testFeedbackVisible(TestFeedback testFeedback, TestSessionState testSessionState) {
		//<xsl:variable name="identifierMatch" select="boolean(qw:value-contains(qw:get-test-outcome-value(@outcomeIdentifier), @identifier))" as="xs:boolean"/>
		Identifier outcomeIdentifier = testFeedback.getOutcomeIdentifier();
		Value outcomeValue = testSessionState.getOutcomeValue(outcomeIdentifier);
		boolean identifierMatch = valueContains(outcomeValue, testFeedback.getOutcomeValue());
		//<xsl:if test="($identifierMatch and @showHide='show') or (not($identifierMatch) and @showHide='hide')">
		if((identifierMatch && testFeedback.getVisibilityMode() == VisibilityMode.SHOW_IF_MATCH)
				|| (!identifierMatch && testFeedback.getVisibilityMode() == VisibilityMode.HIDE_IF_MATCH)) {
			return true;
		}
		return false;
	}
	
}