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
package org.olat.core.gui.components.form.flexible.elements;

import java.util.List;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

/**
 * 
 * Initial date: 13 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TextAreaElement extends TextElement {
	
	public int getRows();
	
	public void setRows(int rows);
	
	public void setStripedBackgroundEnabled(boolean stripedBackgroundEnabled);
	
	public void setLineNumbersEnbaled(boolean lineNumbersEnabled);
	
	public void setOriginalLineBreaks(boolean originalLineBreaks);
	
	public void setFixedFontWidth(boolean fixedFontWidth);
	
	public void setAutosave(boolean autosave);
	
	public boolean isStripedBackgroundEnabled();
	
	public boolean isLineNumbersEnabled();
	
	public boolean isOriginalLineBreaks();
	
	public boolean isFixedFontWidth();
	
	public void setErrors(List<Integer> rows);
	
	public List<Integer> getErrors();
	
	public String getErrorsAsString();
	
	/**
	 * This event is fired without refresh of the GUI!
	 * 
	 * Initial date: 21 Dec 2022<br>
	 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
	 *
	 */
	public static class TextAreaAutosaveEvent extends FormEvent {

		private static final long serialVersionUID = 3323978400930044102L;

		private final String text;
		
		public TextAreaAutosaveEvent(FormItem source, String text) {
			super("text-area-autosave", source, ONCHANGE);
			this.text = text;
		}
		
		public String getText() {
			return text;
		}
		
	}
}
