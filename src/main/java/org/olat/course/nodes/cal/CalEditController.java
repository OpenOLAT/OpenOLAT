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

package org.olat.course.nodes.cal;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.run.calendar.CourseCalendarController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * <h3>Description:</h3> Edit controller for calendar course nodes<br/>
 * <p>
 * Initial Date: 4 nov. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CalEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {
	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	private static final String PANE_TAB_CALCONFIG = "pane.tab.calconfig";

	private static final String CONFIG_START_DATE = "startDate";
	private static final String CONFIG_AUTO_DATE = "autoDate";

	private static final String[] paneKeys = { PANE_TAB_CALCONFIG, PANE_TAB_ACCESSIBILITY };

	private ModuleConfiguration moduleConfiguration;
	private ConditionEditController accessCondContr;
	private DisplayConfigTabForm displayForm;
	private TabbedPane tabs;
	private Panel main;
	private CourseCalendarController calCtr;
	private VelocityContainer editAccessVc;
	private ConditionEditController editCondContr;
	
	private CalCourseNode calCourseNode;

	/**
	 * Constructor for calendar page editor controller
	 * 
	 * @param config The node module configuration
	 * @param ureq The user request
	 * @param calCourseNode The current calendar page course node
	 * @param course
	 */
	public CalEditController(ModuleConfiguration config, UserRequest ureq, WindowControl wControl, CalCourseNode calCourseNode,
			ICourse course, UserCourseEnvironment euce) {
		super(ureq, wControl);
		this.moduleConfiguration = config;
		this.calCourseNode = calCourseNode;

		main = new Panel("calmain");

		editAccessVc = createVelocityContainer("edit_access");
		CourseEditorTreeModel editorModel = course.getEditorTreeModel();
		// Accessibility precondition
		Condition accessCondition = calCourseNode.getPreConditionAccess();
		accessCondContr = new ConditionEditController(ureq, getWindowControl(), euce, accessCondition,
				AssessmentHelper.getAssessableNodes(editorModel, calCourseNode));
		this.listenTo(accessCondContr);
		editAccessVc.put("readerCondition", accessCondContr.getInitialComponent());

		// cal read / write preconditions
		Condition editCondition = calCourseNode.getPreConditionEdit();
		editCondContr = new ConditionEditController(ureq, getWindowControl(), euce, editCondition,
				AssessmentHelper.getAssessableNodes(editorModel, calCourseNode));
		listenTo(editCondContr);
		editAccessVc.put("editCondition", editCondContr.getInitialComponent());

		displayForm = new DisplayConfigTabForm(moduleConfiguration, ureq, wControl);
		listenTo(displayForm);
		main.setContent(displayForm.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == displayForm) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == accessCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessCondContr.getCondition();
				calCourseNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == editCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = editCondContr.getCondition();
				calCourseNode.setPreConditionEdit(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
		tabs = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), editAccessVc);
		tabbedPane.addTab(translate(PANE_TAB_CALCONFIG), main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// child controllers registered with listenTo() get disposed in
		// BasicController
		if (calCtr != null) {
			calCtr.dispose();
			calCtr = null;
		}
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController#getPaneKeys()
	 */
	public String[] getPaneKeys() {
		return paneKeys;
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController#getTabbedPane()
	 */
	public TabbedPane getTabbedPane() {
		return tabs;
	}

	public static Date getStartDate(ModuleConfiguration config) {
		String timeStr = config.getStringValue(CONFIG_START_DATE);
		if (StringHelper.containsNonWhitespace(timeStr)) {
			try {
				Long time = Long.parseLong(timeStr);
				return new Date(time);
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public static void setStartDate(ModuleConfiguration config, Date startDate) {
		if (startDate == null) config.setStringValue(CONFIG_START_DATE, "");
		else {
			String timeStr = String.valueOf(startDate.getTime());
			config.setStringValue(CONFIG_START_DATE, timeStr);
		}
	}

	public static boolean getAutoDate(ModuleConfiguration config) {
		String autoStr = config.getStringValue(CONFIG_AUTO_DATE);
		if (StringHelper.containsNonWhitespace(autoStr)) { return new Boolean(autoStr); }
		return Boolean.FALSE;
	}

	public static void setAutoDate(ModuleConfiguration config, boolean autoDate) {
		config.setStringValue(CONFIG_AUTO_DATE, Boolean.toString(autoDate));
	}

	private class DisplayConfigTabForm extends FormBasicController {
		private DateChooser dateChooser;
		private SingleSelection autoDateEl;
		private ModuleConfiguration config;

		public DisplayConfigTabForm(ModuleConfiguration config, UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			this.config = config;
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			setFormTitle("pane.tab.calconfig");
			//setFormDescription("pane.tab.calconfigdesc");

			boolean autoDate = getAutoDate(config);
			String[] keys = new String[]{"auto","selected"};
			String[] values = new String[]{translate("pane.tab.auto_date"),translate("pane.tab.manual_date")};
			autoDateEl = uifactory.addRadiosVertical("pane.tab_auto_date", null, formLayout, keys, values);

			autoDateEl.setHelpText(translate("fhelp.start_date"));
			autoDateEl.select(autoDate ? keys[0] : keys[1], autoDate);
			autoDateEl.setLabel("pane.tab.start_date", null);
			autoDateEl.addActionListener(FormEvent.ONCLICK);
			
			Date startDate = getStartDate(config);
			Date selectedDate = startDate == null ? new Date() : startDate;
			dateChooser = uifactory.addDateChooser("pane.tab.start_date_chooser", null, null, formLayout);
			dateChooser.setDate(selectedDate);
			dateChooser.setVisible(!autoDate);
			
			// Create submit and cancel buttons
			final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
			formLayout.add(buttonLayout);
			uifactory.addFormSubmitButton("save", buttonLayout);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(source == autoDateEl) {
				boolean autoDate = isAutoDate();
				dateChooser.setVisible(!autoDate);
				flc.setDirty(true);
			}
		}

		@Override
		protected void formOK(UserRequest ureq) {
			setStartDate(config, getDate());
			setAutoDate(config, isAutoDate());
			fireEvent(ureq, Event.DONE_EVENT);
		}

		public Date getDate() {
			return dateChooser.getDate();
		}

		public boolean isAutoDate() {
			return autoDateEl.isSelected(0);
		}
	}
}