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

package org.olat.ims.qti.container.qtielements;

import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.AbstractAttribute;
import org.olat.core.logging.AssertException;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.core.util.openxml.OpenXMLDocument.Columns;
import org.olat.core.util.openxml.OpenXMLDocument.Unit;
import org.olat.ims.qti.editor.beecom.parser.ItemParser;
import org.w3c.dom.Node;

/**
 * Initial Date: 25.11.2004
 * 
 * @author Mike Stock
 */
public class Render_choice extends GenericQTIElement {

	private static final long serialVersionUID = 4578254743045719445L;

	/**
	 * Comment for <code>xmlClass</code>
	 */
	public static final String xmlClass = "render_choice";

	private boolean shuffle = false;
	private boolean kprim = false;
	private int minnumber = -1;
	private int maxnumber = -1;

	/**
	 * @param el_element
	 */
	public Render_choice(Element el_element) {
		super(el_element);

		// fetch shuffle
		String sShuffle = el_element.attributeValue("shuffle");
		if (sShuffle != null) {
			if (sShuffle.equals("Yes")) shuffle = true;
			else if (sShuffle.equals("No")) shuffle = false;
			else throw new AssertException("Attribute shuffle has invalid value.");
		}

		// get min/max number
		String sInteger = el_element.attributeValue("minnumber");
		if (sInteger != null) {
			try {
				minnumber = Integer.parseInt(sInteger);
			} catch (NumberFormatException nfe) {
				throw new AssertException("Attribute minnumber has invalid value.");
			}
		}
		sInteger = el_element.attributeValue("maxnumber");
		if (sInteger != null) {
			try {
				maxnumber = Integer.parseInt(sInteger);
			} catch (NumberFormatException nfe) {
				throw new AssertException("Attribute maxnumber has invalid value.");
			}
		}
		// find Kprim questions
		List<?> ident = el_element.selectNodes("../../../@ident");
		if (ident != null && ident.size() == 1) {
			try {
				if (((AbstractAttribute) ident.get(0)).getValue().startsWith(ItemParser.ITEM_PREFIX_KPRIM)) kprim = true;
			} catch (Throwable t) {
				// not a kprim
			}
		}
	}

	/**
	 * @return maxnumber
	 */
	public int getMaxnumber() {
		return maxnumber;
	}

	/**
	 * @return minnumber
	 */
	public int getMinnumber() {
		return minnumber;
	}

	/**
	 * @return shuffle
	 */
	public boolean isShuffle() {
		return shuffle;
	}

	/**
	 * @see org.olat.ims.qti.container.qtielements.QTIElement#render(StringBuilder,
	 *      RenderInstructions)
	 */
	@Override
	public void render(StringBuilder buffer, RenderInstructions ri) {
		if (kprim) {
			ri.put(RenderInstructions.KEY_RENDER_CLASS, "kprim");
			buffer.append("<table class='table o_qti_item_kprim'>")
			      .append("<thead><tr>")
			      .append("<th class='o_qti_item_kprim_input'>+</th><th class='o_qti_item_kprim_input'>-</th><th></th>")
			      .append("</tr></thead><tbody>");
		} else {
			if (ri.containsKey(RenderInstructions.KEY_RENDER_AUTOENUM_LIST)) {
				ri.put(RenderInstructions.KEY_RENDER_AUTOENUM_IDX, "0");
			} 
			ri.put(RenderInstructions.KEY_RENDER_CLASS, "choice");
			buffer.append("<div class=\"o_qti_item_choice\">");
		}

		super.render(buffer, ri);
		if (kprim) {
			buffer.append("</tbody></table>");
		} else {
			buffer.append("</div>");
		}
	}

	@Override
	public void renderOpenXML(OpenXMLDocument document, RenderInstructions ri) {
		Node table = null;
		document.appendBreak(false);
		if (kprim) {
			ri.put(RenderInstructions.KEY_RENDER_CLASS, "kprim");
			//open a table with 3 columns
			table = document.appendTable(5000, Columns.valueOf(9062, 1116, 1116));
			//draw header with +/-
			Node row = document.createTableRow();
			Node emptyCell = row.appendChild(document.createTableCell(null, 9062, Unit.dxa));
			emptyCell.appendChild(document.createParagraphEl(""));
			Node plusCell = row.appendChild(document.createTableCell(null, 1116, Unit.dxa));
			plusCell.appendChild(document.createParagraphEl("+"));
			Node minusCell = row.appendChild(document.createTableCell(null, 1116, Unit.dxa));
			minusCell.appendChild(document.createParagraphEl("-"));
			table.appendChild(row);
		} else {
			if (ri.containsKey(RenderInstructions.KEY_RENDER_AUTOENUM_LIST)) {
				ri.put(RenderInstructions.KEY_RENDER_AUTOENUM_IDX, "0");
			} 
			ri.put(RenderInstructions.KEY_RENDER_CLASS, "choice");
			//open a table with 2 columns
			//{10178, 1116}
			table = document.appendTable(5000, Columns.valueOf(8468, 745));
		}
		document.pushCursor(table);
		super.renderOpenXML(document, ri);
		document.resetCursor();
	}
}
