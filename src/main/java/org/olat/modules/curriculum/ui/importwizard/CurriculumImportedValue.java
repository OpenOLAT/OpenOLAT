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

/**
 * 
 * Initial date: 10 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumImportedValue {
	
	private Level level;
	private final String column;

	private String placeholder;
	private String message;
	
	private Object beforeValue;
	private Object afterValue;
	
	public CurriculumImportedValue(String column) {
		this.column = column;
	}
	
	public boolean isChanged() {
		return level == Level.CHANGE;
	}
	
	public boolean isWarning() {
		return level == Level.WARNING;
	}
	
	public boolean isError() {
		return level == Level.ERROR;
	}
	
	public String getColumn() {
		return column;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public void setChanged(Object before, Object after) {
		this.level = Level.CHANGE;
		this.beforeValue = before;
		this.afterValue = after;
	}
	
	public Object getBeforeValue() {
		return beforeValue;
	}

	public Object getAfterValue() {
		return afterValue;
	}
	
	public String getPlaceholder() {
		return placeholder;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setError(String placeholder, String message) {
		this.level = Level.ERROR;
		this.placeholder = placeholder;
		this.message = message;
	}
	
	public void setWarning(String placeholder, String message) {
		this.level = Level.WARNING;
		this.placeholder = placeholder;
		this.message = message;
	}
	
	public enum Level {
		ERROR("o_icon_validation_error"),
		WARNING("o_icon_validation_warning"),
		CHANGE("o_icon_changes");
		
		private final String iconCssClass;
		
		private Level(String iconCssClass) {
			this.iconCssClass = iconCssClass;
		}
		
		public String iconCssClass() {
			return iconCssClass;
		}
		
	}
}
