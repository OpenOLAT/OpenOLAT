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
package org.olat.modules.video.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.modules.video.VideoModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * administration mainform of videomodule
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoAdminSetController extends FormBasicController  {
	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement enableCourseNodeEl;
	private MultipleSelectionElement enableTranscodingEl;
	private MultipleSelectionElement enable2160SelectionEl;
	private MultipleSelectionElement enable1080SelectionEl;
	private MultipleSelectionElement enable720SelectionEl;
	private MultipleSelectionElement enable480SelectionEl;
	private SingleSelection defaultResEl;
	private DialogBoxController deactivationHintController;

	@Autowired
	private VideoModule videoModule;

	public VideoAdminSetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl,"settings");
		initForm(ureq);
	}

	@Override	
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer mainCont = FormLayoutContainer.createDefaultFormLayout("mainCont", getTranslator());
		mainCont.setFormTitle(translate("admin.config.title"));
		mainCont.setRootForm(mainForm);

		formLayout.add(mainCont);
		String[] enableKeys = new String[]{ "on" };
		String[] enableValues = new String[]{ translate("on") };

		enableEl = uifactory.addCheckboxesHorizontal("admin.config.enable", mainCont, enableKeys, enableValues);
		enableEl.select("on", videoModule.isEnabled());
		enableEl.addActionListener(FormEvent.ONCHANGE);

		enableCourseNodeEl = uifactory.addCheckboxesHorizontal("admin.config.videoNode", mainCont, enableKeys, enableValues);
		enableCourseNodeEl.select("on", videoModule.isCoursenodeEnabled());
		enableCourseNodeEl.setVisible(enableEl.isSelected(0));
		enableCourseNodeEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer transcodingCont = FormLayoutContainer.createDefaultFormLayout("resCont", getTranslator());
		transcodingCont.setFormTitle(translate("admin.menu.transcoding.title"));
		formLayout.add(transcodingCont);

		enableTranscodingEl = uifactory.addCheckboxesHorizontal("admin.config.transcoding", transcodingCont, enableKeys, enableValues);
		enableTranscodingEl.select("on", videoModule.isTranscodingEnabled());
		enableTranscodingEl.setVisible(enableEl.isSelected(0));
		enableTranscodingEl.addActionListener(FormEvent.ONCHANGE);
		
		uifactory.addSpacerElement("spacer", transcodingCont, false);
		
		enable2160SelectionEl = uifactory.addCheckboxesHorizontal("quality.resolution.2160", transcodingCont, enableKeys, enableValues);
		enable2160SelectionEl.addActionListener(FormEvent.ONCHANGE);
		
		enable1080SelectionEl = uifactory.addCheckboxesHorizontal("quality.resolution.1080", transcodingCont, enableKeys, enableValues);
		enable1080SelectionEl.addActionListener(FormEvent.ONCHANGE);

		enable720SelectionEl = uifactory.addCheckboxesHorizontal("quality.resolution.720", transcodingCont, enableKeys, enableValues);
		enable720SelectionEl.addActionListener(FormEvent.ONCHANGE);
		
		enable480SelectionEl = uifactory.addCheckboxesHorizontal("quality.resolution.480", transcodingCont, enableKeys, enableValues);
		enable480SelectionEl.addActionListener(FormEvent.ONCHANGE);
		
		defaultResEl = uifactory.addDropdownSingleselect("quality.resolution.default", transcodingCont, new String[3], new String[3], null);
		defaultResEl.addActionListener(FormEvent.ONCHANGE);
		
		updateResolutionOptions();
	}

	private boolean containsResolution(final int[] array, final int key) {
	    return IntStream.of(array).anyMatch(n -> n == key);
	}
	
	/**
	 * initialize the resolution-GUI-Elements with values from moduleconfig
	 */
	private void updateResolutionOptions(){
		//check if transconding generally is enabled
		boolean transcodingEnabled = enableTranscodingEl.isSelected(0);
		enable2160SelectionEl.setVisible(transcodingEnabled);
		enable1080SelectionEl.setVisible(transcodingEnabled);
		enable720SelectionEl.setVisible(transcodingEnabled);
		enable480SelectionEl.setVisible(transcodingEnabled);
		defaultResEl.setVisible(transcodingEnabled);
		//get active resolutions from moduleconfig
		int[] resolutions = videoModule.getTranscodingResolutions();
		enable2160SelectionEl.select("on", containsResolution(resolutions, 2160));
		enable1080SelectionEl.select("on", containsResolution(resolutions, 1080));
		enable720SelectionEl.select("on", containsResolution(resolutions, 720));
		enable480SelectionEl.select("on", containsResolution(resolutions, 480));
		updateDefaultResOptions(videoModule.getPreferredDefaultResolution());
	}
	
	private void updateDefaultResOptions(int defRes){
		int[] resolutions = videoModule.getTranscodingResolutions();
		if(!containsResolution(resolutions, defRes)){
			defRes = resolutions[0];
			videoModule.setPreferedTranscodingResolution(defRes);
		}
		if(resolutions.length != 0){
			String[] dropdown = new String[resolutions.length];
			for(int x=0; x< resolutions.length; x++){
				dropdown[x] = Integer.toString(resolutions[x]);
			}
			defaultResEl.setKeysAndValues(dropdown, dropdown, null);
			defaultResEl.select(Integer.toString(defRes), true);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == deactivationHintController) {
			if (event == Event.CANCELLED_EVENT) {
			} else {
				if (DialogBoxUIFactory.isYesEvent(event)) {
					videoModule.setTranscodingEnabled(false);
					enableTranscodingEl.select("on", false);
					updateResolutionOptions();
				} else {
					MultipleSelectionElement el = (MultipleSelectionElement) deactivationHintController.getUserObject();
					el.select("on", true);
				}
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		//update config with values from UI
		if(source == enableEl){
			videoModule.setEnabled(enableEl.isSelected(0));
			enableCourseNodeEl.setVisible(enableEl.isSelected(0));
			enableTranscodingEl.setVisible(enableEl.isSelected(0));
			enableTranscodingEl.select("on", videoModule.isCoursenodeEnabled());
			enableCourseNodeEl.select("on", videoModule.isCoursenodeEnabled());
			updateResolutionOptions();
		}
		if(source == enableCourseNodeEl){
			videoModule.setCoursenodeEnabled(enableCourseNodeEl.isSelected(0));
		}
		if(source == enableTranscodingEl){
			videoModule.setTranscodingEnabled(enableTranscodingEl.isSelected(0));
			updateResolutionOptions();
		}

		if(source == enable2160SelectionEl || source == enable1080SelectionEl || source == enable720SelectionEl || source == enable480SelectionEl) {
			//update config with values from gui
			List<Integer> resolutions = new ArrayList<>();
			if(enable2160SelectionEl.isSelected(0)) resolutions.add(2160);
			if(enable1080SelectionEl.isSelected(0)) resolutions.add(1080);
			if(enable720SelectionEl.isSelected(0)) resolutions.add(720);
			if(enable480SelectionEl.isSelected(0)) resolutions.add(480);
			if(resolutions.size() <= 0){
				deactivationHintController = activateYesNoDialog(ureq, translate("admin.config.hint.title"), translate("admin.config.hint"), deactivationHintController);
				deactivationHintController.setUserObject(source);
				return;
			}
			//translate the list to an int[]-Array
		    int[] ret = new int[resolutions.size()];
		    for (int i=0; i < ret.length; i++)
		    {
		        ret[i] = resolutions.get(i).intValue();
		    }
		    videoModule.setTranscodingResolutions(ret);
		    updateResolutionOptions();
		}
		
		if(source == defaultResEl) {
			String defRes = defaultResEl.getSelectedKey();
			Integer defaultResValue;
			try {
				defaultResValue = Integer.valueOf(defRes);
				videoModule.setPreferedTranscodingResolution(defaultResValue);
				updateDefaultResOptions(defaultResValue);
			} catch (NumberFormatException e) {
				logError("Cannot parse default resolution from form::" + defRes, e);
			}

		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {

	}
}
