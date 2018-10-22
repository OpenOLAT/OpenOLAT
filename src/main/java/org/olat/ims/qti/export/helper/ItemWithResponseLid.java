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

import org.dom4j.Element;
import org.dom4j.Node;
import org.olat.ims.qti.QTIResult;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;


/**
 * @author 
 */

public class ItemWithResponseLid implements QTIItemObject {
	private boolean	isSingle;
	private String	itemIdent	= null;
	private String	itemTitle	= null;
	private String	itemMinValue	= null;
	private String	itemMaxValue	= null;
	private String	itemCutValue	= null;

	// CELFI#107
	private String	questionText	= "";
	// CELFI#107 END

	private String positionsOfResponses	= null;
	private List<String> responseColumnHeaders = new ArrayList<String>(5);
	private List<String> responseLabelIdents = new ArrayList<String>(5);
	private List<String> responseLabelMaterials = new ArrayList<String>(5);

	/**
	 * Constructor for ItemWithResponseLid.
	 * 
	 * @param el_item
	 */
	public ItemWithResponseLid(Element el_item) {
		this.itemTitle = el_item.attributeValue("title");
		this.itemIdent = el_item.attributeValue("ident");

		List<Node> responseLids = el_item.selectNodes(".//response_lid");

		// Question text
		// CELFI#107		
		Node temp = el_item.selectSingleNode(".//presentation/material/mattext");
		if (temp != null) this.questionText = ((Element) temp).getTextTrim();
		
		int i = 1;
		for (Iterator<Node> itresponseLid = responseLids.iterator(); itresponseLid.hasNext();) {	
			Element el_responseLid = (Element)itresponseLid.next();
			isSingle = el_responseLid.attributeValue("rcardinality").equals("Single");
			
			List<Node> labels = el_responseLid.selectNodes(".//response_label");
			Element decvar = (Element) el_item.selectSingleNode(".//outcomes/decvar");
			if (decvar != null) {
				this.itemMinValue = decvar.attributeValue("minvalue");
				this.itemMaxValue = decvar.attributeValue("maxvalue");
				this.itemCutValue = decvar.attributeValue("cutvalue");
			}
			
			for (Iterator<Node> itlabel = labels.iterator(); itlabel.hasNext();) {
				Element el_label = (Element)itlabel.next();
				String sIdent = el_label.attributeValue("ident");
				responseLabelIdents.add(sIdent);
				
				List<Node> materials = el_label.selectNodes(".//mattext");
				StringBuilder mat = new StringBuilder();
				for (Iterator<Node> itmaterial = materials.iterator(); itmaterial.hasNext();) {
					Element el_material = (Element)itmaterial.next();
					mat.append(el_material.getText());
				}
				responseLabelMaterials.add(mat.length() == 0 ? "IDENT: " + sIdent : mat.toString());				
				responseColumnHeaders.add((isSingle ? "R" : "C") + i); // R -> Radio button, C -> Check box
				i++;
			}
			
		}
		
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

	public String getItemTitle() {
		return itemTitle;
	}

	// CELFI#107
	public String getQuestionText() {
		return this.questionText;
	}

	public List<String> getResponseColumnHeaders() {
		return responseColumnHeaders;
	}

	/**
	 * @see org.olat.ims.qti.export.helper.QTIItemObject#getResponseColumns(org.olat.ims.qti.QTIResult)
	 */
	public List<String> getResponseColumns(QTIResult qtiresult) {
		List<String> responseColumns = new ArrayList<String>();
		positionsOfResponses = null;
		if (qtiresult == null) {
			// item has not been choosen
			addTextAndTabs(responseColumns, "", getNumColumnHeaders());
		} else {
			String answer = qtiresult.getAnswer();
			// item submitted without choosing any checkboxes at all
			boolean submittedWithoutChoosing = answer.equals("[]");
			// test started and finished without submitting item
			boolean finishedWithoutSubmitting = answer.equals("");

			if (finishedWithoutSubmitting) {
				addTextAndTabs(responseColumns, "", getNumColumnHeaders());
				return responseColumns;
			}
			String itemIdentifier = qtiresult.getItemIdent();
			
			// special case KPRIM
			if (itemIdentifier.startsWith(ItemParser.ITEM_PREFIX_KPRIM)) {
				List<String> answerList = QTIResultManager.parseResponseLidAnswers(answer);
				StringBuilder sb = new StringBuilder();

				int pos = 0;
				boolean firstAppendDone = false;
				for (Iterator<String> iter = responseLabelIdents.iterator(); iter.hasNext();) {
					String labelid = iter.next();
					boolean foundLabelId = false;
					for (Iterator<String> iterator = answerList.iterator(); iterator.hasNext();) {
						String answerid = iterator.next();
						if (answerid.startsWith(labelid)) {
							pos++;
							if (answerid.endsWith("correct")) {
								responseColumns.add("+");
								if (firstAppendDone) {
									sb.append(" ");
								}
								sb.append(String.valueOf(pos));
								pos++;
							} else {
								responseColumns.add("-");
								if (firstAppendDone) {
									sb.append(" ");
								}
								pos++;
								sb.append(String.valueOf(pos));
							}
							firstAppendDone = true;
							foundLabelId = true;
						}
					}
					if (!foundLabelId) {
						responseColumns.add(".");
						if (firstAppendDone) sb.append(" ");
						sb.append("0");
						firstAppendDone = true;
						pos = pos + 2;
					}
				}
				positionsOfResponses = sb.toString();
			} else if (submittedWithoutChoosing) {
				addTextAndTabs(responseColumns, ".", getNumColumnHeaders());
				positionsOfResponses = null;
			} else if (finishedWithoutSubmitting) {
				addTextAndTabs(responseColumns, "", getNumColumnHeaders());
			} else {
				List<String> answerList = QTIResultManager.parseResponseLidAnswers(answer);
				StringBuilder sb = new StringBuilder();
				int pos = 1;
				boolean firstLoopDone = false;
				for (Iterator<String> iter = responseLabelIdents.iterator(); iter.hasNext();) {
					String element = iter.next();
					if (answerList.contains(element)) {
						responseColumns.add("1");
						if (firstLoopDone) {
							sb.append(" ");
						}
						sb.append(String.valueOf(pos));
						firstLoopDone = true;
					} else {
						responseColumns.add("0");
					}
					pos++;
				}
				positionsOfResponses = sb.toString();
			}		
		}
		
		return responseColumns;
	}

	public TYPE getItemType() {
		return isSingle ? TYPE.R : TYPE.C;
	}

	public List<String> getResponseIdentifier() {
		return responseLabelIdents;
	}

	public List<String> getResponseLabelMaterials() {
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
		return true;
	}

	public String getPositionsOfResponses() {
		return positionsOfResponses;
	}

}
