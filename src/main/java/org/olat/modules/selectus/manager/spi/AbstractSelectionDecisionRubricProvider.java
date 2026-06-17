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
package org.olat.modules.selectus.manager.spi;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.manager.DecisionRubricSPI;
import org.olat.modules.selectus.model.DecisionRubric;

/**
 * 
 * Initial date: 18 nov. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractSelectionDecisionRubricProvider implements DecisionRubricSPI  {
	
	private static final Logger log = Tracing.createLoggerFor(AbstractSelectionDecisionRubricProvider.class);
	
	
	@Override
	public String getValue(DecisionRubric rubric) {
		if(rubric != null && rubric.getIntegerValue() != null) {
			return rubric.getIntegerValue().toString();
		}
		return null;
	}

	protected FormItem createElement(DecisionRubric rubric, String[] theKeys, String[] theValues, FormItemContainer formLayout, FormUIFactory uifactory) {
		SingleSelection selectionEl = uifactory.addDropdownSingleselect("abc_" + CodeHelper.getRAMUniqueID(), null, formLayout, theKeys, theValues, null);
		boolean selected = false;
		if(rubric != null && rubric.getIntegerValue() != null) {
			String selectedKey = Integer.toString(rubric.getIntegerValue().intValue());
			for(String theKey:theKeys) {
				if(theKey.equals(selectedKey)) {
					selectionEl.select(theKey, true);
					selected = true;
				}
			}
		}
		if(!selected) {
			selectionEl.select(theKeys[0], true);
		}
		return selectionEl;
	}
	
	@Override
	public void commitValue(DecisionRubric rubric, FormItem item) {
		try {
			if(item instanceof SingleSelection) {
				SingleSelection selectionEl = (SingleSelection)item;
				if(selectionEl.isOneSelected()) {
					String selectedKey = selectionEl.getSelectedKey();
					if("-".equals(selectedKey)) {
						rubric.setIntegerValue(null);
					} else if(StringHelper.isLong(selectedKey)) {
						Integer value = Integer.parseInt(selectedKey);
						rubric.setIntegerValue(value);
					} else {
						rubric.setIntegerValue(null);
					}
				}
			}
		} catch (NumberFormatException e) {
			log.error("", e);
		}
	}
}
