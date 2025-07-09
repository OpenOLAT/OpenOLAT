package org.olat.course.nodes.tu;

import org.olat.admin.privacy.PrivacyAdminController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TUAdminConfigurationController extends FormBasicController {
	
	private static final String ON_KEY = "on";

	private FormToggle enableEl;
	private StaticTextElement tunnelInfosEl;
	private MultipleSelectionElement tunnelEl;

	@Autowired
	private TUModule tuModule;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public TUAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(PrivacyAdminController.class, ureq.getLocale()));
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.menu.title");
		this.setFormInfo("admin.tunnel.descr");
		
		enableEl = uifactory.addToggleButton("enabled", "enabled", translate("on"), translate("off"), formLayout);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.toggle(tuModule.isEnabled());
		
		String infos = "<p class='o_info_with_icon'><strong>" + translate("tunnel.title") + "</strong><br>" + translate("tunnel.desc") + "</p>";
		tunnelInfosEl = uifactory.addStaticTextElement("cbb.infos", null, infos, formLayout);
		tunnelInfosEl.setDomWrapperElement(DomWrapperElement.div);
		
		SelectionValues onKP = new SelectionValues();
		onKP.add(SelectionValues.entry(ON_KEY, ""));
		tunnelEl = uifactory.addCheckboxesHorizontal("tunnel.cbb", formLayout, onKP.keys(), onKP.values());
		tunnelEl.select(ON_KEY, "enabled".equals(securityModule.getUserInfosTunnelCourseBuildingBlock()));
		tunnelEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isOn();
		tunnelEl.setVisible(enabled);
		tunnelInfosEl.setVisible(enabled);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			tuModule.setEnabled(enableEl.isOn());
			updateUI();
		} else if (source == tunnelEl) {
			boolean headerEnabled = tunnelEl.isAtLeastSelected(1);
			securityModule.setUserInfosTunnelCourseBuildingBlock(headerEnabled ? "enabled" : "disabled");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}
