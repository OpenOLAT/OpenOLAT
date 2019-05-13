/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.ims.qti.editor;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti.container.qtielements.RenderInstructions;
import org.olat.ims.qti.editor.beecom.objects.Item;

/**
 * Initial Date: Oct 21, 2004 <br>
 * 
 * @author mike
 */
public class ItemPreviewController extends DefaultController implements ControllerEventListener {
	private static final Logger log = Tracing.createLoggerFor(ItemPreviewController.class);
	private static final String VC_ROOT = Util.getPackageVelocityRoot(ItemPreviewController.class);

	private VelocityContainer main;
	private SimpleStackedPanel mainPanel;
	
	private final Item item;
	private final String mediaBaseUrl;
	private RenderInstructions renderInstructions;

	/**
	 * @param item
	 * @param qtiPackage
	 * @param translator
	 */
	public ItemPreviewController(WindowControl wControl, Item item, QTIEditorPackage qtiPackage, Translator translator) {
		this(wControl, item, qtiPackage.getMediaBaseURL(), translator);
	}

	public ItemPreviewController(WindowControl wControl, Item item, String mediaBaseUrl, Translator translator) {
		super(wControl);
		this.item = item;
		this.mediaBaseUrl = mediaBaseUrl;
		renderInstructions = new RenderInstructions();
		if(mediaBaseUrl != null && !mediaBaseUrl.startsWith("http")) {
			mediaBaseUrl = Settings.createServerURI() + mediaBaseUrl;
		}
		renderInstructions.put(RenderInstructions.KEY_STATICS_PATH, mediaBaseUrl + "/");
		renderInstructions.put(RenderInstructions.KEY_LOCALE, translator.getLocale());
		renderInstructions.put(RenderInstructions.KEY_RENDER_TITLE, Boolean.TRUE);
		
		main = new VelocityContainer("vcItemPreview", VC_ROOT + "/tab_itemPreview.html", translator, this);
		main.contextPut("itemPreview", getQuestionPreview(item));
		mainPanel = new SimpleStackedPanel("itemPreviewPanel");
		mainPanel.setContent(main);
		setInitialComponent(mainPanel);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof TabbedPaneChangedEvent) {
			TabbedPaneChangedEvent tpcEvent = (TabbedPaneChangedEvent) event;
			if (tpcEvent.getNewComponent() == mainPanel) {
				main.contextPut("itemPreview", getQuestionPreview(item));
			}
		}
	}

	private String getQuestionPreview(Item theItem) {
		try {
			Element el = DocumentFactory.getInstance().createElement("dummy");
			theItem.addToElement(el);
			StringBuilder sb = new StringBuilder();
			org.olat.ims.qti.container.qtielements.Item foo = new org.olat.ims.qti.container.qtielements.Item((Element) el.elements().get(0));
			foo.render(sb, renderInstructions);
			return FilterFactory.getBaseURLToMediaRelativeURLFilter(mediaBaseUrl)
					.filter(sb.toString());
		} catch (Exception e) {
			log.warn("Cannot render preview of an QTI 1.2 item: " + theItem);
			return "ERROR";
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		main = null;
	}

}