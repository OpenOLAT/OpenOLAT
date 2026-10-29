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
package org.olat.repository.ui;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.author.AuthorListConfiguration;
import org.olat.repository.ui.author.AuthorListController;
import org.olat.repository.ui.author.AuthoringEntryRow;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Redesigned learning-resource selection dialog. Wraps the same
 * {@link AuthorListController} used by the main author list (filter tabs,
 * Favourites/Status/More-filter, column customization) as the body of a
 * {@link org.olat.core.gui.control.generic.closablewrapper.FooterModalController},
 * with its own sticky-footer Select/Cancel buttons.
 *
 * Initial date: 2026-10-26<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RepositoryEntrySelectionController extends BasicController {

	private final VelocityContainer footerVC;
	private final Link selectButton;
	private final Link cancelButton;
	private final AuthorListController authorListCtrl;

	private RepositoryEntry selectedEntry;

	@Autowired
	private RepositoryManager repositoryManager;

	public RepositoryEntrySelectionController(UserRequest ureq, WindowControl wControl, String tableId,
											  List<String> resourceTypes, List<Organisation> defaultOrganisations, 
											  RepositoryEntryReferenceProvider referenceProvider) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));

		AuthorListConfiguration configuration = AuthorListConfiguration.selectRessource(tableId, resourceTypes);
		configuration.setSharedWithMeTab(true);
		configuration.setTableConfiguration(referenceProvider.getTableConfiguration());
		configuration.setMyTabLabel(referenceProvider.getMyTabLabel());
		configuration.setEmptyScreenDataForNoEntriesYet(referenceProvider.getEmptyScreenDataForNoEntriesYet());
		configuration.setEmptyScreenDataForNoEntriesInView(referenceProvider.getEmptyScreenDataForNoEntriesInView());
		configuration.setEnterSearchTermMessage(referenceProvider.getEnterSearchTermMessage());
		configuration.setRedesignedSelectionDialog(true);
		configuration.setImportRessources(false);
		configuration.setCreateRessources(false);

		SearchAuthorRepositoryEntryViewParams searchParams = new SearchAuthorRepositoryEntryViewParams(
				getIdentity(), ureq.getUserSession().getRoles());
		searchParams.setResourceTypes(resourceTypes);
		if (defaultOrganisations != null && !defaultOrganisations.isEmpty()) {
			List<OrganisationRef> organisationRefs = defaultOrganisations.stream()
					.map(OrganisationRef.class::cast)
					.collect(Collectors.toList());
			searchParams.setEntryOrganisations(organisationRefs);
		}

		authorListCtrl = new AuthorListController(ureq, wControl, searchParams, configuration);
		listenTo(authorListCtrl);
		authorListCtrl.selectFilterTab(ureq, authorListCtrl.getMyTab());

		footerVC = createVelocityContainer("repository_entry_selection_footer");
		selectButton = LinkFactory.createButton("select", footerVC, this);
		selectButton.setElementCssClass("o_sel_re_selection_select");
		selectButton.setPrimary(true);
		cancelButton = LinkFactory.createButton("cancel", footerVC, this);
		cancelButton.setElementCssClass("o_sel_re_selection_cancel");

		putInitialPanel(authorListCtrl.getInitialComponent());
	}

	/**
	 * The scrollable body for the wrapping {@code FooterModalController}.
	 */
	public Component getBodyComponent() {
		return authorListCtrl.getInitialComponent();
	}

	/**
	 * The sticky-footer Select/Cancel row for the wrapping {@code FooterModalController}.
	 */
	public Component getFooterComponent() {
		return footerVC;
	}

	public RepositoryEntry getSelectedEntry() {
		return selectedEntry;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == selectButton) {
			List<AuthoringEntryRow> rows = authorListCtrl.getMultiSelectedRows();
			if (rows.size() == 1) {
				selectedEntry = repositoryManager.lookupRepositoryEntry(rows.get(0).getKey());
				fireEvent(ureq, ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED);
			} else {
				showWarning("bulk.update.nothing.selected");
			}
		} else if (source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
		}
	}
}
