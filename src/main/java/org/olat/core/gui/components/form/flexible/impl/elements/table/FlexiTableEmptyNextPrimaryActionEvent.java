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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;

/**
 * This event is fired when the next primary action button in the empty table
 * state has been clicked
 * 
 * Initial date: 08.04.2021<br>
 * 
 * @author gnaegi, Florian Gnägi @frentix.com, https://www.frentix.com
 *
 */
public class FlexiTableEmptyNextPrimaryActionEvent extends FlexiTableReduceEvent {

	private static final long serialVersionUID = 1506734364410290088L;

	protected FlexiTableEmptyNextPrimaryActionEvent(FormItem source) {
		super(source, FormEvent.ONCLICK);
	}

}
