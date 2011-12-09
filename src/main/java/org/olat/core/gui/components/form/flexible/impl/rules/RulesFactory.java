/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.gui.components.form.flexible.impl.rules;

import java.util.HashSet;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormItemDependencyRule;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * TODO: patrickb Class Description for RulesFactory
 * 
 * <P>
 * Initial Date: 10.02.2007 <br>
 * 
 * @author patrickb
 */
public class RulesFactory {
	/**
	 * Creates a custom rule which is triggered whenever
	 * <code>triggerElement</code> is set to <code>triggerValue</code>. The code
	 * which gets executed resides in the <code>apply</code> method of a
	 * {@link DependencyRuleApplayable} object. An implementation that executes if
	 * a trigger element changes to "true" could look like this:
	 * 
	 * <pre>
	 * {@code
	 * RulesFactory.createCustomRule(triggerElement, &quot;true&quot;, new HashSet&lt;FormItem&gt;(Arrays.asList(target1, target2)), formLayout).setDependencyRuleApplayable(new DependencyRuleApplayable() {
	 *   public void apply(FormItem triggerElement, Object triggerVal, Set&lt;FormItem&gt; targets) {
	 *     for (FormItem target : targets) {
	 *       target.setEnabled(true);
	 *     }
	 *   }
	 * }); 
	 * }
	 * </pre>
	 * 
	 * (Note that this example uses an anonymous class which makes it unnecessary
	 * for you to create a separate class which implements
	 * <code>DependencyRuleApplayable</code> for each of your rules.) Don't forget
	 * to add action listeners for
	 * {@link org.olat.core.gui.components.form.flexible.impl.FormEvent.ONCHANGE}
	 * to your trigger elements.
	 * 
	 * @param triggerElement The element that is being watched for changes.
	 * @param triggerValue Triggers if the <code>triggerElement</code>'s key
	 *          changes to <code>triggerValue</code>
	 * @param targets The targets.
	 * @param formLayout The container.
	 * @return The rule
	 * @see		DependencyRuleApplayable
	 */
	public static FormItemDependencyRule createCustomRule(FormItem triggerElement, Object triggerValue, Set<FormItem> targets,
			FormItemContainer formLayout) {
		FormItemDependencyRule fidr = createRule(triggerElement, triggerValue, targets, FormItemDependencyRuleImpl.CUSTOM);
		formLayout.addDependencyRule(fidr);
		return fidr;
	}

	/**
	 * creates a custom rule, it is a must to define and set the applayable
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param target
	 * @param formLayout
	 * @return
	 */
	public static FormItemDependencyRule createCustomRule(FormItem triggerElement, Object triggerValue, FormItem target,
			FormItemContainer formLayout) {
		Set<FormItem> targets = new HashSet<FormItem>();
		targets.add(target);
		return createCustomRule(triggerElement, triggerValue, targets, formLayout);
	}

	/**
	 * creates a reset rule
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param targets
	 * @param formLayout
	 * @return
	 */
	public static FormItemDependencyRule createResetRule(FormItem triggerElement, Object triggerValue, Set<FormItem> targets,
			FormItemContainer formLayout) {
		FormItemDependencyRule fidr = createRule(triggerElement, triggerValue, targets, FormItemDependencyRuleImpl.RESET);
		formLayout.addDependencyRule(fidr);
		return fidr;
	}

	/**
	 * creates a reset rule
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param target
	 * @param formLayout
	 * @return
	 */
	public static FormItemDependencyRule createResetRule(FormItem triggerElement, Object triggerValue, FormItem target,
			FormItemContainer formLayout) {
		Set<FormItem> targets = new HashSet<FormItem>();
		targets.add(target);
		return createResetRule(triggerElement, triggerValue, targets, formLayout);
	}

	/**
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param targets
	 * @return
	 */
	public static FormItemDependencyRule createHideRule(FormItem triggerElement, Object triggerValue, Set<FormItem> targets,
			FormItemContainer formLayout) {
		FormItemDependencyRule fidr = createRule(triggerElement, triggerValue, targets, FormItemDependencyRuleImpl.MAKE_INVISIBLE);
		formLayout.addDependencyRule(fidr);
		return fidr;
	}

	/**
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param target
	 * @return
	 */
	public static FormItemDependencyRule createHideRule(FormItem triggerElement, Object triggerValue, FormItem target,
			FormItemContainer formLayout) {
		Set<FormItem> targets = new HashSet<FormItem>();
		targets.add(target);
		return createHideRule(triggerElement, triggerValue, targets, formLayout);
	}

	/**
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param targets
	 * @return
	 */
	public static FormItemDependencyRule createShowRule(FormItem triggerElement, Object triggerValue, Set<FormItem> targets,
			FormItemContainer formLayout) {
		FormItemDependencyRule fidr = createRule(triggerElement, triggerValue, targets, FormItemDependencyRuleImpl.MAKE_VISIBLE);
		formLayout.addDependencyRule(fidr);
		return fidr;
	}

	/**
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param target
	 * @return
	 */
	public static FormItemDependencyRule createShowRule(FormItem triggerElement, Object triggerValue, FormItem target,
			FormItemContainer formLayout) {
		Set<FormItem> targets = new HashSet<FormItem>();
		targets.add(target);
		return createShowRule(triggerElement, triggerValue, targets, formLayout);
	}

	/**
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param targets
	 * @return
	 */
	public static FormItemDependencyRule createReadOnlyRule(FormItem triggerElement, Object triggerValue, Set<FormItem> targets,
			FormItemContainer formLayout) {
		FormItemDependencyRule fidr = createRule(triggerElement, triggerValue, targets, FormItemDependencyRuleImpl.MAKE_READONLY);
		formLayout.addDependencyRule(fidr);
		return fidr;
	}

	/**
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param target
	 * @return
	 */
	public static FormItemDependencyRule createReadOnlyRule(FormItem triggerElement, Object triggerValue, FormItem target,
			FormItemContainer formLayout) {
		Set<FormItem> targets = new HashSet<FormItem>();
		targets.add(target);
		return createReadOnlyRule(triggerElement, triggerValue, targets, formLayout);
	}

	/**
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param targets
	 * @return
	 */
	public static FormItemDependencyRule createWritableRule(FormItem triggerElement, Object triggerValue, Set<FormItem> targets,
			FormItemContainer formLayout) {
		FormItemDependencyRule fidr = createRule(triggerElement, triggerValue, targets, FormItemDependencyRuleImpl.MAKE_WRITABLE);
		formLayout.addDependencyRule(fidr);
		return fidr;
	}

	/**
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param target
	 * @return
	 */
	public static FormItemDependencyRule createWritableRule(FormItem triggerElement, Object triggerValue, FormItem target,
			FormItemContainer formLayout) {
		Set<FormItem> targets = new HashSet<FormItem>();
		targets.add(target);
		return createWritableRule(triggerElement, triggerValue, targets, formLayout);
	}

	/**
	 * 
	 * @param triggerElement
	 * @param triggerValue
	 * @param targets
	 * @param type
	 * @return
	 */
	private static FormItemDependencyRule createRule(FormItem triggerElement, Object triggerValue, Set<FormItem> targets, int type) {
		FormItemDependencyRule retVal = null;
		//
		if (triggerElement instanceof SingleSelection) {
			retVal = new SingleSelectionTriggerdDependencyRule((SingleSelection) triggerElement, (String) triggerValue, targets, type);
		} else if (triggerElement instanceof MultipleSelectionElement) {
			retVal = new MultiSelectionTriggerdDependencyRule((MultipleSelectionElement) triggerElement, (String) triggerValue, targets, type);
		} else if (triggerElement instanceof TextElement) {
			retVal = new TextElementTriggerdDependencyRule((TextElement) triggerElement, (String) triggerValue, targets, type);
		} else {
			throw new AssertException("Form Item of type <" + triggerElement.getClass().getName() + "> not yet supported as TRIGGERELEMENT");
		}
		//
		return retVal;
	}
}
