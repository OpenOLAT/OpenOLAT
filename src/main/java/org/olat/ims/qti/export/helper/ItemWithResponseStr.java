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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;


/**
 * @author 
 */

public class ItemWithResponseStr implements QTIItemObject {
	private boolean	isEssay					= true;
	private String	itemIdent				= null;
	private String	itemTitle				= null;
	private String	itemMinValue			= null;
	private String	itemMaxValue			= null;
	private String	itemCutValue			= null;

	// CELFI#107
	private String	quetionText				= "";
	// CELFI#107 END

	private List<String> responseColumnHeaders = new ArrayList<>(5);
	private List<String> responseStrIdents = new ArrayList<>(5);

	// CELFI#107
	private List<String> responseLabelMaterials	= new ArrayList<>(5);

	// CELFI#107 END

	/**
	 * Constructor for ItemWithResponseLid.
	 * 
	 * @param el_item
	 */
	public ItemWithResponseStr(Element el_item) {
		// CELFI#107
		this.itemTitle = el_item.attributeValue("title");
		this.itemIdent = el_item.attributeValue("ident");

		Element decvar = (Element) el_item.selectSingleNode(".//outcomes/decvar");

		if (decvar != null) {
			this.itemMinValue = decvar.attributeValue("minvalue");
			this.itemMaxValue = decvar.attributeValue("maxvalue");
			this.itemCutValue = decvar.attributeValue("cutvalue");
		}

		List el_presentationElements = el_item.selectNodes(".//presentation//mattext | .//presentation//response_str");

		int i = 1;
		boolean lastWasMattext = false;
		for (Iterator itPresentations = el_presentationElements.iterator(); itPresentations.hasNext();) {
			Element el_presentation = (Element) itPresentations.next();
			String el_qname = el_presentation.getQualifiedName();
			if (el_qname.equalsIgnoreCase("mattext")) {
				this.quetionText += el_presentation.getTextTrim();
				lastWasMattext = true;
			} else {

				responseStrIdents.add(el_presentation.attributeValue("ident"));
				Element render_fib = el_presentation.element("render_fib");
				if (render_fib != null) {
					isEssay = (render_fib.attributeValue("rows") == null) ? false : true;					
					responseColumnHeaders.add((isEssay ? "A" : "B") + i); // A -> Area, B -> Blank

					Element responseValue = (Element) el_item.selectSingleNode(".//varequal[@respident='" + el_presentation.attributeValue("ident") + "']");
					if (responseValue != null) {
						responseLabelMaterials.add(responseValue.getTextTrim());
						if (lastWasMattext) {
							this.quetionText += " [" + responseValue.getTextTrim() + "] ";
							lastWasMattext = false;
						}
					} else responseLabelMaterials.add("");

				} else {
					responseColumnHeaders.add("unknownType");

					responseLabelMaterials.add("");
				}
				i++;
			}
		}
		// CELFI#107 END
	}

	/**
	 * @see org.olat.ims.qti.export.helper.QTIItemObject#getNumColumnHeaders()
	 */
	public int getNumColumnHeaders() {
		return responseColumnHeaders.size();
	}

	/**
	 * @see org.olat.ims.qti.export.helper.QTIItemObject#extractQTIResult(java.util.List)
	 */
	public QTIResult extractQTIResult(List<QTIResult> resultSet) {
		for (Iterator<QTIResult> iter = resultSet.iterator(); iter.hasNext();) {
			QTIResult element = iter.next();
			if (element.getItemIdent().equals(itemIdent)) {
				resultSet.remove(element);
				return element;
			}
		}
		return null;
	}

	private void addTextAndTabs(List<String> responseColumns, String s, int num) {
		for (int i = 0; i < num; i++) {
			responseColumns.add(s);
		}
	}

	/**
	 * @return itemTitle
	 */
	public String getItemTitle() {
		return itemTitle;
	}

	// CELFI#107
	public String getQuestionText() {
		return this.quetionText;
	}

	public List<String> getResponseColumnHeaders() {
		return responseColumnHeaders;
	}

	/**
	 * @see org.olat.ims.qti.export.helper.QTIItemObject#getResponseColumns(org.olat.ims.qti.QTIResult)
	 */
	public List<String> getResponseColumns(QTIResult qtiresult) {
		List<String> responseColumns = new ArrayList<>();
		if (qtiresult == null) {
			// item has not been choosen
			addTextAndTabs(responseColumns, "", getNumColumnHeaders());
		} else {
			String answer = qtiresult.getAnswer();
			if (answer.length() == 0) addTextAndTabs(responseColumns, ".", getNumColumnHeaders());
			else {
				Map<String,String> answerMap = QTIResultManager.parseResponseStrAnswers(answer);
				
				for (Iterator<String> iter = responseStrIdents.iterator(); iter.hasNext();) {
					String element = iter.next();
					if (answerMap.containsKey(element)) {
						responseColumns.add(answerMap.get(element));	
					} else {
						// should not happen
					}
				}
			}	
		}
		
		return responseColumns;
	}

	public TYPE getItemType() {
		return isEssay ? TYPE.A : TYPE.B; // A -> Area, B -> Blank
	}

	/**
	 * @see org.olat.ims.qti.export.helper.QTIItemObject#getResponseIdentifier()
	 */
	public List<String> getResponseIdentifier() {
		return responseStrIdents;
	}

	public List<String> getResponseLabelMaterials() {
		// CELFI#107
		return responseLabelMaterials;
	}

	/**
	 * @see org.olat.ims.qti.export.helper.QTIItemObject#getItemIdent()
	 */
	public String getItemIdent() {
		return itemIdent;
	}

	public String getItemMinValue() {
		return itemMinValue;
	}

	public String getItemMaxValue() {
		return itemMaxValue;
	}

	public String getItemCutValue() {
		return itemCutValue;
	}

	public boolean hasPositionsOfResponses() {
		return false;
	}

	public String getPositionsOfResponses() {
		return null;
	}

}
