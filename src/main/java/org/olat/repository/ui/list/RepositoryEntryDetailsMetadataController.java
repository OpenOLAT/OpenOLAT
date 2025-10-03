/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.rating.RatingFormEvent;
import org.olat.core.gui.components.rating.RatingWithAverageFormItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryDetailsMetadataController extends FormBasicController {
	
	private FormLink markLink;
	private RatingWithAverageFormItem ratingEl;
	
	private final RepositoryEntry entry;
	private final boolean isMember;
	private final boolean guestOnly;
	
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CatalogV2Module catalogModule;
	@Autowired
	private CatalogManager catalogManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private EfficiencyStatementManager effManager;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private MarkManager markManager;

	public RepositoryEntryDetailsMetadataController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			boolean isMember, boolean isGuestOnly) {
		super(ureq, wControl, Util.getPackageVelocityRoot(RepositoryEntryDetailsController.class) + "/details_metadata.html");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.isMember = isMember;
		this.guestOnly = isGuestOnly;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int counter = 0;
		if (formLayout instanceof FormLayoutContainer layoutCont) {

			layoutCont.contextPut("v", entry);
			layoutCont.contextPut("guestOnly", Boolean.valueOf(guestOnly));
			boolean closed = entry.getEntryStatus().decommissioned();
			layoutCont.contextPut("closed", Boolean.valueOf(closed));
			
			// Catalog V1 categories
			if (repositoryModule.isCatalogEnabled()) {
				List<CatalogEntry> categories = catalogManager.getCatalogEntriesReferencing(entry);
				List<String> categoriesLink = new ArrayList<>(categories.size());
				for(CatalogEntry category:categories) {
					String id = "cat_" + counter++;
					String title = StringHelper.escapeHtml(category.getParent().getName());
					FormLink catLink = uifactory.addFormLink(id, "category", title, null, layoutCont, Link.LINK | Link.NONTRANSLATED);
					catLink.setIconLeftCSS("o_icon o_icon-fw o_icon_catalog");
					catLink.setUserObject(category.getKey());
					categoriesLink.add(id);
				}
				layoutCont.contextPut("categories", categoriesLink);
			}
			// taxonomy levels
			String labelI18nKey = catalogModule.isEnabled()? "cif.taxonomy.levels.catalog": "cif.taxonomy.levels";
			layoutCont.contextPut("taxonomyLevelsLabel", translate(labelI18nKey));
			String taxonomyLevelTags = TaxonomyUIFactory.getTags(getTranslator(), repositoryService.getTaxonomy(entry));
			layoutCont.contextPut("taxonomyLevelTags", taxonomyLevelTags);
			
			if (!guestOnly) {
				boolean marked = markManager.isMarked(entry, getIdentity(), null);
				markLink = uifactory.addFormLink("mark", "mark", marked ? "details.bookmark.remove" : "details.bookmark", null, layoutCont, Link.LINK);
				markLink.setElementCssClass("o_bookmark");
				markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			}
			
			RepositoryEntryStatistics statistics = entry.getStatistics();
			if (repositoryModule.isRatingEnabled()) {
				Integer myRating = userRatingsDao.getRatingValue(getIdentity(), entry, null);
				
				Double averageRating = statistics.getRating();
				long numOfRatings = statistics.getNumOfRatings();
				float ratingValue = myRating == null ? 0f : myRating.floatValue();
				float averageRatingValue = averageRating == null ? 0f : averageRating.floatValue();
				ratingEl = new RatingWithAverageFormItem("rating", ratingValue, averageRatingValue, 5, numOfRatings);
				ratingEl.setEnabled(!guestOnly);
				layoutCont.add("rating", ratingEl);
			}
			
			if (isMember) {
				//show the list of groups
				SearchBusinessGroupParams params = new SearchBusinessGroupParams(getIdentity(), true, true);
				List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
				List<String> groupLinkNames = new ArrayList<>(groups.size());
				for (BusinessGroup group:groups) {
					String groupLinkName = "grp_" + counter++;
					String groupName = StringHelper.escapeHtml(group.getName());
					FormLink link = uifactory.addFormLink(groupLinkName, "group", groupName, null, layoutCont, Link.LINK | Link.NONTRANSLATED);
					link.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
					link.setUserObject(group.getKey());
					groupLinkNames.add(groupLinkName);
				}
				layoutCont.contextPut("groups", groupLinkNames);
			}
			
			if("CourseModule".equals(entry.getOlatResource().getResourceableTypeName())) {
				boolean passed = false;
				boolean failed = false;
				String score = null;
				try {
					ICourse course = CourseFactory.loadCourse(entry);
					if (course != null && course.getCourseConfig().isEfficiencyStatementEnabled()) {
						UserEfficiencyStatement statement = effManager.getUserEfficiencyStatementLightByRepositoryEntry(entry, getIdentity());
						if (statement != null) {
							Boolean p = statement.getPassed();
							if (p != null) {
								passed = p.booleanValue();
								failed = !p.booleanValue();
							}
							
							Float scoreVal = statement.getScore();
							if (scoreVal != null) {
								score = AssessmentHelper.getRoundedScore(scoreVal);
							}
						}
					}
				} catch (CorruptedCourseException e) {
					// Display no values
				}
				layoutCont.contextPut("passed", passed);
				layoutCont.contextPut("failed", failed);
				layoutCont.contextPut("score", score);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("category".equals(cmd)) {
				Long categoryKey = (Long)link.getUserObject();
				doOpenCategory(ureq, categoryKey);
			} else if ("mark".equals(cmd)) {
				boolean marked = doMark();
				markLink.setI18nKey(marked ? "details.bookmark.remove" : "details.bookmark");
				markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			} else if ("group".equals(cmd)) {
				Long groupKey = (Long)link.getUserObject();
				doOpenGroup(ureq, groupKey);
			}
		} else if (ratingEl == source && event instanceof RatingFormEvent ratingEvent) {
			doRating(ratingEvent.getRating());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected boolean doMark() {
		OLATResourceable item = OresHelper.clone(entry);
		if (markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			return false;
		}
		String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
		markManager.setMark(item, getIdentity(), null, businessPath);
		return true;
	}
	
	private void doRating(float rating) {
		userRatingsDao.updateRating(getIdentity(), entry, null, Math.round(rating));
	}
	
	protected void doOpenCategory(UserRequest ureq, Long categoryKey) {
		String businessPath = "[CatalogEntry:" + categoryKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	protected void doOpenGroup(UserRequest ureq, Long groupKey) {
		String businessPath = "[BusinessGroup:" + groupKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

}
