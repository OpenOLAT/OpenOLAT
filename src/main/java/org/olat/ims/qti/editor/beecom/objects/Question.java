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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.CodeHelper;
import org.olat.ims.qti.editor.QTIEditHelper;

/**
 * @author rkulow<br>
 * Handles both single choice and multiple choice.
 */
public class Question {

	public static final int TYPE_SC = 1; // single select
	public static final int TYPE_MC = 2; // multi select
	public static final int TYPE_FIB = 3; // fill-in blank
	public static final int TYPE_ESSAY = 4; // essay
	public static final int TYPE_KPRIM = 5; // kprim

	private int type;
	private String lable = null;
	private Material question = new Material();
	private List<Response> responses = new ArrayList<>(5);
	private String ident = null;
	private float minValue = 0;
	private float maxValue = 0;
	private boolean singleCorrect = true;
	private float singleCorrectScore = 1;

	private boolean shuffle = false;
	private String solutionText = null;
	private String hintText = null;

	public Question() {
		ident = "" + CodeHelper.getRAMUniqueID();
	}

	protected float parseFloat(String sFloat) {
		if (sFloat == null) return 0;
		try {
			return Float.parseFloat(sFloat);
		} catch (Exception e) {
			//  
		}
		return 0;
	}

	/**
	 * ************************ GETTERS and SETTERS
	 * ********************************
	 */

	/**
	 * Returns the lable.
	 * 
	 * @return String
	 */
	public String getLable() {
		return lable;
	}

	/**
	 * Returns the questions.
	 * 
	 * @return String
	 */
	public Material getQuestion() {
		return question;
	}

	/**
	 * Sets the lable.
	 * 
	 * @param lable The lable to set
	 */
	public void setLable(String lable) {
		this.lable = lable;
	}

	/**
	 * Sets the questions.
	 * 
	 * @param questions The questions to set
	 */
	public void setQuestion(Material question) {
		this.question = question;
	}

	/**
	 * Returns the ident.
	 * 
	 * @return String
	 */
	public String getIdent() {
		return ident;
	}

	/**
	 * Sets the ident.
	 * 
	 * @param ident The ident to set
	 */
	public void setIdent(String ident) {
		this.ident = ident;
	}

	/**
	 * @return
	 */
	public boolean isShuffle() {
		return shuffle;
	}

	/**
	 * @param b
	 */
	public void setShuffle(boolean b) {
		shuffle = b;
	}

	/**
	 * @return
	 */
	public float getMaxValue() {
		return maxValue;
	}

	/**
	 * @return
	 */
	public float getMinValue() {
		return minValue;
	}

	/**
	 * @return
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param i
	 */
	public void setMaxValue(float i) {
		maxValue = i;
	}

	/**
	 * Set new max value for this question. If the max value is larger than the
	 * possible value by selecting all responses with positive values the maxValue
	 * is corrected to represent the maximal possible value
	 * 
	 * @param i
	 */
	public void setMaxValue(String i) {
		maxValue = parseFloat(i);

		// don't allow max value that is larger than theoretically possible value
		// smaller value is allowed
		float maxScore = QTIEditHelper.calculateMaxScore(this);
		maxValue = maxScore > maxValue ? maxValue : maxScore;
	}

	/**
	 * @param i
	 */
	public void setMinValue(float i) {
		minValue = i;
	}

	/**
	 * @param i
	 */
	public void setMinValue(String i) {
		minValue = parseFloat(i);
	}

	/**
	 * @param i
	 */
	public void setType(int i) {
		type = i;
	}

	/**
	 * @return
	 */
	public String getHintText() {
		return hintText;
	}

	/**
	 * @return
	 */
	public String getSolutionText() {
		return solutionText;
	}

	/**
	 * @param string
	 */
	public void setHintText(String string) {
		hintText = string;
	}

	/**
	 * @param string
	 */
	public void setSolutionText(String string) {
		solutionText = string;
	}

	/**
	 * @return
	 */
	public boolean isSingleCorrect() {
		return singleCorrect;
	}

	/**
	 * @param b
	 */
	public void setSingleCorrect(boolean b) {
		singleCorrect = b;
	}

	/**
	 * @return
	 */
	public float getSingleCorrectScore() {
		return singleCorrectScore;
	}

	/**
	 * @param i
	 */
	public void setSingleCorrectScore(float i) {
		singleCorrectScore = i;
		if (isSingleCorrect()) maxValue = i;
	}

	/**
	 * @param i
	 */
	public void setSingleCorrectScore(String i) {
		setSingleCorrectScore(parseFloat(i));
	}

	/**
	 * @return list of responses
	 */
	public List<Response> getResponses() {
		return responses;
	}

	/**
	 * @param list
	 */
	public void setResponses(List<Response> list) {
		responses = list;
	}

}