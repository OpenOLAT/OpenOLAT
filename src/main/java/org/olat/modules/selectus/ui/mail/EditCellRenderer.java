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
package org.olat.modules.selectus.ui.mail;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionDelegateCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.ui.mail.PositionMailTemplateRow.Type;

/**
 * 
 * Initial date: 24 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditCellRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {
	
	private final Translator translator;
	private final FlexiCellRenderer editDelegate;
	private final FlexiCellRenderer customizeDelegate;
	
	public EditCellRenderer(FlexiCellRenderer customizeDelegate, FlexiCellRenderer editDelegate, Translator translator) {
		this.customizeDelegate = customizeDelegate;
		this.editDelegate = editDelegate;
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator transl) {
		if(cellValue instanceof PositionMailTemplateRow) {
			PositionMailTemplateRow template = (PositionMailTemplateRow)cellValue;
			if(!template.isEnabled()) {
				target.append(translator.translate("feature.not.enabled"));
			} else if(template.getType() == Type.system) {
				if(template.getMailTemplate() == null) {
					customizeDelegate.render(null, target, cellValue, row, source, ubu, translator);
				} else {
					editDelegate.render(null, target, cellValue, row, source, ubu, translator);
				}
			} else if(template.isCustomized()) {
				editDelegate.render(null, target, cellValue, row, source, ubu, translator);
			} else {
				customizeDelegate.render(null, target, cellValue, row, source, ubu, translator);
			}
		}
	}
	
	@Override
	public List<String> getActions() {
		List<String> actions = new ArrayList<>(2);
		getActions(customizeDelegate, actions);
		getActions(editDelegate, actions);
		return actions;
	}
	
	private void getActions(FlexiCellRenderer delegate, List<String> actions) {
		if(delegate instanceof ActionDelegateCellRenderer) {
			List<String> delegateActions = ((ActionDelegateCellRenderer)delegate).getActions();
			if(delegateActions != null && !delegateActions.isEmpty()) {
				actions.addAll(delegateActions);
			}
		}
	}
}
