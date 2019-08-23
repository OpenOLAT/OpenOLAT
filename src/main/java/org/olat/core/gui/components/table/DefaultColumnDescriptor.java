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
* <p>
*/

package org.olat.core.gui.components.table;

import java.sql.Timestamp;
import java.text.Collator;
import java.util.Date;
import java.util.Locale;

import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;

import javax.annotation.Nullable;


/**
*  Description:<br>
*
* @author Felix Jost
*/
public class DefaultColumnDescriptor implements ColumnDescriptor {

	public final static String DEFAULT_POPUP_ATTRIBUTES = "height=600, width=600, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no";
	private Formatter formatter;
	private String action;
	private String headerKey;
	private int alignment;
	private int headerAlignment;
	private boolean popUpWindowAction;
	private String popUpWindowAttributes;
	//protected to allow overriding of compare method
	protected Locale locale;
	protected Collator collator; 
	protected Table table; 
	protected int dataColumn;
	private EscapeMode escapeHtml = EscapeMode.html;
	private boolean translateHeaderKey = true; 

	/**
	 * Constructor for table default column descriptor
	 * @param headerKey translation key for column header
	 * @param dataColumn position of column
	 * @param action name of event that should be fired when rows column is clicken. null allowed for no action
	 * @param locale the users locale
	 */
	public DefaultColumnDescriptor(final String headerKey, final int dataColumn, @Nullable final String action, final Locale locale) {
		this(headerKey, dataColumn, action, locale, ColumnDescriptor.ALIGNMENT_LEFT);
	}

	/**
	 * 
	 * @param headerKey
	 * @param dataColumn
	 * @param action
	 * @param locale used ONLY for method getRenderValue in case the Object is of type Date to provide locale-sensitive Date formatting
	 * @param alignment left, middle or right; constants in ColumnDescriptor
	 */
	public DefaultColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale, final int alignment) {
		this(headerKey, dataColumn, action, locale, alignment, ColumnDescriptor.ALIGNMENT_LEFT);
	}
	
	public DefaultColumnDescriptor(final String headerKey, final int dataColumn, final String action, final Locale locale, final int alignment, final int headerAlignment) {
		this.dataColumn = dataColumn;
		this.headerKey = headerKey;
		this.action = action;
		this.locale = locale;
		this.alignment = alignment;
		this.headerAlignment = headerAlignment;
		if (locale != null) {
			formatter = Formatter.getInstance(locale);
			collator = Collator.getInstance(locale);
		}
	}
	
	@Override
	public String getHeaderKey() {
		return headerKey;
	}
	
	@Override
	public boolean translateHeaderKey() {
		return translateHeaderKey;
	}
	
	public void setTranslateHeaderKey(final boolean translateHeaderKey) {
		this.translateHeaderKey = translateHeaderKey;
	}
	
	public void setEscapeHtml(EscapeMode escape) {
		this.escapeHtml = escape;
	}

	/**
	 * 
	 * @param row the row in the table
	 * @return the object to be rendered given the row
	 */
	protected Object getModelData(final int row) {
		return table.getTableDataModel().getValueAt(table.getSortedRow(row),dataColumn);
	}
	
	@Override
	public void renderValue(final StringOutput sb, final int row, final Renderer renderer) {
		Object val = getModelData(row);
		if (val == null) {
			return;
		}
		if (val instanceof Date) {
			String res =  formatter.formatDateAndTime((Date)val);
			sb.append(res);
		} else if(val instanceof String) {
			renderString(sb, (String)val);
		} else {
			renderString(sb, val.toString());
		}
	}
	
	private void renderString(StringOutput sb, String val) {
		switch(escapeHtml) {
			case none:
				sb.append(val);
				break;
			case html:
				StringHelper.escapeHtml(sb, val);
				break;
			case antisamy:
				sb.append(new OWASPAntiSamyXSSFilter().filter(val));
				break;
			default : StringHelper.escapeHtml(sb, val);
		}
	}

	@Override
	public int getAlignment() {
		return alignment;
	}

	@Override
	public int getHeaderAlignment() {
		return headerAlignment;
	}

	/**
	 * is called repeatedly caused by Collections.sort(...);
	 * @see org.olat.core.gui.components.table.ColumnDescriptor#compareTo(int, int)
	 */
	@Override
	public int compareTo(final int rowa, final int rowb) {
		Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
		Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
		// depending on the class of the Objects, we compare
		if (a == null || b == null) {
			return compareNullObjects(a, b);
		}
		if (a instanceof String && b instanceof String) {
			return collator.compare(a, b);
		}
		if(a instanceof Date && b instanceof Date) {
			return compareDateAndTimestamps((Date)a, (Date)b);
		}
		if (a instanceof Boolean && b instanceof Boolean) {
			// faster than string compare, boolean are comparable
			return compareBooleans((Boolean)a, (Boolean)b);
		}
		if (a instanceof Comparable && a.getClass().equals(b.getClass())) {
			//compare the same things
			return ((Comparable)a).compareTo(b);
		}
		return a.toString().compareTo(b.toString());
	}
	
	protected int compareString(final String a, final String b) {
		if (a == null || b == null) {
			return compareNullObjects(a, b);
		}
		return collator == null ? a.compareTo(b) : collator.compare(a, b);
	}

	protected int compareBooleans(final Boolean a, final Boolean b) {
		if (a == null || b == null) {
			return compareNullObjects(a, b);
		}
		
		boolean ba = a.booleanValue();
		boolean bb = b.booleanValue();
		return ba? (bb? 0: -1):(bb? 1: 0);
	}
	
	protected int compareDateAndTimestamps(Date a, Date b) {
		if (a == null || b == null) {
			return compareNullObjects(a, b);
		}
		
		if (a instanceof Timestamp) { // a timestamp (a) cannot compare a date (b), but vice versa is ok.
			if(b instanceof Timestamp) {
				return ((Timestamp)a).compareTo((Timestamp)b);
			} else {
				Timestamp ta = (Timestamp)a;
				Date aAsDate = new Date(ta.getTime());
				return aAsDate.compareTo(b);
			}
		} else if (b instanceof Timestamp) {
			Timestamp tb = (Timestamp)b;
			Date bAsDate = new Date(tb.getTime());
			return a.compareTo(bAsDate);
		}
		return a.compareTo(b);
	}

	protected int compareNullObjects(final Object a, final Object b) {
		boolean ba = (a == null);
		boolean bb = (b == null);
		return ba? (bb? 0: -1):(bb? 1: 0);
	}

	@Override
	public void setTable(final Table table) {
		this.table = table;
	}

	@Override
	public String getAction(final int row) {
		return action;
	}
	
	public void setAlignment(final int alignment) {
		this.alignment= alignment;
	}
	
	public void setHeaderAlignment(final int headerAlignment) {
		this.headerAlignment= headerAlignment;
	}

	protected Table getTable() {
		return table;
	}

	@Override
	public void modelChanged() {
	    // empty
	}

	@Override
	public void sortingAboutToStart() {
		// empty
	}

	@Override
	public int getDataColumn() {
		return dataColumn;
	}

	@Override
	public void otherColumnDescriptorSorted() {
		// empty
	}

	@Override
	public boolean isSortingAllowed() {
		return true;
	}

	public Locale getLocale() {
		return locale;
	}

	@Override
	public boolean isPopUpWindowAction() {
		return popUpWindowAction;
	}

	@Override
	public String getPopUpWindowAttributes() {
		return popUpWindowAttributes;
	}

	/**
	 * TODO:fj:b replace with PopupObject which is easily configurable
	 * 
	 * Optional action link configuration
	 * @param popUpWindowAction true: action link will open in new window, false: action opens in current window
	 * @param popUpWindowAttributes javascript window.open attributes or null if default values are used
	 * e.g. something like this: "height=600, width=600, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no"
	 */
	public void setIsPopUpWindowAction(final boolean popUpWindowAction, final String popUpWindowAttributes) {
		this.popUpWindowAction = popUpWindowAction;
		this.popUpWindowAttributes = popUpWindowAttributes;
	}
}
