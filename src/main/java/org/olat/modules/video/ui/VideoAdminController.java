package org.olat.modules.video.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.video.VideoModule;
import org.springframework.beans.factory.annotation.Autowired;

public class VideoAdminController extends FormBasicController  {

	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement enableCourseNodeEl;
	private MultipleSelectionElement enableTranscodingEl;

	@Autowired
	private VideoModule videoModule;

	public VideoAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.config.title");

		String[] enableKeys = new String[]{ "on" };
		String[] enableValues = new String[]{ translate("on") };

		enableEl = uifactory.addCheckboxesHorizontal("admin.config.enable", formLayout, enableKeys, enableValues);
		enableEl.select("on", videoModule.isEnabled());
		enableEl.addActionListener(FormEvent.ONCHANGE);

		enableCourseNodeEl = uifactory.addCheckboxesHorizontal("admin.config.videoNode", formLayout, enableKeys, enableValues);
		enableCourseNodeEl.select("on", videoModule.isCoursenodeEnabled());
		enableCourseNodeEl.setVisible(enableEl.isSelected(0));
		enableCourseNodeEl.addActionListener(FormEvent.ONCHANGE);

		enableTranscodingEl = uifactory.addCheckboxesHorizontal("admin.config.transcoding", formLayout, enableKeys, enableValues);
		enableTranscodingEl.select("on", videoModule.isTranscodingEnabled());
		enableTranscodingEl.setVisible(enableEl.isSelected(0));
		enableTranscodingEl.addActionListener(FormEvent.ONCHANGE);

	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enableEl){
			videoModule.setEnabled(enableEl.isSelected(0));
			enableCourseNodeEl.setVisible(enableEl.isSelected(0));
			enableTranscodingEl.setVisible(enableEl.isSelected(0));
			enableCourseNodeEl.select("on", videoModule.isCoursenodeEnabled());
			enableTranscodingEl.select("on", videoModule.isTranscodingEnabled());
		}
		if(source == enableCourseNodeEl){
			videoModule.setCoursenodeEnabled(enableCourseNodeEl.isSelected(0));
		}
		if(source == enableTranscodingEl){
			videoModule.setTranscodingEnabled(enableTranscodingEl.isSelected(0));
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}
}