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
package org.olat.repository.wizard.ui;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.RepositoyUIFactory;
import org.olat.repository.ui.author.MediaContainerFilter;
import org.olat.repository.wizard.InfoMetadata;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class InfoMetadataController extends StepFormBasicController {
	
	public static final String RUN_CONTEXT_KEY = "infoMetadata";

	private TextElement displayNameEl;
	private TextElement externalRefEl;
	private RichTextElement descriptionEl;
	private TextElement authorsEl;
	private MultipleSelectionElement taxonomyLevelEl;
	
	private final RepositoryEntry entry;
	private final InfoMetadata context;
	private VFSContainer mediaContainer;
	
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	@Autowired
	private TaxonomyService taxonomyService;

	public InfoMetadataController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setBasePackage(RepositoryService.class);
		entry = (RepositoryEntry) getFromRunContext("repoEntry");
		context = (InfoMetadata)getOrCreateFromRunContext(RUN_CONTEXT_KEY, getInfoMetaSupplier());
		
		initForm(ureq);
	}
	
	private Supplier<Object> getInfoMetaSupplier() {
		return () -> {
			InfoMetadata infoMetadata = new InfoMetadata();
			infoMetadata.setDisplayName(entry.getDisplayname());
			return infoMetadata;
		};
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("details.info.title");
		
		displayNameEl = uifactory.addTextElement("cif.displayname", "cif.displayname", 100, context.getDisplayName(), formLayout);
		displayNameEl.setDisplaySize(30);
		displayNameEl.setMandatory(true);
		
		externalRefEl = uifactory.addTextElement("cif.externalref", "cif.externalref", 255, context.getExternalRef(), formLayout);
		
		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(entry);
		mediaContainer = handler.getMediaContainer(entry);
		if(mediaContainer != null && mediaContainer.getName().equals("media")) {
			mediaContainer = mediaContainer.getParentContainer();
			mediaContainer.setDefaultItemFilter(new MediaContainerFilter(mediaContainer));
		}
		
		String desc = (context.getDescription() != null ? context.getDescription() : " ");
		descriptionEl = uifactory.addRichTextElementForStringData("cif.description", "cif.description",
				desc, 10, -1, false, mediaContainer, null, formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.getEditorConfiguration().setFileBrowserUploadRelPath("media");
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		
		authorsEl = uifactory.addTextElement("cif.authors", "cif.authors", 255, context.getAuthors(), formLayout);
		authorsEl.setDisplaySize(60);
		
		String taxonomyTreeKey = repositoryModule.getTaxonomyTreeKey();
		if(StringHelper.isLong(taxonomyTreeKey)) {
			TaxonomyRef taxonomyRef = new TaxonomyRefImpl(Long.valueOf(taxonomyTreeKey));
			initFormTaxonomy(formLayout, taxonomyRef);
		}
	}
	
	private void initFormTaxonomy(FormItemContainer formLayout, TaxonomyRef taxonomyRef) {
		List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomyRef);

		KeyValues keyValues = RepositoyUIFactory.createTaxonomyLevelKV(allTaxonomyLevels);
		taxonomyLevelEl = uifactory.addCheckboxesDropdown("taxonomyLevels", "cif.taxonomy.levels", formLayout,
				keyValues.keys(), keyValues.values(), null, null);
		if (context.getTaxonomyLevelRefs() != null) {
			RepositoyUIFactory.selectTaxonomyLevels(taxonomyLevelEl, context.getTaxonomyLevelRefs());
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= RepositoyUIFactory.validateTextElement(displayNameEl, true, 110);
		allOk &= RepositoyUIFactory.validateTextElement(externalRefEl, false, 255);
		allOk &= RepositoyUIFactory.validateTextElement(descriptionEl, false, 80000);
		allOk &= RepositoyUIFactory.validateTextElement(authorsEl, false, 2000);
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String title = displayNameEl.getValue();
		context.setDisplayName(title);
		
		String externalRef = externalRefEl != null && StringHelper.containsNonWhitespace(externalRefEl.getValue())
				? externalRefEl.getValue()
				: null;
		context.setExternalRef(externalRef);
		
		String description = descriptionEl.getValue().trim();
		context.setDescription(description);
		
		String authors = authorsEl.getValue().trim();
		context.setAuthors(authors);
		
		Collection<TaxonomyLevelRef> taxonomyLevelRefs = taxonomyLevelEl != null && taxonomyLevelEl.isAtLeastSelected(1)
				? taxonomyLevelEl.getSelectedKeys().stream()
						.map(Long::valueOf)
						.map(TaxonomyLevelRefImpl::new)
						.collect(Collectors.toList())
				: null;
		context.setTaxonomyLevelRefs(taxonomyLevelRefs);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
