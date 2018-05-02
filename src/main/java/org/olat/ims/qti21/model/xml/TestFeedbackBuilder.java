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

import java.io.StringReader;
import java.util.List;

import org.cyberneko.html.parsers.SAXParser;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.QTI21Constants;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import uk.ac.ed.ph.jqtiplus.node.expression.Expression;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedback;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeCondition;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeIf;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeRule;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;
import uk.ac.ed.ph.jqtiplus.value.SingleValue;

/**
 * 
 * Initial date: 11.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TestFeedbackBuilder {
	
	private static final OLog log = Tracing.createLoggerFor(TestFeedbackBuilder.class);
	
	private final TestFeedback testFeedback;
	private final AssessmentTest assessmentTest;
	
	private String title;
	private String text;
	private Identifier identifier;
	
	public TestFeedbackBuilder(AssessmentTest assessmentTest, TestFeedback testFeedback) {
		this.assessmentTest = assessmentTest;
		this.testFeedback = testFeedback;
		if(testFeedback != null) {
			text = new AssessmentHtmlBuilder().flowStaticString(testFeedback.getChildren());
			title = testFeedback.getTitle();
			identifier = testFeedback.getOutcomeValue();
		}
	}
	
	public TestFeedback getTestFeedback() {
		return testFeedback;
	}
	
	public Identifier getModalFeedbackIdentifier() {
		return testFeedback.getOutcomeValue();
	}
	
	public OutcomeRule getResponseRule() {
		return findFeedbackRule(testFeedback.getOutcomeValue());
	}
	
	public boolean isPassedRule() {
		OutcomeRule feedbackRule = findFeedbackRule(testFeedback.getOutcomeValue());
		return findFeedbackMatch(feedbackRule, true, QTI21Constants.PASS_CLX_IDENTIFIER);
	}
	
	public boolean isFailedRule() {
		OutcomeRule feedbackRule = findFeedbackRule(testFeedback.getOutcomeValue());
		return findFeedbackMatch(feedbackRule, false, QTI21Constants.PASS_CLX_IDENTIFIER);
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public Identifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Identifier identifier) {
		this.identifier = identifier;
	}

	private OutcomeRule findFeedbackRule(Identifier feedbackIdentifier) {
		List<OutcomeRule> outcomeRules = assessmentTest.getOutcomeProcessing().getOutcomeRules();
		for(OutcomeRule outcomeRule:outcomeRules) {
			if(outcomeRule instanceof OutcomeCondition) {
				if(findFeedbackRuleInSetOutcomeVariable(outcomeRule, feedbackIdentifier)) {
					return outcomeRule;
				}
			}
		}
		return null;
	}
	
	private boolean findFeedbackRuleInSetOutcomeVariable(OutcomeRule responseRule, Identifier feedbackIdentifier) {
		if(responseRule instanceof OutcomeCondition) {
			OutcomeCondition outcomeCondition = (OutcomeCondition)responseRule;
			OutcomeIf outcomeIf = outcomeCondition.getOutcomeIf();
			List<OutcomeRule> ifOutcomeRules = outcomeIf.getOutcomeRules();
			for(OutcomeRule ifOutcomeRule:ifOutcomeRules) {
				if(ifOutcomeRule instanceof SetOutcomeValue) {
					SetOutcomeValue setOutcomeValue = (SetOutcomeValue)ifOutcomeRule;
					if(findFeedbackRuleInExpression(setOutcomeValue.getExpression(), feedbackIdentifier)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean findFeedbackMatch(OutcomeRule outcomeRule, boolean pass, ComplexReferenceIdentifier id) {
		if(outcomeRule instanceof OutcomeCondition) {
			OutcomeCondition outcomeCondition = (OutcomeCondition)outcomeRule;
			OutcomeIf outcomeIf = outcomeCondition.getOutcomeIf();
			if(outcomeIf != null && outcomeIf.getExpressions().size() == 1) {
				Expression mustBeAnd = outcomeIf.getExpressions().get(0);
				if(mustBeAnd instanceof And && mustBeAnd.getExpressions().size() == 1) {
					Expression mustBeMatch = mustBeAnd.getExpressions().get(0);
					if(mustBeMatch instanceof Match && mustBeMatch.getExpressions().size() == 2) {
						return findFeedbackMatch((Match)mustBeMatch, pass, id);
					}
				}
			}
		}
		return false;
	}
	
	private boolean findFeedbackMatch(Match match, boolean pass, ComplexReferenceIdentifier id) {
		Expression firstExpression = match.getExpressions().get(0);
		Expression secondExpression = match.getExpressions().get(1);
		if(findBaseValue(firstExpression, pass) && findVariable(secondExpression, id)) {
			return true;
		}
		if(findBaseValue(secondExpression, pass) && findVariable(firstExpression, id)) {
			return true;
		}
		return false;
	}

	private boolean findBaseValue(Expression expression, boolean value) {
		if(expression instanceof BaseValue) {
			BaseValue bValue = (BaseValue)expression;
			SingleValue sValue = bValue.getSingleValue();
			if(sValue instanceof BooleanValue) {
				BooleanValue booleanValue = (BooleanValue)sValue;
				return booleanValue.booleanValue() == value;
			}
		}
		return false;
	}
	
	private boolean findVariable(Expression expression, ComplexReferenceIdentifier variableIdentifier) {
		if(expression instanceof Variable) {
			Variable variable = (Variable)expression;
			return variable.getIdentifier() != null && variable.getIdentifier().equals(variableIdentifier);
		}
		return false;
	}
	
	private boolean findFeedbackRuleInExpression(Expression expression, Identifier feedbackIdentifier) {
		if(expression instanceof BaseValue) {
			BaseValue bValue = (BaseValue)expression;
			SingleValue sValue = bValue.getSingleValue();
			if(sValue instanceof IdentifierValue) {
				IdentifierValue iValue = (IdentifierValue)sValue;
				if(feedbackIdentifier.equals(iValue.identifierValue())) {
					return true;
				}
			}
		} else {
			List<Expression> childExpressions = expression.getExpressions();
			for(Expression childExpression:childExpressions) {
				if(findFeedbackRuleInExpression(childExpression, feedbackIdentifier)) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param value The content of the feedback.
	 * @return
	 */
	public static boolean isEmpty(String value) {
		if(!StringHelper.containsNonWhitespace(value)) {
			return true;
		}
		if(StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(value))) {
			return false;
		}
		
		try {
			SAXParser parser = new SAXParser();
			HTMLHandler contentHandler = new HTMLHandler();
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new StringReader(value)));
			return !contentHandler.hasRelevantTags();
		} catch (Exception e) {
			log.error("", e);
		}
		return true;
	}
	
	private static class HTMLHandler extends DefaultHandler {

		private int count = 0;
		
		public boolean hasRelevantTags() {
			return count > 0;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			String elem = localName.toLowerCase();
			if("img".equals(elem)) {
				count++;
			} else if("span".equals(elem) ) {
				String cssClass = attributes.getValue("class");
				if(cssClass != null && cssClass.contains("olatFlashMovieViewer")) {
					count++;
				}
			}
		}
	}
}
