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
package org.olat.modules.contacttracing.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 * Initial date: 15.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingReportGeneratorStep1 extends BasicStep {

    private PrevNextFinishConfig prevNextFinishConfig;
    private ContactTracingReportGeneratorContextWrapper contextWrapper;

    public ContactTracingReportGeneratorStep1(UserRequest ureq, ContactTracingReportGeneratorContextWrapper contextWrapper) {
        super(ureq);
        setI18nTitleAndDescr("contact.tracing.report.generator.step.1.title", "contact.tracing.report.generator.step.1.description");
        setNextStep(new ContactTracingReportGeneratorStep2(ureq, contextWrapper));

        this.prevNextFinishConfig = new PrevNextFinishConfig(false, true, false);
        this.contextWrapper = contextWrapper;
    }

    @Override
    public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
        return prevNextFinishConfig;
    }

    @Override
    public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form form) {
        stepsRunContext.put("data", contextWrapper);
        return new ContactTracingReportGeneratorStep1Controller(ureq, wControl, form, stepsRunContext);
    }
}
