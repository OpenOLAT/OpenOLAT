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
package org.olat.user.ui.organisation.element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * Initial date: 2025-03-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OrgSelectorElementImpl extends FormItemImpl implements OrgSelectorElement, FormItemCollection, ControllerEventListener {

	private final static Long ROOT_ORG_KEY = -1L;
	
	private final OrgSelectorComponent component;
	private final WindowControl wControl;
	private final Map<String, FormItem> components = new HashMap<>();
	private final FormLink button;
	private List<OrgRow> orgRows;
	private OrgNode orgRoot;

	private OrgSelectorController orgSelectorCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	private Set<Long> orgKeys = new HashSet<>();
	private Set<Long> selectedKeys = new HashSet<>();
	private boolean multipleSelection;
	private String noSelectionText;
	private final Translator orgTranslator;

	public OrgSelectorElementImpl(WindowControl wControl, String name, List<Organisation> orgs, Locale locale) {
		super(name);
		orgTranslator = Util.createPackageTranslator(OrgSelectorElementImpl.class, locale);
		if (orgs == null) {
			orgs = new ArrayList<>();
		}
		initOrgTree(orgs);
		initOrgRows(orgs);
		this.wControl = wControl;
		this.component = new OrgSelectorComponent(name, this);
		String dispatchId = component.getDispatchID();
		String buttonId = dispatchId + "_org_sel";
		button = new FormLinkImpl(buttonId, buttonId, "", Link.BUTTON | Link.NONTRANSLATED);
		button.setDomReplacementWrapperRequired(false);
		button.setElementCssClass("o_org_selector_button o_can_have_focus");
		button.getComponent().setLabelCSS("o_org_selector_span");
		button.setIconRightCSS("o_icon o_icon_caret");
		components.put(buttonId, button);
		rootFormAvailable(button);
		updateButtonUI();
	}

	@Override
	public Set<Long> getKeys() {
		return orgKeys;
	}
	
	private void initOrgRows(List<Organisation> orgs) {
		Map<Long, String> orgKeyToName = new HashMap<>();
		orgKeys = new HashSet<>();
		for (Organisation org : orgs) {
			orgKeyToName.put(org.getKey(), org.getDisplayName());
			orgKeys.add(org.getKey());
		}
		addMissingOrgKeys(orgKeyToName, orgs, orgKeys);
		
		orgRows = orgs.stream().map(org -> mapToOrgRow(org, orgKeyToName))
				.sorted(this::orgPathComparator)
				.collect(Collectors.toList());
	}

	private void addMissingOrgKeys(Map<Long, String> orgKeyToName, List<Organisation> orgs, Set<Long> orgKeys) {
		Set<Long> missingOrgKeys = getMissingOrgKeys(orgs, orgKeys);
		if (missingOrgKeys.isEmpty()) {
			return;
		}
		OrganisationDAO organisationDAO = CoreSpringFactory.getImpl(OrganisationDAO.class);
		List<OrganisationRef> missingOrgRefs = missingOrgKeys.stream().map(OrganisationRefImpl::new)
				.map(o -> (OrganisationRef) o).toList();
		List<Organisation> missingOrgs = organisationDAO.getOrganisations(missingOrgRefs);
		for (Organisation missingOrg : missingOrgs) {
			orgKeyToName.put(missingOrg.getKey(), missingOrg.getDisplayName());
			orgKeys.add(missingOrg.getKey());
		}
	}

	private Set<Long> getMissingOrgKeys(List<Organisation> orgs, Set<Long> orgKeys) {
		HashSet<Long> referencedOrgKeys = getReferencedOrgKeys(orgs);
		referencedOrgKeys.removeAll(orgKeys);
		return referencedOrgKeys;
	}
	
	private HashSet<Long> getReferencedOrgKeys(Collection<Organisation> orgs) {
		HashSet<Long> keys = new HashSet<>();
		try {
			for (Organisation org : orgs) {
				if (org.getMaterializedPathKeys() != null) {
					for (String orgKeyString : org.getMaterializedPathKeys().split("/")) {
						if (StringHelper.containsNonWhitespace(orgKeyString)) {
							Long orgKey = Long.parseLong(orgKeyString);
							keys.add(orgKey);
						}
					}
				}
			}
		} catch(Exception e) {
			//
		}
		return keys;
	}

	private int orgPathComparator(OrgRow orgRow1, OrgRow orgRow2) {
		return orgRow1.path.compareTo(orgRow2.path);
	}

	private void initOrgTree(List<Organisation> orgs) {
		orgRoot = buildOrgTree(orgs);
	}

	public static OrgNode buildOrgTree(List<Organisation> orgs) {
		Map<Long, OrgNode> workingMap = new HashMap<>();
		OrgNode rootOrgNode = new OrgNode(null);
		workingMap.put(ROOT_ORG_KEY, rootOrgNode);

		for (Organisation org : orgs) {
			OrgNode orgNode = new OrgNode(org);
			workingMap.put(org.getKey(), orgNode);
		}

		// Set the parent node for all nodes:
		OrgNode root = workingMap.get(ROOT_ORG_KEY);
		for (OrgNode orgNode : workingMap.values()) {
			if (orgNode.data == null) {
				continue;
			}
			if (orgNode.data.getParent() == null || !workingMap.containsKey(orgNode.data.getParent().getKey())) {
				orgNode.setParent(root);
			} else {
				OrgNode parent = workingMap.get(orgNode.data.getParent().getKey());
				orgNode.setParent(parent);
			}
		}

		// Add each node to the children list of its parent:
		for (OrgNode orgNode : workingMap.values()) {
			if (orgNode.getParent() == null) {
				continue;
			}
			orgNode.getParent().getChildren().add(orgNode);
		}

		OrgNode orgRoot = workingMap.get(ROOT_ORG_KEY);
		orgRoot.calculateNumberOfElements();

		return orgRoot;
	}

	private OrgRow mapToOrgRow(Organisation org, Map<Long, String> orgKeyToName) {
		Long key = org.getKey();
		String path = StringHelper.containsNonWhitespace(org.getMaterializedPathKeys()) ? 
				Arrays.stream(org.getMaterializedPathKeys().split("/")).map(String::trim)
				.filter(StringHelper::containsNonWhitespace)
				.map(Long::parseLong).map(orgKeyToName::get)
				.filter(StringHelper::containsNonWhitespace)
				.collect(Collectors.joining("/")) : "";
		String displayPath = toDisplayPath(path);
		String title = org.getDisplayName();
		String location = org.getLocation();
		OrgNode orgNode = orgRoot.find(org.getKey());
		int numberOfElements = orgNode != null ? orgNode.getNumberOfElements() : -1;
		
		return new OrgRow(key, path, displayPath, title, location, numberOfElements);
	}
	
	private String toDisplayPath(String path) {
		path = path.trim();
		
		int lastSlashIndex = path.lastIndexOf('/');
		if (lastSlashIndex == -1) {
			return "/";
		}

		return "/" + path.substring(0, lastSlashIndex) + "/";
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		button.setEnabled(isEnabled);
		super.setEnabled(isEnabled);
	}

	@Override
	public void setSelection(Collection<Long> orgKeys) {
		if (!multipleSelection && orgKeys != null && orgKeys.size() > 1) {
			throw new AssertionError("Trying to select multiple organisations with multiple selection turned off");
		}
		selectedKeys = orgKeys == null ? new HashSet<>() : new HashSet<>(orgKeys);
		updateButtonUI();
	}

	@Override
	public void setSelection(Long orgKey) {
		if (orgKey == null) {
			setSelection(new HashSet<>());
			return;
		}
		setSelection(new HashSet<>(Set.of(orgKey)));
	}

	@Override
	public void select(Long orgKey, boolean selected) {
		if (selectedKeys == null) {
			selectedKeys = new HashSet<>();
		}
		if (selected) {
			selectedKeys.add(orgKey);
		} else {
			selectedKeys.remove(orgKey);
		}
		updateButtonUI();
	}

	@Override
	public void setNoSelectionText(String noSelectionText) {
		this.noSelectionText = noSelectionText;
	}

	@Override
	public Long getSingleSelection() {
		if (multipleSelection) {
			throw new AssertionError("Trying to read a single selection with multiple selection turned on.");
		}
		if (selectedKeys != null && selectedKeys.size() == 1) {
			return selectedKeys.iterator().next();
		}
		return null;
	}

	@Override
	public Set<Long> getSelection() {
		return selectedKeys;
	}

	@Override
	public Set<Long> getSelectedKeys() {
		return getSelection();
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
	}

	private void rootFormAvailable(FormLink button) {
		if (button != null && button.getRootForm() != getRootForm()) {
			button.setRootForm(getRootForm());
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
		if (button != null && button.getFormDispatchId().equalsIgnoreCase(dispatchuri)) {
			doOpenSelector(ureq);
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if (orgSelectorCtrl == source) {
			if (event instanceof OrgSelectorController.OrgsSelectedEvent orgsSelectedEvent) {
				selectedKeys = new HashSet<>(orgsSelectedEvent.getKeys());
				calloutCtrl.deactivate();
				cleanUp();
				updateButtonUI();
				if (getAction() == FormEvent.ONCHANGE) {
					getRootForm().fireFormEvent(ureq, new FormEvent("ONCHANGE", this, FormEvent.ONCHANGE));
				}
				Command focusCommand = FormJSHelper.getFormFocusCommand(getRootForm().getFormName(), button.getForId());
				getRootForm().getWindowControl().getWindowBackOffice().sendCommandTo(focusCommand);
			} else if (event == OrgSelectorController.RESIZED_EVENT) {
				calloutCtrl.handleResized();
			} else if (event == Event.CANCELLED_EVENT) {
				if (calloutCtrl != null) {
					calloutCtrl.deactivate();
				}
				cleanUp();
			}
		} else if (calloutCtrl == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		orgSelectorCtrl = cleanUp(orgSelectorCtrl);
		calloutCtrl = cleanUp(calloutCtrl);
	}
	
	private <T extends Controller> T cleanUp(T ctrl) {
		if (ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}
	
	private void updateButtonUI() {
		String linkTitle = orgRows.stream()
				.filter(orgRow -> selectedKeys.contains(orgRow.key()))
				.sorted(Comparator.comparing(OrgRow::title))
				.map(OrgRow::title)
				.collect(Collectors.joining(", "));

		if (button == null) {
			return;
		}

		if (StringHelper.containsNonWhitespace(linkTitle)) {
			button.setI18nKey(StringHelper.escapeHtml(linkTitle));
		} else {
			setNoSelectionButtonText();
		}
		if (!StringHelper.containsNonWhitespace(button.getI18nKey())) {
			button.setAriaLabel(orgTranslator.translate("selector.selection"));
		}
		component.setDirty(true);
	}

	private void setNoSelectionButtonText() {
		if (StringHelper.containsNonWhitespace(noSelectionText)) {
			button.setI18nKey(StringHelper.escapeHtml(noSelectionText));
			return;
		}

		button.setI18nKey(orgTranslator.translate("selector.none"));
	}

	private void doOpenSelector(UserRequest ureq) {
		orgSelectorCtrl = new OrgSelectorController(ureq, wControl, orgRows, selectedKeys, multipleSelection);
		orgSelectorCtrl.addControllerListener(this);

		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, orgSelectorCtrl.getInitialComponent(),
				button.getFormDispatchId(), "", true, "",
				new CalloutSettings(false, CalloutSettings.CalloutOrientation.bottomOrTop, false, null));
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();
	}

	@Override
	public void setMultipleSelection(boolean multipleSelection) {
		this.multipleSelection = multipleSelection;
	}

	@Override
	public boolean isExactlyOneSelected() {
		return selectedKeys != null && selectedKeys.size() == 1;
	}

	public boolean isMultipleSelection() {
		return multipleSelection;
	}

	public record OrgRow(long key, String path, String displayPath, String title, String location, int numberOfElements) {}

	public static class OrgNode {
		private final Organisation data;
		private OrgNode parent = null;
		private final List<OrgNode> children = new ArrayList<>();
		private int numberOfElements;

		public OrgNode(Organisation data) {
			this.data = data;
		}

		public Organisation getData() {
			return data;
		}

		public void setParent(OrgNode parent) {
			this.parent = parent;
		}
		
		public OrgNode getParent() {
			return parent;
		}

		public List<OrgNode> getChildren() {
			return children;
		}
		
		public int calculateNumberOfElements() {
			numberOfElements = 1; // self
			for (OrgNode child : children) {
				numberOfElements += child.calculateNumberOfElements();
			}
			return numberOfElements;
		}

		public int getNumberOfElements() {
			return numberOfElements;
		}

		public OrgNode find(Long key) {
			for (OrgNode child : children) {
				OrgNode result = child.find(key);
				if (result != null) {
					return result;
				}
			}

			if (data != null) {
				if (data.getKey().equals(key)) {
					return this;
				}
				return null;
			}

			if (ROOT_ORG_KEY.equals(key)) {
				return this;
			}
			return null;
		}
	}
}
