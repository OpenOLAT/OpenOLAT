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
package org.olat.modules.portfolio.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.BinderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PortfolioServiceImpl implements PortfolioService {
	
	@Autowired
	private PageDAO pageDao;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private CategoryDAO categoryDao;
	
	@Override
	public Binder createNewBinder(String title, String summary, Identity owner) {
		BinderImpl portfolio = binderDao.createAndPersist(title, summary);
		groupDao.addMembership(portfolio.getBaseGroup(), owner, GroupRoles.owner.name());
		return portfolio;
	}

	@Override
	public void appendNewSection(String title, String description, Date begin, Date end, BinderRef binder) {
		Binder reloadedBinder = binderDao.loadByKey(binder.getKey());
		binderDao.createSection(title, description, begin, end, reloadedBinder);
	}
	
	

	@Override
	public Section updateSection(Section section) {
		return binderDao.merge(section);
	}

	@Override
	public List<Section> getSections(BinderRef binder) {
		return binderDao.getSections(binder);
	}

	@Override
	public Section getSection(SectionRef section) {
		return binderDao.loadSectionByKey(section.getKey());
	}

	@Override
	public List<Page> getPages(BinderRef binder) {
		return pageDao.getPages(binder);
	}

	@Override
	public List<Page> getPages(SectionRef section) {
		return pageDao.getPages(section);
	}

	@Override
	public List<Binder> searchOwnedBinders(Identity owner) {
		return binderDao.searchOwnedBinders(owner);
	}
	
	@Override
	public Binder getBinderByKey(Long portfolioKey) {
		return binderDao.loadByKey(portfolioKey);
	}

	@Override
	public List<Identity> getMembers(BinderRef binder, String... roles) {
		return binderDao.getMembers(binder, roles);
	}

	@Override
	public List<Category> getCategories(Binder binder) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Binder.class, binder.getKey());
		return categoryDao.getCategories(ores);
	}

	@Override
	public void updateCategories(Binder binder, List<String> categories) {
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Binder.class, binder.getKey());
		List<Category> currentCategories = categoryDao.getCategories(ores);
		Map<String,Category> currentCategoryMap = new HashMap<>();
		for(Category category:currentCategories) {
			currentCategoryMap.put(category.getName(), category);
		}
		
		List<String> newCategories = new ArrayList<>(categories);
		for(String newCategory:newCategories) {
			if(!currentCategoryMap.containsKey(newCategory)) {
				Category category = categoryDao.createAndPersistCategory(newCategory);
				categoryDao.appendRelation(ores, category);
			}
		}
		
		for(Category currentCategory:currentCategories) {
			String name = currentCategory.getName();
			if(!newCategories.contains(name)) {
				categoryDao.removeRelation(ores, currentCategory);
			}
		}
	}

	@Override
	public List<Page> searchOwnedPages(IdentityRef owner) {
		List<Page> pages = pageDao.getOwnedPages(owner);
		return pages;
	}

	@Override
	public Page appendNewPage(String title, String summary, SectionRef section) {
		Section reloadedSection = binderDao.loadSectionByKey(section.getKey());
		return pageDao.createAndPersist(title, summary, reloadedSection, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U extends PagePart> U appendNewPagePart(Page page, U part) {
		PageBody body = pageDao.loadPageBodyByKey(page.getBody().getKey());
		return (U)pageDao.persistPart(body, part);
	}

	@Override
	public List<PagePart> getPageParts(Page page) {
		return pageDao.getParts(page.getBody());
	}

	@Override
	public PagePart updatePart(PagePart part) {
		return pageDao.merge(part);
	}
	
	
	
	
}
