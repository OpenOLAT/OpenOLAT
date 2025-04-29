/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.list;

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumElementImageMapper;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryManager;
import org.olat.resource.accesscontrol.ui.OpenAccessOfferController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationHeaderController extends FormBasicController {
	
	private FormLink markLink;
	private FormLink selectLink;
	private FormLink detailsLink;
	
	private final CurriculumElement element;
	private final String curriculumElementImageMapperUrl;
	private final CurriculumElementImageMapper curriculumElementImageMapper;
	
	@Autowired
	private MarkManager markManager;
	@Autowired
	private CurriculumService curriculumService;
	
	public ImplementationHeaderController(UserRequest ureq, WindowControl wControl, CurriculumElement element) {
		super(ureq, wControl, "row_1");
		setTranslator(Util.createPackageTranslator(OpenAccessOfferController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		this.element = element;
		curriculumElementImageMapper = new CurriculumElementImageMapper(curriculumService);
		curriculumElementImageMapperUrl = registerCacheableMapper(ureq, CurriculumElementImageMapper.DEFAULT_ID,
				curriculumElementImageMapper, CurriculumElementImageMapper.DEFAULT_EXPIRATION_TIME);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			InPreparationRow row = new InPreparationRow(element.getKey(), element, null,  false);
			layoutCont.contextPut("row", row);
			
			String imageUrl = curriculumElementImageMapper.getImageUrl(curriculumElementImageMapperUrl,
					() -> row.getCurriculumElementKey(), CurriculumElementFileType.teaserImage);
			if (imageUrl != null) {
				row.setThumbnailRelPath(imageUrl);
			}
			
			String displayName = StringHelper.escapeHtml(row.getDisplayName());
			selectLink = uifactory.addFormLink("select_" + row.getOlatResource().getKey(), displayName, null, layoutCont, Link.NONTRANSLATED);
			row.setSelectLink(selectLink);
			
			markLink = uifactory.addFormLink("mark_" + row.getOlatResource().getKey(), "", null, layoutCont, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
			markLink.setAriaLabel(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
			row.setMarkLink(markLink);

			OLATResourceable item = OresHelper.createOLATResourceableInstance(CurriculumElement.class, element.getKey());
			boolean marked = markManager.isMarked(item, getIdentity(), null);
			decoratedMarkLink(marked);
			
			detailsLink = uifactory.addFormLink("details_" + row.getOlatResource().getKey(), "details", "learn.more", null, flc, Link.LINK);
			detailsLink.setIconRightCSS("o_icon o_icon_details");
			detailsLink.setCustomEnabledLinkCSS("btn btn-sm btn-primary o_details o_in_preparation");
			detailsLink.setTitle("details");
			row.setDetailsLink(detailsLink);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == mainForm.getInitialComponent()) {
			if("ONCLICK".equals(event.getCommand())) {
				String rowKeyStr = ureq.getParameter("select_row");
				if(StringHelper.isLong(rowKeyStr)) {
					fireEvent(ureq, new ImplementationEvent());
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(markLink == source) {
			doMark(ureq);
		} else if(selectLink == source || detailsLink == source) {
			fireEvent(ureq, new ImplementationEvent());
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private boolean doMark(UserRequest ureq) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance(CurriculumElement.class, element.getKey());
		String businessPath = "[MyCoursesSite:0][CurriculumElement:" + item.getResourceableId() + "]";

		boolean marked;
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			marked = false;
		} else {
			markManager.setMark(item, getIdentity(), null, businessPath);
			marked = true;
		}
		decoratedMarkLink(marked);
		fireEvent(ureq, Event.CHANGED_EVENT);
		return marked;
	}
	
	private void decoratedMarkLink(boolean marked) {
		markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setAriaLabel(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
	}
}
