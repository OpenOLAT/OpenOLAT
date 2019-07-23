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

package org.olat.ims.qti.container;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.dom4j.Element;
import org.dom4j.Node;
import org.olat.ims.qti.container.qtielements.Objectives;
import org.olat.ims.qti.container.qtielements.SectionFeedback;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.QTIHelper;
import org.olat.ims.qti.process.elements.ExpressionBuilder;
import org.olat.ims.qti.process.elements.ScoreBooleanEvaluable;

/**
 * @author Potable Shop 
 */
public class SectionContext implements Serializable {
	private String ident;
	//private String title;
	private AssessmentInstance assessInstance;

	// readonly ref!: the ref to the el_section; transient since it we don't want
	// to serialize it (too long) and can reattach it later
	private Element el_section;
	private Objectives objectives;
	private List<ItemContext> itemContexts;

	private float totalScore; // only floats and integers supported at the moment
	private int cutvalue;
	private int currentItemContextPos;
	private long timeOfStart;
	// -1 = not started yet; server time at the time of the start of the section
	private long durationLimit; // 
	private long timesAnswered;
	private long latestAnswerTime;
	private Output output;

	//private boolean outcomesProcessing;
	private boolean feedbacktesting; // has the section feedback calculation
	private boolean feedbackswitchedon; // is feedback allowed
	private boolean feedbackavailable; // is feedback currently available?
	private String scoremodel;

	/**
	 * default constructor needed for persistence
	 */
	public SectionContext() {
	//  
	}

	/**
	 * 
	 */
	public void init() {
		totalScore = 0.0f;
		currentItemContextPos = -1;
		timeOfStart = -1; // not started yet
		timesAnswered = 0; // not answered yet (this flag has no direct meaning in
		// qti)
		latestAnswerTime = -1; //outcomesProcessing = false;
		feedbacktesting = false;
		cutvalue = -1;
		feedbackavailable = false;
	}

	/**
	 * Start duration counters.
	 */
	public void start() {
		if (timeOfStart == -1) { // if not started already
			timeOfStart = System.currentTimeMillis();
		}
	}

	/**
	 *
	 */
	public void sectionWasSubmitted() {
		timesAnswered++;
		latestAnswerTime = System.currentTimeMillis();
	}

	/**
	 * Method eval.
	 */
	public void eval() {
		if (assessInstance.isSurvey()) return;
		int itccnt = getItemContextCount();
		for (int i = 0; i < itccnt; i++) {
			ItemContext ict = getItemContext(i);
			ict.eval();
		}
		calcScore(); // calc feedback
		if (feedbacktesting) calcFeedBack();
	}

	/**
	 * @param assessInstance
	 * @param el_section
	 * @param sw
	 */
	public void setUp(AssessmentInstance assessInstance, Element el_section, Switches sw) {
		this.assessInstance = assessInstance;
		this.el_section = el_section;
		this.ident = el_section.attributeValue("ident");
		init();

		Element dur = (Element) el_section.selectSingleNode("duration");
		if (dur == null) {
			durationLimit = -1; // no limit
		} else {
			String sdur = dur.getText();
			durationLimit = QTIHelper.parseISODuration(sdur);
			if (durationLimit == 0) durationLimit = -1; // Assesst Designer fix
		}

		// get objectives
		Element el_objectives = (Element)el_section.selectSingleNode("objectives");
		if (el_objectives != null) objectives = new Objectives(el_objectives);
		
		// feedback switches
		// ---------------------------------------------------------
		if (sw == null) { // no switches from the assessment context dominate
			// retrieve section switches
			Element el_control = (Element) el_section.selectSingleNode("sectioncontrol");
			if (el_control != null) {
				String feedbackswitch = el_control.attributeValue("feedbackswitch");
				String hintswitch = el_control.attributeValue("hintswitch");
				String solutionswitch = el_control.attributeValue("solutionswitch");
				feedbackswitchedon = (feedbackswitch == null) ? true : feedbackswitch.equals("Yes");
				boolean hints = (hintswitch == null) ? true : hintswitch.equals("Yes");
				boolean solutions = (solutionswitch == null) ? true : solutionswitch.equals("Yes");
				sw = new Switches(feedbackswitchedon, hints, solutions);
			}
		}

		// ----------------------- selection
		List<Element> el_items = new ArrayList<>();
		// determine which items (sections not implemented) will be chosen/selected
		// for this section
		// --- 1. take all items and resolved itemrefs which are in the section
		List items = el_section.selectNodes("item|itemref");
		for (Iterator iter = items.iterator(); iter.hasNext();) {
			Element el_item = (Element) iter.next();
			//<!ELEMENT itemref (#PCDATA)> <!ATTLIST itemref %I_LinkRefId; > <!ENTITY
			// % I_LinkRefId " linkrefid CDATA #REQUIRED">
			if (el_item.getName().equals("itemref")) {
				// resolve the entity first
				String linkRefId = el_item.attributeValue("linkrefid");
				el_item = (Element) el_section.selectSingleNode("//item[@ident='" + linkRefId + "']");
				// if item == null -> TODO Error
			}
			el_items.add(el_item);
		}
		// --- 2. select all items from the objectbank which fulfill the selection
		// criteria

		Element el_selordering = (Element) el_section.selectSingleNode("selection_ordering");
		if (el_selordering != null) {
			// do some selection and ordering
			// here comes the selection....
			// xpath =
			// "//item[itemmetadata/qtimetadata/qtimetadatafield[fieldlabel[text()='qmd_dificulty']
			// and fieldentry[text()='4']] or
			// itemmetadata/qtimetadata/qtimetadatafield[fieldlabel[text()='qmd_author']
			// and fieldentry[text()='felix']]]"
			//<!ELEMENT selection_ordering (qticomment? , sequence_parameter* ,
			// selection* , order?)>
			//<!ATTLIST selection_ordering sequence_type CDATA #IMPLIED >
			//<!ELEMENT selection (sourcebank_ref? , selection_number? ,
			// selection_metadata? ,
			//				(and_selection | or_selection | not_selection | selection_extension)?)>
			//<!ELEMENT sourcebank_ref (#PCDATA)>
			//not <!ELEMENT order (order_extension?)>
			//<!ATTLIST order order_type CDATA #REQUIRED >
			//<!ELEMENT selection_number (#PCDATA)>
			//not <!ELEMENT sequence_parameter (#PCDATA)>
			//not <!ATTLIST sequence_parameter %I_Pname; >
			List el_selections = el_selordering.selectNodes("selection");

			// iterate over all selection elements : after each we have some items to
			// add to the run-time-section
			for (Iterator it_selection = el_selections.iterator(); it_selection.hasNext();) {
				List selectedItems;
				Element el_selection = (Element) it_selection.next();
				Element el_sourcebankref = (Element) el_selection.selectSingleNode("sourcebank_ref");
				if (el_sourcebankref == null) {
					// no reference to sourcebank, -> take internal one, but dtd disallows
					// it!?? TODO
					/*
					 * 2:27 PM] <felix.jost> aus ims qti sao: [2:27 PM] <felix.jost> 3.2.1
					 * <sourcebank_ref> Description: Identifies the objectbank to which
					 * the selection and ordering rules are to be applied. This objectbank
					 * may or may not be contained in the same <questestinterop> package.
					 * [2:27 PM] <felix.jost> aber dtd: [2:28 PM] <felix.jost> <!ELEMENT
					 * questestinterop (qticomment? , (objectbank | assessment | (section |
					 * item)+))>
					 */
					selectedItems = new ArrayList();
				} else {
					String sourceBankRef = el_sourcebankref.getText();
					Element objectBank = assessInstance.getResolver().getObjectBank(sourceBankRef);

					// traverse 1.: process "and" or "or" or "not" selection to get the
					// items, if existing, otherwise take all items
					//          2.: do the selection_number
					Element andornot_selection = (Element) el_selection
							.selectSingleNode("and_selection|or_selection|not_selection|selection_metadata");
					StringBuilder select_expr = new StringBuilder("//item");
					if (andornot_selection != null) {
						// some criteria, extend above xpath to select only the appropriate
						// elements
						select_expr.append("[");
						String elName = andornot_selection.getName();
						ExpressionBuilder eb = QTIHelper.getExpressionBuilder(elName);
						eb.buildXPathExpression(andornot_selection, select_expr, false, true);
						select_expr.append("]");
					}
					selectedItems = objectBank.selectNodes(select_expr.toString());
					el_items.addAll(selectedItems);
				}
				Element el_selection_number = (Element) el_selection.selectSingleNode("selection_number");
				// --- 3. if selection_number exists, pick out some items
				if (el_selection_number != null) {
					String sNum = el_selection_number.getText();
					int num = new Integer(sNum).intValue();
					// now choose some x out of the items if selection_number exists
					List<Element> newList = new ArrayList<>();
					Random r = new Random();
					int size = el_items.size();
					// if num > size ??e.g. 5 elements should be picked, but there are
					// only four
					if (num > size) num = size;
					for (int i = 0; i < num; i++) {
						int n = r.nextInt(size--);
						Element o = el_items.remove(n);
						newList.add(o);
					}
					el_items = newList;
					/*
					 * pick out items -> remove unused items from section
					 */
					items.removeAll(el_items);
					for (Iterator iter = items.iterator(); iter.hasNext();) {
						el_section.remove((Node)iter.next());						
					}
					
					
				}
				// append found items to existing ones
			}
		} // end of el_ordering != null

		//	if there is order = random -> shuffle
		//<order order_type="Random"/>
		if (el_selordering != null) {
			Element el_order = (Element) el_selordering.selectSingleNode("order");
			if (el_order != null) {
				String order_type = el_order.attributeValue("order_type");
				if (order_type.equals("Random")) {
					Collections.shuffle(el_items);
				}
			}
		}

		// now wrap all item contexts
		itemContexts = new ArrayList<>(10);
		for (Iterator<Element> iter = el_items.iterator(); iter.hasNext();) {
			Element item = iter.next();
			item.detach();
			ItemContext itc = new ItemContext();
			itc.setUp(assessInstance, item, sw);
			if (durationLimit != -1 && assessInstance.isSectionPage()) itc.clearDurationLimit();
			itemContexts.add(itc);
		}

		//outcomesProcessing
		//<!ELEMENT section (qticomment? , duration? , qtimetadata* ,
		//  objectives* , sectioncontrol* , sectionprecondition* ,
		// sectionpostcondition* ,
		// rubric* , presentation_material? ,
		// outcomes_processing* , sectionproc_extension? ,
		// sectionfeedback* , selection_ordering? ,
		// reference? , (itemref | item | sectionref | section)*)>

		//<!ELEMENT outcomes_processing (qticomment? , outcomes ,
		// objects_condition* , processing_parameter* , map_output* ,
		// outcomes_feedback_test*)>
		//<!ELEMENT outcomes (qticomment? , (decvar , interpretvar*)+)>

		//<!ELEMENT decvar (#PCDATA)>
		//<!ATTLIST decvar %I_VarName; .......cutvalue CDATA #IMPLIED >
		Element el_outpro = (Element) el_section.selectSingleNode("outcomes_processing");
		if (el_outpro != null) {
			// get the scoring model: we need it later for calculating the score
			//<!ENTITY % I_ScoreModel " scoremodel CDATA #IMPLIED">
			scoremodel = el_outpro.attributeValue("scoremodel");
			// may be null -> then assume SumOfScores

			// set the cutvalue if given (only variable score)
			cutvalue = QTIHelper.getIntAttribute(el_outpro, "outcomes/decvar[@varname='SCORE']", "cutvalue");
			List el_oft = el_outpro.selectNodes("outcomes_feedback_test");
			if (el_oft.size() != 0) {
				feedbacktesting = true;
			}
		}
	}

	/**
	 * Method calcFeedBack.
	 */
	private void calcFeedBack() {
		List el_ofts = el_section.selectNodes("outcomes_processing/outcomes_feedback_test");
		feedbackavailable = false;
		for (Iterator it_oft = el_ofts.iterator(); it_oft.hasNext();) {
			Element el_oft = (Element) it_oft.next();
			//<!ELEMENT outcomes_feedback_test (test_variable , displayfeedback+)>
			Element el_testvar = (Element) el_oft.selectSingleNode("test_variable");
			// must exist: dtd
			//<!ELEMENT test_variable (variable_test | and_test | or_test |
			// not_test)>
			Element el_varandornot = (Element) el_testvar.selectSingleNode("variable_test|and_test|or_test|not_test");
			String elname = el_varandornot.getName();
			ScoreBooleanEvaluable sbe = QTIHelper.getSectionBooleanEvaluableInstance(elname);
			float totalscore = getScore();
			boolean fulfilled = sbe.eval(el_varandornot, totalscore);
			if (fulfilled) {
				// get feedback
				Element el_displayfeedback = (Element) el_oft.selectSingleNode("displayfeedback");
				String linkRefId = el_displayfeedback.attributeValue("linkrefid");
				// must exist (dtd)
				// ignore feedbacktype, since we section or assess feedback only accepts
				// material, no hints or solutions
				Element el_resolved = (Element) el_section.selectSingleNode(".//sectionfeedback[@ident='" + linkRefId + "']");
				getOutput().setEl_response(new SectionFeedback(el_resolved));
				// give the whole sectionfeedback to render
				feedbackavailable = true;
			}
		}
	}

	/**
	 * @return List of ItemContext instances
	 */
	public List<ItemContext> getItemContextsToRender() {
		return itemContexts;
	}

	/**
	 * Method calcScore.
	 */
	private void calcScore() {
		totalScore = 0;
		if (scoremodel == null || scoremodel.equalsIgnoreCase("SumOfScores")) { // sumofScores
			for (Iterator<ItemContext> iter = itemContexts.iterator(); iter.hasNext();) {
				ItemContext ict = iter.next();
				totalScore += ict.getScore();
			}
		} else if (scoremodel.equalsIgnoreCase("NumberCorrect")) {
			totalScore = 0;
			int tmpscore = 0;
			// correct number of items: an item is correct if it reaches the cutvalue
			for (Iterator<ItemContext> iter = itemContexts.iterator(); iter.hasNext();) {
				ItemContext ict = iter.next();

				Variable var = ict.getVariables().getSCOREVariable();
				if (var == null) {
					// we demand that a SCORE variable must always exist
					throw new RuntimeException("no SCORE def for " + ict.getIdent());
				} else {
					float itemscore = var.getTruncatedValue();
					float itemcutval = var.getCutValue();
					if (itemscore >= itemcutval) tmpscore++; // count items correct
				}
			}
			if (tmpscore >= cutvalue) totalScore = 1.0f; // cutvalue of the section
		} else {
			throw new RuntimeException("scoring algorithm " + scoremodel + " not supported");
		}
	}

	/**
	 * @return
	 */
	public float getScore() {
		calcScore();
		return totalScore;
	}

	/**
	 * @return
	 */
	public String getIdent() {
		return ident;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return el_section.attributeValue("title");
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "<br />section:" + getIdent() + " score:" + getScore() + ",items" + itemContexts.toString() + "=" + super.toString();
	}

	/**
	 * Returns the sectionItems.
	 * 
	 * @return List
	 */
	public List<ItemContext> getSectionItemContexts() {
		return itemContexts;
	}

	/**
	 * Method getItemContext.
	 * 
	 * @param sIdent
	 * @return ItemContext
	 */
	public ItemContext getItemContext(String sIdent) {
		for (Iterator<ItemContext> it_icts = getSectionItemContexts().iterator(); it_icts.hasNext();) {
			ItemContext itc = it_icts.next();
			if (itc.getIdent().equals(sIdent)) { return itc; }
		}

		// not found: for a user answer, no corresponding item could be found ->
		// error (in form?,logic...)
		// the other way round: no answer of user = the ItemInput of an ItemContext
		// is simply not set
		throw new RuntimeException("could not find an itemcontext with ident " + ident + " , but got an itemInput with this ident!");
	}

	/**
	 * @return
	 */
	public int getItemsAttemptedCount() {
		int total = 0;
		int itcnt = getItemContextCount();
		for (int i = 0; i < itcnt; i++) {
			if (getItemContext(i).getTimesAnswered() > 0) total++;
		}
		return total;
	}

	/**
	 * @return
	 */
	public int getItemsPresentedCount() {
		int total = 0;
		int itcnt = getItemContextCount();
		for (int i = 0; i < itcnt; i++) {
			if (getItemContext(i).isStarted()) total++;
		}
		return total;
	}
	
	/**
	 * @return
	 */
	public int getItemsAnsweredCount() {
		int total = 0;
		int itcnt = getItemContextCount();
		for (int i = 0; i < itcnt; i++) {
			if (getItemContext(i).getTimesAnswered()>0) total++;
		}
		return total;
	}
	
	/**
	 * @return
	 */
	public int getItemsOpenCount() {
		int total = 0;
		int itcnt = getItemContextCount();
		for (int i = 0; i < itcnt; i++) {
			if (getItemContext(i).isOpen()) total++;
		}
		return total;
	}
	
	/**
	 * @return
	 */
	public int getItemContextCount() {
		return itemContexts.size();
	}

	/**
	 * Returns the currentItemContextPos.
	 * 
	 * @return int
	 */
	public int getCurrentItemContextPos() {
		return currentItemContextPos;
	}

	/**
	 * Sets the currentItemContextPos.
	 * 
	 * @param currentItemContextPos The currentItemContextPos to set
	 */
	public void setCurrentItemContextPos(int currentItemContextPos) {
		this.currentItemContextPos = currentItemContextPos;
	}

	/**
	 * Method getCurrentItemContext.
	 * 
	 * @return Element
	 */
	public ItemContext getCurrentItemContext() {
		return itemContexts.get(currentItemContextPos);
	}

	/**
	 * checks whether the user may still submit answers
	 * 
	 * @return
	 */
	public boolean isOpen() {
		// not started yet or no timelimit or within timelimit
		return onTime();
	}

	/**
	 * @return
	 */
	public boolean onTime() {
		// ok if not started yet or no time limit or within limit
		return (timeOfStart == -1) || (durationLimit == -1) || (System.currentTimeMillis() < (timeOfStart + durationLimit));
	}

	/**
	 * @return
	 */
	public boolean isStarted() {
		return (timeOfStart != -1);
	}

	/**
	 * @param pos
	 * @return
	 */
	public ItemContext getItemContext(int pos) {
		return itemContexts.get(pos);
	}

	/**
	 * Returns the timeOfStart.
	 * 
	 * @return long
	 */
	public long getTimeOfStart() {
		return timeOfStart;
	}

	/**
	 * Returns the timeOfStop.
	 * 
	 * @return long
	 */
	public long getLatestAnswerTime() {
		return latestAnswerTime;
	}

	/**
	 * Get the time limit set on this section
	 * 
	 * @return
	 */
	public long getDurationLimit() {
		return durationLimit;
	}

	/**
	 * Get the time to completion for this section
	 * 
	 * @return
	 */
	public long getDuration() {
		if (timesAnswered == 0) return -1;
		return latestAnswerTime - timeOfStart;
	}

	/**
	 * @return
	 */
	public float getMaxScore() {
		float score = 0.0f;
		for (int i = 0; i < getItemContextCount(); i++) {
			ItemContext itctx = getItemContext(i);
			float maxScore = itctx.getMaxScore();
			if (maxScore == -1) return -1;
			else score += maxScore;
		}
		return score;
	}

	/**
	 * @return Output
	 */
	public Output getOutput() {
		if (output == null) {
			output = new Output();
		}
		return output;
	}

	/**
	 * @return boolean
	 */
	public boolean isFeedbackavailable() {
		return feedbackavailable;
	}

	public Objectives getObjectives() {
		return objectives;
	}

	public int getCutValue() {
		return cutvalue;
	}
}