/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui;

/**
 * 
 * Initial date: 9 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum AcademicalDateFormat {
	
	monthYear("mm.yyyy"),
	year("yyyy");
	
	private final String format;

	private AcademicalDateFormat(String format) {
		this.format = format;
	}
	
	public static AcademicalDateFormat[] yearsOnly() {
		return new AcademicalDateFormat[] { AcademicalDateFormat.year };
	}
	
	public String format() {
		return format;
	}
	
	public static AcademicalDateFormat format(String text) {
		for(AcademicalDateFormat value:values()) {
			if(value.format.equals(text)) {
				return value;
			}
		}
		return year;
	}
	
	public static boolean hasFormat(AcademicalDateFormat format, AcademicalDateFormat[] formatArray) {
		boolean found = false;
		if(formatArray == null) {
			found = false;
		} else if(formatArray.length == 1) {
			found = formatArray[0] == format;
		} else {
			AcademicalDateFormat[] allFormats = values();
			for(int i=allFormats.length; i-->0; ) {
				if(allFormats[i] == format) {
					found = true;
				}
			}
		}
		return found;
	}
}
