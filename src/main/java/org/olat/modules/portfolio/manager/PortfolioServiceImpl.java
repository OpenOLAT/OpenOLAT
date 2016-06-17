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

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderRef;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PortfolioElement;
import org.olat.modules.portfolio.PortfolioRoles;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionRef;
import org.olat.modules.portfolio.model.AccessRightChange;
import org.olat.modules.portfolio.model.AccessRights;
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
	@Autowired
	private SharedByMeQueries sharedByMeQueries;
	@Autowired
	private SharedWithMeQueries sharedWithMeQueries;
	@Autowired
	private PortfolioFileStorage portfolioFileStorage;
	
	@Override
	public Binder createNewBinder(String title, String summary, String imagePath, Identity owner) {
		BinderImpl portfolio = binderDao.createAndPersist(title, summary, imagePath);
		groupDao.addMembership(portfolio.getBaseGroup(), owner, GroupRoles.owner.name());
		return portfolio;
	}

	@Override
	public Binder updateBinder(Binder binder) {
		return binderDao.updateBinder(binder);
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
	public List<Binder> searchSharedBindersBy(Identity owner) {
		return sharedByMeQueries.searchSharedBinders(owner);
	}

	@Override
	public List<Binder> searchSharedBindersWith(Identity member) {
		return sharedWithMeQueries.searchSharedBinders(member);
	}
	
	@Override
	public Binder getBinderByKey(Long portfolioKey) {
		return binderDao.loadByKey(portfolioKey);
	}

	@Override
	public Binder getBinderBySection(SectionRef section) {
		return binderDao.loadBySection(section);
	}

	@Override
	public List<Identity> getMembers(BinderRef binder, String... roles) {
		return binderDao.getMembers(binder, roles);
	}

	@Override
	public List<AccessRights> getAccessRights(Binder binder) {
		List<AccessRights> rights = binderDao.getBinderAccesRights(binder, null);
		List<AccessRights> sectionRights = binderDao.getSectionAccesRights(binder, null);
		rights.addAll(sectionRights);
		List<AccessRights> pageRights = binderDao.getPageAccesRights(binder, null);
		rights.addAll(pageRights);
		return rights;
	}
	
	@Override
	public List<AccessRights> getAccessRights(Binder binder, Identity identity) {
		List<AccessRights> rights = binderDao.getBinderAccesRights(binder, identity);
		List<AccessRights> sectionRights = binderDao.getSectionAccesRights(binder, identity);
		rights.addAll(sectionRights);
		List<AccessRights> pageRights = binderDao.getPageAccesRights(binder, identity);
		rights.addAll(pageRights);
		return rights;
	}

	@Override
	public void addAccessRights(PortfolioElement element, Identity identity, PortfolioRoles role) {
		Group baseGroup = element.getBaseGroup();
		if(!groupDao.hasRole(baseGroup, identity, role.name())) {
			groupDao.addMembership(baseGroup, identity, role.name());
		}
	}
	
	@Override
	public void changeAccessRights(List<Identity> identities, List<AccessRightChange> changes) {
		for(Identity identity:identities) {
			for(AccessRightChange change:changes) {
				Group baseGroup = change.getElement().getBaseGroup();
				if(change.isAdd()) {
					if(!groupDao.hasRole(baseGroup, identity, change.getRole().name())) {
						Group group = getGroup(change.getElement());
						groupDao.addMembership(group, identity, change.getRole().name());
					}
				} else {
					if(groupDao.hasRole(baseGroup, identity, change.getRole().name())) {
						Group group = getGroup(change.getElement());
						groupDao.removeMembership(group, identity, change.getRole().name());
					}
				}
			}
		}
	}
	
	private Group getGroup(PortfolioElement element) {
		if(element instanceof Page) {
			return pageDao.getGroup((Page)element);
		}
		if(element instanceof SectionRef) {
			return binderDao.getGroup((SectionRef)element);
		}
		if(element instanceof BinderRef) {
			return binderDao.getGroup((BinderRef)element);
		}
		return null;
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
	public File getPosterImage(Binder binder) {
		String imagePath = binder.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = portfolioFileStorage.getRootDirectory();
			return new File(bcroot, imagePath);
		}
		return null;
	}

	@Override
	public String addPosterImageForBinder(File file, String filename) {
		File dir = portfolioFileStorage.generateBinderSubDirectory();
		File destinationFile = new File(dir, filename);
		String renamedFile = FileUtils.rename(destinationFile);
		if(renamedFile != null) {
			destinationFile = new File(dir, renamedFile);
		}
		FileUtils.copyFileToFile(file, destinationFile, false);
		return portfolioFileStorage.getRelativePath(destinationFile);
	}

	@Override
	public void removePosterImage(Binder binder) {
		String imagePath = binder.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = portfolioFileStorage.getRootDirectory();
			File file = new File(bcroot, imagePath);
			if(file.exists()) {
				file.delete();
			}
		}
	}

	@Override
	public List<Page> searchOwnedPages(IdentityRef owner) {
		List<Page> pages = pageDao.getOwnedPages(owner);
		return pages;
	}

	@Override
	public Page appendNewPage(Identity owner, String title, String summary, String imagePath, SectionRef section) {
		Section reloadedSection = section == null ? null : binderDao.loadSectionByKey(section.getKey());
		Page page = pageDao.createAndPersist(title, summary, null, reloadedSection, null);
		groupDao.addMembership(page.getBaseGroup(), owner, PortfolioRoles.owner.name());
		return page;
	}

	@Override
	public Page getPageByKey(Long key) {
		return pageDao.loadByKey(key);
	}

	@Override
	public Page updatePage(Page page) {
		return pageDao.updatePage(page);
	}

	@Override
	public File getPosterImage(Page page) {
		String imagePath = page.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = portfolioFileStorage.getRootDirectory();
			return new File(bcroot, imagePath);
		}
		return null;
	}

	@Override
	public String addPosterImageForPage(File file, String filename) {
		File dir = portfolioFileStorage.generatePageSubDirectory();
		File destinationFile = new File(dir, filename);
		String renamedFile = FileUtils.rename(destinationFile);
		if(renamedFile != null) {
			destinationFile = new File(dir, renamedFile);
		}
		FileUtils.copyFileToFile(file, destinationFile, false);
		return portfolioFileStorage.getRelativePath(destinationFile);
	}

	@Override
	public void removePosterImage(Page page) {
		String imagePath = page.getImagePath();
		if(StringHelper.containsNonWhitespace(imagePath)) {
			File bcroot = portfolioFileStorage.getRootDirectory();
			File file = new File(bcroot, imagePath);
			if(file.exists()) {
				file.delete();
			}
		}
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
