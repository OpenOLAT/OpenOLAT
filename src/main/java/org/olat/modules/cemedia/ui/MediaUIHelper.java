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
package org.olat.modules.cemedia.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.model.jpa.MediaPart;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.MediaUsage;

/**
 * 
 * Initial date: 15 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaUIHelper {
	
	private MediaUIHelper() {
		//
	}
	
	public static boolean showBusinessPath(String businessPath) {
		return StringHelper.containsNonWhitespace(businessPath) && !businessPath.contains("[MediaCenter:0]");
	}
	
	public static String toMediaCenterBusinessPath(ImageElement imageElement) {
		String businessPath = "[HomeSite:0][MediaCenter:0]";
		if(imageElement instanceof MediaPart part) {
			businessPath += "[Media:" + part.getMedia().getKey() + "]";
		}
		return businessPath;
	}
	
	public static MediaTabComponents addMediaVersionTab(FormItemContainer formLayout, TabbedPaneItem tabbedPane,
			MediaPart mediaPart, List<MediaVersion> versions,
			FormUIFactory uifactory, Translator translator) {
		
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("versions", translator);
		formLayout.add(layoutCont);
		tabbedPane.addTab(translator.translate("tab.media"), layoutCont);
		
		StaticTextElement nameEl = null;
		SingleSelection versionEl = null;
		if(mediaPart.getStoredData() != null) {
			String filename = mediaPart.getStoredData().getRootFilename();
			if(StringHelper.containsNonWhitespace(filename)) {
				nameEl = uifactory.addStaticTextElement("media.name", "media.name", filename, layoutCont);
			}
			
			if(versions != null && !versions.isEmpty()) {
				List<MediaVersion> versionList = new ArrayList<>(versions);
				
				String selectedKey = null;
				SelectionValues versionsVK = new SelectionValues();
				for(int i=0; i<versionList.size(); i++) {
					MediaVersion version = versionList.get(i);
					String value;
					if(i == 0) {
						value = translator.translate("last.version");
					} else {
						value = translator.translate("version", version.getVersionName());
					}
					
					String versionKey = version.getKey().toString();
					versionsVK.add(SelectionValues.entry(versionKey, value));
					if(mediaPart.getStoredData().equals(version)) {
						selectedKey = versionKey;
					}
				}
				
				versionEl = uifactory.addDropdownSingleselect("image.versions", layoutCont,
						versionsVK.keys(), versionsVK.values());
				versionEl.addActionListener(FormEvent.ONCHANGE);
				if(selectedKey == null && !versionsVK.isEmpty()) {
					versionEl.select(versionsVK.keys()[0], true);
				} else if(selectedKey != null && versionsVK.containsKey(selectedKey)) {
					versionEl.select(selectedKey, true);
				}
			}
		}
		
		FormLink mediaCenterLink = null;
		if(mediaPart.getMedia() != null) {
			mediaCenterLink = uifactory.addFormLink("goto.media.center", layoutCont, Link.LINK);
			mediaCenterLink.setIconLeftCSS("o_icon o_icon_external_link");
		}
		
		return new MediaTabComponents(nameEl, versionEl, mediaCenterLink);
	}
	
	public static MediaVersion getVersion(List<MediaVersion> versions, String selectedKey) {
		for(MediaVersion version:versions) {
			if(selectedKey.equals(version.getKey().toString())) {
				return version;
			}
		}
		return null;
	}
	
	public record MediaTabComponents (StaticTextElement nameEl, SingleSelection versionEl, FormLink mediaCenterLink) {
		//
	}
	
	public static void open(UserRequest ureq, WindowControl wControl, MediaUsage mediaUsage) {
		open(ureq, wControl, mediaUsage.binderKey(), mediaUsage.pageKey(), mediaUsage.repositoryEntryKey(), mediaUsage.subIdent());
	}
	
	public static String businessPath(Long binderKey, Long pageKey, Long repositoryEntryKey, String subIdent) {
		String businessPath = null;
		if(binderKey != null) {
			businessPath = "[HomeSite:0][PortfolioV2:0][MyBinders:0][Binder:" + binderKey + "][Entries:0][Entry:" + pageKey + "]";
		} else if(repositoryEntryKey != null) {
			businessPath = "[RepositoryEntry:" + repositoryEntryKey + "]";
			if(StringHelper.containsNonWhitespace(subIdent)) {
				businessPath += "[CourseNode:" + subIdent + "]";
			}
		} else if(pageKey != null) {
			//http://localhost:8081/auth/HomeSite/720898/PortfolioV2/0/MyPages/0/Entry/89
			businessPath = "[HomeSite:0][PortfolioV2:0][MyPages:0][Entry:" + pageKey + "]";
		} else  {
			businessPath = "[HomeSite:0][PortfolioV2:0]";
		}
		return businessPath;
	}
	
	public static void open(UserRequest ureq, WindowControl wControl, Long binderKey, Long pageKey, Long repositoryEntryKey, String subIdent) {
		String businessPath = businessPath(binderKey, pageKey, repositoryEntryKey, subIdent);
		if(StringHelper.containsNonWhitespace(businessPath)) {
			NewControllerFactory.getInstance().launch(businessPath, ureq, wControl);
		}
	}
}
