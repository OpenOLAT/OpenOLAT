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

import org.dom4j.Element;


/**
 * Initial Date:  Sep 23, 2003
 *
 * @author gnaegi<br>
 * 
 * Comment: selection_ordering object (at level section). 
 * Values that can be changed: order_type and selection_number. A selection number
 * of 0 stands for 'all items selected'.
 * </pre>
 */
public class SelectionOrdering implements QTIObject {

	private int selectionNumber = 0;		// default value: '0' means 'all'
	private String orderType = SEQUENTIAL;	// default value
	
	// Strings used in outcomes processing
	public static final String ORDER_TYPE 	= "order_type";
	public static final String SEQUENTIAL 	= "Sequential";
	public static final String RANDOM 		= "Random";
	
	
	/* (non-Javadoc)
	 * @see org.olat.ims.qti.editor.beecom.objects.QTIObject#addToElement(org.dom4j.Element)
	 */
	public void addToElement(Element root) {
		Element selection_ordering = root.addElement("selection_ordering");

		Element selection = selection_ordering.addElement("selection");
		if (selectionNumber > 0) {
			Element selection_number = selection.addElement("selection_number");
			selection_number.addText(String.valueOf(selectionNumber));
		} 

		Element order = selection_ordering.addElement("order");
		order.addAttribute(ORDER_TYPE, orderType);
	}


	/**
	 * @return
	 */
	public String getOrderType() {
		return orderType;
	}

	/**
	 * @return
	 */
	public int getSelectionNumber() {
		return selectionNumber;
	}

	/**
	 * Set the order type: Sequential (default) or Random
	 * @param string
	 */
	public void setOrderType(String string) {
		orderType = string;
	}

	/**
	 * Set the number of items to be selected.
	 * 0 means to select all items, any other number bigger than 0 means the exact number
	 * of items to be choosen.
	 * @param i
	 */
	public void setSelectionNumber(int i) {
		selectionNumber = i;
	}

	/**
	 * Set the number of items to be selected.
	 * 0 means to select all items, any other number bigger than 0 means the exact number
	 * of items to be choosen. Throws exception if string is not an integer value
	 * @param i
	 */
	public void setSelectionNumber(String i) {
		selectionNumber = Integer.parseInt(i);
	}


}