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
package org.olat.modules.portfolio.ui.component;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.ui.wizard.CollectArtefactController;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaCollectorComponent extends AbstractComponent implements ControllerEventListener {
	
	private CloseableModalController cmc;
	private CollectArtefactController collectorCtrl;
	
	private static final ComponentRenderer RENDERER = new MediaCollectorComponentRenderer();

	private FormItem item;
	private final WindowControl wControl;
	
	private final Object media;
	private final String businessPath;
	private final MediaHandler handler;
	
	public MediaCollectorComponent(String name, WindowControl wControl, Object media, MediaHandler handler, String businessPath) {
		super(name);
		this.wControl = wControl;
		this.media = media;
		this.handler = handler;
		this.businessPath = businessPath;
	}
	
	public FormItem getItem() {
		return item;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		if("pfcollect".equalsIgnoreCase(cmd)) {
			doOpenCollector(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(collectorCtrl == source) {
			
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		if(collectorCtrl != null) {
			//collectorCtrl.addControllerListener(el);
			collectorCtrl = null;
		}
		if(cmc != null) {
			cmc = null;
		}
		
	}

	private void doOpenCollector(UserRequest ureq) {
		collectorCtrl = new CollectArtefactController(ureq, wControl, media, handler, businessPath);
		collectorCtrl.addControllerListener(this);
		
		String title = "Media";
		cmc = new CloseableModalController(wControl, null, collectorCtrl.getInitialComponent(), true, title, true);
		cmc.addControllerListener(this);
		cmc.activate();
		
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
