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
package org.olat.course.assessment;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EfficiencyStatementAssessmentController extends FormBasicController implements GenericEventListener {

	private FormLink configButton;
	private FormLink recalculateButton;
	private DialogBoxController recalculateEfficiencyDC;
	
	private final OLATResourceable ores;
	private final RepositoryEntry courseEntry;
	
	public EfficiencyStatementAssessmentController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry) {
		super(ureq, wControl, "assessment_eff_statement");
		this.courseEntry = courseEntry;
		this.ores = OresHelper.clone(courseEntry.getOlatResource());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), ores);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer container = (FormLayoutContainer)formLayout;
			ICourse course = CourseFactory.loadCourse(courseEntry);
		
			boolean enabled = course.getCourseEnvironment().getCourseConfig().isEfficencyStatementEnabled();
			String enableStr = this.translate(enabled ? "efficiencystatement.config.on" : "efficiencystatement.config.off");
			container.contextPut("enabledStr", enableStr);
			container.contextPut("enabled", Boolean.valueOf(enabled));
			
			configButton = uifactory.addFormLink("config", "efficiencystatement.config", null, container, Link.BUTTON);
			recalculateButton = uifactory.addFormLink("recalculate", "efficiencystatement.recalculate", null, container, Link.BUTTON);
		}
	}
	
	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, ores);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == configButton) {
			openConfiguration(ureq);
		}	else if(source == recalculateButton) {
			askBeforeRecalculate(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof EfficiencyStatementEvent) {
			EfficiencyStatementEvent e = (EfficiencyStatementEvent)event;
			if(EfficiencyStatementEvent.CMD_FINISHED.equals(event.getCommand())
					&& ores.getResourceableId().equals(e.getCourseResourceId())) {
				flc.contextPut("recalculating", Boolean.FALSE);
				showInfo("efficiencystatement.recalculate.finished");
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == recalculateEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {				
				recalculate();
			}
		}
	}

	private void openConfiguration(UserRequest ureq) {
		String resourceUrl = "[RepositoryEntry:" + courseEntry.getKey() + "][Settings:0][Certificates:0]";
		BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}

	private void askBeforeRecalculate(UserRequest ureq) {
		recalculateEfficiencyDC = activateYesNoDialog(ureq, null, translate("efficiencystatement.recalculate.warning"), recalculateEfficiencyDC);
	}
	
	private void recalculate() {
		flc.contextPut("recalculating", Boolean.TRUE);
		EfficiencyStatementEvent recalculateEvent = new EfficiencyStatementEvent(EfficiencyStatementEvent.CMD_RECALCULATE, ores.getResourceableId());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(recalculateEvent, CourseModule.ORESOURCEABLE_TYPE_COURSE);
	}
}
