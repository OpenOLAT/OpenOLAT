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
package org.olat.ims.qti21.manager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.manager.audit.AuditLogFormatter;
import org.olat.ims.qti21.model.LogViewerEntry;
import org.olat.ims.qti21.model.LogViewerEntry.Answer;
import org.olat.ims.qti21.model.LogViewerEntry.Answers;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.audit.CandidateItemEventType;
import org.olat.ims.qti21.model.audit.CandidateTestEventType;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;

import uk.ac.ed.ph.jqtiplus.attribute.Attribute;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.node.content.basic.InlineStatic;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.variable.TextOrVariable;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.DrawingInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HottextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.OrderInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.UploadInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.Choice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleAssociableChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;

/**
 * 
 * Initial date: 24 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LogViewerDeserializer {
	
	private static final Logger log = Tracing.createLoggerFor(LogViewerDeserializer.class);
	
	private static final String OUTCOMES = "outcomes=";
	private static final String PARAMS = "params=";
	
	private static final String TEST_EVENT = "TestEvent";
	private static final String ITEM_EVENT = "ItemEvent";
	private static final String TEST_ITEM_KEY = "TestItemKey";
	
	private final File logFile;
	
	private final Translator translator;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private final DateFormat dateFormat = new SimpleDateFormat(AuditLogFormatter.DATE_FORMAT); 
	
	public LogViewerDeserializer(File logFile, ResolvedAssessmentTest resolvedAssessmentTest, Translator translator) {
		this.logFile = logFile;
		this.translator = translator;
		this.resolvedAssessmentTest = resolvedAssessmentTest;
	}
	
	public List<LogViewerEntry> readEntries() {
		List<LogViewerEntry> entries = new ArrayList<>();
		try(FileReader fReader = new FileReader(logFile);
				BufferedReader bReader = new BufferedReader(fReader)) {
			String line;
	        while ((line = bReader.readLine()) != null) {
	        	readLine(line, entries);
	        }
		} catch(Exception e) {
			log.error("", e);
		}
		return entries;
	}
	
	private void readLine(String line, List<LogViewerEntry> entries) {
		try {
			LogViewerEntry entry = parseLine(line);
			if(entry != null) {
				entries.add(entry);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private LogViewerEntry parseLine(String line) throws Exception {
		int auditMarkerIndex = line.indexOf(AuditLogFormatter.AUDIT_MARKER);
		if(auditMarkerIndex >= 0) {
			return parseAuditLine(auditMarkerIndex, line);
		}
		int manualCorrectionMarkerIndex = line.indexOf(AuditLogFormatter.MANUAL_CORRECTION_MARKER);
		if(manualCorrectionMarkerIndex >= 0) {
			return parseManualCorrectionLine(manualCorrectionMarkerIndex, line);
		}
		return null;
	}

	private LogViewerEntry parseManualCorrectionLine(int markerIndex, String line) throws Exception {
		String dateString  = line.substring(0, markerIndex).trim();
		Date date = dateFormat.parse(dateString);
		LogViewerEntry entry = new LogViewerEntry(date);
		entry.setManualCorrection(true);

		String strippedLine = line.substring(markerIndex + AuditLogFormatter.MANUAL_CORRECTION_MARKER.length()).trim();
		String[] splittedLine = strippedLine.split(" ");
		if(splittedLine.length > 0) {
			for(String keyValuePair:splittedLine) {
				if(!StringHelper.containsNonWhitespace(keyValuePair)) {
					continue;
				}
				
				String[] keyValues = keyValuePair.split("=");
				if(keyValues.length == 2) {
					String key = keyValues[0];
					String value = keyValues[1];
					parseManualCorrectionKeyValue(key, value, entry);
				}
			}
			return entry;
		}
		return null;
	}
	
	private void parseManualCorrectionKeyValue(String key, String value, LogViewerEntry entry) throws Exception {
		if("identifier".equals(key)) {
			parseTestItemKey(value, entry);
		} else if("manualScore".equals(key)) {
			entry.setScore(toDouble(value)); 
		}
	}
	
	private LogViewerEntry parseAuditLine(int markerIndex, String line) throws Exception {
		String dateString  = line.substring(0, markerIndex).trim();
		Date date = dateFormat.parse(dateString);
		LogViewerEntry entry = new LogViewerEntry(date);
		
		String data = line.substring(markerIndex + AuditLogFormatter.AUDIT_MARKER.length()).trim();
		if(data.startsWith("[")) {
			entry.setOutcomes(false);
			int index = data.indexOf(']');
			if(index > 0 && index + 1 < data.length()) {
				parseEvent(data.substring(index + 1), entry);
			}
			
		} else if(data.startsWith(OUTCOMES)) {
			entry.setOutcomes(true);
			parseOutcomes(data, entry);
		}
		return entry;
	}
	
	private void parseEvent(String event, LogViewerEntry entry) {
		String line = event;
		do {
			Chunk chunk = nextChunk(line);
			line = chunk.nextStrings();
			String text = chunk.string();
			parseEventChunk(text, entry);
		} while(line != null);
	}
	
	private void parseEventChunk(String text, LogViewerEntry entry) {
		if(text.startsWith(PARAMS)) {
			String params = text.substring(PARAMS.length() + 1);
			parseParams(params, entry);
		} else if(text.startsWith(TEST_ITEM_KEY)) {
			String val = text.substring(TEST_ITEM_KEY.length(), text.length());
			String nodeId = removeParenthesis(val);
			parseTestItemKey(nodeId, entry);
		} else {
			String[] arr = text.split(":");
			if(arr.length == 2) {
				String type = arr[0];
				String val = arr[1];
				if(TEST_EVENT.equals(type)) {
					entry.setTestEventType(parseTestEventType(val));
				} else if(ITEM_EVENT.equals(type)) {
					entry.setItemEventType(parseItemEventType(val));
				}
			}
		}
	}
	
	private void parseParams(String params, LogViewerEntry entry) {
		String[] paramsArr = params.split("[|]");
		List<Answer> answers = new ArrayList<>(paramsArr.length);
		for(String param:paramsArr) {
			String[] responseValue = param.split("[=]");
			if(responseValue.length == 2) {
				String id = responseValue[0];
				String value = responseValue[1];
				if(entry == null || entry.getAssessmentItem() == null) {
					answers.add(new Answer(null, List.of(value)));	
				} else {
					List<QtiNode> nodes = searchById(Identifier.assumedLegal(id), entry.getAssessmentItem());
					if(nodes.size() != 1) {
						answers.add(new Answer(null, List.of(value)));
					} else {
						answers.add(parseParams(entry.getAssessmentItem(), nodes.get(0), value));
					}
				}
			}
		}
		entry.setAnswers(new Answers(answers));
	}
	
	private Answer parseParams(AssessmentItem item, QtiNode node, String value) {
		switch(node.getQtiClassName()) {
			case ExtendedTextInteraction.QTI_CLASS_NAME:
				return new Answer(null, List.of(removeParenthesis(value)));
			case ChoiceInteraction.QTI_CLASS_NAME:
				return parseParams((ChoiceInteraction)node, value);
			case MatchInteraction.QTI_CLASS_NAME:
				return parseParams(item, (MatchInteraction)node, value);
			case InlineChoiceInteraction.QTI_CLASS_NAME:
				return parseParams((InlineChoiceInteraction)node, value);
			case TextEntryInteraction.QTI_CLASS_NAME:
				return new Answer(null, List.of(removeParenthesis(value)));
			case HotspotInteraction.QTI_CLASS_NAME:
				return new Answer(CorrectResponsesUtil.parseResponses(value), null);
			case HottextInteraction.QTI_CLASS_NAME:
				return parseParams((HottextInteraction)node, value);
			case OrderInteraction.QTI_CLASS_NAME:
				return parseParams((OrderInteraction)node, value);
			case UploadInteraction.QTI_CLASS_NAME, DrawingInteraction.QTI_CLASS_NAME:
				return new Answer(null, List.of(value));
			default:
				return new Answer(CorrectResponsesUtil.parseResponses(value), null);
		}
	}
	
	private Answer parseParams(AssessmentItem item, MatchInteraction matchInteraction, String value) {
		List<String> responsesIds = CorrectResponsesUtil.parseResponses(value);
		List<String> values = new ArrayList<>();
		
		QTI21QuestionType matchType = QTI21QuestionType.getTypeOfMatch(item, matchInteraction);
		if(matchType == QTI21QuestionType.kprim) {
			for(String responseId:responsesIds) {
				if(responseId.endsWith("correct")) {
					values.add(translator.translate("kprim.plus"));
				} else if(responseId.endsWith("wrong")) {
					values.add(translator.translate("kprim.minus"));
				}
			}
		} else if(matchType == QTI21QuestionType.matchtruefalse) {
			for(String responseId:responsesIds) {
				if(responseId.contains("unanswered")) {
					values.add(translator.translate("match.unanswered"));
				} else if(responseId.contains("right")) {
					values.add(translator.translate("match.true"));
				} else if(responseId.contains("wrong")) {
					values.add(translator.translate("match.false"));
				}
			}
		} else {
			SimpleMatchSet sourceMatchSet = matchInteraction.getSimpleMatchSets().get(0);
			List<SimpleAssociableChoice> sourceChoices = sourceMatchSet.getSimpleAssociableChoices();
			SimpleMatchSet targetMatchSet = matchInteraction.getSimpleMatchSets().get(1);
			List<SimpleAssociableChoice> targetChoices = targetMatchSet.getSimpleAssociableChoices();
			for(String responseId:responsesIds) {
				String source = getChoice(responseId, sourceChoices);
				String target = getChoice(responseId, targetChoices);
				if(StringHelper.containsNonWhitespace(source) && StringHelper.containsNonWhitespace(target)) {
					values.add(source + " - " + target);
				}
			}
		}
		
		return new Answer(responsesIds, values);
	}
	
	private String getChoice(String responseId, List<SimpleAssociableChoice> choices) {
		for(SimpleAssociableChoice choice:choices) {
			if(responseId.contains(choice.getIdentifier().toString())) {
				return flowStaticToString(choice.getFlowStatics());
			}
		}
		return "";
	}

	private Answer parseParams(HottextInteraction hottextInteraction, String value) {
		List<Hottext> texts = QueryUtils.search(Hottext.class, hottextInteraction);
		List<String> responsesIds = CorrectResponsesUtil.parseResponses(value);
		List<String> values = new ArrayList<>();
		for(String responseId:responsesIds) {
			for(Hottext choice:texts) {
				if(responseId.equals(choice.getIdentifier().toString())) {
					values.add(inlineStaticToString(choice.getInlineStatics()));
				}
			}
		}
		return new Answer(responsesIds, values);
	}
	
	private Answer parseParams(OrderInteraction orderInteraction, String value) {
		List<String> responsesIds = CorrectResponsesUtil.parseResponses(value);
		List<SimpleChoice> choices = orderInteraction.getSimpleChoices();
		List<String> values = new ArrayList<>();
		for(String responseId:responsesIds) {
			for(SimpleChoice choice:choices) {
				if(responseId.equals(choice.getIdentifier().toString())) {
					values.add(flowStaticToString(choice.getFlowStatics()));
				}
			}
		}
		return new Answer(responsesIds, values);
	}
	
	private Answer parseParams(InlineChoiceInteraction inlineChoiceInteraction, String value) {
		List<InlineChoice> choices = inlineChoiceInteraction.getInlineChoices();
		return parseParams(choices, value);
	}
	
	private Answer parseParams(ChoiceInteraction choiceInteraction, String value) {
		List<SimpleChoice> choices = choiceInteraction.getSimpleChoices();
		return parseParams(choices, value);
	}

	private Answer parseParams(List<? extends Choice> choices, String value) {
		List<String> ids = new ArrayList<>();
		List<String> values = new ArrayList<>();
		for(int i=0; i<choices.size(); i++) {
			Choice choice = choices.get(i);
			Identifier choiceIdentifier = choice.getIdentifier();
			if(value.contains("[" + choiceIdentifier + "]")) {
				ids.add(choiceIdentifier.toString());
				String answer = null;
				if(choice instanceof SimpleChoice simpleChoice) {
					answer = flowStaticToString(simpleChoice.getFlowStatics());
				} else if(choice instanceof InlineChoice inlineChoice) {
					answer = textOrVariableToString(inlineChoice.getTextOrVariables());
				}
				if(StringHelper.containsNonWhitespace(answer)) {
					values.add(answer);
				}
			}
		}
		
		return new Answer(ids, values);
	}
	
	private static final String inlineStaticToString(List<InlineStatic> flow) {
		String answer = new AssessmentHtmlBuilder().inlineStaticString(flow);
		if(StringHelper.containsNonWhitespace(answer)) {
			answer = FilterFactory.getHtmlTagAndDescapingFilter().filter(answer);
			answer = answer.trim();
		}
		return answer;
	}
	
	private static final String flowStaticToString(List<FlowStatic> flow) {
		String answer = new AssessmentHtmlBuilder().flowStaticString(flow);
		if(StringHelper.containsNonWhitespace(answer)) {
			answer = FilterFactory.getHtmlTagAndDescapingFilter().filter(answer);
			answer = answer.trim();
		}
		return answer;
	}
	
	private static final String textOrVariableToString(List<TextOrVariable> values) {
		for(TextOrVariable value:values) {
			if(value instanceof TextRun text) {
				return text.getTextContent();
			}
		}
		return null;
	}
	
    private static final List<QtiNode> searchById(Identifier identifier, AssessmentItem item) {
        final List<QtiNode> results = new ArrayList<>();
        QueryUtils.walkTree(node -> {
        	for(Attribute<?> attr: node.getAttributes()) {
        		if("responseIdentifier".equals(attr.getLocalName()) && identifier.equals(attr.getValue())) {
        			results.add(node);
        		}
        	}
        	return true;
        }, item.getItemBody());
        return results;
    }
	
	private void parseTestItemKey(String nodeId, LogViewerEntry entry) {
		entry.setTestPlanNodeId(nodeId);
		String assessmentItemId = extractAssessmentItemId(nodeId);
		entry.setAssessmentItemId(assessmentItemId);
		
		if(resolvedAssessmentTest != null) {
			AssessmentItemRef itemRef = resolvedAssessmentTest.getItemRefsByIdentifierMap().get(Identifier.assumedLegal(assessmentItemId));
			if(itemRef != null) {
				ResolvedAssessmentItem resolvedItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
				AssessmentItem assessmentItem = resolvedItem == null ? null : resolvedItem.getRootNodeLookup().extractAssumingSuccessful();
				if(assessmentItem != null) {
					entry.setAssessmentItemTitle(assessmentItem.getTitle());
					QTI21QuestionType questionType = QTI21QuestionType.getTypeRelax(assessmentItem);
					entry.setQuestionType(questionType);
					entry.setAssessmentItem(assessmentItem);
				}
			}
		}
	}
	
	private CandidateTestEventType parseTestEventType(String val) {
		try {
			return CandidateTestEventType.valueOf(val);
		} catch (Exception e) {
			log.warn("Not an event type: {}", val);
			return null;
		}
	}
	
	private CandidateItemEventType parseItemEventType(String val) {
		try {
			return CandidateItemEventType.valueOf(val);
		} catch (Exception e) {
			log.warn("Not an event type: {}", val);
			return null;
		}
	}
	
	private void parseOutcomes(String outcomes, LogViewerEntry entry) throws Exception {
		String params = cutAtBeginning(outcomes, OUTCOMES);
		params = cutAtBeginning(params, PARAMS);
		
		String[] paramsArr = params.split("[|]");
		for(String param:paramsArr) {
			if(StringHelper.containsNonWhitespace(param)) {
				String[] paramArr = param.split("[=]");
				if(paramArr.length == 2) {
					parseOutcome(paramArr[0], paramArr[1], entry);
				}
			}
		}
	}
	
	private void parseOutcome(String param, String value, LogViewerEntry entry) {
		switch(param) {
			case "MINSCORE": entry.setMinScore(toDouble(value)); break;
			case "MAXSCORE": entry.setMaxScore(toDouble(value)); break;
			case "SCORE": entry.setScore(toDouble(value)); break;
			case "PASS": entry.setPassed(toBoolean(value)); break;
			default: log.debug("Unknow outcome: {} / {}", param, value);
		}
	}
	
	private String extractAssessmentItemId(String testPlanNodeId) {
		int index = testPlanNodeId.indexOf(':');
		if(index >= 0) {
			testPlanNodeId = testPlanNodeId.substring(0, index);
		}
		return testPlanNodeId;
	}
	
	private String removeParenthesis(String val) {
		if(val.startsWith("[")) {
			val = val.substring(1);
		}
		if(val.endsWith("]")) {
			val = val.substring(0, val.length() - 1);
		}
		return val;
	}
	
	private Boolean toBoolean(String value) {
		if("true".equals(value)) {
			return Boolean.TRUE;
		}
		return "false".equals(value) ? Boolean.FALSE : null;
	}
	
	private Double toDouble(String value) {
		return Double.valueOf(value);
	}
	
	private String cutAtBeginning(String string, String toCut) {
		if(string.startsWith(toCut)) {
			return string.substring(toCut.length());
		}
		return string;
	}
	
	private Chunk nextChunk(String string) {
		int index = string.indexOf(' ');
		if(index == 0) {
			string = string.substring(1, string.length());
			index = string.indexOf(' ');
		}
		if(string.startsWith(PARAMS)) {
			return new Chunk(string, null);
		}
		if(index >= 0) {
			String chunk = string.substring(0, index);
			String next = string.substring(index);
			return new Chunk(chunk, next);
		}
		return new Chunk(string, null);
	}
	
	private record Chunk(String string, String nextStrings) {
		//
	}
}
