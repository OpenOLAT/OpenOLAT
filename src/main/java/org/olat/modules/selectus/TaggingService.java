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
package org.olat.modules.selectus;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;

/**
 * 
 * Initial date: 16 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TaggingService {
	
	/**
	 * Create a new system or position specific category.
	 * 
	 * @param name The name (mandatory)
	 * @param color A color (optional)
	 * @param position A position if the category is position specific
	 * @return
	 */
	public Category createCategory(String name, String color, Position position);
	
	public Category updateCategory(Category category);
	
	/**
	 * 
	 * @param name The name of the category
	 * @param position The position to limit the search for
	 * @return A list of categories
	 */
	public List<Category> getCategoriesByName(String name, Position position);
	
	/**
	 * Returns the categories linked to the applications of the
	 * specified position.
	 * 
	 * @param position A position
	 * @return A list of categories
	 */
	public List<Category> getCategories(PositionRef position);
	
	
	/**
	 * Returns all the categories defined at system level.
	 * 
	 * @return A list of categories
	 */
	public List<Category> getSystemCategories();
	
	/**
	 * Returns all the position specific categories.
	 * 
	 * @param position The position
	 * @return A list of categories
	 */
	public List<Category> getPositionCategories(PositionRef position);
	
	/**
	 * Returns the list of categories for the specified position with the
	 * configuration of the system and the position. The list is deduplicated
	 * and the position categories have precedence.
	 * 
	 * @param position The position
	 * @return A deduplicated list of categories
	 */
	public List<Category> getAvailableCategoriesFor(Position position);
	
	public Category getCategoryByKey(Long categoryKey);
	
	public int countApplications(Category category);
	
	public void deleteCategory(Category category);
	
	public void deleteCategory(Category category, Category replacementCategory);
	
	/**
	 * Set categories to an application.
	 * 
	 * @param application The application to modify
	 * @param categories The categories to set
	 * @param administrativeCategoriesAllowed Allowed to add/remove administrative categories
	 * @param identity The identity which sets the categories
	 * @param locale Locale used to log the changes
	 */
	public void setCategories(Application application, Collection<String> categories, boolean administrativeCategoriesAllowed,
			Position position, Identity identity, Locale locale);
	
	/**
	 * Add categories to an application, the method will ignore categories already linked
	 * to the specified application.
	 * 
	 * @param application The application to modify
	 * @param categories The categories to add
	 * @param administrativeCategoriesAllowed Allowed to add/remove administrative categories
	 * @param identity The identity which adds the categories
	 * @param locale Locale used to log the changes
	 * @return The effectively added categories
	 */
	public List<Category> addCategories(Application application, Collection<String> categories, boolean administrativeCategoriesAllowed,
			Position position, Identity identity, Locale locale);
	
	/**
	 * Remove categories to an application, the method will ignore categories not linked
	 * to the specified application.
	 * 
	 * @param application The application to modify
	 * @param categories The categories to remove
	 * @param identity The identity which remove the categories
	 * @param locale Locale used to log the changes
	 */
	public void removeCategories(Application application, Collection<String> categories, Identity identity, Locale locale);

	/**
	 * 
	 * @param position The position
	 * @param administrativeIncluded Include or exclude the administrative tags
	 * @return A list of application to categories objects
	 */
	public List<ApplicationCategoryInfos> getApplicationCategories(Position position, boolean administrativeIncluded);
	
	/**
	 * Returns the categories linked to the specified application.
	 * 
	 * @param position The position
	 * @param application The application
	 * @param administrativeIncluded Include or exclude the administrative tags
	 * @return A list of application to categories objects
	 */
	public List<ApplicationCategoryInfos> getApplicationCategories(Position position, ApplicationRef application, boolean administrativeIncluded);

	public List<ApplicationCategoryInfos> getApplicationCategories(Position position, List<ApplicationRef> applications, boolean administrativeIncluded);
	
	public boolean hasApplicationCategories(PositionRef position, boolean systemCategories, boolean positionCategories);
	
	public boolean removeApplicationCategories(PositionRef position, boolean systemCategories, boolean positionCategories);
	
	/**
	 * Returns a map application key to the list of categories if the tagging
	 * service is enable or an empty map otherwise.
	 * 
	 * @param position The position
	 * @return A map where the application key is the key and the categories are the associated value
	 */
	public Map<Long,List<ApplicationCategoryInfos>> getApplicationToCategories(Position position, boolean administrativeIncluded);

}
