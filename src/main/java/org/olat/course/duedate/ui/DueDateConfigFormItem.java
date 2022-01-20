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
package org.olat.course.duedate.ui;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.course.duedate.DueDateConfig;

/**
 * 
 * Initial date: 3 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface DueDateConfigFormItem extends FormItem, FormItemCollection {
	
	public static DueDateConfigFormItem create(String name, SelectionValues relativeToDates, boolean relative,
			DueDateConfig dueDateConfig) {
		return new DueDateFormItemImpl(name, relativeToDates, relative, dueDateConfig);
	}
	
	public void setRelativeToDates(SelectionValues relativeToDates);

	public void setRelative(boolean relative);
	
	public DueDateConfig getDueDateConfig();
	
	public void setDueDateConfig(DueDateConfig dueDateConfig);
	
	/**
	 * Fine grain control of the action event, especially the onchange
	 * event on the hours and minutes fields of the date chooser.
	 * 
	 * @param action The action
	 * @param dateOnly true if the hour/minute fields of the date chooser doesn't send onchange events.
	 */
	public void addActionListener(int action, boolean dateOnly);

}
