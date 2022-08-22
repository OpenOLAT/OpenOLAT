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
package org.olat.modules.taxonomy.ui.component;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.ui.CompetenceBrowserController;
import org.olat.modules.taxonomy.ui.CompetenceBrowserController.TaxonomyLevelSelectionEvent;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelectionController.SelectionEvent;

/**
 * 
 * Custom form item to select taxonomy levels. It is influenced by the
 * AutoCompletionMultiSelection, but has some convenience methods to handle
 * taxonomy levels.
 * 
 * Initial date: 5 Jul 2022<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelSelectionImpl extends FormItemImpl implements TaxonomyLevelSelection, FormItemCollection, ControllerEventListener {
	
	private final TaxonomyLevelSelectionComponent component;
	private final WindowControl wControl;
	private final Map<String, FormItem> components = new HashMap<>(1);
	private final FormLink button;
	private Translator taxonomyTranslator;
	
	private CloseableCalloutWindowController calloutCtrl;
	private TaxonomyLevelSelectionController selectionCtrl;
	private CloseableModalController cmc;
	private CompetenceBrowserController browseCtrl;
	
	private final Set<TaxonomyLevel> allTaxonomyLevels;
	private final List<Taxonomy> allTaxonomies;
	private String displayNameHeader;
	private Set<Long> selectedKeys = new HashSet<>(3);

	public TaxonomyLevelSelectionImpl(WindowControl wControl, String name, Set<TaxonomyLevel> allTaxonomyLevels) {
		super(name);
		this.wControl = wControl;
		this.allTaxonomyLevels = allTaxonomyLevels;
		this.allTaxonomies = allTaxonomyLevels.stream().map(TaxonomyLevel::getTaxonomy).distinct().collect(Collectors.toList());
		this.component = new TaxonomyLevelSelectionComponent(name, this);
		
		String dispatchId = component.getDispatchID();
		String id = dispatchId + "_tl";
		button = new FormLinkImpl(id, id, "", Link.BUTTON | Link.NONTRANSLATED);
		button.setDomReplacementWrapperRequired(false);
		button.setTranslator(translator);
		button.setElementCssClass("o_tax_ls_button");
		button.setIconRightCSS("o_icon o_icon_caret");
		components.put(id, button);
		rootFormAvailable(button);
	}

	@Override
	public void setDisplayNameHeader(String displayNameHeader) {
		this.displayNameHeader = displayNameHeader;
	}

	@Override
	public void setSelection(Collection<? extends TaxonomyLevelRef> taxonomyLevels) {
		selectedKeys = taxonomyLevels == null 
				? selectedKeys = new HashSet<>(3)
				: taxonomyLevels.stream().map(TaxonomyLevelRef::getKey).collect(Collectors.toSet());
		updateButtonUI();
	}

	@Override
	public Set<TaxonomyLevelRef> getSelection() {
		return selectedKeys.stream()
				.map(TaxonomyLevelRefImpl::new)
				.collect(Collectors.toSet());
	}

	public FormLink getButton() {
		return button;
	}
	
	@Override
	public Iterable<FormItem> getFormItems() {
		return new ArrayList<>(components.values());
	}

	@Override
	public FormItem getFormComponent(String name) {
		return components.get(name);
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	protected void rootFormAvailable() {
		rootFormAvailable(button);
		taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, getTranslator().getLocale());
	}
	
	private final void rootFormAvailable(FormItem item) {
		if (item != null && item.getRootForm() != getRootForm()) {
			item.setRootForm(getRootForm());
		}
	}

	@Override
	public void reset() {
		//
	}
	
	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String dispatchuri = form.getRequestParameter("dispatchuri");
		if(button != null && button.getFormDispatchId().equals(dispatchuri)) {
			doOpenSelection(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(selectionCtrl == source) {
			if(event == TaxonomyLevelSelectionController.BROWSE_EVENT) {
				calloutCtrl.deactivate();
				doOpenBrowser(ureq);
			} else if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				selectedKeys = se.getKeys();
				calloutCtrl.deactivate();
				cleanUp();
				updateButtonUI();
				getFormItemComponent().setDirty(true);
				Command dirtyOnLoad = FormJSHelper.getFlexiFormDirtyOnLoadCommand(getRootForm());
				wControl.getWindowBackOffice().sendCommandTo(dirtyOnLoad);
			} else if(event == Event.CANCELLED_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(calloutCtrl == source) {
			cleanUp();
		} else if (source == browseCtrl) {
			if (event instanceof TaxonomyLevelSelectionEvent) {
				TaxonomyLevelSelectionEvent tlse = (TaxonomyLevelSelectionEvent)event;
				List<TaxonomyLevel> taxonomyLevels = tlse.getTaxonomyLevels();
				doAddTaxonomyLevels(taxonomyLevels);
			}
			cmc.deactivate();
			cleanUpBrowse();
			calloutCtrl.activate();
		} else if(cmc == source) {
			cleanUpBrowse();
			calloutCtrl.activate();
		}
	}

	private void cleanUp() {
		calloutCtrl = cleanUp(calloutCtrl);
		selectionCtrl = cleanUp(selectionCtrl);
		cleanUpBrowse();
	}
	
	private void cleanUpBrowse() {
		browseCtrl = cleanUp(browseCtrl);
		cmc = cleanUp(cmc);
	}
	
	private <T extends Controller> T cleanUp(T ctrl) {
		if (ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}
	
	private void updateButtonUI() {
		String linkTitle = allTaxonomyLevels.stream()
				.filter(level -> selectedKeys.contains(level.getKey()))
				.map(level -> TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, level))
				.filter(Objects::nonNull)
				.sorted(Collator.getInstance(getTranslator().getLocale()))
				.map(this::toLabel)
				.collect(Collectors.joining());
		if (!StringHelper.containsNonWhitespace(linkTitle)) {
			linkTitle = "&nbsp;";
		}
		linkTitle = "<div class=\"o_tax_ls_tags\">" + linkTitle + "</div>";
		button.setI18nKey(linkTitle);
	}
	
	private String toLabel(String levelName) {
		return "<span class=\"o_tax_ls_tag o_tag\">" + levelName + "</span>";
	}
	
	private void doOpenSelection(UserRequest ureq) {
		selectionCtrl = new TaxonomyLevelSelectionController(ureq, wControl, allTaxonomyLevels, selectedKeys);
		selectionCtrl.addControllerListener(this);

		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, selectionCtrl.getInitialComponent(),
				button.getFormDispatchId(), "", true, "", new CalloutSettings(false, CalloutOrientation.bottom, false, null));
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();
	}

	private void doAddTaxonomyLevels(List<TaxonomyLevel> taxonomyLevels) {
		Collection<Long> keys = taxonomyLevels.stream().map(TaxonomyLevel::getKey).collect(Collectors.toList());
		selectionCtrl.addTaxonomyLevelKeys(keys);
	}
	
	private void doOpenBrowser(UserRequest ureq) {
		browseCtrl = new CompetenceBrowserController(ureq, wControl, allTaxonomies, allTaxonomyLevels, true, displayNameHeader);
		browseCtrl.addControllerListener(this);
		
		String title = taxonomyTranslator.translate("taxonomy.level.selection.browse");
		cmc = new CloseableModalController(wControl, "close", browseCtrl.getInitialComponent(), true, title);
		cmc.activate();
		cmc.addControllerListener(this);
	}

}
