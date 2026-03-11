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
package org.olat.modules.curriculum.ui.importwizard;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 11 mars 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record CurriculumImportedValues(String column, List<CurriculumImportedValue> values) {

	public void addError(String placeholder, String message) {
		CurriculumImportedValue val = new CurriculumImportedValue(column);
		val.setError(placeholder, message);
		values.add(val);
	}
	
	public void addWarning(String placeholder, String message) {
		CurriculumImportedValue val = new CurriculumImportedValue(column);
		val.setWarning(placeholder, message);
		values.add(val);
	}
	
	public void addChanged(Object before, Object after) {
		CurriculumImportedValue val = new CurriculumImportedValue(column);
		val.setChanged(before, after);
		values.add(val);
	}
	
	public static final CurriculumImportedValues valueOf(String column) {
		return new CurriculumImportedValues(column, new ArrayList<>(1));
	}
}
