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
package org.olat.ims.qti;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.editor.ItemPreviewController;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.parser.ParserManager;
import org.olat.ims.resources.IMSEntityResolver;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12PreviewController extends BasicController {
	
	private final Panel mainPanel;
	private final VelocityContainer mainVC;
	private ItemPreviewController previewCtrl;
	private QTI12MetadataController metadataCtrl;
	
	private final QPoolService qpoolService;

	public QTI12PreviewController(UserRequest ureq, WindowControl wControl, QuestionItem qitem) {
		super(ureq, wControl);
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);

		mainVC = createVelocityContainer("qti_preview");
		mainPanel = new Panel("qti12preview");
		
		VFSLeaf leaf = qpoolService.getRootFile(qitem);
		if(leaf == null) {
			//no data to preview
		} else {
			Item item = readItemXml(leaf);
			if(item != null) {
				Translator translator = Util.createPackageTranslator(QTIEditorPackage.class, getLocale());
				VFSContainer directory = qpoolService.getRootDirectory(qitem);
				String mapperUrl = registerMapper(ureq, new QItemDirectoryMapper(directory));
				previewCtrl = new ItemPreviewController(wControl, item, mapperUrl, translator);
				listenTo(previewCtrl);
				mainPanel.setContent(previewCtrl.getInitialComponent());
				
				metadataCtrl = new QTI12MetadataController(ureq, getWindowControl(), item);
				listenTo(metadataCtrl);
				mainVC.put("metadatas", metadataCtrl.getInitialComponent());
			}
		}
		
		mainVC.put("preview", mainPanel);
		putInitialPanel(mainVC);
	}
	
	private Item readItemXml(VFSLeaf leaf) {
		Document doc = null;
		try {
			InputStream is = leaf.getInputStream();
			XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
			doc = xmlParser.parse(is, false);
			
			Element item = (Element)doc.selectSingleNode("questestinterop/item");
		  ParserManager parser = new ParserManager();
		  Item qtiItem = (Item)parser.parse(item);

			is.close();
			return qtiItem;
		} catch (Exception e) {
			logError("", e);
			return null;
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private class QItemDirectoryMapper implements Mapper {
		private final VFSContainer itemBaseContainer;
		
		private QItemDirectoryMapper(VFSContainer container) {
			itemBaseContainer = container;
		}
		
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSItem vfsItem = itemBaseContainer.resolve(relPath);
			MediaResource mr;
			if (vfsItem == null || !(vfsItem instanceof VFSLeaf)) {
				mr = new NotFoundMediaResource(relPath);
			} else {
				mr = new VFSMediaResource((VFSLeaf) vfsItem);
			}
			return mr;
		}
	}
}
