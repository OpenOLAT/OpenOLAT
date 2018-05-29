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
package org.olat.modules.webFeed.manager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.DefaultGlobalSettings;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.EmptyURLBuilder;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.velocity.VelocityRenderDecorator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.filters.SystemItemFilter;
import org.olat.modules.portfolio.ui.MediaCenterController;
import org.olat.modules.webFeed.Item;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class FeedUserDataManager implements UserDataExportable {
	
	private static final OLog log = Tracing.createLoggerFor(FeedUserDataManager.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ItemDAO itemDao;
	@Autowired
	private FeedManager feedManager;

	@Override
	public String getExporterID() {
		return "feeds";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		List<Item> items = itemDao.loadItemsByAuthor(identity);
		dbInstance.commitAndCloseSession();
		
		File itemsDirectory = new File(archiveDirectory, "blogspodcasts");
		itemsDirectory.mkdir();
		for(Item item:items) {
			export(item, itemsDirectory, locale);
		}
	}
	
	private void export(Item item, File itemsDirectory, Locale locale) {
		String title = StringHelper.transformDisplayNameToFileSystemName(item.getTitle());
		String name = item.getKey() + "_" + title;
		File itemDir = new File(itemsDirectory, name);
		itemDir.mkdir();

		VFSContainer itemContainer = feedManager.getItemContainer(item);
		List<File> attachments = new ArrayList<>();
		if(itemContainer != null) {
			List<VFSItem> attachmentItems = itemContainer.getItems(new SystemItemFilter());
			for(VFSItem attachmentItem:attachmentItems) {
				FileUtils.copyItemToDir(attachmentItem, itemDir, "Copy blog/podcast files");
				attachments.add(new File(itemDir, attachmentItem.getName()));
			}
		}
		
		File mediaFile = new File(itemDir, "index.html");
		try(OutputStream out = new FileOutputStream(mediaFile)) {
			String content = exportContent(item, attachments, locale);
			out.write(content.getBytes("UTF-8"));
			out.flush();
		} catch(IOException e) {
			log.error("", e);
		}
	}

	private String exportContent(Item item,  List<File> attachments, Locale locale) {
		StringOutput sb = new StringOutput(10000);
		Translator translator = Util.createPackageTranslator(MediaCenterController.class, locale);
		String pagePath = Util.getPackageVelocityRoot(FeedUserDataManager.class) + "/export_item.html";
		VelocityContainer component = new VelocityContainer("html", pagePath, translator, null);
		component.contextPut("item", item);
		component.contextPut("content", item.getContent());
		component.contextPut("attachments", attachments);

		Renderer renderer = Renderer.getInstance(component, translator, new EmptyURLBuilder(), new RenderResult(), new DefaultGlobalSettings());
		try(VelocityRenderDecorator vrdec = new VelocityRenderDecorator(renderer, component, sb)) {
			component.contextPut("r", vrdec);
			renderer.render(sb, component, null);
		} catch(IOException e) {
			log.error("", e);
		}
		return sb.toString();
	}

}
