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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.BinderLight;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.manager.MetadataXStream;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaDetailsController extends FormBasicController implements Activateable2 {
	
	private int counter;
	private Media media;
	private MediaHandler handler;
	
	private Controller mediaCtrl;
	
	@Autowired
	private UserManager userManager;
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

			layoutCont.contextPut("media", media);
			String author = userManager.getUserDisplayName(media.getAuthor());
			layoutCont.contextPut("author", author);
			
			if(media.getCollectionDate() != null) {
				String collectionDate = Formatter.getInstance(getLocale()).formatDate(media.getCollectionDate());
				layoutCont.contextPut("collectionDate", collectionDate);
			}
			
			if(StringHelper.containsNonWhitespace(media.getMetadataXml())) {
				Object metadata = MetadataXStream.get().fromXML(media.getMetadataXml());
				layoutCont.contextPut("metadata", metadata);
			}
			
			List<Category> categories = portfolioService.getCategories(media);
			if(categories != null && categories.size() > 0) {
				Map<String,String> categoriesMap = categories.stream()
						.collect(Collectors.toMap(c -> c.getName(), c -> c.getName()));
				TextBoxListElement categoriesEl = uifactory.addTextBoxListElement("categories", "categories", "categories.hint", categoriesMap, formLayout, getTranslator());
				categoriesEl.setElementCssClass("o_sel_ep_tagsinput");
				categoriesEl.setEnabled(false);
			}
			
			List<BinderLight> usedInList = portfolioService.getUsedInBinders(media);
			List<FormLink> binderLinks = new ArrayList<>(usedInList.size());
			for(BinderLight binder:usedInList) {
				FormLink link = uifactory.addFormLink("binder_" + (++counter), binder.getTitle(), null, layoutCont, Link.LINK | Link.NONTRANSLATED);
				link.setUserObject(binder);
				binderLinks.add(link);
			}
			layoutCont.contextPut("binderLinks", binderLinks);
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

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			Object uobject = link.getUserObject();
			if(uobject instanceof BinderLight) {
				String businessPath = "[Binder:" + ((BinderLight)uobject).getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());	
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
}
