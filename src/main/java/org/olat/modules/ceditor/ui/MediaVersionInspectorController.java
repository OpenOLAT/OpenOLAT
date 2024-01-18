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
package org.olat.modules.ceditor.ui;

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem.TabIndentation;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.MediaSettings;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ChangeVersionPartEvent;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.cemedia.ui.MediaUIHelper.MediaTabComponents;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaVersionInspectorController extends FormBasicController implements PageElementInspectorController {

	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private StaticTextElement nameEl;
	private FormLink mediaCenterLink;
	private SingleSelection versionEl;
	
	private MediaPart mediaPart;
	private PageElementStore<MediaPart> store;
	private List<MediaVersion> versions;
	private final boolean sharedWithMe;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MediaService mediaService;

	public MediaVersionInspectorController(UserRequest ureq, WindowControl wControl, MediaPart mediaPart, PageElementStore<MediaPart> store) {
		super(ureq, wControl, "image_inspector");
		this.store = store;
		this.mediaPart = mediaPart;
		versions = mediaService.getVersions(mediaPart.getMedia());
		sharedWithMe = mediaService.isMediaShared(getIdentity(), mediaPart.getMedia(), null);
		
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		String url = mediaPart.getMediaVersionUrl();
		if (StringHelper.containsNonWhitespace(url)) {
			String title = mediaPart.getMedia().getTitle();
			if (StringHelper.containsNonWhitespace(title)) {
				return "<i class=\"o_icon o_icon_video\"> </i> " + title;
			}
			return translate("inspector." + mediaPart.getType(), url);
		}

		String filename = mediaPart.getStoredData() == null ? "<unkown>" : mediaPart.getStoredData().getRootFilename();
		return translate("inspector." + mediaPart.getType(), filename);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addMediaTab(formLayout, getTranslator());
		addLayoutTab(formLayout, getTranslator());
	}

	private void addMediaTab(FormItemContainer formLayout, Translator translator) {
		MediaTabComponents mediaCmps = MediaUIHelper
				.addMediaVersionTab(formLayout, tabbedPane, mediaPart, versions, uifactory, getTranslator());
		mediaCenterLink = mediaCmps.mediaCenterLink();
		mediaCenterLink.setVisible(sharedWithMe);
		versionEl = mediaCmps.versionEl();
		nameEl = mediaCmps.nameEl();
	}

	private void addLayoutTab(FormItemContainer formLayout, Translator translator) {
		BlockLayoutSettings layoutSettings = getLayoutSettings(getMediaSettings());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, translator, uifactory, layoutSettings, velocity_root);
	}

	private BlockLayoutSettings getLayoutSettings(MediaSettings mediaSettings) {
		if (mediaSettings.getLayoutSettings() != null) {
			return mediaSettings.getLayoutSettings();
		}
		return new BlockLayoutSettings();
	}

	private MediaSettings getMediaSettings() {
		if (mediaPart.getMediaSettings() != null) {
			return mediaPart.getMediaSettings();
		}
		return new MediaSettings();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(versionEl == source) {
			doSetVersion(ureq, versionEl.getSelectedKey());
		} else if(mediaCenterLink == source) {
			doOpenInMediaCenter(ureq);
		} else if(layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenInMediaCenter(UserRequest ureq) {
		String businessPath = MediaUIHelper.toMediaCenterBusinessPath(mediaPart);
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doSetVersion(UserRequest ureq, String versionKey) {
		MediaVersion version = MediaUIHelper.getVersion(versions, versionKey);
		if(version != null) {
			mediaPart.setMediaVersion(version);
			mediaPart = store.savePageElement(mediaPart);
			if(nameEl != null) {
				nameEl.setValue(version.getRootFilename());
			}
			dbInstance.commit();
		}
		fireEvent(ureq, new ChangeVersionPartEvent(mediaPart));
	}

	private void doChangeLayout(UserRequest ureq) {
		MediaSettings mediaSettings = getMediaSettings();

		BlockLayoutSettings layoutSettings = getLayoutSettings(mediaSettings);
		layoutTabComponents.sync(layoutSettings);
		mediaSettings.setLayoutSettings(layoutSettings);

		mediaPart.setMediaSettings(mediaSettings);
		doSaveMediaPart(ureq);

		getInitialComponent().setDirty(true);
	}

	private void doSaveMediaPart(UserRequest ureq) {
		mediaPart = store.savePageElement(mediaPart);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(mediaPart));
	}
}
