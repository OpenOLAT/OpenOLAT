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

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.ims.qti.container.qtielements.AssessFeedback;
import org.olat.ims.qti.container.qtielements.Objectives;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.QTIHelper;
import org.olat.ims.qti.process.elements.ExpressionBuilder;
import org.olat.ims.qti.process.elements.ScoreBooleanEvaluable;

/**
 * contains the sections of the assignment. assumption: each toplevel-section of
 * an assignment means one screen <!ELEMENT assessment (qticomment? , duration? ,
 * qtimetadata* , objectives* , assessmentcontrol* , rubric* ,
 * presentation_material? , outcomes_processing* , assessproc_extension? ,
 * assessfeedback* , selection_ordering? , reference? , (sectionref |
 * section)+)> <!ATTLIST assessment %I_Ident; %I_Title; xml:lang CDATA #IMPLIED >
 * 
 * @author Felix Jost
 */
public class AssessmentContext implements Serializable {
	// readonly ref!: the ref to the el_assessment; transient since it we don't
	// want to serialize it (too long) and can reattach it later
	//private transient Element el_assessment;
	private String ident;
	private String title;
	private AssessmentInstance assessInstance;

	private Element el_assessment;
	private Objectives objectives;
	private Switches switches = null;
	private Output output;

	// the sectioncontexts of this assessment
	private List<SectionContext> sectionContexts;

	// the current section beeing chosen by the user or forced by the system
	private int currentSectionContextPos;
	private long timeOfStart;
	// server time at the time of the start of the assessment
	private long timeOfStop;
	// server time at the time of the start of the assessment
	private long durationLimit; // 
	private float cutvalue = -1.0f;
	private String scoremodel;
	private boolean feedbacktesting;
	private boolean feedbackavailable;

	/**
	 * default constructor needed for persistence
	 */
	public AssessmentContext() {
	//
	}

	/**
	 * 
	 */
	public void init() {
		currentSectionContextPos = -1;
		feedbacktesting = false;
		feedbackavailable = false;
		timeOfStart = -1; // not started yet
		timeOfStop = -1; // not stopped yet
	}

	/**
	 * Method setUp.
	 * 
	 * @param assessInstance
	 */
	public void setUp(AssessmentInstance assessInstance) {
		this.assessInstance = assessInstance;
		init();

		Document el_questestinterop = assessInstance.getResolver().getQTIDocument();
		el_assessment = (Element) el_questestinterop.selectSingleNode("questestinterop/assessment");

		ident = el_assessment.attributeValue("ident");
		title = el_assessment.attributeValue("title");
		Element dur = (Element) el_assessment.selectSingleNode("duration");

		if (dur == null) {
			durationLimit = -1; // no limit
		} else {
			String sdur = dur.getText();
			durationLimit = QTIHelper.parseISODuration(sdur);
			if (durationLimit == 0) durationLimit = -1; // Assesst Designer fix
		}

		// get objectives
		Element el_objectives = (Element)el_assessment.selectSingleNode("objectives");
		if (el_objectives != null) objectives = new Objectives(el_objectives);
		
		// set feedback, hint, and solutions switches
		//<!ENTITY % I_FeedbackSwitch " feedbackswitch (Yes | No ) 'Yes'">
		//<!ENTITY % I_HintSwitch " hintswitch (Yes | No ) 'Yes'">
		//<!ENTITY % I_SolutionSwitch " solutionswitch (Yes | No ) 'Yes'">

		//<!ELEMENT assessment (qticomment? , duration? , qtimetadata* ,
		// objectives* , assessmentcontrol* , rubric* , presentation_material? ,
		// outcomes_processing* , assessproc_extension? , assessfeedback* ,
		// selection_ordering? , reference? , (sectionref | section)+)>
		//<!ELEMENT assessmentcontrol (qticomment?)>
		Element el_control = (Element) el_assessment.selectSingleNode("assessmentcontrol");
		if (el_control != null) {
			String feedbackswitch = el_control.attributeValue("feedbackswitch");
			String hintswitch = el_control.attributeValue("hintswitch");
			String solutionswitch = el_control.attributeValue("solutionswitch");
			boolean feedback = (feedbackswitch == null) ? true : feedbackswitch.equals("Yes");
			boolean hints = (hintswitch == null) ? true : hintswitch.equals("Yes");
			boolean solutions = (solutionswitch == null) ? true : solutionswitch.equals("Yes");
			switches = new Switches(feedback, hints, solutions);
		}

		// scoring model and outcomes processing
		Element el_outpro = (Element) el_assessment.selectSingleNode("outcomes_processing");
		if (el_outpro != null) {
			// get the scoring model: we need it later for calculating the score
			//<!ENTITY % I_ScoreModel " scoremodel CDATA #IMPLIED">
			scoremodel = el_outpro.attributeValue("scoremodel");
			// may be null -> then assume SumOfScores

			// set the cutvalue if given (only variable score)
			cutvalue = QTIHelper.getFloatAttribute(el_outpro, "outcomes/decvar[@varname='SCORE']", "cutvalue");
			List el_oft = el_outpro.selectNodes("outcomes_feedback_test");
			if (el_oft.size() != 0) {
				feedbacktesting = true;
			}
		}

		initSections(el_assessment, switches);
		init();
	}

	private void initSections(Element assessment, Switches sw) {
		sectionContexts = new ArrayList<>(2);
		

		List<Element> el_sections = new ArrayList<>();

		//<!ELEMENT sectionref (#PCDATA)>
		//<!ATTLIST sectionref %I_LinkRefId; >
		List sections = assessment.selectNodes("section|sectionref");
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			Element el_section = (Element) iter.next();
			
			// resolve sectionref into the correct sections
			if (el_section.getName().equals("sectionref")) {
				String linkRefId = el_section.attributeValue("linkrefid");
				el_section = (Element) el_section.selectSingleNode("//section[@ident='" + linkRefId + "']");
				if (el_section == null) { 
					throw new RuntimeException("sectionref with ref '" + linkRefId + "' could not be resolved");
				}
			}
			
			el_sections.add(el_section);
		}
		
		Element el_selordering = (Element) assessment.selectSingleNode("selection_ordering");
		if (el_selordering != null) {
			// do some selection and ordering
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
				List selectedSections;
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
					selectedSections = new ArrayList();
				} else {
					String sourceBankRef = el_sourcebankref.getText();
					Element objectBank = assessInstance.getResolver().getObjectBank(sourceBankRef);

					// traverse 1.: process "and" or "or" or "not" selection to get the
					// items, if existing, otherwise take all items
					//          2.: do the selection_number
					Element andornot_selection = (Element) el_selection
							.selectSingleNode("and_selection|or_selection|not_selection|selection_metadata");
					StringBuilder select_expr = new StringBuilder("//section");
					if (andornot_selection != null) {
						// some criteria, extend above xpath to select only the appropriate
						// elements
						select_expr.append("[");
						String elName = andornot_selection.getName();
						ExpressionBuilder eb = QTIHelper.getExpressionBuilder(elName);
						eb.buildXPathExpression(andornot_selection, select_expr, false, true);
						select_expr.append("]");
					}
					selectedSections = objectBank.selectNodes(select_expr.toString());
					el_sections.addAll(selectedSections);
				}
				Element el_selection_number = (Element) el_selection.selectSingleNode("selection_number");
				// --- 3. if selection_number exists, pick out some items
				if (el_selection_number != null) {
					String sNum = el_selection_number.getText();
					int num = new Integer(sNum).intValue();
					// now choose some x out of the items if selection_number exists
					List<Element> newList = new ArrayList<>();
					Random r = new Random();
					int size = el_sections.size();
					// if num > size ??e.g. 5 elements should be picked, but there are
					// only four
					if (num > size) num = size;
					for (int i = 0; i < num; i++) {
						int n = r.nextInt(size--);
						Element o = el_sections.remove(n);
						newList.add(o);
					}
					el_sections = newList;
					/*
					 * pick out items -> remove unused items from section
					 */
					sections.removeAll(el_sections);
					for (Iterator iter = sections.iterator(); iter.hasNext();) {
						el_sections.remove(iter.next());						
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
					Collections.shuffle(el_sections);
				}
			}
		}

		for (Iterator<Element> iter = el_sections.iterator(); iter.hasNext();) {
			Element section = iter.next();
			SectionContext sc = new SectionContext();
			sc.setUp(assessInstance, section, sw);
			sectionContexts.add(sc);
		}
	}

	/**
	 * start assessment
	 */
	public void start() {
		// if not started yet, start
		if (timeOfStart == -1) {
			timeOfStart = System.currentTimeMillis();
		}
	}

	/**
	 * stop assessment
	 */
	public void stop() {
		if (timeOfStart != -1 && timeOfStop == -1) {
			timeOfStop = System.currentTimeMillis();
		}
		if (getCurrentSectionContext() != null) getCurrentSectionContext().sectionWasSubmitted();
	}

	/**
	 * 
	 */
	public void eval() {
		if (assessInstance.isSurvey()) return;
		int sccnt = getSectionContextCount();
		for (int i = 0; i < sccnt; i++) {
			SectionContext sc = getSectionContext(i);
			sc.eval();
		}
		if (feedbacktesting) calcFeedBack();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "<br /><br />assessment:" + sectionContexts.toString() + "=" + super.toString();
	}

	/**
	 * Method getIdent.
	 * 
	 * @return String
	 */
	public String getIdent() {
		return ident;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return
	 */
	public SectionContext getCurrentSectionContext() {
		if (currentSectionContextPos == -1) return null;
		SectionContext sc = sectionContexts.get(currentSectionContextPos);
		return sc;
	}

	/**
	 * Sets the currentSectionPos.
	 * 
	 * @param currentSectionPos The currentSectionPos to set
	 */
	public void setCurrentSectionPos(int currentSectionPos) {
		if (currentSectionPos >= sectionContexts.size()) { throw new RuntimeException("error"); }
		this.currentSectionContextPos = currentSectionPos;
	}

	/**
	 * Method getSectionContextCount.
	 * 
	 * @return int
	 */
	public int getSectionContextCount() {
		return sectionContexts.size();
	}

	/**
	 * Return the total items in all sections of the assessment.
	 * @return Total number of items
	 */
	public int getItemContextCount() {
		int count = 0;
		int sccnt = getSectionContextCount();

		for (int i = 0; i < sccnt; i++) {
			SectionContext sc = getSectionContext(i);
			count += sc.getItemContextCount();
		}
		return count;
	}

	/**
	 * Get the position of the current item within the assessment.
	 * @return position of the current item within the assessment.
	 */
	public int getItemPosWithinAssessment() {
		if (currentSectionContextPos == -1) return 1; // first question
		int currentPos = 1;
		for (int i=0; i < getCurrentSectionContextPos(); i++) {
			// count all items in previous section
			currentPos += getSectionContext(i).getItemContextCount();
		}
		SectionContext curSectionContext = getCurrentSectionContext();
		if (curSectionContext.getCurrentItemContextPos() != -1)
			// this is a section page, just add 1 item to the current pos
			currentPos += curSectionContext.getCurrentItemContextPos();
		return currentPos;
	}
	
	/**
	 * Get the position of the first item within the assessment.
	 * @return position of the first item within the assessment.
	 */
	public int getFirstItemPosWithinSection() {
		if (currentSectionContextPos == -1) return 1; // first question
		int currentPos = 1;
		for (int i=0; i < getCurrentSectionContextPos(); i++) {
			// count all items in previous section
			currentPos += getSectionContext(i).getItemContextCount();
		}
		return currentPos;
	}
	
	/**
	 * Get the position of the last item of the current section within the assessment.
	 * @return position of the last item of the current section within the assessment.
	 */
	public int getLastItemPosWithinSection() {
		int currentPos = 0;
		for (int i=0;getCurrentSectionContextPos() > -1 &&  i < getCurrentSectionContextPos(); i++) {
			// count all items in previous section
			currentPos += getSectionContext(i).getItemContextCount();
		}
		getCurrentSectionContext();
		if (getCurrentSectionContextPos()>-1) {
			currentPos += getSectionContext(getCurrentSectionContextPos()).getItemContextCount();
		}
		
		return currentPos;
	}
	/**
	 * Method setCurrentSectionContextPos.
	 * 
	 * @param i
	 */
	public void setCurrentSectionContextPos(int i) {
		currentSectionContextPos = i;
	}

	/**
	 * Returns the currentSectionContextPos.
	 * 
	 * @return int
	 */
	public int getCurrentSectionContextPos() {
		return currentSectionContextPos;
	}

	/**
	 * checks whether the user may still submit answers
	 * 
	 * @return
	 */
	public boolean isOpen() {
		// not started yet or no timelimit or within timelimit
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
	public SectionContext getSectionContext(int pos) {
		return sectionContexts.get(pos);
	}

	/**
	 * @return long
	 */
	public long getDurationLimit() {
		return durationLimit;
	}

	/**
	 * Return the time to completion for this assessment
	 * 
	 * @return long Millis to completion
	 */
	public long getDuration() {
		if (timeOfStart == -1 | timeOfStop == -1) return 0;
		return timeOfStop - timeOfStart;
	}

	/**
	 * Get the maximum score for this assessment. (Sum of maxscore of all items)
	 * 
	 * @return
	 */
	public float getMaxScore() {
		float count = 0.0f;
		for (Iterator<SectionContext> iter = sectionContexts.iterator(); iter.hasNext();) {
			SectionContext sc = iter.next();
			float maxScore = sc.getMaxScore();
			if (maxScore == -1) return -1;
			else count += maxScore;
		}
		return count;
	}

	/**
	 * @return
	 */
	public float getScore() {
		if (scoremodel == null || scoremodel.equalsIgnoreCase("SumOfScores")) { // sumofScores

			float count = 0;
			for (Iterator<SectionContext> iter = sectionContexts.iterator(); iter.hasNext();) {
				SectionContext sc = iter.next();
				count += sc.getScore();
			}
			return count;
		} else if (scoremodel.equalsIgnoreCase("NumberCorrect")) {
			float tmpscore = 0.0f;
			// calculate correct number of sections: an section is correct if its
			// correct items reach the section's cutvalue
			for (Iterator<SectionContext> iter = sectionContexts.iterator(); iter.hasNext();) {
				SectionContext sc = iter.next();
				float sscore = sc.getScore();
				if (sscore >= cutvalue) tmpscore++; // count items correct
			}
			return tmpscore;
		} else {
			throw new RuntimeException("scoring algorithm " + scoremodel + " not supported");
		}

	}

	/**
	 * @return
	 */
	public boolean isPassed() {
		float score = getScore();
		return (score >= cutvalue);
	}

	/**
	 * @return
	 */
	public int getItemsPresentedCount() {
		int count = 0;
		for (Iterator<SectionContext> iter = sectionContexts.iterator(); iter.hasNext();) {
			SectionContext sc = iter.next();
			count += sc.getItemsPresentedCount();
		}
		return count;
	}
	
	/**
	 * @return
	 */
	public int getItemsAnsweredCount() {
		int count = 0;
		for (Iterator<SectionContext> iter = sectionContexts.iterator(); iter.hasNext();) {
			SectionContext sc = iter.next();
			count += sc.getItemsAnsweredCount();
		}
		return count;
	}
	
	/**
	 * @return
	 */
	public int getItemsAttemptedCount() {
		int count = 0;
		for (Iterator<SectionContext> iter = sectionContexts.iterator(); iter.hasNext();) {
			SectionContext sc = iter.next();
			count += sc.getItemsAttemptedCount();
		}
		return count;
	}

	/**
	 * Method calcFeedBack.
	 */
	private void calcFeedBack() {
		if (feedbacktesting) {
			List el_ofts = el_assessment.selectNodes("outcomes_processing/outcomes_feedback_test");
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
					// ignore feedbacktype, since we section or assess feedback only
					// accepts material, no hints or solutions
					Element el_resolved = (Element) el_assessment.selectSingleNode(".//assessfeedback[@ident='" + linkRefId + "']");
					getOutput().setEl_response(new AssessFeedback(el_resolved));
					// give the whole assessmentfeedback to render
					feedbackavailable = true;
				}
			}
		}
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
	 * @return
	 */
	public Switches getSwitches() {
		return switches;
	}

	/**
	 * @param switches
	 */
	public void setSwitches(Switches switches) {
		this.switches = switches;
	}

	/**
	 * @return
	 */
	public boolean isFeedbackavailable() {
		return feedbackavailable;
	}

	/**
	 * @param b
	 */
	public void setFeedbackavailable(boolean b) {
		feedbackavailable = b;
	}

	/**
	 * @return float
	 */
	public float getCutvalue() {
		return cutvalue;
	}

	/**
	 * @return
	 */
	public long getTimeOfStart() {
		return timeOfStart;
	}

	/**
	 * @return
	 */
	public long getTimeOfStop() {
		return timeOfStop;
	}

	public Objectives getObjectives() {
		return objectives;
	}
}