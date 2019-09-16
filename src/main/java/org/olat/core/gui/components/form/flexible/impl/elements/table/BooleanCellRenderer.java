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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Delegate will be rendered or not.
 * 
 * Initial date: 15.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BooleanCellRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {
	
	private final FlexiCellRenderer trueDelegate;
	private final FlexiCellRenderer falseDelegate;
	
	public BooleanCellRenderer(FlexiCellRenderer trueDelegate, FlexiCellRenderer falseDelegate) {
		this.trueDelegate = trueDelegate;
		this.falseDelegate = falseDelegate;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		if(cellValue != null && Boolean.TRUE.equals(cellValue)) {
			if(trueDelegate != null) {
				trueDelegate.render(null, target, cellValue, row, source, ubu, translator);
			}
		} else if(falseDelegate != null) {
			falseDelegate.render(null, target, cellValue, row, source, ubu, translator);
		}
	}
	
	@Override
	public List<String> getActions() {
		List<String> actions = new ArrayList<>(2);
		getActions(trueDelegate, actions);
		if(falseDelegate != null) {
			getActions(falseDelegate, actions);
		}
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
