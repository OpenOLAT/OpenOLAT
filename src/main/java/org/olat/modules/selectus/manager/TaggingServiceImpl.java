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
package org.olat.modules.selectus.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationCategory;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.comparator.ApplicationCategoryInfosComparator;

/**
 * 
 * Initial date: 15 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaggingServiceImpl implements TaggingService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private SelectusCategoryDAO categoryDao;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private ApplicationCategoryDAO applicationCategoryDao;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	@Override
	public Category createCategory(String name, String color, Position position) {
		return categoryDao.createCategory(name, color, position);
	}

	@Override
	public Category updateCategory(Category category) {
		return categoryDao.updateCategory(category);
	}

	@Override
	public List<Category> getCategoriesByName(String name, Position position) {
		boolean systemTag = recruitingModule.isSystemTagsEnabled(position);
		boolean positionTag = position != null && recruitingModule.isPositionTagsEnabled(position);
		return categoryDao.getCategoriesByName(name, position, systemTag, positionTag);
	}

	@Override
	public int countApplications(Category category) {
		return applicationCategoryDao.countApplications(category);
	}

	@Override
	public void deleteCategory(Category category) {
		applicationCategoryDao.delete(category);
		categoryDao.delete(category);
	}

	@Override
	public void deleteCategory(Category category, Category replacementCategory) {
		applicationCategoryDao.replaceCategory(category, replacementCategory);
		dbInstance.commit();
		applicationCategoryDao.delete(category);
		categoryDao.delete(category);
	}

	@Override
	public List<Category> getCategories(PositionRef position) {
		return applicationCategoryDao.getCategories(position);
	}
	
	@Override
	public List<Category> getSystemCategories() {
		return categoryDao.getSystemCategories();
	}

	@Override
	public List<Category> getAvailableCategoriesFor(Position position) {
		List<Category> categories = new ArrayList<>(32);
		
		Set<String> deduplicateNames = new HashSet<>();
		if(recruitingModule.isPositionTagsEnabled() && position.isPositionTagsEnabled()) {
			List<Category> positionCategories = categoryDao.getPositionCategories(position);
			for(Category positionCategory:positionCategories) {
				if(!deduplicateNames.contains(positionCategory.getName())) {
					categories.add(positionCategory);
					deduplicateNames.add(positionCategory.getName());
				}
			}
		}
		
		if(recruitingModule.isSystemTagsEnabled() && position.isSystemTagsEnabled()) {
			List<Category> systemCategories = categoryDao.getSystemCategories();
			for(Category systemCategory:systemCategories) {
				if(!deduplicateNames.contains(systemCategory.getName())) {
					categories.add(systemCategory);
					deduplicateNames.add(systemCategory.getName());
				}
			}
		}
		return categories;
	}

	@Override
	public List<Category> getPositionCategories(PositionRef position) {
		return categoryDao.getPositionCategories(position);
	}

	@Override
	public Category getCategoryByKey(Long categoryKey) {
		return categoryDao.loadCategory(categoryKey);
	}

	@Override
	public List<Category> addCategories(Application application, Collection<String> categoryNames, boolean administrativeCategoriesAllowed,
			Position position, Identity actingIdentity, Locale locale) {
		boolean systemTags = recruitingModule.isSystemTagsEnabled(position);
		boolean positionTags = recruitingModule.isPositionTagsEnabled(position);
		List<ApplicationCategory> currentAppCategories = applicationCategoryDao.getApplicationCategories(application, systemTags, positionTags);
		if(!administrativeCategoriesAllowed) {
			currentAppCategories = currentAppCategories.stream()
					.filter(appCat -> !appCat.isAdministrative())
					.collect(Collectors.toList());
			categoryNames = categoryNames.stream()
					.filter(name -> !name.startsWith("a:"))
					.collect(Collectors.toList());
		}
		
		List<String> currentCategoryNames = new ArrayList<>();
		for(ApplicationCategory currentAppCategory:currentAppCategories) {
			// with a:name for administrative ones
			String tagName = categoryNameShortcut(currentAppCategory);
			currentCategoryNames.add(tagName);
		}
		
		List<Category> addedCategories = new ArrayList<>();
		for(String categoryName:categoryNames) {
			if(!currentCategoryNames.contains(categoryName)) {
				String name = categoryName;
				boolean administrative = name.startsWith("a:");
				if(administrative) {
					name = name.substring(2, categoryName.length());
				}

				Category category = findCategoryByName(name, position, systemTags, positionTags);
				if(category == null) {
					category = categoryDao.createCategory(name, null, application.getPosition());
				}
				applicationCategoryDao.createApplicationCategory(application, category, administrative);
				addedCategories.add(category);
			}
		}
		if(!categoryNames.isEmpty()) {
			List<String> addedCategoriesNames = categoriesNamesToStringList(categoryNames);
			List<String> currentCategoriesNames = applicationCategoriesToStringList(currentAppCategories);
			logAddCategories(addedCategoriesNames, currentCategoriesNames, application, actingIdentity, locale);
		}
		return addedCategories;
	}
	
	
	private void logAddCategories(List<String> addedCategoriesNames, List<String> currentCategoriesNames,
			Application app, Identity actingIdentity, Locale locale) {
		
		addedCategoriesNames.removeAll(currentCategoriesNames);
		if(addedCategoriesNames.isEmpty()) {
			return;
		}
		
		String beforeCategories = stringListToString(currentCategoriesNames);
		String addedCategories = stringListToString(addedCategoriesNames);
		
		currentCategoriesNames.addAll(addedCategoriesNames);
		String afterCategories = stringListToString(currentCategoriesNames);

		String messageI18n;
		if(addedCategoriesNames.size() == 1) {
			messageI18n = "audit.log.application.add.category";
		} else {
			messageI18n = "audit.log.application.add.categories";
		}
		String[] messageArgs = new String[] {
				salutationGenerator.getTitleFullname(app, locale), app.getId().toString(), addedCategories
			};
		Translator translator = Util.createPackageTranslator(PositionController.class, locale);
		auditService.auditApplicationLog(Action.update, ActionTarget.application, beforeCategories, afterCategories,
				messageI18n, messageArgs, translator, app.getPosition(), app, actingIdentity);
	}
	
	private Category findCategoryByName(String categoryName, Position position, boolean systemTags, boolean positionTags) {
		List<Category> categories = categoryDao.getCategoriesByName(categoryName, position, systemTags, positionTags);
		for(Category category:categories) {
			if(category.getPosition() != null) {
				return category;
			}
		}
		return categories != null && !categories.isEmpty() ? categories.get(0) : null;
	}

	@Override
	public void removeCategories(Application application, Collection<String> categoryNames, Identity actingIdentity, Locale locale) {
		List<ApplicationCategory> currentAppCategories = applicationCategoryDao.getApplicationCategories(application, true, true);

		List<ApplicationCategory> removedCategories = new ArrayList<>();
		for(ApplicationCategory appCategory:currentAppCategories) {
			String tagName;
			if(appCategory.isAdministrative()) {
				tagName = "a:".concat(appCategory.getCategory().getName());
			} else {
				tagName = appCategory.getCategory().getName();
			}
			if(categoryNames.contains(tagName)) {
				applicationCategoryDao.delete(appCategory);
				removedCategories.add(appCategory);
			}
		}
		dbInstance.commit();
		if(!removedCategories.isEmpty()) {
			List<String> removedCategoriesNames = applicationCategoriesToStringList(removedCategories);
			List<String> currentCategoriesNames = applicationCategoriesToStringList(currentAppCategories);
			logRemoveCategories(removedCategoriesNames, currentCategoriesNames, application, actingIdentity, locale);
		}
	}
	
	private void logRemoveCategories(List<String> removedCategoriesNames, List<String> currentCategoriesNames,
			Application app, Identity actingIdentity, Locale locale) {
		
		removedCategoriesNames.retainAll(currentCategoriesNames);
		if(removedCategoriesNames.isEmpty()) {
			return;
		}
		
		String beforeCategories = stringListToString(currentCategoriesNames);
		String removedCategories = stringListToString(removedCategoriesNames);
		
		currentCategoriesNames.removeAll(removedCategoriesNames);
		String afterCategories = stringListToString(currentCategoriesNames);

		String messageI18n;
		if(removedCategoriesNames.size() == 1) {
			messageI18n = "audit.log.application.remove.category";
		} else {
			messageI18n = "audit.log.application.remove.categories";
		}
		String[] messageArgs = new String[] {
				salutationGenerator.getTitleFullname(app, locale), app.getId().toString(), removedCategories
			};
		Translator translator = Util.createPackageTranslator(PositionController.class, locale);
		auditService.auditApplicationLog(Action.update, ActionTarget.application, beforeCategories, afterCategories,
				messageI18n, messageArgs, translator, app.getPosition(), app, actingIdentity);
	}

	@Override
	public void setCategories(Application application, Collection<String> categoryNames, boolean administrativeCategoriesAllowed,
			Position position, Identity actingIdentity, Locale locale) {
		boolean systemTags = recruitingModule.isSystemTagsEnabled(position);
		boolean positionTags = recruitingModule.isPositionTagsEnabled(position);
		List<ApplicationCategory> currentAppCategories = applicationCategoryDao.getApplicationCategories(application, systemTags, positionTags);
		if(!administrativeCategoriesAllowed) {
			currentAppCategories = currentAppCategories.stream()
					.filter(appCat -> !appCat.isAdministrative())
					.collect(Collectors.toList());
			categoryNames = categoryNames.stream()
					.filter(name -> !name.startsWith("a:"))
					.collect(Collectors.toList());
		}
		
		List<String> currentCategoryNames = new ArrayList<>();
		for(ApplicationCategory currentAppCategory:currentAppCategories) {
			// category name or a:name for administrative tags
			String tagName = categoryNameShortcut(currentAppCategory);
			if(categoryNames.contains(tagName)) {
				currentCategoryNames.add(tagName);
			} else {
				applicationCategoryDao.delete(currentAppCategory);
			}
		}
		
		for(String categoryName:categoryNames) {
			if(!currentCategoryNames.contains(categoryName)) {
				String name = categoryName;
				boolean administrative = name.startsWith("a:");
				if(administrative) {
					name = categoryName.substring(2, name.length());
				}

				Category category = findCategoryByName(name, position, systemTags, positionTags);
				if(category == null) {
					category = categoryDao.createCategory(name, null, application.getPosition());
				}
				applicationCategoryDao.createApplicationCategory(application, category, administrative);
			}
		}
		
		List<String> categoryNamesList = categoriesNamesToStringList(categoryNames);
		List<String> currentCategoriesNamesList = applicationCategoriesToStringList(currentAppCategories);
		logUpdateCategories(categoryNamesList, currentCategoriesNamesList, application, actingIdentity, locale);
	}
	
	private static final String categoryNameShortcut(ApplicationCategory applicationCategory) {
		if(applicationCategory.isAdministrative()) {
			return "a:".concat(applicationCategory.getCategory().getName());
		}
		return applicationCategory.getCategory().getName();
	}
	
	private static final List<String> categoriesNamesToStringList(Collection<String> categoryNames) {
		if(categoryNames == null) {
			return new ArrayList<>();
		}
		List<String> namesList = new ArrayList<>(categoryNames);
		Collections.sort(namesList);
		return namesList;
	}
	
	private static final List<String> applicationCategoriesToStringList(List<ApplicationCategory> categories) {
		if(categories == null) {
			return new ArrayList<>();
		}
		List<String> namesList = categories.stream()
				.map(TaggingServiceImpl::categoryNameShortcut)
				.collect(Collectors.toList());
		Collections.sort(namesList);
		return namesList;
	}
	
	private static final String stringListToString(List<String> list) {
		StringBuilder sb = new StringBuilder(32);
		for(String category:list) {
			if(sb.length() > 0) sb.append(", ");
			sb.append(category);
		}
		return sb.toString();
	}
	
	private void logUpdateCategories(List<String> categoryNames, List<String> currentCategoriesNames, Application app, Identity actingIdentity, Locale locale) {
		if(categoryNames.equals(currentCategoriesNames)) {
			return;
		}

		String beforeCategories = stringListToString(currentCategoriesNames);
		String afterCategories = stringListToString(categoryNames);
		
		List<String> removedCategories = new ArrayList<>(currentCategoriesNames);
		removedCategories.removeAll(categoryNames);
		String removedCategoriesArg = stringListToString(removedCategories);

		List<String> addedCategories = new ArrayList<>(categoryNames);
		addedCategories.removeAll(currentCategoriesNames);
		String addedCategoriesArg = stringListToString(addedCategories);
		
		if(!addedCategories.isEmpty()) {
			String messageI18n;
			if(addedCategories.size() == 1) {
				messageI18n = "audit.log.application.add.category";
			} else {
				messageI18n = "audit.log.application.add.categories";
			}

			String[] messageArgs = new String[] {
					salutationGenerator.getTitleFullname(app, locale), app.getId() == null ? "-"  : app.getId().toString(), addedCategoriesArg
				};
			Translator translator = Util.createPackageTranslator(PositionController.class, locale);
			auditService.auditApplicationLog(Action.update, ActionTarget.application, beforeCategories, afterCategories,
					messageI18n, messageArgs, translator, app.getPosition(), app, actingIdentity);
		}
		
		if(!removedCategories.isEmpty()) {
			String messageI18n;
			if(removedCategories.size() == 1) {
				messageI18n = "audit.log.application.remove.category";
			} else {
				messageI18n = "audit.log.application.remove.categories";
			}
			String[] messageArgs = new String[] {
					salutationGenerator.getTitleFullname(app, locale), app.getId() == null ? "-"  : app.getId().toString(), removedCategoriesArg
				};
			Translator translator = Util.createPackageTranslator(PositionController.class, locale);
			auditService.auditApplicationLog(Action.update, ActionTarget.application, beforeCategories, afterCategories,
					messageI18n, messageArgs, translator, app.getPosition(), app, actingIdentity);
		}
	}

	@Override
	public List<ApplicationCategoryInfos> getApplicationCategories(Position position, boolean administrativeIncluded) {
		boolean systemTags = recruitingModule.isSystemTagsEnabled(position);
		boolean positionTags = recruitingModule.isPositionTagsEnabled(position);
		return applicationCategoryDao.getApplicationCategoriesInfos(position, null,
				systemTags, positionTags, administrativeIncluded);
	}
	
	@Override
	public List<ApplicationCategoryInfos> getApplicationCategories(Position position, ApplicationRef application,
			boolean administrativeIncluded) {
		if(application == null) return new ArrayList<>();

		boolean systemTags = recruitingModule.isSystemTagsEnabled(position);
		boolean positionTags = recruitingModule.isPositionTagsEnabled(position);
		List<ApplicationRef> applications = Collections.singletonList(application);
		return applicationCategoryDao.getApplicationCategoriesInfos(position, applications,
				systemTags, positionTags, administrativeIncluded);
	}
	
	@Override
	public List<ApplicationCategoryInfos> getApplicationCategories(Position position, List<ApplicationRef> applications,
			boolean administrativeIncluded) {
		if(applications == null || applications.isEmpty()) return new ArrayList<>();

		boolean systemTags = recruitingModule.isSystemTagsEnabled(position);
		boolean positionTags = recruitingModule.isPositionTagsEnabled(position);
		return applicationCategoryDao.getApplicationCategoriesInfos(position, applications,
				systemTags, positionTags, administrativeIncluded);
	}

	@Override
	public boolean hasApplicationCategories(PositionRef position, boolean systemCategories,
			boolean positionCategories) {
		return applicationCategoryDao.hasApplicationCategories(position, systemCategories, positionCategories);
	}
	
	@Override
	public boolean removeApplicationCategories(PositionRef position, boolean systemCategories,
			boolean positionCategories) {
		List<ApplicationCategory> applicationCategories = applicationCategoryDao.getApplicationCategories(position, systemCategories, positionCategories);
		for(ApplicationCategory applicationCategory:applicationCategories) {
			applicationCategoryDao.delete(applicationCategory);
		}
		dbInstance.commit();
		return true;
	}

	@Override
	public Map<Long, List<ApplicationCategoryInfos>> getApplicationToCategories(Position position, boolean administrativeIncluded) {
		Map<Long, List<ApplicationCategoryInfos>> appToCategories = new HashMap<>();
		if(recruitingModule.isTaggingToolEnabled()) {
			boolean systemTags = recruitingModule.isSystemTagsEnabled(position);
			boolean positionTags = recruitingModule.isPositionTagsEnabled(position);
			List<ApplicationCategoryInfos> appCategories = applicationCategoryDao.getApplicationCategoriesInfos(position, null,
					systemTags, positionTags, administrativeIncluded);
			for(ApplicationCategoryInfos appCategory:appCategories) {
				appToCategories
					.computeIfAbsent(appCategory.getApplicationKey(), k -> new ArrayList<>())
					.add(appCategory);
			}
		}
		
		ApplicationCategoryInfosComparator comparator = new ApplicationCategoryInfosComparator();
		for(Map.Entry<Long,List<ApplicationCategoryInfos>> entry: appToCategories.entrySet()) {
			List<ApplicationCategoryInfos> categories = entry.getValue();
			if(categories != null && categories.size() > 1) {
				Collections.sort(categories, comparator);
			}
		}
		return appToCategories;
	}
}
