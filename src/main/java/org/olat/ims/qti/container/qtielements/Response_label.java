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
import java.util.Map;

import org.dom4j.Element;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.core.util.openxml.OpenXMLDocument;
import org.olat.core.util.openxml.OpenXMLDocument.Style;
import org.olat.core.util.openxml.OpenXMLDocument.Unit;
import org.olat.ims.qti.container.ItemInput;
import org.w3c.dom.Node;

/**
 *    Initial Date:  25.11.2004
 *   
 *    @author Mike Stock
 */
public class Response_label extends GenericQTIElement {

	private static final long serialVersionUID = -4391486220424218044L;
	/**
	 * Comment for <code>xmlClass</code>
	 */
	public static final String xmlClass = "response_label";
	private static String PARA = "§";
	
	/**
	 * @param el_element
	 */
	public Response_label(Element el_element) {
		super(el_element);
	}

	/**
	 * @see org.olat.ims.qti.container.qtielements.QTIElement#render(StringBuilder,
	 *      RenderInstructions)
	 */
	@Override
	public void render(StringBuilder buffer, RenderInstructions ri) {
		ItemInput iinput = (ItemInput) ri.get(RenderInstructions.KEY_ITEM_INPUT);
		String responseIdent = (String) ri.get(RenderInstructions.KEY_RESPONSE_IDENT);
		// find parent render_xxx element
		String renderClass = (String) ri.get(RenderInstructions.KEY_RENDER_CLASS);
		if(renderClass == null) {
			//we don't know what to do
			throw new AssertException("Render class must be set previousely to call respnse_label.render.");
		}

		// OLATNG-208: Back-porting OLAT-6617 (use renderMode variable to generate disabled attribute when needed)
		int renderMode = RenderInstructions.RENDER_MODE_FORM;
		if (ri.containsKey(RenderInstructions.KEY_RENDER_MODE)) {
			renderMode = (Integer) ri.get(RenderInstructions.KEY_RENDER_MODE);
		}

		if (renderClass.equals("choice")) {
			renderChoice(buffer, ri, iinput, renderMode);
		} else if (renderClass.equals("kprim")) {
			buffer.append("<tr><td class='o_qti_item_kprim_input'><input id=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent()).append("\" type=\"radio\" class=\"radio\" name=\"");
			appendParameterIdent(buffer, ri);
			buffer.append("\" value=\"" + getQTIIdent() + ":correct");
			if (iinput != null && !iinput.isEmpty()) {
				List<String> responses = iinput.getAsList(responseIdent);
				if (responses != null && responses.contains(getQTIIdent() + ":correct")) buffer.append("\" checked=\"checked");
			}
			if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
				buffer.append("\" disabled=\"disabled");
			}
			buffer.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\"/>");
			buffer.append("</td><td class='o_qti_item_kprim_input'><input id=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent()).append("\" type=\"radio\" class=\"radio\" name=\"");
			appendParameterIdent(buffer, ri);
			buffer.append("\" value=\"" + getQTIIdent() + ":wrong");
			if (iinput != null && !iinput.isEmpty()) {
				List<String> responses = iinput.getAsList(responseIdent);
				if (responses != null && responses.contains(getQTIIdent() + ":wrong")) buffer.append("\" checked=\"checked");
			}
			if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
				buffer.append("\" disabled=\"disabled");
			}
			buffer.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\"/>");
			buffer.append("</td><td>");
			super.render(buffer, ri);
			buffer.append("</td></tr>");
			ri.put(RenderInstructions.KEY_FLOW_LABEL, new Integer(RenderInstructions.RENDER_FLOW_BLOCK));
			addBr(ri, buffer);

		} else if (renderClass.equals("fib")) {
			Integer rows = (Integer) ri.get(RenderInstructions.KEY_FIB_ROWS);
			Integer columns = (Integer) ri.get(RenderInstructions.KEY_FIB_COLUMNS);
			Integer maxlength = (Integer) ri.get(RenderInstructions.KEY_FIB_MAXLENGTH);
			if (rows == null || columns == null || maxlength == null) throw new AssertException(
					"Rows and/or columns attribute not specified for render_fib.");
			if (rows.intValue() > 1) {
				// render as textarea
				buffer.append("<textarea id=\"QTI_").append(getQTIIdent()).append("\" name=\"");
				appendParameterIdent(buffer, ri);
				if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
					buffer.append("\" readonly=\"readonly");
				}
				buffer.append("\" class='form-control' rows=\"").append(rows).append("\" cols=\"").append(columns)
						.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\">");
				if (iinput != null && !iinput.isEmpty() && iinput.getSingle(responseIdent) != null) {
					buffer.append(iinput.getSingle(getQTIIdent()));
				}
				buffer.append("</textarea>");
				
			} else {
				// render as input string
				buffer.append("<span> </span><input id=\"QTI_").append(getQTIIdent()).append("\" name=\"");
				appendParameterIdent(buffer, ri);
				buffer.append("\" type=\"text\" size=\"").append(columns).append("\" maxlength=\"").append(maxlength);
				if (iinput != null && !iinput.isEmpty() && iinput.getSingle(responseIdent) != null) {
					// OLATNG-199 (back-porting OLAT-6989: escaped fillInText)
					String fillInText = StringHelper.escapeHtml(iinput.getSingle(getQTIIdent()));
					buffer.append("\" value=\"").append(fillInText);
				}
				if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
					buffer.append("\" disabled=\"disabled");
				}
				buffer.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\" /><span> </span>");
			}
			addBr(ri, buffer);
		}
	}
	
	private void renderChoice(StringBuilder buffer, RenderInstructions ri, ItemInput iinput, int renderMode) {
		String responseIdent = (String) ri.get(RenderInstructions.KEY_RESPONSE_IDENT);
		// render multiple/single choice
		buffer.append("<div class='form-group o_qti_item_choice_option");
		if (!wantBr(ri)){
			buffer.append("_flow");
		}
		buffer.append("'>");			
					
		Object o = ri.get(RenderInstructions.KEY_RENDER_AUTOENUM_LIST);
		if (o != null) {
			String[] s = o.toString().split(",");
			o = ri.get(RenderInstructions.KEY_RENDER_AUTOENUM_IDX);
				
			int i = o == null ? 0 : Integer.valueOf(o.toString());
			buffer.append("<div class=\"o_qti_item_choice_option_autoenum\">");
			if (s.length > i) {
				buffer.append("<span>").append(s[i]).append("</span>");
				ri.put(RenderInstructions.KEY_RENDER_AUTOENUM_IDX, Integer.toString(i+1));
			}
			buffer.append("</div>");
		}			
					
		Integer rCardinality = (Integer) ri.get(RenderInstructions.KEY_RESPONSE_RCARDINALITY);
		if (rCardinality == null) {
			throw new AssertException("Cardinality must be set previousely to call respnse_label.render for a render_choice class.");
		}
		
		if (rCardinality.intValue() == Response_lid.RCARDINALITY_SINGLE) {
			// single choice
			
			buffer.append("<div class='radio'>")
			      .append("<label for=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent()).append("\">")
			      .append("<input id=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent()).append("\" type='radio' name=\"")
			      .append("qti").append(PARA).append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(PARA)
			      .append(ri.get(RenderInstructions.KEY_RESPONSE_IDENT)).append(PARA).append("choice")
			      .append("\" value=\"").append(getQTIIdent());
			if (iinput != null && !iinput.isEmpty()) {
				String response = iinput.getSingle(responseIdent);
				if (response.equals(getQTIIdent())) buffer.append("\" checked=\"checked");
			}
			if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
				buffer.append("\" disabled=\"disabled");
			}
			buffer.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\" />");
			super.render(buffer, ri);
			buffer.append("</label></div>");
		} else if (rCardinality.intValue() == Response_lid.RCARDINALITY_MULTIPLE) {
			// multiple choice
			
			buffer.append("<div class='checkbox'>")
			      .append("<label for=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent()).append("\">")
			      .append("<input id=\"QTI_").append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(getQTIIdent()).append("\" type='checkbox' class='o_checkbox' name=\"");
			appendParameterIdent(buffer, ri);
			buffer.append("\" value=\"").append(getQTIIdent());
			if (iinput != null) {
				List<String> responses = iinput.getAsList(responseIdent);
				if (responses != null && responses.contains(getQTIIdent())) buffer.append("\" checked=\"checked");
			}
			if (renderMode == RenderInstructions.RENDER_MODE_STATIC) {
				buffer.append("\" disabled=\"disabled");
			}
			buffer.append("\" onchange=\"return setFormDirty('ofo_iq_item')\" onclick=\"return setFormDirty('ofo_iq_item')\" />");
			super.render(buffer, ri);
			buffer.append("</label></div>");
		}
		buffer.append("</div>");
	}
	
	private void addBr (RenderInstructions ri, StringBuilder buffer) {
			if (wantBr(ri)) buffer.append("<br />");
	}
	
	private boolean wantBr (RenderInstructions ri) {
		Integer flowLabelClass = (Integer) ri.get(RenderInstructions.KEY_FLOW_LABEL);
		if (flowLabelClass != null) {
			return flowLabelClass.intValue() == RenderInstructions.RENDER_FLOW_LIST;
		} else {
			return true;
		}
	}

	private void appendParameterIdent(StringBuilder buffer, RenderInstructions ri) {
		buffer.append("qti").append(PARA).append(ri.get(RenderInstructions.KEY_ITEM_IDENT)).append(PARA).append(
				ri.get(RenderInstructions.KEY_RESPONSE_IDENT)).append(PARA).append(getQTIIdent());
	}

	@Override
	public void renderOpenXML(OpenXMLDocument document, RenderInstructions ri) {
		String renderClass = (String) ri.get(RenderInstructions.KEY_RENDER_CLASS);
		
		
		if(renderClass == null) {
			//we don't know what to do
		} else if(renderClass.equals("choice")) {
			Node row = document.createTableRow();
			//answer
			Node answerCell = row.appendChild(document.createTableCell("E9EAF2", 4560, Unit.pct));
			document.pushCursor(answerCell);
			super.renderOpenXML(document, ri);
			document.popCursor(answerCell);

			//checkbox
			boolean correct = isCorrectMCResponse(ri);
			appendCheckBox(correct, row, document);
			//append row
			document.getCursor().appendChild(row);
		} else if (renderClass.equals("kprim")) {
			Node row = document.createTableRow();
			//answer
			Node answerCell = row.appendChild(document.createTableCell("E9EAF2", 4120, Unit.pct));
			document.pushCursor(answerCell);
			super.renderOpenXML(document, ri);
			document.popCursor(answerCell);
			
			//checkbox
			boolean correct = isCorrectKPrimResponse(ri, "correct");
			appendCheckBox(correct, row, document);
			boolean wrong = isCorrectKPrimResponse(ri, "wrong");
			appendCheckBox(wrong, row, document);
			
			//append row
			document.getCursor().appendChild(row);
		} else if (renderClass.equals("fib")) {
			Boolean render = (Boolean)ri.get(RenderInstructions.KEY_RENDER_CORRECT_RESPONSES);
			@SuppressWarnings("unchecked")
			Map<String,String> iinput = (Map<String,String>)ri.get(RenderInstructions.KEY_CORRECT_RESPONSES_MAP);
			
			if(render != null && render.booleanValue() && iinput != null
					&& StringHelper.containsNonWhitespace(iinput.get(getQTIIdent()))) {
				//show the response
				String response = iinput.get(getQTIIdent());
				response = response.replace(";", ", ");
				document.appendText(response, false, Style.underline);
			} else {
				Integer rows = (Integer)ri.get(RenderInstructions.KEY_FIB_ROWS);
				if (rows != null && rows.intValue() > 1) {
					document.appendFillInBlanckWholeLine(rows.intValue());
				} else {
					Integer maxlength = (Integer)ri.get(RenderInstructions.KEY_FIB_MAXLENGTH);
					int length = (maxlength == null ? 8 : maxlength.intValue()) / 2;
					document.appendFillInBlanck(length, false);
				}
			}
		}
	}
	
	private void appendCheckBox(boolean checked, Node row, OpenXMLDocument document) {
		Node checkboxCell = row.appendChild(document.createTableCell(null, 369, Unit.pct));
		Node responseEl = document.createCheckbox(checked);
		Node wrapEl = document.wrapInParagraph(responseEl);
		//Node responseEl = document.createParagraphEl("OK");
		checkboxCell.appendChild(wrapEl);
	}
	
	private boolean isCorrectMCResponse(RenderInstructions ri) {
		Boolean render = (Boolean)ri.get(RenderInstructions.KEY_RENDER_CORRECT_RESPONSES);
		if(render == null || !render.booleanValue()) return false;
		@SuppressWarnings("unchecked")
		Map<String,String> iinput = (Map<String,String>)ri.get(RenderInstructions.KEY_CORRECT_RESPONSES_MAP);
		if(iinput != null && iinput.containsKey(getQTIIdent())) {
			return true;
		}
		return false;	
	}
	
	private boolean isCorrectKPrimResponse(RenderInstructions ri, String add) {
		Boolean render = (Boolean)ri.get(RenderInstructions.KEY_RENDER_CORRECT_RESPONSES);
		if(render == null || !render.booleanValue()) return false;
		@SuppressWarnings("unchecked")
		Map<String,String> iinput = (Map<String,String>)ri.get(RenderInstructions.KEY_CORRECT_RESPONSES_MAP);
		if(iinput != null && iinput.containsKey(getQTIIdent() + ":" + add)) {
			return true;
		}
		return false;	
	}
}
