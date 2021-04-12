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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Action;
import org.olat.modules.forms.model.xml.Container;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.Rule;
import org.olat.modules.forms.model.xml.VisibilityAction;
import org.olat.modules.forms.rules.RulesEngine.RuleFulfiledListener;
import org.olat.modules.forms.rules.ui.ActionEditorFragment;
import org.olat.modules.forms.rules.ui.VisibilityActionFragement;
import org.olat.modules.forms.ui.model.ExecutionFragment;

/**
 * 
 * Initial date: 6 Apr 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class VisibilityHandler implements ActionHandler {

	@Override
	public String getI18nKey() {
		return "action.visibility";
	}
	
	@Override
	public String getActionType() {
		return VisibilityAction.TYPE;
	}

	@Override
	public boolean accepts(AbstractElement element) {
		return Container.TYPE.equals(element.getType());
	}

	@Override
	public ActionEditorFragment getEditorFragment(FormUIFactory uifactory, Action action, Form form) {
		VisibilityAction visibilityAction = action instanceof VisibilityAction
				? (VisibilityAction)action
				: null;
		return new VisibilityActionFragement(uifactory, visibilityAction, form);
	}
	
	
	public static void registerListeners(RulesEngine rulesEngine, Form form, List<ExecutionFragment> fragments) {
		for (AbstractElement abstractElement : form.getElements()) {
			if (abstractElement instanceof Container) {
				Container container = (Container)abstractElement;
				
				Map<Rule, Boolean> ruleToVisibilty = rulesEngine.getRules().stream()
						.filter(rule -> rule.getAction() instanceof VisibilityAction)
						.filter(rule -> container.getId().equals(((VisibilityAction)rule.getAction()).getElementId()))
						.collect(Collectors.toMap(Function.identity(), rule -> Boolean.TRUE));
				
				if (!ruleToVisibilty.isEmpty()) {
					List<String> containerElementIds = container.getContainerSettings().getAllElementIds();
					List<ExecutionFragment> containerFragments = fragments.stream()
							.filter(f -> containerElementIds.contains(f.getElementId()))
							.collect(Collectors.toList());
					
					ContainerVisibilityListener listener = new ContainerVisibilityListener(ruleToVisibilty, containerFragments);
					rulesEngine.registerListener(listener);
				}
			}
		}
	}

	private static final class ContainerVisibilityListener implements RuleFulfiledListener {
		
		private Map<Rule, Boolean> ruleToVisibilty;
		private List<ExecutionFragment> containerFragments;

		private ContainerVisibilityListener(Map<Rule, Boolean> ruleToVisibilty, List<ExecutionFragment> containerFragments) {
			this.ruleToVisibilty = ruleToVisibilty;
			this.containerFragments = containerFragments;
		}
		
		@Override
		public void onFulfilledChanged(Rule rule, boolean fulfilled) {
			if (ruleToVisibilty.containsKey(rule)) {
				ruleToVisibilty.put(rule, Boolean.valueOf(fulfilled));
			}
			boolean visible = ruleToVisibilty.isEmpty() || ruleToVisibilty.containsValue(Boolean.TRUE);
			containerFragments.forEach(fragment -> fragment.setVisible(visible));
		}
		
	}

}
