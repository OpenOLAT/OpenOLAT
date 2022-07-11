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

import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ReadOnlyCommentsSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageProvider;
import org.olat.modules.ceditor.ui.PageController;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.BinderSecurityCallbackFactory;
import org.olat.modules.portfolio.MediaHandler;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.handler.ContainerHandler;
import org.olat.modules.portfolio.handler.EvaluationFormHandler;
import org.olat.modules.portfolio.handler.HTMLRawPageElementHandler;
import org.olat.modules.portfolio.handler.MathPageElementHandler;
import org.olat.modules.portfolio.handler.ParagraphPageElementHandler;
import org.olat.modules.portfolio.handler.SpacerElementHandler;
import org.olat.modules.portfolio.handler.TablePageElementHandler;
import org.olat.modules.portfolio.handler.TitlePageElementHandler;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderOnePageController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private int counter = 0;
	private List<String> components = new ArrayList<>();
	private final PageElementRenderingHints renderingHints;
	
	@Autowired
	private PortfolioService portfolioService;

	@Autowired
	private UserManager userManager;
	
	public BinderOnePageController(UserRequest ureq, WindowControl wControl,
			BinderRef binderRef, PageElementRenderingHints renderingHints, boolean print) {
		super(ureq, wControl);
		this.renderingHints = renderingHints;
		
		mainVC = createVelocityContainer("binder_one_page");
		mainVC.contextPut("components", components);
		mainVC.contextPut("print", print);
		mainVC.contextPut("mainCssClass", "o_binder_export");
		putInitialPanel(mainVC);
		loadMetadataAndComponents(ureq, binderRef);
	}
	
	public BinderOnePageController(UserRequest ureq, WindowControl wControl,
			Page page, PageElementRenderingHints renderingHints, boolean print) {
		super(ureq, wControl);
		this.renderingHints = renderingHints;
		
		mainVC = createVelocityContainer("binder_one_page");
		mainVC.contextPut("components", components);
		mainVC.contextPut("print", print);
		mainVC.contextPut("mainCssClass", "o_page_export");
		putInitialPanel(mainVC);
		loadPage(ureq, page);
	}
	
	private void loadMetadataAndComponents(UserRequest ureq, BinderRef binderRef) {
		Binder binder = portfolioService.getBinderByKey(binderRef.getKey());
		// load metadata
		List<Identity> owners = portfolioService.getMembers(binder, PortfolioRoles.owner.name());
		StringBuilder ownerSb = new StringBuilder();
		for(Identity owner:owners) {
			if(ownerSb.length() > 0) ownerSb.append(", ");
			ownerSb.append(userManager.getUserDisplayName(owner));
		}
		mainVC.contextPut("owners", ownerSb.toString());
		mainVC.contextPut("binderTitle", binder.getTitle());
		mainVC.contextPut("binderKey", binder.getKey());		
		//load pages
		List<Section> sections = portfolioService.getSections(binder);
		List<Page> pages = portfolioService.getPages(binder);
		
		for(Section section:sections) {
			loadSection(section);
			for(Page page:pages) {
				if(section.equals(page.getSection())) {
					loadPage(ureq, page);
				}
			}
		}
	}
	
	private void loadSection(Section section) {
		String id = "section_" + (++counter);
		VelocityContainer rowVC = createVelocityContainer(id, "portfolio_element_row");

		AssessmentSection assessmentSection = null;
		PortfolioElementRow row = new PortfolioElementRow(section, assessmentSection, false, false);
		rowVC.contextPut("row", row);
		rowVC.contextPut("rowIndex", 0);
		mainVC.put(id, rowVC);
		components.add(id);
	}
	
	
	private void loadPage(UserRequest ureq, Page page) {
		String id = "page_w_" + (++counter);
		VelocityContainer pageVC = createVelocityContainer(id, "page_content_print");
		mainVC.put(id, pageVC);
		components.add(id);
		
		BinderSecurityCallback secCallback = BinderSecurityCallbackFactory.getReadOnlyCallback();
		PageMetadataController metadatCtrl = new PageMetadataController(ureq, getWindowControl(), secCallback, page, false);
		listenTo(metadatCtrl);
		
		Component pageMetaCmp = metadatCtrl.getInitialComponent();
		pageVC.put("meta", pageMetaCmp);

		PageController pageCtrl = new PageController(ureq, getWindowControl(), new PortfolioPageProvider(page), renderingHints);
		listenTo(pageCtrl);
		pageCtrl.loadElements(ureq);
		
		Component pageCmp = pageCtrl.getInitialComponent();
		pageVC.put("page", pageCmp);

		CommentAndRatingSecurityCallback commentSecCallback = new ReadOnlyCommentsSecurityCallback();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, page.getKey());
		UserCommentsController commentsCtrl = new UserCommentsController(ureq, getWindowControl(), ores, null, null, commentSecCallback);
		listenTo(commentsCtrl);
		if(commentsCtrl.getNumOfComments() > 0) {
			pageVC.put("comments", commentsCtrl.getInitialComponent());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private class PortfolioPageProvider implements PageProvider {
		
		private final List<PageElementHandler> handlers = new ArrayList<>();
		
		private Page page;

		public PortfolioPageProvider(Page page) {
			this.page = page; 

			//handler for title
			TitlePageElementHandler titleRawHandler = new TitlePageElementHandler();
			handlers.add(titleRawHandler);
			//handler simple HTML
			ParagraphPageElementHandler paragraphHandler = new ParagraphPageElementHandler();
			handlers.add(paragraphHandler);
			//handler for HTML code
			SpacerElementHandler hrHandler = new SpacerElementHandler();
			handlers.add(hrHandler);
			//handler for container
			ContainerHandler containerHandler = new ContainerHandler();
			handlers.add(containerHandler);
			//handler for form
			EvaluationFormHandler formHandler = new EvaluationFormHandler();
			handlers.add(formHandler);
			//handler for HTML code
			HTMLRawPageElementHandler htlmRawHandler = new HTMLRawPageElementHandler();
			handlers.add(htlmRawHandler);
			// handler for table
			TablePageElementHandler tableHandler = new TablePageElementHandler();
			handlers.add(tableHandler);
			//handler for LaTeX code
			MathPageElementHandler mathHandler = new MathPageElementHandler();
			handlers.add(mathHandler);
			
			List<MediaHandler> mediaHandlers = portfolioService.getMediaHandlers();
			for(MediaHandler mediaHandler:mediaHandlers) {
				if(mediaHandler instanceof PageElementHandler) {
					handlers.add((PageElementHandler)mediaHandler);
				}
			}
		}

		@Override
		public List<? extends PageElement> getElements() {
			return portfolioService.getPageParts(page);
		}

		@Override
		public List<PageElementHandler> getAvailableHandlers() {
			return handlers;
		}
	}

}
