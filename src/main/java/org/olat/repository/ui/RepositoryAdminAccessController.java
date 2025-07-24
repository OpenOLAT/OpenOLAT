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

import java.util.List;
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
	
	private static final String OWNER_COACH_ACCESS_KEY = "ownercoach";
	private static final String COACHING_KEY = "coaching";
	
	private static final String SITE_COACHING_TOOL = "olatsites_coaching";
	private static final String SITE_MY_COURSES = "olatsites_mycourses";
	
	private FormToggle hintEl;
	private FormLink sitesButton;
	private SingleSelection accessEl;
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
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(),
				Util.createPackageTranslator(SitesAdminController.class, ureq.getLocale())));
		Map<String,SiteDefinition> siteDefs = sitesModule.getAllSiteDefinitionsList();
		myCoursesSiteDef = siteDefs.get(SITE_MY_COURSES);
		coachingToolSiteDef = siteDefs.get(SITE_COACHING_TOOL);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.access");
		
		SelectionValues accessPk = new SelectionValues();
		accessPk.add(SelectionValues.entry(OWNER_COACH_ACCESS_KEY, translate("admin.access.coaching.with.owner"),
				translate("admin.access.coaching.with.owner.descr"), null, null, true));
		accessPk.add(SelectionValues.entry(COACHING_KEY, translate("admin.access.coaching.with.tool"),
				translate("admin.access.coaching.with.tool.descr"), null, null, true));
		
		accessEl = uifactory.addCardSingleSelectHorizontal("admin.access.coaching", formLayout,
				accessPk.keys(), accessPk.values(), accessPk.descriptions(), accessPk.icons());
		accessEl.addActionListener(FormEvent.ONCLICK);
		if(repositoryModule.isMyCoursesParticipantsOnly()) {
			accessEl.select(COACHING_KEY, true);
		} else {
			accessEl.select(OWNER_COACH_ACCESS_KEY, true);
		}
		
		hintEl = uifactory.addToggleButton("admin.coaching.hint", "admin.coaching.hint", translate("on"), translate("off"), formLayout);
		hintEl.setHelpTextKey("admin.coaching.hint.help", null);
		hintEl.toggle(repositoryModule.isMyCoursesCoachingToolHint());
		
		String overviewPage = velocity_root + "/repository_admin_overview.html";
		overviewCont = uifactory.addCustomFormLayout("overview", null, overviewPage, formLayout);
		overviewCont.setFormLayout("0_12");
		overviewCont.contextPut("warningSite", "");
		
		myCoursesSiteEl = uifactory.addStaticTextElement("admin.coaching.courses", "admin.coaching.courses", "", overviewCont);
		coachingToolSiteEl = uifactory.addStaticTextElement("admin.coaching.tool", "admin.coaching.tool", "", overviewCont);
		
		sitesButton = uifactory.addFormLink("admin.coaching.sites", overviewCont, Link.BUTTON);
		sitesButton.setGhost(true);
	}
	
	private void updateUI() {
		boolean coachingEnabled = accessEl.isOneSelected() && COACHING_KEY.equals(accessEl.getSelectedKey());
		hintEl.setVisible(coachingEnabled);
		
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
		StringBuilder sb = new StringBuilder();
		sb.append(siteDef.isEnabled() ? translate("admin.access.enabled") : translate("admin.access.disabled"));
		
		SiteConfiguration config = siteDefinitions.getConfigurationSite(siteDef);
		if(StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
			sb.append(" | ").append(translate(config.getSecurityCallbackBeanId()));
		}
		return sb.toString();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(accessEl == source) {
			updateUI();
			doSaveSettings();
		} else if(hintEl == source) {
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
		boolean participantsOnly = accessEl.isOneSelected() && COACHING_KEY.equals(accessEl.getSelectedKey());
		repositoryModule.setMyCoursesParticipantsOnly(participantsOnly);
		boolean showHint = participantsOnly && hintEl.isVisible() && hintEl.isOn();
		repositoryModule.setMyCoursesCoachingToolHint(showHint);
		
		if(participantsOnly) {
			SiteConfiguration coachingToolConfig = siteDefinitions.getConfigurationSite(coachingToolSiteDef);
			List<SiteConfiguration> configs = sitesModule.getSitesConfiguration();
			for(SiteConfiguration config:configs) {
				if(config.getId().equals(coachingToolConfig.getId()) && !config.isEnabled()) {
					config.setEnabled(true);
					sitesModule.setSitesConfiguration(configs);
					break;
				}
			}
		}
	}
	
	private void doOpenSitesSettings(UserRequest ureq) {
		String businessPath = "[AdminSite:0][coursesites:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
}
