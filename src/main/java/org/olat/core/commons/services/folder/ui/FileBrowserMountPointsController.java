/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.commons.services.folder.ui;

import java.util.List;
import java.util.Locale;

import org.olat.admin.sysinfo.manager.CustomStaticFolderManager;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.vfs.VFSContainer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Apr 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FileBrowserMountPointsController extends BasicController {

	private static final String CMD_OPEN = "open";
	
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackedPanel;

	private FolderSelectionController folderSelectionCtrl;

	private final FileBrowserSelectionMode selectionMode;
	private final String submitButtonText;
	private final IdentityEnvironment identityEnv;
	private int counter = 0;
	
	@Autowired
	private WebDAVModule webdavModule;
	@Autowired
	private CustomStaticFolderManager staticFolderManager;

	protected FileBrowserMountPointsController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackedPanel, FileBrowserSelectionMode selectionMode, String submitButtonText) {
		super(ureq, wControl);
		this.stackedPanel = stackedPanel;
		this.selectionMode = selectionMode;
		this.submitButtonText = submitButtonText;
		this.identityEnv = new IdentityEnvironment(getIdentity(), ureq.getUserSession().getRoles());
		
		mainVC = createVelocityContainer("browser_mega_buttons");
		putInitialPanel(mainVC);
		
		List<Link> links = webdavModule.getWebDAVProviders().values().stream()
				.filter(provider -> !staticFolderManager.getMountPoint().equals(provider.getMountPoint()))
				.filter(provider -> provider.hasAccess(identityEnv))
				.map(provider -> new TranslatedWebDAVProvider(provider, getLocale()))
				.sorted((p1, p2) -> p1.getName().compareToIgnoreCase(p2.getName()))
				.map(this::createLink)
				.toList();
		mainVC.contextPut("links", links);
	}

	private Link createLink(TranslatedWebDAVProvider provider) {
		Link link = LinkFactory.createCustomLink("cont_" + counter++, CMD_OPEN, null, Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, mainVC, this);
		link.setElementCssClass("btn btn-default o_button_mega");
		link.setIconLeftCSS("o_icon o_icon-xl " + provider.getProvider().getIconCss());
		String text = "<div class=\"o_mega_headline\">" + provider.getName() + "</div>";
		text += "<div class=\"o_mega_subline\">" + provider.getDescription() + "</div>";
		link.setCustomDisplayText(text);
		link.setUserObject(provider.getProvider());
		return link;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			String command = link.getCommand();
			if (CMD_OPEN.equals(command)) {
				if (link.getUserObject() instanceof WebDAVProvider provider) {
					doOpen(ureq, provider);
				}
			}
		}
	}
	
	private void doOpen(UserRequest ureq, WebDAVProvider provider) {
		VFSContainer vfsContainer = provider.getContainer(identityEnv);
		folderSelectionCtrl = new FolderSelectionController(ureq, getWindowControl(), stackedPanel, vfsContainer,
				selectionMode, submitButtonText);
		listenTo(folderSelectionCtrl);
		
		String providerName = provider.getName(getLocale());
		stackedPanel.pushController(providerName, folderSelectionCtrl);
		
		fireEvent(ureq, new FileBrowserTitleEvent(providerName));
		fireEvent(ureq, new FileBrowserPushEvent());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == folderSelectionCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	public static final class TranslatedWebDAVProvider {
		
		private final WebDAVProvider provider;
		private final String name;
		private final String description;
		
		public TranslatedWebDAVProvider(WebDAVProvider provider, Locale locale) {
			this.provider = provider;
			this.name = provider.getName(locale);
			this.description = provider.getDescription(locale);
		}

		public WebDAVProvider getProvider() {
			return provider;
		}
		
		public String getName() {
			return name;
		}
		
		public String getDescription() {
			return description;
		}
		
	}

}
