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
package org.olat.modules.fo.portfolio;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DownloadComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaRenderingHints;
import org.olat.modules.portfolio.ui.MediaMetadataController;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumMessageMediaController extends BasicController {
	
	@Autowired
	private UserManager userManager;

	public ForumMessageMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));

		VelocityContainer mainVC = createVelocityContainer("messageDetails");
		mainVC.contextPut("text", media.getContent());
				
		String desc = media.getDescription();
		mainVC.contextPut("description", StringHelper.containsNonWhitespace(desc) ? desc : null);
		String title = media.getTitle();
		mainVC.contextPut("title", StringHelper.containsNonWhitespace(title) ? title : null);

		mainVC.contextPut("creationdate", media.getCreationDate());
		mainVC.contextPut("author", userManager.getUserDisplayName(media.getAuthor()));

		if (StringHelper.containsNonWhitespace(media.getStoragePath())) {
			VFSContainer attachmentsContainer = VFSManager.olatRootContainer("/" + media.getStoragePath(), null);
			List<VFSItem> attachments = attachmentsContainer.getItems(new VFSSystemItemFilter());
			List<VFSItem> attachmentsToShow = new ArrayList<>();
			int i=1; //vc-shift!
			for (VFSItem attachment : attachments) {
				if(attachment instanceof VFSLeaf) {
					VFSLeaf file = (VFSLeaf)attachment;
					DownloadComponent downlC = new DownloadComponent("download"+i, file, true,
							file.getName() + " (" + String.valueOf(file.getSize() / 1024) + " KB)", null,
							CSSHelper.createFiletypeIconCssClassFor(file.getName()));
					mainVC.put("download"+i, downlC);
					attachmentsToShow.add(file);
					i++;
				}
			}
			mainVC.contextPut("attachments", attachmentsToShow);
			mainVC.contextPut("hasAttachments", !attachmentsToShow.isEmpty());
			
		} 		
		
		if(hints.isExtendedMetadata()) {
			MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, media);
			listenTo(metaCtrl);
			mainVC.put("meta", metaCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}

