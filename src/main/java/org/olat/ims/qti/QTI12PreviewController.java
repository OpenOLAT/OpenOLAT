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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.xml.XMLParser;
import org.olat.ims.qti.editor.ItemPreviewController;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.parser.ParserManager;
import org.olat.ims.resources.IMSEntityResolver;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12PreviewController extends BasicController {
	
	private final Panel mainPanel;
	private ItemPreviewController previewCtrl;

	public QTI12PreviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainPanel = new Panel("qti12preview");
		
		Item item = readItemXml();
		if(item != null) {
			Translator translator = Util.createPackageTranslator(QTIEditorPackage.class, getLocale());
			previewCtrl = new ItemPreviewController(wControl, item, "/Users/srosse", translator);
			listenTo(previewCtrl);
			mainPanel.setContent(previewCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainPanel);
	}
	
	private Item readItemXml() {

		XMLParser xmlParser = new XMLParser(new IMSEntityResolver());
		Document doc = null;
		try {
			File itemXml = new File("/Users/srosse/Desktop/mchc_i_001.xml");
			InputStream is = new FileInputStream(itemXml);
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
}
