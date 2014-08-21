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
package org.olat.core.commons.controllers.impressum;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.olat.core.commons.controllers.impressum.ImpressumModule.Position;
import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImpressumAdminController extends FormBasicController {
	
	private static final String[] positionKeys = new String[]{ Position.top.name(), Position.footer.name() };
	
	private SingleSelection positionEl;

	private CloseableModalController cmc;
	private HTMLEditorController editorCtrl;
	
	private final VFSContainer impressumDir;
	private final VFSContainer termsOfUseDir;
	
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private ImpressumModule impressumModule;
	
	public ImpressumAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		impressumDir = new LocalFolderImpl(impressumModule.getImpressumDirectory());
		termsOfUseDir = new LocalFolderImpl(impressumModule.getTermsOfUseDirectory());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("menu.impressum");
		
		String[] positionValues = new String[]{ translate("position.top"), translate("position.footer") };
		positionEl = uifactory.addDropdownSingleselect("position", "position", formLayout, positionKeys, positionValues, null);
		positionEl.addActionListener(FormEvent.ONCHANGE);
		if(impressumModule.getPosition() != null) {
			switch(impressumModule.getPosition()) {
				case top: positionEl.select(positionKeys[0], true); break;
				case footer: positionEl.select(positionKeys[1], true); break;
			}
		}
		
		FormLayoutContainer impressum = FormLayoutContainer.createButtonLayout("impressums", getTranslator());
		impressum.setLabel("impressum.file", null);
		formLayout.add(impressum);
		
		for(String lang:I18nModule.getEnabledLanguageKeys()) {
			FormLink link = uifactory.addFormLink("impressum." + lang, "impressum", getTranslated(lang), "impressum.file", impressum, Link.BUTTON | Link.NONTRANSLATED);
			link.setLabel(null, null);
			String filePath = "index_" + lang + ".html";
			if(checkContent(impressumDir.resolve(filePath))) {
				link.setIconLeftCSS("o_icon o_icon_check");
			}
			link.setUserObject(lang);
		}
		
		FormLayoutContainer cont = FormLayoutContainer.createButtonLayout("terms", getTranslator());
		cont.setLabel("termofuse.file", null);
		formLayout.add(cont);
		
		for(String lang:I18nModule.getEnabledLanguageKeys()) {
			
			FormLink link = uifactory.addFormLink("termofuser." + lang, "termsofuse", getTranslated(lang), "termofuse.file", cont, Link.BUTTON | Link.NONTRANSLATED);
			link.setLabel(null, null);
			String filePath = "index_" + lang + ".html";
			if(checkContent(termsOfUseDir.resolve(filePath))) {
				link.setIconLeftCSS("o_icon o_icon_check");
			}
			link.setUserObject(lang);
		}
	}
	
	private String getTranslated(String lang) {
		String langName;
		Locale locale = i18nManager.getLocaleOrNull(lang);
		if(locale != null) {
			langName = locale.getDisplayName(getLocale());
		} else {
			langName = lang;
		}
		return langName;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(positionEl == source) {
			if(positionEl.isOneSelected()) {
				String key = positionEl.getSelectedKey();
				impressumModule.setPosition(key);
				getWindowControl().getWindowBackOffice().getWindow().setDirty(true);
				Windows.getWindows(ureq).getChiefController().wishReload(true);
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			String lang = (String)source.getUserObject();
			if("impressum".equals(cmd)) {
				doEdit(ureq, link, impressumDir, lang);
			} else if("termsofuse".equals(cmd)) {
				doEdit(ureq, link, termsOfUseDir, lang);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editorCtrl == source) {
			FormLink link = (FormLink)editorCtrl.getUserObject();
			String cmd = link.getCmd();
			String lang = (String)link.getUserObject();
			String filePath = "index_" + lang + ".html";
			
			boolean exists = false;
			if("impressum".equals(cmd)) {
				exists = checkContent(impressumDir.resolve(filePath));
			} else if("termsofuse".equals(cmd)) {
				exists = checkContent(termsOfUseDir.resolve(filePath));
			}
			
			if(exists) {
				link.setIconLeftCSS("o_icon o_icon_check");
			} else {
				link.setIconLeftCSS(null);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private boolean checkContent(VFSItem file) {
		boolean check = false;
		if(file instanceof VFSLeaf && file.exists() ) {
			if(file instanceof LocalFileImpl) {
				File f = ((LocalFileImpl)file).getBasefile();
				try {
					String content = FileUtils.readFileToString(f);
					content = FilterFactory.getHtmlTagAndDescapingFilter().filter(content);
					if(content.length() > 0) {
						content = content.trim();
					}
					if(content.length() > 0) {
						check = true;
					}
				} catch (IOException e) {
					logError("", e);
				}
			} else {
				check = true;
			}
		}
		return check;
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editorCtrl);
		removeAsListenerAndDispose(cmc);
		editorCtrl = null;
		cmc = null;
	}
	
	private void doEdit(UserRequest ureq, FormLink link, VFSContainer rootDir, String lang) {
		String filePath = "index_" + lang + ".html";
		if(rootDir.resolve(filePath) == null) {
			rootDir.createChildLeaf(filePath);
		}
		editorCtrl =  WysiwygFactory.createWysiwygController(ureq, getWindowControl(), rootDir, filePath, true, false);
		editorCtrl.setUserObject(link);
		listenTo(editorCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", editorCtrl.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
}
