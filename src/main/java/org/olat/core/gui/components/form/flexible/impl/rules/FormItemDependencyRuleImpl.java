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

import java.util.Iterator;
import java.util.Set;

import org.olat.core.gui.components.form.flexible.DependencyRuleApplayable;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormItemDependencyRule;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * TODO: patrickb Class Description for FormItemDependencyRuleImpl
 * <P>
 * Initial Date: 09.02.2007 <br>
 * 
 * @author patrickb
 */
public abstract class FormItemDependencyRuleImpl implements FormItemDependencyRule {
	public static final int MAKE_INVISIBLE = 0;
	public static final int MAKE_VISIBLE = 1;
	public static final int MAKE_READONLY = 2;
	public static final int MAKE_WRITABLE = 3;
	public static final int HIDE_ERROR = 4;
	public static final int RESET = 5;
	public static final int CUSTOM = 16;

	protected Object triggerVal;
	protected FormItem triggerElement;
	protected Set<FormItem> targets;
	private int actiontype;
	private String identifier;

	private DependencyRuleApplayable applayable = null;
	
	
	/**
	 * @param triggerElement
	 * @param triggerValue
	 * @param targets
	 */
	public FormItemDependencyRuleImpl(FormItem triggerElement, Object triggerValue, Set<FormItem> targets, int type) {
		this.triggerVal = triggerValue;
		this.triggerElement = triggerElement;
		this.targets = targets;
		this.actiontype = type;
		//
		this.identifier = triggerElement.getName();
		this.identifier += "~" + triggerValue;
		this.identifier += "~" + type;
		this.identifier += "~" + targets.hashCode();

	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.api.FormItemDependencyRule#applyRule()
	 */
	public boolean applyRule(FormItemContainer formLayout) {
		if (!doesTrigger() || targets == null) {
			// stop applying
			return false;
		}
		/*
		 * apply rule
		 */
		if (actiontype == FormItemDependencyRuleImpl.CUSTOM) {
			applayable.apply(triggerElement, triggerVal, targets);
		} else {
			for (Iterator<FormItem> iter = targets.iterator(); iter.hasNext();) {
				FormItem element = iter.next();
				switch (actiontype) {
					case FormItemDependencyRuleImpl.MAKE_INVISIBLE:
						element.setVisible(false);
						break;
					case FormItemDependencyRuleImpl.MAKE_VISIBLE:
						element.setVisible(true);
						break;
					case FormItemDependencyRuleImpl.MAKE_READONLY:
						element.setEnabled(false);
						break;
					case FormItemDependencyRuleImpl.MAKE_WRITABLE:
						element.setEnabled(true);
						break;
					case FormItemDependencyRuleImpl.HIDE_ERROR:
						element.showError(false);
						break;
					case FormItemDependencyRuleImpl.RESET:
						element.reset();
						break;
					default:
						throw new AssertException("unsupported action in dependency rule");
				}
			}
		}

		return true;
	}

	public void setDependencyRuleApplayable(DependencyRuleApplayable applayable){
		this.applayable = applayable;
	}

	abstract protected boolean doesTrigger();

	/**
	 * @see org.olat.core.gui.components.form.flexible.api.FormItemDependencyRule#getIdentifier()
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.api.FormItemDependencyRule#getTargetElements()
	 */
	public Set<FormItem> getTargetElements() {
		return targets;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.api.FormItemDependencyRule#getTriggerElement()
	 */
	public FormItem getTriggerElement() {
		return triggerElement;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.api.FormItemDependencyRule#getTriggerValue()
	 */
	public Object getTriggerValue() {
		return triggerVal;
	}

}
