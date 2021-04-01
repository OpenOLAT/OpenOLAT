/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.forms.model.xml;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInput extends AbstractElement {

	private static final long serialVersionUID = 2420712254825004290L;
	
	public static final String TYPE = "formtextinput";

	private boolean mandatory;
	private int rows;
	private boolean singleRow;
	private boolean numeric;
	private Double numericMin;
	private Double numericMax;
	private boolean date;

	@Override
	public String getType() {
		return TYPE;
	}
	
	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public boolean isSingleRow() {
		return singleRow;
	}

	public void setSingleRow(boolean singleRow) {
		this.singleRow = singleRow;
	}

	public boolean isNumeric() {
		return numeric;
	}

	public void setNumeric(boolean numeric) {
		this.numeric = numeric;
	}

	public Double getNumericMin() {
		return numericMin;
	}

	public void setNumericMin(Double numericMin) {
		this.numericMin = numericMin;
	}

	public Double getNumericMax() {
		return numericMax;
	}

	public void setNumericMax(Double numericMax) {
		this.numericMax = numericMax;
	}

	public boolean isDate() {
		return date;
	}

	public void setDate(boolean date) {
		this.date = date;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof TextInput) {
			TextInput input = (TextInput)obj;
			return getId() != null && getId().equals(input.getId());
		}
		return super.equals(obj);
	}
}
