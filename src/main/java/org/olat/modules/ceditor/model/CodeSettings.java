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
package org.olat.modules.ceditor.model;

/**
 * Initial date: 2023-12-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CodeSettings {

	private CodeLanguage codeLanguage;
	private boolean lineNumbersEnabled;
	private boolean displayAllLines;
	private int numberOfLinesToDisplay;
	private BlockLayoutSettings layoutSettings;
	private AlertBoxSettings alertBoxSettings;

	public CodeSettings() {
		codeLanguage = CodeLanguage.auto;
		lineNumbersEnabled = false;
		displayAllLines = true;
		numberOfLinesToDisplay = 20;
	}

	public CodeLanguage getCodeLanguage() {
		return codeLanguage;
	}

	public void setCodeLanguage(CodeLanguage codeLanguage) {
		this.codeLanguage = codeLanguage;
	}

	public boolean isLineNumbersEnabled() {
		return lineNumbersEnabled;
	}

	public void setLineNumbersEnabled(boolean lineNumbersEnabled) {
		this.lineNumbersEnabled = lineNumbersEnabled;
	}

	public boolean isDisplayAllLines() {
		return displayAllLines;
	}

	public void setDisplayAllLines(boolean displayAllLines) {
		this.displayAllLines = displayAllLines;
	}

	public int getNumberOfLinesToDisplay() {
		return numberOfLinesToDisplay;
	}

	public void setNumberOfLinesToDisplay(int numberOfLinesToDisplay) {
		this.numberOfLinesToDisplay = numberOfLinesToDisplay;
	}

	public BlockLayoutSettings getLayoutSettings() {
		return layoutSettings;
	}

	public void setLayoutSettings(BlockLayoutSettings layoutSettings) {
		this.layoutSettings = layoutSettings;
	}

	public AlertBoxSettings getAlertBoxSettings() {
		return alertBoxSettings;
	}

	public void setAlertBoxSettings(AlertBoxSettings alertBoxSettings) {
		this.alertBoxSettings = alertBoxSettings;
	}
}
