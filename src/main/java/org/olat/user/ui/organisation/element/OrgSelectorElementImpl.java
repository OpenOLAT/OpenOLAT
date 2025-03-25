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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
	private boolean liveUpdate;
	private Translator orgTranslator;

	public OrgSelectorElementImpl(WindowControl wControl, String name, List<Organisation> orgs) {
		super(name);
		initOrgTree(orgs);
		initOrgRows(orgs);
		this.wControl = wControl;
		this.component = new OrgSelectorComponent(name, this);
		String dispatchId = component.getDispatchID();
		String buttonId = dispatchId + "_org_sel";
		button = new FormLinkImpl(buttonId, buttonId, "", Link.BUTTON | Link.NONTRANSLATED);
		button.setDomReplacementWrapperRequired(false);
		button.setTranslator(translator);
		button.setElementCssClass("o_org_selector_button o_can_have_focus");
		button.getComponent().setLabelCSS("o_org_selector_span");
		button.setIconRightCSS("o_icon o_icon_caret");
		components.put(buttonId, button);
		rootFormAvailable(button);
	}

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
		orgRows = orgs.stream().map(org -> mapToOrgRow(org, orgKeyToName))
				.sorted(this::orgPathComparator)
				.collect(Collectors.toList());
	}

	private int orgPathComparator(OrgRow orgRow1, OrgRow orgRow2) {
		return orgRow1.path.compareTo(orgRow2.path);
	}

	private void initOrgTree(List<Organisation> orgs) {
		Map<Long, OrgNode> workingMap = new HashMap<>();
		workingMap.put(ROOT_ORG_KEY, new OrgNode(null));

		for (Organisation org : orgs) {
			OrgNode orgNode = new OrgNode(org);
			workingMap.put(org.getKey(), orgNode);
		}

		// Set all parents in all nodes:
		OrgNode root = workingMap.get(ROOT_ORG_KEY);
		for (OrgNode orgNode : workingMap.values()) {
			if (orgNode.isRootNode()) {
				continue;
			}
			if (orgNode.data.getParent() == null) {
				orgNode.setParent(root);
			} else {
				OrgNode parent = workingMap.get(orgNode.data.getParent().getKey());
				orgNode.setParent(parent);
			}
		}

		// Set all children in all nodes:
		for (OrgNode orgNode : workingMap.values()) {
			if (orgNode.isRootNode()) {
				continue;
			}
			OrgNode parent = orgNode.getParent();
			parent.getChildren().add(orgNode);
		}
		
		orgRoot = workingMap.get(-1L);
		orgRoot.calculateNumberOfElements();
	}

	private OrgRow mapToOrgRow(Organisation org, Map<Long, String> orgKeyToName) {
		Long key = org.getKey();
		String path = Arrays.stream(org.getMaterializedPathKeys().split("/")).map(String::trim)
				.filter(StringHelper::containsNonWhitespace)
				.map(Long::parseLong).map(orgKeyToName::get).collect(Collectors.joining(" / "));
		String title = org.getDisplayName();
		String location = org.getLocation();
		OrgNode orgNode = orgRoot.find(org.getKey());
		int numberOfElements = orgNode != null ? orgNode.getNumberOfElements() : -1;
		
		return new OrgRow(key, path, title, location, numberOfElements);
	}

	@Override
	public void setSelection(Collection<Long> orgKeys) {
		if (!multipleSelection && orgKeys.size() > 1) {
			throw new AssertionError("Trying to select multiple organisations with multiple selection turned off");
		}
		selectedKeys = orgKeys == null ? new HashSet<>() : new HashSet<>(orgKeys);
		updateButtonUI();
	}

	@Override
	public void setSelection(Long orgKey) {
		setSelection(Set.of(orgKey));
	}

	@Override
	public Set<Long> getSelection() {
		return selectedKeys;
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
				selectedKeys = orgsSelectedEvent.getKeys();
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
		boolean noOrgSelected = false;
		
		String linkTitle = orgRows.stream()
				.filter(orgRow -> selectedKeys.contains(orgRow.key()))
				.map(OrgRow::title)
				.collect(Collectors.joining(", "));

		if (!StringHelper.containsNonWhitespace(linkTitle)) {
			linkTitle = "&nbsp;";
			noOrgSelected = true;
		}
		if (button != null) {
			button.setI18nKey(linkTitle);
			if (noOrgSelected) {
				if (orgTranslator == null) {
					if (getTranslator() != null) {
						orgTranslator = Util.createPackageTranslator(OrgSelectorElementImpl.class, getTranslator().getLocale());
					}
				}
				if (orgTranslator != null) {
					button.setI18nKey(orgTranslator.translate("selector.none"));
				}
			}
			component.setDirty(true);
		}
	}
	
	private void doOpenSelector(UserRequest ureq) {
		orgSelectorCtrl = new OrgSelectorController(ureq, wControl, orgRows, selectedKeys, multipleSelection, liveUpdate);
		orgSelectorCtrl.addControllerListener(this);

		calloutCtrl = new CloseableCalloutWindowController(ureq, wControl, orgSelectorCtrl.getInitialComponent(),
				button.getFormDispatchId(), "", true, "",
				new CalloutSettings(false, CalloutSettings.CalloutOrientation.bottomOrTop, false, null));
		calloutCtrl.addControllerListener(this);
		calloutCtrl.activate();
	}

	public void setMultipleSelection(boolean multipleSelection) {
		this.multipleSelection = multipleSelection;
	}

	public boolean isMultipleSelection() {
		return multipleSelection;
	}

	public void setLiveUpdate(boolean liveUpdate) {
		this.liveUpdate = liveUpdate;
	}

	public record OrgRow(long key, String path, String title, String location, int numberOfElements) {}

	public static class OrgNode {
		private final Organisation data;
		private OrgNode parent = null;
		private final List<OrgNode> children = new ArrayList<>();
		private int numberOfElements;

		public OrgNode(Organisation data) {
			this.data = data;
		}

		boolean isRootNode() {
			return data == null;
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
