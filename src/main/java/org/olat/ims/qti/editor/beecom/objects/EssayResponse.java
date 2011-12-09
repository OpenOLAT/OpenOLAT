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

import org.dom4j.Attribute;


/**
 * Initial Date:  Oct 10, 2003
 *
 * @author gnaegi<br>
 * 
 * Comment:  
 * Response object for essay question type. Has rows and column information
 * that is used to render the size of the essay input field.
 * </pre>
 */
public class EssayResponse extends Response {
	
	public static int 	 ROWS_DEFAULT = 5;
	public static int 	 COLUMNS_DEFAULT = 50;
	
	private int columns;
	private int rows;
	
	public EssayResponse() {
		super();
	}


	/**
	 * Returns the row size for this fib blank. If size is set to 0, the default 
	 * row size is returned
	 * @return the current row size
	 */
	public int getRows() {
		if (rows == 0) return ROWS_DEFAULT;
		else return rows;
	}

	/**
	 * Sets the row size. If the given int is 0 the default row size is used
	 * instead
	 * @param i
	 */
	public void setRows(int i) {
		if (i == 0) rows = ROWS_DEFAULT;
		else rows = i;
	}

	/**
	 * Sets the row size to the value stored in this column attribute. 
	 * It the attribute is null the row size is set to the default 
	 * value
	 * @param i
	 */
	public void setRows(Attribute i) {
		if (i == null) {
			rows = ROWS_DEFAULT;
		} else {
			String value = i.getStringValue();
			setRowsFromString(value);
		}
	}

	/**
	 * Sets the column size to the given string value. If string is null or a not  
	 * stored in this column attribute.It the attribute is null the column size 
	 * is set to the default size 
	 * value
	 * @param i
	 */
	public void setRowsFromString(String value) {
		if (value == null){
			rows = ROWS_DEFAULT;
			return;
		}
		try {
			setRows(Integer.parseInt(value));
		}
		catch (NumberFormatException e) {
			rows = ROWS_DEFAULT;
		}
	}

	/**
	 * Returns the column size for this fib blank. If size is set to 0, the 
	 * default column size is returned
	 * @return the current size
	 */
	public int getColumns() {
		if (columns == 0) return COLUMNS_DEFAULT;
		else return columns;
	}

	/**
	 * Sets the column size. If the given int is 0 the default column size is used
	 * instead
	 * @param i
	 */
	public void setColumns(int i) {
		if (i == 0) columns = COLUMNS_DEFAULT;
		else columns = i;
	}

	/**
	 * Sets the column size to the value stored in this column attribute. 
	 * It the attribute is null the size is set to the default 
	 * value
	 * @param i
	 */
	public void setColumns(Attribute i) {
		if (i == null) {
			columns = COLUMNS_DEFAULT;
		} else {
			String value = i.getStringValue();
			setColumnsFromString(value);
		}
	}

	/**
	 * Sets the column size to the given string value. If string is null or a not  
	 * stored in this column attribute.It the attribute is null the column size 
	 * is set to the default column size 
	 * value
	 * @param i
	 */
	public void setColumnsFromString(String value) {
		if (value == null){
			columns = COLUMNS_DEFAULT;
			return;
		}
		try {
			setColumns(Integer.parseInt(value));
		}
		catch (NumberFormatException e) {
			columns = COLUMNS_DEFAULT;
		}
	}

}
