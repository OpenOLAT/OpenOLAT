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

import org.dom4j.Element;
import org.olat.core.logging.AssertException;
import org.olat.core.util.ConsumableBoolean;
import org.olat.core.util.openxml.OpenXMLDocument;

/**
 * Initial Date:  25.11.2004
 *
 * @author Mike Stock
 */
public class Render_fib extends GenericQTIElement {

	private static final long serialVersionUID = -1754178897546204287L;

	/**
	 * Comment for <code>xmlClass</code>
	 */
	public static final String xmlClass = "render_fib";
	
	/**
	 * Comment for <code>FIB_TYPE_STRING</code>
	 */
	public static final int FIB_TYPE_STRING = 0;
	/**
	 * Comment for <code>FIB_TYPE_INTEGER</code>
	 */
	public static final int FIB_TYPE_INTEGER = 0;
	/**
	 * Comment for <code>FIB_TYPE_DECIMAL</code>
	 */
	public static final int FIB_TYPE_DECIMAL = 0;
	/**
	 * Comment for <code>FIB_TYPE_SCIENTIFIC</code>
	 */
	public static final int FIB_TYPE_SCIENTIFIC = 0;
	/**
	 * Comment for <code>FIB_TYPE_BOOLEAN</code>
	 */
	public static final int FIB_TYPE_BOOLEAN = 0;
	
	private int rows = 1;
	private int columns = 20;
	private int maxchars = 50;
	private int fibType = FIB_TYPE_STRING;
	
	/**
	 * @param el_element
	 */
	public Render_fib(Element el_element) {
		super(el_element);
		
		// fetch rows/columns
		String sInteger = el_element.attributeValue("rows");
		if (sInteger != null) {
			try { rows = Integer.parseInt(sInteger); }
			catch (NumberFormatException nfe) {
				throw new AssertException("Invalid value for attribute rows"); 
			}
		}
		sInteger = el_element.attributeValue("columns");
		if (sInteger != null) {
			try { columns = Integer.parseInt(sInteger); }
			catch (NumberFormatException nfe) {
				throw new AssertException("Invalid value for attribute columns"); 
			}
		}
		sInteger = el_element.attributeValue("maxchars");
		if (sInteger != null) {
			try { maxchars = Integer.parseInt(sInteger); }
			catch (NumberFormatException nfe) {
				throw new AssertException("Invalid value for attribute maxchars"); 
			}
		}
		
		// fetch fibtype
		String sFibType = el_element.attributeValue("fibtype");
		if (sFibType != null) {
			if (sFibType.equals("String")) { fibType = FIB_TYPE_STRING; }
			else if (sFibType.equals("Integer")) { fibType = FIB_TYPE_INTEGER; }
			else if (sFibType.equals("Decimal")) { fibType = FIB_TYPE_DECIMAL; }
			else if (sFibType.equals("Scientific")) { fibType = FIB_TYPE_SCIENTIFIC; }
			else if (sFibType.equals("Boolean")) { fibType = FIB_TYPE_BOOLEAN; }
			else throw new AssertException("Invalid value for attribute fibtype.");
		}
	}

	/**
	 * @return columns
	 */
	public int getColumns() {
		return columns;
	}
	/**
	 * @param columns
	 */
	public void setColumns(int columns) {
		this.columns = columns;
	}
	/**
	 * @return fibType
	 */
	public int getFibType() {
		return fibType;
	}
	/**
	 * @param fibType
	 */
	public void setFibType(int fibType) {
		this.fibType = fibType;
	}
	/**
	 * @return maxchars
	 */
	public int getMaxchars() {
		return maxchars;
	}
	/**
	 * @param maxchars
	 */
	public void setMaxchars(int maxchars) {
		this.maxchars = maxchars;
	}
	/**
	 * @return rows
	 */
	public int getRows() {
		return rows;
	}
	/**
	 * @param rows
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * @see org.olat.ims.qti.container.qtielements.QTIElement#render(StringBuilder, RenderInstructions)
	 */
	@Override
	public void render(StringBuilder buffer, RenderInstructions ri) {
		enrichInstructions(ri);
		super.render(buffer, ri);
	}

	@Override
	public void renderOpenXML(OpenXMLDocument document, RenderInstructions ri) {
		enrichInstructions(ri);
		super.renderOpenXML(document, ri);
	}
	
	private final void enrichInstructions(RenderInstructions ri) {
		ri.put(RenderInstructions.KEY_RENDER_CLASS, "fib");
		ri.put(RenderInstructions.KEY_FIB_ROWS, new Integer(rows));
		ri.put(RenderInstructions.KEY_FIB_COLUMNS, new Integer(columns));
		ri.put(RenderInstructions.KEY_FIB_MAXLENGTH, new Integer(maxchars));
		ri.put(RenderInstructions.KEY_FIB_MAXLENGTH, new Integer(maxchars));
		ri.put(RenderInstructions.KEY_BREAK_DELAY, new ConsumableBoolean(true));
	}
}
