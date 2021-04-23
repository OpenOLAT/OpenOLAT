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
package org.olat.modules.forms.rules;

import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Action;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.rules.ui.ActionEditorFragment;

/**
 * 
 * Initial date: 6 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ActionHandler {
	
	public String getI18nKey();
	
	public String getActionType();
	
	public boolean accepts(AbstractElement element);

	public ActionEditorFragment getEditorFragment(FormUIFactory uifactory, Action action, Form form);

	public boolean actionsAvailable(Form form);
	
}
