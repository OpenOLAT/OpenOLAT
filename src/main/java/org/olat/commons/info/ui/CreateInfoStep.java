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

package org.olat.commons.info.ui;

import java.util.List;

import org.olat.commons.info.InfoMessage;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Description:<br>
 * First step of the wizard, create and fill the message.
 * 
 * <P>
 * Initial Date:  27 jul. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class CreateInfoStep extends BasicStep {
	
	private final InfoMessage message;
	private final OLATResourceable ores;
	private final String resSubPath;
	
	public CreateInfoStep(UserRequest ureq, OLATResourceable ores, String subPath, SendMailOption subscriberOption, List<SendMailOption> courseOptions,
						  List<SendMailOption> groupOptions, List<SendMailOption> curriculaOptions, InfoMessage message) {
		super(ureq);
		this.ores = ores;
		this.resSubPath = subPath;
		this.message = message;
		setI18nTitleAndDescr("wizard.step0.title", "wizard.step0.description");
		setNextStep(new SendMailStep(ureq, subscriberOption, courseOptions, groupOptions, curriculaOptions));
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(false, true, true);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CreateInfoStepController(ureq, wControl, runContext, form, message, ores, resSubPath);
	}
}
