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

package org.olat.course.nodes.portfolio;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.StructureStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Small controller to use in a popup to change the deadline.
 * 
 * <P>
 * Initial Date:  11 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class DeadlineController extends FormBasicController {
	
	private DateChooser deadlineChooser;
	
	private EPStructuredMap map;
	
	@Autowired
	private EPFrontendManager ePFMgr;
	
	public DeadlineController(UserRequest ureq, WindowControl wControl, EPStructuredMap map) {
		super(ureq, wControl);
		this.map = map;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("map.deadline.change.title");
		setFormDescription("map.deadline.change.description");
		
		deadlineChooser = uifactory.addDateChooser("map.deadline", null, formLayout);
		if((map != null && map.getDeadLine() == null)) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DATE, 7);
			deadlineChooser.setDate(cal.getTime());
		} else if(map != null) {
			deadlineChooser.setDate(map.getDeadLine());
		}
		deadlineChooser.setValidDateCheck("map.deadline.invalid");
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("ok-cancel", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("ok", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Date newDeadLine = deadlineChooser.getDate();
		// OLAT-6335: refresh map in case it was changed meanwhile
		if(map != null) {
			map = (EPStructuredMap) ePFMgr.reloadPortfolioStructure(map); 
			map.setDeadLine(newDeadLine);
			map.setStatus(StructureStatusEnum.OPEN);
			ePFMgr.savePortfolioStructure(map);
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	public EPStructuredMap getMap() {
		return map;
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#validateFormLogic(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		Date newDeadLine = deadlineChooser.getDate();
		if (newDeadLine != null && newDeadLine.before(new Date())) {
			deadlineChooser.setErrorKey("map.deadline.invalid.before", null);
			allOk &= false;
		}
		
		return allOk & super.validateFormLogic(ureq);
	}
}