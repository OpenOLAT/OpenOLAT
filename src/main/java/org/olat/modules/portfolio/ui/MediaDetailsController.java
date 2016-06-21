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
package org.olat.modules.portfolio.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaDetailsController extends FormBasicController implements Activateable2 {
	
	private Media media;
	private MediaHandler handler;
	
	private Controller mediaCtrl;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public MediaDetailsController(UserRequest ureq, WindowControl wControl, Media media) {
		super(ureq, wControl, "media_details");
		this.media = media;
		handler = portfolioService.getMediaHandler(media.getType());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", StringHelper.escapeHtml(media.getTitle()));
			layoutCont.contextPut("description", StringHelper.xssScan(media.getDescription()));
			layoutCont.contextPut("iconCssClass", handler.getIconCssClass(media));
			
			mediaCtrl = handler.getMediaController(ureq, getWindowControl(), media);
			if(mediaCtrl != null) {
				listenTo(mediaCtrl);
				layoutCont.put("media", mediaCtrl.getInitialComponent());
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
