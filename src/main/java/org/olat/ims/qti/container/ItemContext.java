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

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.olat.ims.qti.QTIConstants;
import org.olat.ims.qti.container.qtielements.Item;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.QTIHelper;
import org.olat.ims.qti.process.elements.QTI_item;

/**
 * @author Potable Shop 
 *         restrictions on items so far: - only one resprocessing element is
 *         evaluated. duration: from ims qti 1.2.1 best practice: duration The
 *         duration element is used within the item, section and assessment
 *         elements to define the permitted duration for the enclosed activity.
 *         The duration is defined as the period between the activity's
 *         activation and completion. The ISO 8601 format is used:
 *         PnYnMnDTnHnMnS. in mchc_smimr_104.xml example: 0000:00:00T:01:00:00d
 *         in ...101.xml: pTH02 iso website:
 *         http://www.iso.org/iso/en/prods-services/popstds/datesandtime.html
 *         and http://www.w3.org/TR/NOTE-datetime from some page: specified in
 *         numbers of years [Y], months [M] or weeks [W], days [D], hours [H]
 *         and so on, introduced by a ?P?, for example: P6W2D -> not a date, but
 *         only a duration makes sense: -> I shall take the ISO 8601 Time of the
 *         day hh:mm:ss format for now
 */
public class ItemContext implements Serializable {

	private AssessmentInstance assessInstance;

	// TODO: no, complicated, since in several objectbanks.... readonly ref!: the
	// ref to the el_item; transient since it we don't want to serialize it (too
	// long) and can reattach it later
	private Element el_shuffled_item;

	// Internal representation of an item.
	private Item qtiItem;
	
	// the outcome of a item after it was evaluated.
	// it contains variables with values
	private Variables variables;

	// contains the output after an evaluation like hint, solution, feedback
	private Output output;

	// the last answer of the user concerning this question
	private ItemInput itemInput;

	// number of times this question has been answered
	private int timesAnswered;
	private long timeOfStart;
	// server time (in miliseconds) at the time of the start of the item
	private long latestAnswerTime;
	// server time (in miliseconds) at the time of the latest answering of the
	// item

	// the ident of the item - needed to reattach the correct element of the
	// qti-tree
	// after a deserialize in case of a crash.
	private String ident;

	// -- the following are also in the qti tree, but are here for convenience ---

	// max number of attempts the user may try this question
	private int maxAttempts; // -1 for infinite
	private long durationLimit; // 	
	private boolean evalNeeded;

	private boolean feedback = true; // flags
	private boolean hints = true;
	private boolean solutions = true;

	private int hintLevel;

	// 

	/**
	 * default constructor needed for persistence
	 */
	public ItemContext() {
	//  
	}

	/**
	 * 
	 */
	public void init() {
		ident = el_shuffled_item.attributeValue("ident");
		resetVariables();
		itemInput = null;
		timesAnswered = 0;
		evalNeeded = false;
		output = new Output();
		timeOfStart = -1; // not started yet
		latestAnswerTime = -1; // not yet answered
		hintLevel = 0; // no hint with type "Incremental" has been given so far
		Element dur = (Element) el_shuffled_item.selectSingleNode("duration");
		if (dur == null) {
			durationLimit = -1; // no limit
		} else {
			String sdur = dur.getText();
			durationLimit = QTIHelper.parseISODuration(sdur);
		}
		String strmaxattempts = el_shuffled_item.attributeValue("maxattempts");
		if (strmaxattempts == null) {
			maxAttempts = -1; // no limit
		} else {
			maxAttempts = Integer.parseInt(strmaxattempts);
		}
	}

	/**
	 * Method setUp.
	 * 
	 * @param assessInstance
	 * @param el_orig_item
	 * @param sw
	 */
	public void setUp(AssessmentInstance assessInstance, Element el_orig_item, Switches sw) {
		this.assessInstance = assessInstance;
		this.el_shuffled_item = shuffle(el_orig_item);
		this.qtiItem = new Item(el_shuffled_item);
		
		if (sw == null) { // no section switches dominate, take item switches
			// retrieve item switches
			Element el_control = (Element) el_orig_item.selectSingleNode("itemcontrol");
			if (el_control != null) {
				String feedbackswitch = el_control.attributeValue("feedbackswitch");
				String hintswitch = el_control.attributeValue("hintswitch");
				String solutionswitch = el_control.attributeValue("solutionswitch");
				boolean newFeedback = (feedbackswitch == null) ? true : feedbackswitch.equals("Yes");
				boolean newHints = (hintswitch == null) ? true : hintswitch.equals("Yes");
				boolean newSolutions = (solutionswitch == null) ? true : solutionswitch.equals("Yes");
				sw = new Switches(newFeedback, newHints, newSolutions);
			}
		}

		this.feedback = (sw != null ? sw.isFeedback() : true);
		this.solutions = (sw != null ? sw.isSolutions() : true);
		this.hints = (sw != null ? sw.isHints() : true);
		init();
	}

	/**
	 * Method shuffle. shuffle clones the current item (since the whole qti tree
	 * is readonly) and shuffles it
	 * 
	 * @param item
	 * @return Element
	 */
	private Element shuffle(Element item) {
		// get the render_choice
		XPath choice = DocumentHelper.createXPath(".//render_choice[@shuffle=\"Yes\"]");
		Element tel_rendchoice = (Element) choice.selectSingleNode(item);
		//if shuffle is disable, just return the item
		if (tel_rendchoice == null) return item;
		// else: we have to shuffle
		// assume: all response_label have same parent: either render_choice or a
		// flow_label
		Element shuffleItem = item.createCopy();
		// clone the whole item
		Element el_rendchoice = (Element) choice.selectSingleNode(shuffleItem);
		//	<!ELEMENT render_choice ((material | material_ref | response_label |
		// flow_label)* ,response_na?)>
		// <!ATTLIST response_label rshuffle (Yes | No ) 'Yes' .....
		List<Node> el_labels = el_rendchoice.selectNodes(".//response_label[@rshuffle=\"Yes\"]");
		int shusize = el_labels.size();

		// set up a list of children with their parents and the position of the
		// child (in case several children have the same parent
		List<Element> respList = new ArrayList<>(shusize);
		List<Element> parentList = new ArrayList<>(shusize);
		int[] posList = new int[shusize];
		int j = 0;

		for (Iterator<Node> responses = el_labels.iterator(); responses.hasNext();) {
			Element response = (Element) responses.next();
			Element parent = response.getParent();
			int pos = parent.elements().indexOf(response);
			posList[j++] = pos;
			respList.add((Element)response.clone()); // need to use clones so they are not attached anymore
			parentList.add(parent);
		}
		Collections.shuffle(respList);
		// put the children back to the parents
		for (int i = 0; i < parentList.size(); i++) {
			Element parent = parentList.get(i);
			int pos = posList[i];
			Element child = respList.get(i);
			@SuppressWarnings("unchecked")
			List<Element> elements = parent.elements();
			if(pos < elements.size()) {
				elements.set(pos, child);
			} else {
				elements.add(child);
			}
		}
		return shuffleItem;
	}

	/**
	 * @return Variables
	 */
	public Variables getVariables() {
		return variables;
	}

  
	/**
	 * @return
	 */
	public Output getOutput() {
		return output;
	}

	/**
	 * @return ItemInput
	 */
	public ItemInput getItemInput() {
		return itemInput;
	}

	/**
	 * Sets the itemInput.
	 * @param theItemInput The itemInput to set. max be null for "unanswered"
	 * @return the status of the add operation, e.g. ok
	 */
	public int addItemInput(ItemInput theItemInput) {
		boolean underMax = isUnderMaxAttempts();
		boolean onTime = isOnTime();
		if (underMax && onTime) {
			start();
			timesAnswered++;
			latestAnswerTime = System.currentTimeMillis();
			this.itemInput = theItemInput;
			evalNeeded = true;
			return QTIConstants.ITEM_SUBMITTED;
		} else {
			if (!underMax) return QTIConstants.ERROR_SUBMITTEDITEM_TOOMANYATTEMPTS;
			else return QTIConstants.ERROR_SUBMITTEDITEM_OUTOFTIME;
		}
	}

	/**
	 * @return int
	 */
	public int getTimesAnswered() {
		return timesAnswered;
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
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "item:" + getIdent() + ":inp:" + itemInput + ",vars:" + variables + ",out:" + output + "=" + super.toString();
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
	 * @return
	 */
	public boolean isOpen() {
		// open when in timelimit or not started yet or no timelimit AND maxattempts
		// undef or attempts < maxattempts
		boolean ok = isOnTime() && isUnderMaxAttempts();
		return ok;
	}

	/**
	 * @return
	 */
	public boolean isOnTime() {
		return (timeOfStart == -1) || (durationLimit == -1) || (System.currentTimeMillis() < timeOfStart + durationLimit);
	}

	/**
	 * @return
	 */
	public boolean isUnderMaxAttempts() {
		return (maxAttempts == -1 || timesAnswered < maxAttempts);
	}

	/**
	 * @return Element
	 */
	public Element getEl_item() {
		return el_shuffled_item;
	}

	/**
	 * Method start.
	 */
	public void start() {
		if (timeOfStart == -1) { // if not started already
			timeOfStart = System.currentTimeMillis();
		}
	}

	/**
	 * return duration for answered items
	 * @return
	 */
	public long getTimeSpent() {
		if (timesAnswered == 0) return -1;
		return latestAnswerTime - timeOfStart;
	}

	/**
	 * Returns the duration.
	 * 
	 * @return long
	 */
	public long getDurationLimit() {
		return durationLimit;
	}

	/**
	 * @return
	 */
	public boolean isStarted() {
		return (timeOfStart != -1);
	}

	/**
	 * @return int
	 */
	public int getMaxAttempts() {
		return maxAttempts;
	}

	/**
	 * @return long
	 */
	public long getLatestAnswerTime() {
		return latestAnswerTime;
	}

	/**
	 * 
	 */
	public void eval() {
		if (assessInstance.isSurvey()) return;
		if (evalNeeded) {
			QTI_item qtiItem = QTIHelper.getQtiItem();
			output = new Output(); // clear any previous feedback, hints, solutions
			qtiItem.evalAnswer(this);
			evalNeeded = false;
		}
	}

	/**
	 * @return
	 */
	public float getMaxScore() {
		Variable var = getVariables().getSCOREVariable();
		return var.getMaxValue();
	}

	/**
	 * Returns the value of the SCORE variable
	 * 
	 * @return
	 */
	public float getScore(boolean nanAsZero) {
		Variable var = getVariables().getSCOREVariable();
		if (var == null) {
			if (ident.startsWith("QTIEDIT:ESSAY")) {
				return (nanAsZero ? 0.0f : Float.NaN);
			}
			
			// we demand that a SCORE variable must always exist
			throw new RuntimeException("no SCORE def for " + getIdent());
		} else {
			return var.getTruncatedValue(nanAsZero);
		}
	}

	/**
	 * @return boolean
	 */
	public boolean isFeedback() {
		return feedback;
	}

	/**
	 * @return boolean
	 */
	public boolean isHints() {
		return hints;
	}

	/**
	 * @return boolean
	 */
	public boolean isSolutions() {
		return solutions;
	}

	/**
	 * 
	 */
	public void resetVariables() {
		Element el_outcomes = (Element) getEl_item().selectSingleNode("resprocessing/outcomes");
		variables = QTIHelper.declareVariables(el_outcomes);
	}

	/**
	 * @return int
	 */
	public int getHintLevel() {
		return hintLevel;
	}

	/**
	 * Sets the hintLevel.
	 * 
	 * @param hintLevel The hintLevel to set
	 */
	public void setHintLevel(int hintLevel) {
		this.hintLevel = hintLevel;
	}

	/**
	 * 
	 */
	public void clearDurationLimit() {
		durationLimit = -1;
	}

	/**
	 * @return item
	 */
	public Item getQtiItem() {
		return qtiItem;
	}
}
