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
package org.olat.modules.catalog.filter;


import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.ui.admin.CatalogFilterBasicController;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ResourceTypeHandler implements CatalogFilterHandler {
	
	private static final String TYPE = "resourcetype";
	
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled(boolean isGuestOnly) {
		return true;
	}

	@Override
	public int getSortOrder() {
		return 110;
	}

	@Override
	public String getTypeI18nKey() {
		return "filter.resource.type.type";
	}

	@Override
	public String getAddI18nKey() {
		return "filter.resource.type.add";
	}

	@Override
	public String getEditI18nKey() {
		return "filter.resource.type.edit";
	}

	@Override
	public String getDetails(Translator translator, CatalogFilter catalogFilter) {
		return null;
	}

	@Override
	public boolean isMultiInstance() {
		return false;
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogFilter catalogFilter) {
		return new CatalogFilterBasicController(ureq, wControl, this, catalogFilter);
	}

	@Override
	public FlexiTableExtendedFilter createFlexiTableFilter(Translator translator, CatalogRepositoryEntrySearchParams searchParams, CatalogFilter catalogFilter) {
		Translator repositoryTranslator = Util.createPackageTranslator(RepositoryService.class, translator.getLocale());
		
		Collection<String> launcherResourceTypes = searchParams.getIdentToResourceTypes().get(CatalogRepositoryEntrySearchParams.KEY_LAUNCHER);
		
		// Only one resource type: Filter makes no sense.
		if (launcherResourceTypes != null && launcherResourceTypes.size() == 1) {
			return null;
		}
		
		SelectionValues resourceTypeKV = new SelectionValues();
		repositoryHandlerFactory.getOrderRepositoryHandlers().stream()
				.map(handler -> handler.getHandler().getSupportedType())
				.filter(resourceType -> filterByLauncherTypes(resourceType, launcherResourceTypes))
				.forEach(type -> resourceTypeKV
				.add(new SelectionValue(
						type,
						repositoryTranslator.translate(type),
						null, 
						"o_icon o_icon-fw ".concat(RepositoyUIFactory.getIconCssClass(type)),
						null,
						true)));
		resourceTypeKV.sort(SelectionValues.VALUE_ASC);
		FlexiTableMultiSelectionFilter filter = new FlexiTableMultiSelectionFilter(repositoryTranslator.translate("cif.type"), TYPE,
				resourceTypeKV, catalogFilter.isDefaultVisible());
		filter.setUserObject(catalogFilter.getKey().toString());
		return filter;
	}

	private boolean filterByLauncherTypes(String resourceType, Collection<String> launcherResourceTypes) {
		return launcherResourceTypes == null || launcherResourceTypes.isEmpty()
				? true
				: launcherResourceTypes.contains(resourceType);
	}

	@Override
	public void enrichSearchParams(CatalogRepositoryEntrySearchParams searchParams, FlexiTableFilter flexiTableFilter) {
		List<String> resourceTypes = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		String ident = (String)flexiTableFilter.getUserObject();
		if (resourceTypes != null && !resourceTypes.isEmpty()) {
			searchParams.getIdentToResourceTypes().put(ident, resourceTypes);
		} else {
			searchParams.getIdentToResourceTypes().remove(ident);
		}
	}
	
}
