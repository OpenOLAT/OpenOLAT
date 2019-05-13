/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.editor.beecom.objects;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.olat.ims.qti.editor.beecom.parser.ParserManager;

/**
 * @author rkulow Handles both single choice and multiple choice.
 * @author sandraroth kprim added
 */
public class ChoiceQuestion extends Question implements QTIObject {

	private static final Logger log = Tracing.createLoggerFor(ChoiceQuestion.class);

	private static ParserManager parserManager = new ParserManager();

	/*
	 * If singleCorrect=true, all answers marked as correct have to be selected as
	 * answer by the user in order to get points. Points are given according to
	 * singleCorrectScore in this case. If singleCorrect=false, any of the answers
	 * marked as correct give points. Points are given according to
	 * Choice.getPoints() in this case. singleCorrect can be extracted by looking
	 * at the way respconditions are built. singleCorrect=true builds a single
	 * respcondition with <setvar action="Set"> whereas singleCorrect=false builds
	 * a respcondition for every correct answer with <setvar action="Add">.
	 */

	public static final String BLOCK = "Block";
	public static final String LIST = "List";
	private String flowLabelClass = LIST; // default value

	public ChoiceQuestion() {
		super();
	}

	/**
	 * Called by ItemParser to fetch question/answers.
	 * 
	 * @param item
	 * @return
	 */
	public static ChoiceQuestion getInstance(Element item) {
		ChoiceQuestion instance = new ChoiceQuestion();
		try {
			String item_ident = item.attributeValue("ident");
			if (item_ident.startsWith(ItemParser.ITEM_PREFIX_SCQ)) instance.setType(TYPE_SC);
			else if (item_ident.startsWith(ItemParser.ITEM_PREFIX_MCQ)) instance.setType(TYPE_MC);
			else if (item_ident.startsWith(ItemParser.ITEM_PREFIX_KPRIM)) instance.setType(TYPE_KPRIM);
			else return null;

			Element presentationXML = item.element("presentation");
			Element material = presentationXML.element("material");
			Element flow = presentationXML.element("flow");
			if (material == null && flow != null) {
				/*
				 * This is a bugfix (see OLAT-4194). According to the qti specification,
				 * the presentation element can either have the elements material and
				 * response_lid as children or they may be children of the flow element
				 * which itself is a child of presentation.
				 */
				material = flow.element("material");
			}
			Material matQuestion = (Material) parserManager.parse(material);
			if (matQuestion != null) instance.setQuestion(matQuestion);

			Element response_lid = presentationXML.element("response_lid");
			if (response_lid == null && flow != null) {
				response_lid = flow.element("response_lid");
			}
			String identQuestion = response_lid.attribute("ident").getText();
			instance.setIdent(identQuestion);
			String shuffle = response_lid.element("render_choice").attributeValue("shuffle");
			if (shuffle == null) shuffle = "Yes";
			instance.setShuffle(shuffle.equals("Yes"));

			// Set first flow_label class that is found for entire question. This
			// editor uses the same flow_label on every response
			Element flow_label = (Element) response_lid.selectSingleNode(".//flow_label");
			if (flow_label != null) instance.setFlowLabelClass(flow_label.attributeValue("class"));

			List response_lables = response_lid.selectNodes(".//response_label");
			List<Response> choices = QTIEditHelper.fetchChoices(response_lables);
			instance.setResponses(choices);

			Element resprocessingXML = item.element("resprocessing");
			if (resprocessingXML != null) {

				List respconditions = resprocessingXML.elements("respcondition");
				Map<String,Float> points = QTIEditHelper.fetchPoints(respconditions, instance.getType());

				// postprocessing choices
				for (Iterator<Response> i = choices.iterator(); i.hasNext();) {
					ChoiceResponse choice = (ChoiceResponse) i.next();
					Float fPoints = points.get(choice.getIdent());
					if (fPoints != null) {
						choice.setPoints(fPoints.floatValue());
						choice.setCorrect(true);
					}
				}
				
				// get type of multiple choice
				if (instance.getType() == TYPE_MC) {
					// if does not contain any ANDs, assume only one combination
					// of answers is possible (which sets points by a setvar action="Set")
					if (resprocessingXML.selectNodes(".//setvar[@action='Add']").size() == 0) {
						instance.setSingleCorrect(true);
						Collection<Float> values = points.values();
						if (values.size() > 0) instance.setSingleCorrectScore((values.iterator().next()).floatValue());
					} else {
						instance.setSingleCorrect(false);
					}
					QTIEditHelper.configureMinMaxScore(instance, (Element) resprocessingXML.selectSingleNode(".//decvar"));
				} else if (instance.getType() == TYPE_SC) {
					QTIEditHelper.configureMinMaxScore(instance, (Element) resprocessingXML.selectSingleNode(".//decvar"));
					Collection<Float> values = points.values();
					if (values.size() > 0) {
						instance.setSingleCorrect(true);
						instance.setSingleCorrectScore((values.iterator().next()).floatValue());
					} else {
						instance.setSingleCorrect(false);
						instance.setSingleCorrectScore(0f);
					}
				} else if (instance.getType() == TYPE_KPRIM) {
					instance.setSingleCorrect(false);
					float maxValue = 0;
					try {
						Node score = resprocessingXML.selectSingleNode(".//decvar[@varname='SCORE']/@maxvalue");
						if (score != null) {
							maxValue = Float.parseFloat(score.getText());
						}

					} catch (NumberFormatException e) {
						// set maxValue 0
					}
					for (int i = 0; i < choices.size(); i++) {
						ChoiceResponse choice = (ChoiceResponse) choices.get(i);
						if (resprocessingXML.selectNodes(
								"./respcondition[@title='Mastery']/conditionvar/varequal[text()='" + choice.getIdent() + ":correct']").size() > 0) {
							choice.setCorrect(true);
							choice.setPoints(maxValue / 4);
						} else {
							choice.setCorrect(false);
							choice.setPoints(maxValue / 4);
						}
					}
					QTIEditHelper.configureMinMaxScore(instance, (Element) resprocessingXML.selectSingleNode(".//decvar"));
				} else {
					QTIEditHelper.configureMinMaxScore(instance, (Element) resprocessingXML.selectSingleNode(".//decvar"));
				}

					
			}
		} catch (NullPointerException e) {
			/*
			 * A null pointer exeption may occur (and has occured) due to incomplete
			 * implementation of the qti specification within OLAT. Since the QTI xml
			 * validation at this point already passed, it's hard to still output user
			 * information. At this point, definitely log error.
			 */
			log.error("Reading choice question failed. Might be due to incomplete qti implementation.", e);
		}
		return instance;
	}

	/**
	 * Build XML tree (presentation & resprocessing)
	 */
	public void addToElement(Element root) {
		Element presentationXML = root.addElement("presentation");
		presentationXML.addAttribute("label", getLable());

		// Question
		getQuestion().addToElement(presentationXML);
		Element response_lid = presentationXML.addElement("response_lid");
		response_lid.addAttribute("ident", getIdent());
		response_lid.addAttribute("rcardinality", (getType() == TYPE_SC) ? "Single" : "Multiple");
		response_lid.addAttribute("rtiming", "No");
		Element render_choice = response_lid.addElement("render_choice");
		render_choice.addAttribute("shuffle", isShuffle() ? "Yes" : "No");
		render_choice.addAttribute("minnumber", getType() == TYPE_SC ? "1" : "0");
		render_choice.addAttribute("maxnumber", getType() == TYPE_SC ? "1" : String.valueOf(getResponses().size()));

		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			ChoiceResponse tmpChoice = (ChoiceResponse) i.next();
			Element flow_label = render_choice.addElement("flow_label");
			// Add horizontal or vertical alignment. All flow_labels get the
			// same value
			flow_label.addAttribute("class", getFlowLabelClass());
			Element response_lable = flow_label.addElement("response_label");
			response_lable.addAttribute("ident", tmpChoice.getIdent());
			response_lable.addAttribute("rshuffle", "Yes"); // QTI default value
			tmpChoice.getContent().addToElement(response_lable);
		}
		// ------------------
		Element resprocessingXML = root.addElement("resprocessing");

		Element outcomes = resprocessingXML.addElement("outcomes");
		Element decvar = outcomes.addElement("decvar");
		decvar.addAttribute("varname", "SCORE");
		decvar.addAttribute("vartype", "Decimal");
		decvar.addAttribute("defaultval", "0");
		decvar.addAttribute("minvalue", "" + getMinValue());
		float maxScore = QTIEditHelper.calculateMaxScore(this);
		maxScore = maxScore > getMaxValue() ? getMaxValue() : maxScore;
		decvar.addAttribute("maxvalue", "" + maxScore);
		decvar.addAttribute("cutvalue", "" + maxScore);

		// process respcondition_correct and fail
		if (getType() == TYPE_SC) {
			buildRespconditionSC_mastery(resprocessingXML);
			buildRespcondition_fail(resprocessingXML, true);
		} else if (getType() == TYPE_MC) {
			if (isSingleCorrect()) {
				buildRespconditionMCSingle_mastery(resprocessingXML);
				buildRespcondition_fail(resprocessingXML, true);
			} else {
				buildRespconditionMCMulti_mastery(resprocessingXML);
				buildRespcondition_fail(resprocessingXML, false);
			}
		} else if (getType() == TYPE_KPRIM) {
			buildRespconditionKprim(resprocessingXML);
			buildRespconditionKprim_fail(resprocessingXML);
		}

		// hint
		if (getHintText() != null) QTIEditHelper.addHintElement(root, getHintText());

		// solution
		if (getSolutionText() != null) QTIEditHelper.addSolutionElement(root, getSolutionText());

		// Response feedback
		if (getType() != TYPE_KPRIM) {
			buildRespconditionOlatFeedback(resprocessingXML);

			// Feedback for all other cases eg. none has been selected
			Element respcondition_incorrect = resprocessingXML.addElement("respcondition");
			respcondition_incorrect.addAttribute("title", "Fail");
			respcondition_incorrect.addAttribute("continue", "Yes");
			respcondition_incorrect.addElement("conditionvar").addElement("other");
			Element setvar = respcondition_incorrect.addElement("setvar");
			setvar.addAttribute("varname", "SCORE");
			setvar.addAttribute("action", "Set");
			setvar.addText("0");
			QTIEditHelper.addFeedbackFail(respcondition_incorrect);
			QTIEditHelper.addFeedbackHint(respcondition_incorrect);
			QTIEditHelper.addFeedbackSolution(respcondition_incorrect);
		}

	} // addToElement

	/**
	 * Build resprocessing for single choice item. Set score to correct value and
	 * use mastery feedback
	 * 
	 * @param resprocessingXML
	 */
	private void buildRespconditionSC_mastery(Element resprocessingXML) {
		Element respcondition_correct = resprocessingXML.addElement("respcondition");
		respcondition_correct.addAttribute("title", "Mastery");
		respcondition_correct.addAttribute("continue", "Yes");

		Element conditionvar = respcondition_correct.addElement("conditionvar");
		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			// fetch correct answer (there should be a single instance)
			ChoiceResponse tmpChoice = (ChoiceResponse) i.next();
			if (!tmpChoice.isCorrect()) continue;

			// found correct answer
			Element varequal = conditionvar.addElement("varequal");
			varequal.addAttribute("respident", getIdent());
			varequal.addAttribute("case", "Yes");
			varequal.addText(tmpChoice.getIdent());
			break;
		} // for loop

		// check if conditionvar has correct value
		if (conditionvar.elements().size() == 0) {
			resprocessingXML.remove(respcondition_correct);
			return;
		}

		Element setvar = respcondition_correct.addElement("setvar");
		setvar.addAttribute("varname", "SCORE");
		setvar.addAttribute("action", "Set");
		setvar.addText("" + getSingleCorrectScore());

		// Use mastery feedback
		QTIEditHelper.addFeedbackMastery(respcondition_correct);
	}

	/**
	 * Build resprocessing for multiple choice item with a single correct answer.
	 * Set score to correct value and use mastery feedback.
	 * 
	 * @param resprocessingXML
	 */
	private void buildRespconditionMCSingle_mastery(Element resprocessingXML) {
		Element respcondition_correct = resprocessingXML.addElement("respcondition");
		respcondition_correct.addAttribute("title", "Mastery");
		respcondition_correct.addAttribute("continue", "Yes");

		Element conditionvar = respcondition_correct.addElement("conditionvar");
		Element and = conditionvar.addElement("and");
		Element not = conditionvar.addElement("not");
		Element or = not.addElement("or");
		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			ChoiceResponse tmpChoice = (ChoiceResponse) i.next();
			Element varequal;
			if (tmpChoice.isCorrect()) { // correct answers
				varequal = and.addElement("varequal");
			} else { // incorrect answers
				varequal = or.addElement("varequal");
			}
			varequal.addAttribute("respident", getIdent());
			varequal.addAttribute("case", "Yes");
			varequal.addText(tmpChoice.getIdent());
		} // for loop

		Element setvar = respcondition_correct.addElement("setvar");
		setvar.addAttribute("varname", "SCORE");
		setvar.addAttribute("action", "Set");
		setvar.addText("" + getSingleCorrectScore());

		// Use mastery feedback
		QTIEditHelper.addFeedbackMastery(respcondition_correct);

		// remove whole respcondition if empty
		if (or.element("varequal") == null && and.element("varequal") == null) {
			resprocessingXML.remove(respcondition_correct);
		} else {
			// remove any unset <and> and <not> nodes
			if (and.element("varequal") == null) conditionvar.remove(and);
			if (or.element("varequal") == null) conditionvar.remove(not);
		}

	}

	/**
	 * Build resprocessing for multiple choice question with multiple correct
	 * answers. Sets correct score for positive (mastery) and negative (fail)
	 * conditions and use mastery feedback when all mastery responses have been
	 * selected.
	 * 
	 * @param resprocessingXML
	 */
	private void buildRespconditionMCMulti_mastery(Element resprocessingXML) {
		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			ChoiceResponse tmpChoice = (ChoiceResponse) i.next();
			float points = tmpChoice.getPoints();
			if (points == 0) continue;

			Element respcondition_correct = resprocessingXML.addElement("respcondition");
			respcondition_correct.addAttribute("continue", "Yes");
			if (points > 0) respcondition_correct.addAttribute("title", "Mastery");
			else respcondition_correct.addAttribute("title", "Fail");

			Element varequal = respcondition_correct.addElement("conditionvar").addElement("varequal");
			varequal.addAttribute("respident", getIdent());
			varequal.addAttribute("case", "Yes");
			varequal.addText(tmpChoice.getIdent());

			Element setvar = respcondition_correct.addElement("setvar");
			setvar.addAttribute("varname", "SCORE");
			setvar.addAttribute("action", "Add");
			setvar.addText("" + points);

		} // for loop

		// Resp condition for feedback mastery:
		// all response with points>0 must be selected
		Element respcondition_correct = resprocessingXML.addElement("respcondition");
		respcondition_correct.addAttribute("title", "Mastery");
		respcondition_correct.addAttribute("continue", "Yes");
		Element conditionvar = respcondition_correct.addElement("conditionvar");
		Element and = conditionvar.addElement("and");
		Element not = conditionvar.addElement("not");
		Element or = not.addElement("or");

		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			ChoiceResponse tmpChoice = (ChoiceResponse) i.next();
			Element varequal;
			if (tmpChoice.getPoints() > 0) {
				varequal = and.addElement("varequal");
			} else { // incorrect answers
				varequal = or.addElement("varequal");
			}
			varequal.addAttribute("respident", getIdent());
			varequal.addAttribute("case", "Yes");
			varequal.addText(tmpChoice.getIdent());
		} // for loop

		// Use mastery feedback
		QTIEditHelper.addFeedbackMastery(respcondition_correct);

		// remove whole respcondition if empty
		if (or.element("varequal") == null && and.element("varequal") == null) {
			resprocessingXML.remove(respcondition_correct);
		} else {
			// remove any unset <and> and <not> nodes
			if (and.element("varequal") == null) conditionvar.remove(and);
			if (or.element("varequal") == null) conditionvar.remove(not);
		}
	}

	/**
	 * Build resprocessing for Kprim question. 
	 * @param resprocessingXML
	 */
	
	private void buildRespconditionKprim(Element resprocessingXML) {
		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			ChoiceResponse choice = (ChoiceResponse) i.next();
			if (choice.isCorrect()) {
				addRespcondition(resprocessingXML, choice.getIdent() + ":correct", true, String.valueOf(choice.getPoints()));
				addRespcondition(resprocessingXML, choice.getIdent() + ":correct", false, String.valueOf(-choice.getPoints()));
			} else {
				addRespcondition(resprocessingXML, choice.getIdent() + ":wrong", true, String.valueOf(choice.getPoints()));
				addRespcondition(resprocessingXML, choice.getIdent() + ":wrong", false, String.valueOf(-choice.getPoints()));
			}
		}

		// Resp condition for feedback mastery kprim:
		Element respcondition_correct = resprocessingXML.addElement("respcondition");
		respcondition_correct.addAttribute("title", "Mastery");
		respcondition_correct.addAttribute("continue", "Yes");
		Element conditionvar = respcondition_correct.addElement("conditionvar");
		Element and = conditionvar.addElement("and");

		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			ChoiceResponse choice = (ChoiceResponse) i.next();
			Element varequal;
			varequal = and.addElement("varequal");
			varequal.addAttribute("respident", getIdent());
			varequal.addAttribute("case", "Yes");
			if (choice.isCorrect()) {
				varequal.addText(choice.getIdent() + ":correct");
			} else {
				varequal.addText(choice.getIdent() + ":wrong");
			}
		}

		// Use mastery feedback
		QTIEditHelper.addFeedbackMastery(respcondition_correct);
	}
	
	/**
	 * Feedback, solution and hints in case of failure
	 * @param resprocessingXML
	 */

	private void buildRespconditionKprim_fail(Element resprocessingXML) {
		Element respcondition_fail = resprocessingXML.addElement("respcondition");
		respcondition_fail.addAttribute("title", "Fail");
		respcondition_fail.addAttribute("continue", "Yes");
		Element conditionvar = respcondition_fail.addElement("conditionvar");
		Element not = conditionvar.addElement("not");
		Element and = not.addElement("and");

		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			ChoiceResponse choice = (ChoiceResponse) i.next();
			Element varequal;
			varequal = and.addElement("varequal");
			varequal.addAttribute("respident", getIdent());
			varequal.addAttribute("case", "Yes");
			if (choice.isCorrect()) {
				varequal.addText(choice.getIdent() + ":correct");
			} else { // incorrect answers
				varequal.addText(choice.getIdent() + ":wrong");
			}
		} 
		QTIEditHelper.addFeedbackFail(respcondition_fail);
		QTIEditHelper.addFeedbackHint(respcondition_fail);
		QTIEditHelper.addFeedbackSolution(respcondition_fail);
	}

	/**
	 * Adds condition to resprocessing with ident
	 * 
	 * @param resprocessingXML
	 * @param ident
	 * @param mastery
	 * @param points
	 */
	private void addRespcondition(Element resprocessingXML, String ident, boolean mastery, String points) {
		Element respcondition = resprocessingXML.addElement("respcondition");
		respcondition.addAttribute("continue", "Yes");

		if (mastery) respcondition.addAttribute("title", "Mastery");
		else respcondition.addAttribute("title", "Fail");
		Element condition = respcondition.addElement("conditionvar");
		if (!mastery) condition = condition.addElement("not");
		Element varequal = condition.addElement("varequal");
		varequal.addAttribute("respident", getIdent());
		varequal.addAttribute("case", "Yes");
		varequal.addText(ident);

		Element setvar = respcondition.addElement("setvar");
		setvar.addAttribute("varname", "SCORE");
		setvar.addAttribute("action", "Add");
		setvar.addText(points);
	}

	/**
	 * Build fail resprocessing: Adjust score to 0 (if multiple correct mode) and
	 * set hints, solutions and fail feedback
	 * 
	 * @param resprocessingXML
	 * @param isSingleCorrect
	 */
	private void buildRespcondition_fail(Element resprocessingXML, boolean isSingleCorrect) {
		// build
		Element respcondition_fail = resprocessingXML.addElement("respcondition");
		respcondition_fail.addAttribute("title", "Fail");
		respcondition_fail.addAttribute("continue", "Yes");
		Element conditionvar = respcondition_fail.addElement("conditionvar");
		Element or = conditionvar.addElement("or");

		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			ChoiceResponse tmpChoice = (ChoiceResponse) i.next();
			Element varequal;
			// Add this response to the fail case
			// if single correct type and not correct
			// or multi correct and points negative or 0
			if ((isSingleCorrect && !tmpChoice.isCorrect()) || (!isSingleCorrect && tmpChoice.getPoints() <= 0)) {
				varequal = or.addElement("varequal");
				varequal.addAttribute("respident", getIdent());
				varequal.addAttribute("case", "Yes");
				varequal.addText(tmpChoice.getIdent());
			}
		} // for loop

		if (isSingleCorrect) {
			Element setvar = respcondition_fail.addElement("setvar");
			setvar.addAttribute("varname", "SCORE");
			setvar.addAttribute("action", "Set");
			setvar.addText("0");
		}

		// Use fail feedback, hints and solutions
		QTIEditHelper.addFeedbackFail(respcondition_fail);
		QTIEditHelper.addFeedbackHint(respcondition_fail);
		QTIEditHelper.addFeedbackSolution(respcondition_fail);

		// remove whole respcondition if empty
		if (or.element("varequal") == null) resprocessingXML.remove(respcondition_fail);
	}

	/**
	 * Build resprocessing for olat response feedbacks (uses naming conventions:
	 * respcondition:title is set to _olat_resp_feedback to signal a feedback that
	 * it belongs directly to the response with the same response ident as the
	 * current feedback)
	 * 
	 * @param resprocessingXML
	 */
	private void buildRespconditionOlatFeedback(Element resprocessingXML) {
		for (Iterator<Response> i = getResponses().iterator(); i.hasNext();) {
			Element respcondition = resprocessingXML.addElement("respcondition");
			respcondition.addAttribute("title", "_olat_resp_feedback");
			respcondition.addAttribute("continue", "Yes");

			Element conditionvar = respcondition.addElement("conditionvar");

			ChoiceResponse tmpChoice = (ChoiceResponse) i.next();
			Element varequal = conditionvar.addElement("varequal");
			varequal.addAttribute("respident", getIdent());
			varequal.addAttribute("case", "Yes");
			varequal.addText(tmpChoice.getIdent());
			QTIEditHelper.addFeedbackOlatResp(respcondition, tmpChoice.getIdent());
		}
	}

	/**
	 * ************************ GETTERS and SETTERS
	 * ********************************
	 */

	/**
	 * @return
	 */
	public String getFlowLabelClass() {
		return flowLabelClass;
	}

	/**
	 * Set alignment of response items:<br>
	 * For horizontal alignment: Block <br>
	 * For vertical alignment: List
	 * 
	 * @param string
	 */
	public void setFlowLabelClass(String string) {
		// only allow Block or List as value, default is set to List
		if (string != null && string.equals(BLOCK)) {
			flowLabelClass = BLOCK;
		} else {
			flowLabelClass = LIST;
		}
	}

}