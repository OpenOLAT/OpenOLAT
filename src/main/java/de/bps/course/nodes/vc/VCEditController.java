//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

import de.bps.course.nodes.VCCourseNode;
import de.bps.course.nodes.vc.provider.VCProvider;
import de.bps.course.nodes.vc.provider.VCProviderFactory;

/**
 * Description:<br>
 * Edit controller for dates list course nodes - Virtual Classroom dates .
 * 
 * <P>
 * Initial Date: 30.08.2010 <br>
 * 
 * @author Jens Lindner (jlindne4@hs-mittweida.de)
 * @author skoeber
 */
public class VCEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {

	private static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";
	public static final String PANE_TAB_VCCONFIG = "pane.tab.vcconfig";
	final static String[] paneKeys = { PANE_TAB_VCCONFIG, PANE_TAB_ACCESSIBILITY };

	// GUI
	private VelocityContainer editVc;
	private ConditionEditController accessibilityCondContr;
	private TabbedPane tabPane;
	private Controller configCtr;
	private VCSelectionForm selForm;
	private VCEditForm editForm;
	private DialogBoxController yesNoUpdate;
	private DialogBoxController yesNoDelete;
	
	// runtime data
	private VCCourseNode courseNode;
	private VCConfiguration config;
	private VCProvider provider;
	private String roomId;

	public VCEditController(UserRequest ureq, WindowControl wControl, VCCourseNode courseNode,
			ICourse course, UserCourseEnvironment userCourseEnv, VCProvider provider, VCConfiguration config) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.config = config;
		this.provider = provider;

		editVc = this.createVelocityContainer("edit");

		Condition accessCondition = courseNode.getPreConditionAccess();
		accessibilityCondContr = new ConditionEditController(ureq, wControl, userCourseEnv,
				accessCondition, AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), courseNode));
		listenTo(accessibilityCondContr);
		
		// show selection form when there is more than one registered virtual classroom provider
		List<VCProvider> registeredProviders = VCProviderFactory.getProviders();
		if(registeredProviders.size() > 1) {
			selForm = new VCSelectionForm(ureq, wControl, provider.getProviderId());
			listenTo(selForm);
			editVc.put("VCSelectionForm", selForm.getInitialComponent());
		}
		
		editForm = new VCEditForm(ureq, wControl, provider.getTemplates(), (DefaultVCConfiguration) config);
		listenTo(editForm);
		editVc.put("editForm", editForm.getInitialComponent());
		
		roomId = course.getResourceableId() + "_" + courseNode.getIdent();
		
		configCtr = provider.createConfigController(ureq, wControl, roomId, config);
		listenTo(configCtr);
		editVc.put("configCtr", configCtr.getInitialComponent());
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabPane;
	}

	@Override
	protected void doDispose() {
		if(configCtr != null) {
			removeAsListenerAndDispose(configCtr);
			configCtr = null;
		}
		if(editForm != null) {
			removeAsListenerAndDispose(editForm);
			editForm = null;
		}
		if(selForm != null) {
			removeAsListenerAndDispose(selForm);
			selForm = null;
		}
		if(yesNoDelete != null) {
			removeAsListenerAndDispose(yesNoDelete);
			yesNoDelete = null;
		}
		if(yesNoUpdate != null) {
			removeAsListenerAndDispose(yesNoUpdate);
			yesNoUpdate = null;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessibilityCondContr) {
			if (event == Event.CHANGED_EVENT) {
				Condition cond = accessibilityCondContr.getCondition();
				courseNode.setPreConditionAccess(cond);
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == configCtr | source == editForm) {
			courseNode.getModuleConfiguration().set(VCCourseNode.CONF_VC_CONFIGURATION, config);
			courseNode.getModuleConfiguration().setStringValue(VCCourseNode.CONF_PROVIDER_ID, config.getProviderId());
			/*
			if(provider.existsClassroom(roomId, config)) {
				removeAsListenerAndDispose(yesNoUpdate);
				yesNoUpdate = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), translate("sync.meeting.title"), translate("sync.meeting.text"));
				listenTo(yesNoUpdate);
				yesNoUpdate.activate();
			}
			*/
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		} else if (source == selForm) {
			/*
			 * If classroom already exists and the user changes the provider,
			 * the existing room has to be deleted for cleanup purposes. Ask
			 * the user if this is intended.
			 */
			if(provider.existsClassroom(roomId, config)) {
				removeAsListenerAndDispose(yesNoDelete);
				yesNoDelete = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), translate("delete.meeting.title"), translate("delete.meeting.text"));
				listenTo(yesNoDelete);
				yesNoDelete.activate();
			} else {
				reset(ureq);
			}
		} else if (source == yesNoDelete) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				provider.removeClassroom(roomId, config);
				reset(ureq);
			}
		} else if(source == yesNoUpdate) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				Date allBegin = null, allEnd = null;
				if(config.getMeetingDates() != null) {
					for(MeetingDate date : config.getMeetingDates()) {
						Date begin = date.getBegin();
						Date end = date.getEnd();
						allBegin = allBegin == null ? begin : begin.before(allBegin) ? begin : allBegin;
						allEnd = allEnd == null ? end : end.after(allEnd) ? end : allEnd;
					}
				}
				boolean success = provider.updateClassroom(roomId, courseNode.getShortTitle(), courseNode.getLongTitle(), allBegin, allEnd, config);
				if(success) {
					getWindowControl().setInfo(translate("success.update.room"));
				} else {
					getWindowControl().setError(translate("error.update.room"));
				}
			}
		} else if(event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
			// something has changed, maybe the title or description, thus ask to update
			if(provider.existsClassroom(roomId, config)) {
				removeAsListenerAndDispose(yesNoUpdate);
				yesNoUpdate = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(), translate("sync.meeting.title"), translate("sync.meeting.text"));
				listenTo(yesNoUpdate);
				yesNoUpdate.activate();
			}
		}
	}
	
	private void reset(UserRequest ureq) {
		removeAsListenerAndDispose(editForm);
		removeAsListenerAndDispose(configCtr);
		// prepare new edit view
		String providerId = selForm.getSelectedProvider();
		provider = VCProviderFactory.createProvider(providerId);
		config = provider.createNewConfiguration();
		// create room if configured to do it immediately
		if(config.isCreateMeetingImmediately()) {
			// here, the config is empty in any case, thus there are no start and end dates
			provider.createClassroom(roomId, courseNode.getShortName(), courseNode.getLongTitle(), null, null, config);
		}
		editForm = new VCEditForm(ureq, getWindowControl(), provider.getTemplates(), (DefaultVCConfiguration) config);
		listenTo(editForm);
		editVc.put("editForm", editForm.getInitialComponent());
		configCtr = provider.createConfigController(ureq, getWindowControl(), roomId, config);
		listenTo(configCtr);
		editVc.put("configCtr", configCtr.getInitialComponent());
		editVc.setDirty(true);
		// save the minimal config
		courseNode.getModuleConfiguration().set(VCCourseNode.CONF_VC_CONFIGURATION, config);
		courseNode.getModuleConfiguration().setStringValue(VCCourseNode.CONF_PROVIDER_ID, config.getProviderId());
		
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY),
				accessibilityCondContr.getWrappedDefaultAccessConditionVC(translate("condition.accessibility.title")));
		tabbedPane.addTab(translate(PANE_TAB_VCCONFIG), editVc);

	}

	public static boolean isConfigValid(ModuleConfiguration moduleConfig) {
		List<MeetingDate> dateList = (List<MeetingDate>) moduleConfig.get(VCCourseNode.CONF_VC_CONFIGURATION);
		if (dateList != null) {
			for (MeetingDate date : dateList) {
				if (date.getTitle().isEmpty() || date.getDescription().isEmpty()) { return false; }

			}
			return true;
		}
		return false;
	}
}
//</OLATCE-103>