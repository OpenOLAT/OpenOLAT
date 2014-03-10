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

package org.olat.ims.qti.export.helper;

import java.util.List;

import org.olat.ims.qti.QTIResult;

/**
 * @author schneider
 */
public interface QTIItemObject {
	
	/**
	 * R - stands for Radio Button (SCQ) 
	 * C - stands for Check box (MCQ odr K-Prim)
	 * B - stands for Blank (FIB)
	 * A - stands for Area (ESS)
	 */
	public static enum TYPE {R, C, B, A} //R stands for Radio Button (SCQ)
	
	/**
	 * @param resultSet
	 * @return
	 */
	public QTIResult extractQTIResult(List<QTIResult> resultSet);
	
	/**
	 * @return
	 */
	public int getNumColumnHeaders();
	
	/** 
	 * @return itemIdent
	 */
	public String getItemIdent();
	
	/**
	 * @return itemTitle
	 */
	public String getItemTitle();
	
	//CELFI#107
	public String getQuestionText();
	
	public String getItemMinValue();
	public String getItemMaxValue();
	public String getItemCutValue();
	
	
	/**
	 * 
	 * @return List responseColumnHeaders
	 */ 
	public List<String> getResponseColumnHeaders();
	
	/**
	 * 
	 * @return List responseColumns
	 */
	public List<String> getResponseColumns(QTIResult qtiresult);
	
	/**
	 * @return String
	 * --> "R" Radio Button (SCQ)
	 * --> "C" Check box (MCQ odr K-Prim)
	 * --> "B" Blank (FIB)
	 * --> "A" Area (ESS)
	 * 
	 */
	public TYPE getItemType();
	
	/**
	 * 
	 * @return response_label or response_str ident:
	 *  - in case of ItemWithResponseStr --> response_str ident
	 *  - in case of ItemWithResponseLid --> response_label ident
	 */
	public List<String> getResponseIdentifier();
	
	
	/**
	 * 
	 * @return Null, if the item has no material, otherwise a list of materials
	 */
	public List<String> getResponseLabelMaterials();

	/**
	 * 
	 * @return
	 */ 
	public boolean hasPositionsOfResponses();

	/**
	 * 
	 * @return String 
	 */
	public String getPositionsOfResponses();

	
	
	
	
}
