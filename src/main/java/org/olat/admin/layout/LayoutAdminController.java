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
package org.olat.admin.layout;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.olat.admin.SystemAdminMainController;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.GUISettings;
import org.olat.core.helpers.Settings;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <h3>Description:</h3>
 * Admin workflow to configure the application layout
 * <p>
 * Initial Date: 31.03.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class LayoutAdminController extends FormBasicController {
	
	private static final Set<String> imageMimeTypes = new HashSet<>();
	static {
		imageMimeTypes.add("image/png");
	}
	private FormLink deleteLogo;
	private TextElement logoAltEl, logoUrlEl;
	private SingleSelection logoLinkTypeEl;
	private TextElement footerLine, footerUrl;
	private SingleSelection themeSelection;
	private FormLink forceThemeReload;	
	private FileElement logoUpload;
	
	private static final String[] logoUrlTypeKeys = new String[]{ LogoURLType.landingpage.name(), LogoURLType.custom.name() };

	@Autowired
	private GUISettings guiSettings;
	@Autowired
	private LayoutModule layoutModule;
	@Autowired
	private CoordinatorManager coordinatorManager;
	
	public LayoutAdminController(UserRequest ureq, WindowControl wControl) {
		// use admin package fallback translator to display warn message about not
		// saving the data (see comment in formInnerEvent method)
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(SystemAdminMainController.class, getLocale(), getTranslator()));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//themes
		FormLayoutContainer themeCont = FormLayoutContainer.createDefaultFormLayout("themeAdminFormContainer", getTranslator());
		formLayout.add(themeCont);
		themeCont.setFormTitle(translate("layout.title"));
		themeCont.setFormDescription(translate("layout.intro"));
		
		String[] keys = getThemes();
		String enabledTheme = guiSettings.getGuiThemeIdentifyer();
		themeSelection = uifactory.addDropdownSingleselect("themeSelection", "form.theme", themeCont, keys, keys, null);
		// select current theme if available but don't break on unavailable theme
		for (String theme : keys) {
			if (theme.equals(enabledTheme)) {
				themeSelection.select(enabledTheme, true);				
				break;
			}
		}
		themeSelection.addActionListener(FormEvent.ONCHANGE);
		forceThemeReload = uifactory.addFormLink("forceThemeReload", "form.theme.forceReload", null, themeCont, Link.BUTTON_SMALL);
		forceThemeReload.setExampleKey("form.theme.forceReload.help", null);
		forceThemeReload.setIconLeftCSS("o_icon o_icon_refresh");

		//logo
		FormLayoutContainer logoCont = FormLayoutContainer.createDefaultFormLayout("logo", getTranslator());
		formLayout.add(logoCont);
		logoCont.setFormTitle(translate("customizing.logo"));
		
		File logo = layoutModule.getLogo();
		boolean hasLogo = logo != null && logo.exists();
		
		deleteLogo = uifactory.addFormLink("deleteimg", "delete", null, logoCont, Link.BUTTON);
		deleteLogo.setVisible(hasLogo);
		
		logoUpload = uifactory.addFileElement(getWindowControl(), getIdentity(), "customizing.logo", "customizing.logo", logoCont);
		logoUpload.setMaxUploadSizeKB(1024, null, null);
		logoUpload.setPreview(ureq.getUserSession(), true);
		logoUpload.addActionListener(FormEvent.ONCHANGE);
		if(hasLogo) {
			logoUpload.setPreview(ureq.getUserSession(), true);
			logoUpload.setInitialFile(logo);
		}
		logoUpload.limitToMimeType(imageMimeTypes, "customizing.img.error", null);
		
		String[] logoUrlTypeValues = new String[]{
			translate("customizing.logo.link.landingpage"),
			translate("customizing.logo.link.custom")
		};
		logoLinkTypeEl = uifactory.addDropdownSingleselect("logo.url.type", "customizing.logo.link.type", logoCont,
				logoUrlTypeKeys, logoUrlTypeValues, null);
		logoLinkTypeEl.addActionListener(FormEvent.ONCHANGE);
		String linkType = layoutModule.getLogoLinkType();
		if(StringHelper.containsNonWhitespace(linkType)) {
			for(String key:logoUrlTypeKeys) {
				if(key.equals(linkType)) {
					logoLinkTypeEl.select(key, true);
				}
			}
		}
		
		String customUrl = layoutModule.getLogoLinkUri();
		if(StringHelper.containsNonWhitespace(customUrl) && !StringHelper.containsNonWhitespace(linkType)) {
			logoLinkTypeEl.select(LogoURLType.custom.name(), true);
		}
		
		logoUrlEl = uifactory.addTextElement("linkUrl", "linkUrl.description", 256, customUrl, logoCont);
		logoUrlEl.setPlaceholderKey("linkUrl.default", null);
		boolean custom = logoLinkTypeEl.isOneSelected() && "custom".equals(logoLinkTypeEl.getSelectedKey());
		logoUrlEl.setVisible(custom);
		
		String oldLogoAlt = layoutModule.getLogoAlt();
		logoAltEl = uifactory.addTextElement("logoAlt", "logoAlt.description", 256, oldLogoAlt, logoCont);
		logoAltEl.setPlaceholderKey("logoAlt.default", null);

		//footer
		FormLayoutContainer footerCont = FormLayoutContainer.createDefaultFormLayout("customizing", getTranslator());
		formLayout.add(footerCont);
		footerCont.setFormTitle(translate("customizing.settings"));
		footerCont.setFormDescription(translate("customizing.settings.desc"));

		String oldFooterUrl = layoutModule.getFooterLinkUri();
		footerUrl = uifactory.addTextElement("footerUrl", "footerUrl.description", 256, oldFooterUrl, footerCont);
		footerUrl.setPlaceholderKey("footerUrl.default", null);
		
		String oldFooterLine = layoutModule.getFooterLine();
		footerLine = uifactory.addTextAreaElement("footerLine", "footerLine.description", -1, 3, 50, true, false, oldFooterLine, footerCont);
		footerLine.setPlaceholderKey("footerLine.default", null);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		footerCont.add(buttonsCont);
		uifactory.addFormSubmitButton("save", "submit.save", buttonsCont);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateUrl(logoUrlEl);
		allOk &= validateUrl(footerUrl);
		return allOk;
	}
	
	private boolean validateUrl(TextElement el) {
		boolean allOk = true;
		String value = el.getValue();
		if (StringHelper.containsNonWhitespace(value)) {
			try {
				URL url = new URL(value);
				allOk &= StringHelper.containsNonWhitespace(url.getHost());
			} catch (MalformedURLException e) {
				el.setErrorKey("linkUrl.invalid", null);
				showError("linkUrl.invalid");
				allOk &= false;
			}
		}
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(logoUpload == source) {
			if (logoUpload.isUploadSuccess()) {
				logoUpload.clearError();
				if (logoUpload.validate()) {
					layoutModule.removeLogo();
					File destinationDir = layoutModule.getLogoDirectory();
					File newLogo = logoUpload.moveUploadFileTo(destinationDir);					
					layoutModule.setLogoFilename(newLogo.getName());
					File normalizedLogo = layoutModule.getLogo();
					logoUpload.setInitialFile(normalizedLogo);
					deleteLogo.setVisible(true);
					getWindowControl().getWindowBackOffice().getChiefController().wishReload(ureq, true);
				} else {
					logoUpload.reset();
				}
			}
		} else if(logoLinkTypeEl == source) {
			boolean custom = logoLinkTypeEl.isOneSelected() && "custom".equals(logoLinkTypeEl.getSelectedKey());
			logoUrlEl.setVisible(custom);
		} else if(deleteLogo == source) {
			layoutModule.removeLogo();
			logoUpload.reset();
			deleteLogo.setVisible(false);
			logoUpload.setInitialFile(null);
			getWindowControl().getWindowBackOffice().getChiefController().wishReload(ureq, true);
			
		} else if(themeSelection == source) {
			// set new theme in Settings
			String newThemeIdentifyer = themeSelection.getSelectedKey();
			guiSettings.setGuiThemeIdentifyer(newThemeIdentifyer);
			// use new theme in current window
			getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
			logAudit("GUI theme changed", newThemeIdentifyer);
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (forceThemeReload == source) {
			StaticMediaDispatcher.forceReloadStaticMediaDelivery();
			// make reloading happen for the admin window right away
			getWindowControl().getWindowBackOffice().getWindow().getGuiTheme().init(themeSelection.getSelectedKey());
			getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(logoLinkTypeEl.isOneSelected()) {
			layoutModule.setLogoLinkType(logoLinkTypeEl.getSelectedKey());
		}
		//Logo-Link URI
		if(logoUrlEl.isVisible()) {
			layoutModule.setLogoLinkUri(logoUrlEl.getValue());
		} else {
			layoutModule.setLogoLinkUri("");
		}
		//Logo Alternative Text
		layoutModule.setLogoAlt(logoAltEl.getValue());
		//FooterLine (large property -> text)
		layoutModule.setFooterLinkUri(footerUrl.getValue());
		layoutModule.setFooterLine(footerLine.getValue());
		
		//reload window for changes to take effect, fire event to footer/header
		getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(new LayoutChangedEvent(LayoutChangedEvent.LAYOUTSETTINGSCHANGED), 
				LayoutModule.layoutCustomizingOResourceable);	
		showInfo("settings.saved");
	}
	
	private String[] getThemes(){
		// get all themes from disc
		String staticAbsPath = WebappHelper.getContextRealPath("/static/themes");
		File themesDir = new File(staticAbsPath);
		if(!themesDir.exists()){
			logWarn("Themes dir not found: "+staticAbsPath, null);
			return new String[0];
		}
		File[] themes = themesDir.listFiles(new ThemesFileNameFilter());
		String[] themesStr = new String[themes.length];
		for (int i = 0; i < themes.length; i++) {
			File theme = themes[i];
			themesStr[i] = theme.getName();
		}
		
		// add custom themes from configuration if available
		File customThemesDir = Settings.getGuiCustomThemePath();
		if (customThemesDir != null) {
			File[] customThemes = customThemesDir.listFiles(new ThemesFileNameFilter());
			String[] customThemesStr = new String[customThemes.length];
			for (int i = 0; i < customThemes.length; i++) {
				File theme = customThemes[i];
				customThemesStr[i] = theme.getName();
			}
			themesStr = (String[]) ArrayUtils.addAll(themesStr, customThemesStr);
			Arrays.sort(themesStr);
		}
		
		return themesStr;
	}
	
	/**
	 * just a simple fileNameFilter that skips OS X .DS_Store , CVS and .sass-cache directories
	 * 
	 * @author strentini
	 */
	private static class ThemesFileNameFilter implements FilenameFilter {
		@Override
		public boolean accept(File dir, String name) {
				// remove files - only accept dirs
				if (!new File(dir, name).isDirectory()) {
					return false;
				}
				// remove unwanted meta-dirs
				if (FileUtils.isMetaFilename(name)) {
					return false;
				}
				return true;
		}
	}
}