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
package org.olat.group.ui.wizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGCopySingleGroupStep extends BasicStep {
	
	private final boolean lastGroup;
	private final BusinessGroup groupToCopy;
	
	public BGCopySingleGroupStep(UserRequest ureq, List<BusinessGroup> groups) {
		super(ureq);
		
		groupToCopy = groups.remove(0);
		setI18nTitleAndDescr("copy.wizard.bgstep", "copy.wizard.bgstep");
		
		lastGroup = groups.isEmpty();
		if(lastGroup) {
			setNextStep(Step.NOSTEP);
		} else {
			setNextStep(new BGCopySingleGroupStep(ureq, groups));
		}
	}

	@Override
	public FormItem getStepTitle() {
		String groupName = StringHelper.escapeHtml(groupToCopy.getName());
		String title = getTranslator().translate("copy.wizard.bgstep", new String[]{ groupName });
		FormLink fl = new FormLinkImpl("copy.wizard." + groupToCopy.getKey(), null, title, Link.FLEXIBLEFORMLNK + Link.NONTRANSLATED);
		fl.setTranslator(getTranslator());
		return fl;
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		if(lastGroup) {
			return PrevNextFinishConfig.BACK_FINISH;
		}
		return new PrevNextFinishConfig(false, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl, StepsRunContext stepsRunContext, Form form) {
		return new BGCopySingleGroupStepController(ureq, windowControl, form, stepsRunContext, groupToCopy);
	}
}
