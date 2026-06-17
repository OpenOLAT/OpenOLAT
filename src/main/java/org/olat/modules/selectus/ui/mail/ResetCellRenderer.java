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
 * Delegate will be rendered or not.
 * 
 * Initial date: 15.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetCellRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {
	
	private final FlexiCellRenderer resetDelegate;
	private final FlexiCellRenderer deleteDelegate;
	
	public ResetCellRenderer(FlexiCellRenderer resetDelegate, FlexiCellRenderer deleteDelegate) {
		this.resetDelegate = resetDelegate;
		this.deleteDelegate = deleteDelegate;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue instanceof PositionMailTemplateRow) {
			PositionMailTemplateRow template = (PositionMailTemplateRow)cellValue;
			if(!template.isEnabled()) {
				//
			} else if(template.getType() == Type.system) {
				if(template.getMailTemplate() != null) {
					resetDelegate.render(null, target, cellValue, row, source, ubu, translator);
				}
			} else if(template.getType() == Type.custom) {
				deleteDelegate.render(null, target, cellValue, row, source, ubu, translator);
			} else if(template.isCustomized()) {
				resetDelegate.render(null, target, cellValue, row, source, ubu, translator);
			}
		}
	}
	
	@Override
	public List<String> getActions() {
		List<String> actions = new ArrayList<>(2);
		getActions(deleteDelegate, actions);
		getActions(resetDelegate, actions);
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
