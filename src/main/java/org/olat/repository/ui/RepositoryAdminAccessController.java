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
package org.olat.repository.ui;

import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.admin.site.ui.SitesAdminController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntryFinishedAccessOptions;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryAdminAccessController extends FormBasicController {
	
	private static final String SITE_COACHING_TOOL = "olatsites_coaching";
	private static final String SITE_MY_COURSES = "olatsites_mycourses";
	
	private FormToggle hintEl;
	private FormLink sitesButton;
	private SingleSelection finishedAccessEl;
	private FormLayoutContainer overviewCont;
	private StaticTextElement myCoursesSiteEl;
	private StaticTextElement coachingToolSiteEl;

	private final SiteDefinition myCoursesSiteDef;
	private final SiteDefinition coachingToolSiteDef;

	@Autowired
	private SiteDefinitions sitesModule;
	@Autowired
	private SiteDefinitions siteDefinitions;
	@Autowired
	private RepositoryModule repositoryModule;
	
	public RepositoryAdminAccessController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(),
				Util.createPackageTranslator(SitesAdminController.class, ureq.getLocale())));
		Map<String,SiteDefinition> siteDefs = sitesModule.getAllSiteDefinitionsList();
		myCoursesSiteDef = siteDefs.get(SITE_MY_COURSES);
		coachingToolSiteDef = siteDefs.get(SITE_COACHING_TOOL);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer accessCont = FormLayoutContainer.createDefaultFormLayout("access", getTranslator());
		accessCont.setFormTitle(translate("admin.access"));
		formLayout.add(accessCont);
		accessCont.setRootForm(mainForm);
		
		hintEl = uifactory.addToggleButton("admin.coaching.hint", "admin.coaching.hint", translate("on"), translate("off"), accessCont);
		hintEl.setHelpTextKey("admin.coaching.hint.help", null);
		hintEl.toggle(repositoryModule.isMyCoursesCoachingToolHint());
		
		String overviewPage = velocity_root + "/repository_admin_overview.html";
		overviewCont = uifactory.addCustomFormLayout("overview", null, overviewPage, accessCont);
		overviewCont.setFormLayout("0_12");
		overviewCont.contextPut("warningSite", "");
		
		myCoursesSiteEl = uifactory.addStaticTextElement("admin.coaching.courses", "admin.coaching.courses", "", overviewCont);
		coachingToolSiteEl = uifactory.addStaticTextElement("admin.coaching.tool", "admin.coaching.tool", "", overviewCont);
		
		sitesButton = uifactory.addFormLink("admin.coaching.sites", overviewCont, Link.BUTTON);
		sitesButton.setGhost(true);

		FormLayoutContainer finishedCont = FormLayoutContainer.createDefaultFormLayout("finished", getTranslator());
		finishedCont.setFormTitle(translate("admin.access.finished.title"));
		formLayout.add(finishedCont);
		finishedCont.setRootForm(mainForm);

		SelectionValues finishedPk = new SelectionValues();
		finishedPk.add(SelectionValues.entry(RepositoryEntryFinishedAccessOptions.readonly.name(),
				translate("admin.access.finished.readonly"),
				translate("admin.access.finished.readonly.descr"), null, null, true));
		finishedPk.add(SelectionValues.entry(RepositoryEntryFinishedAccessOptions.noaccess.name(),
				translate("admin.access.finished.noaccess"),
				translate("admin.access.finished.noaccess.descr"), null, null, true));

		finishedAccessEl = uifactory.addCardSingleSelectHorizontal("admin.access.finished", finishedCont,
				finishedPk.keys(), finishedPk.values(), finishedPk.descriptions(), finishedPk.icons());
		finishedAccessEl.addActionListener(FormEvent.ONCLICK);
		finishedAccessEl.select(repositoryModule.getFinishedAccessDefaultOption().name(), true);
	}
	
	private void updateUI() {
		SiteConfiguration coachingToolConfig = siteDefinitions.getConfigurationSite(coachingToolSiteDef);
		if(!coachingToolSiteDef.getDefaultSiteSecurityCallbackBeanId().equals(coachingToolConfig.getSecurityCallbackBeanId())) {
			String defaultId = coachingToolSiteDef.getDefaultSiteSecurityCallbackBeanId();
			String defaultTitle = translate(defaultId);
			overviewCont.contextPut("warningSite", translate("warning.admin.coaching.site", defaultTitle));
		} else {
			overviewCont.contextRemove("warningSite");
		}
		
		String coachingToolOverview = getOverview(coachingToolSiteDef);
		coachingToolSiteEl.setValue(coachingToolOverview);
		String myCoursesOverview = getOverview(myCoursesSiteDef);
		myCoursesSiteEl.setValue(myCoursesOverview);
	}
	
	private String getOverview(SiteDefinition siteDef) {
		SiteConfiguration config = siteDefinitions.getConfigurationSite(siteDef);
		boolean enabled = siteDef.isEnabled() && config.isEnabled();

		StringBuilder sb = new StringBuilder();
		sb.append(enabled ? translate("admin.access.enabled") : translate("admin.access.disabled"));
		if(StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
			sb.append(" | ").append(translate(config.getSecurityCallbackBeanId()));
		}
		return sb.toString();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(hintEl == source || finishedAccessEl == source) {
			doSaveSettings();
		} else if(sitesButton == source) {
			doOpenSitesSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSaveSettings() {
		repositoryModule.setMyCoursesCoachingToolHint(hintEl.isOn());
		
		RepositoryEntryFinishedAccessOptions finishedOption = RepositoryEntryFinishedAccessOptions.readonly;
		if(finishedAccessEl.isOneSelected()) {
			try {
				finishedOption = RepositoryEntryFinishedAccessOptions.valueOf(finishedAccessEl.getSelectedKey());
			} catch (IllegalArgumentException e) {
				finishedOption = RepositoryEntryFinishedAccessOptions.readonly;
			}
		}
		repositoryModule.setFinishedAccessDefaultOption(finishedOption);
	}
	
	private void doOpenSitesSettings(UserRequest ureq) {
		String businessPath = "[AdminSite:0][coursesites:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
}
