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
package org.olat.repository.ui.author;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.ErrorList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.resource.references.ReferenceInfos;
import org.olat.resource.references.ReferenceManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeletePermanentlyController extends FormBasicController {
	
	private FormLink deleteButton;
	private SingleSelection deleteReferencesEl;
	private MultipleSelectionElement acknowledgeEl, referencesEl;
	
	private final int numOfMembers;
	private final boolean notAllDeleteable;
	private final List<RepositoryEntry> rows;
	private final List<ReferenceInfos> references;
	
	private static final String[] yesNo = new String[] { "yes", "no" };
	
	@Autowired
	private ReferenceManager referenceManager;
	@Autowired
	private RepositoryService repositoryService;
	
	public ConfirmDeletePermanentlyController(UserRequest ureq, WindowControl wControl, List<RepositoryEntry> rows, boolean notAllDeleteable) {
		super(ureq, wControl, "confirm_delete_permanent");
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		this.rows = rows;
		this.notAllDeleteable = notAllDeleteable;
		numOfMembers = repositoryService.countMembers(rows, getIdentity());
		references = referenceManager.getReferencesInfos(rows, getIdentity());
		if(references.size() > 1) {
			Collections.sort(references, new ReferenceInfosComparator(Collator.getInstance(getLocale())));
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layout = (FormLayoutContainer)formLayout;
			layout.contextPut("notAllDeleteable", Boolean.valueOf(notAllDeleteable));
			layout.contextPut("numOfMembers", Integer.toString(numOfMembers));

			FormLayoutContainer layoutCont = FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
			formLayout.add("confirm", layoutCont);
			layoutCont.setRootForm(mainForm);
			
			StringBuilder message = new StringBuilder();
			for(RepositoryEntry row:rows) {
				if(message.length() > 0) message.append(", ");
				message.append(StringHelper.escapeHtml(row.getDisplayname()));
			}
			uifactory.addStaticTextElement("rows", "details.delete.entries", message.toString(), layoutCont);

			String[] acknowledge = new String[] { translate("details.delete.acknowledge.msg") };
			acknowledgeEl = uifactory.addCheckboxesHorizontal("confirm", "details.delete.acknowledge", layoutCont, new String[]{ "" },  acknowledge);
			
			int pos = 0;
			boolean hasOrphans = false;
			String[] referenceKeys = new String[references.size()];
			String[] referenceValues = new String[references.size()];
			for(ReferenceInfos reference:references) {
				hasOrphans |= reference.isOrphan() && reference.isOwner() && !reference.isManaged();
				referenceKeys[pos] = reference.getEntry().getKey().toString();
				referenceValues[pos++] = getReferenceElValue(reference);
			}
			String[] yesNoValues = new String[] { translate("yes"), translate("no") };
			deleteReferencesEl = uifactory.addRadiosHorizontal("references.yesno", "details.delete.references", layoutCont, yesNo,  yesNoValues);
			deleteReferencesEl.setVisible(hasOrphans);
			deleteReferencesEl.addActionListener(FormEvent.ONCHANGE);

			referencesEl = uifactory.addCheckboxesVertical("references.list", null, layoutCont, referenceKeys, referenceValues, 1);
			referencesEl.setVisible(false);
			applyDefaultToReferenceList();
			
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			layoutCont.add(buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			deleteButton = uifactory.addFormLink("details.delete", buttonsCont, Link.BUTTON);
		}
	}
	
	private String getReferenceElValue(ReferenceInfos reference) {
		StringBuilder referenceValue = new StringBuilder(64);
		referenceValue.append(reference.getEntry().getDisplayname());
		if(!reference.isOrphan() || !reference.isOwner() || reference.isManaged()) {
			referenceValue.append(" (");
			if(!reference.isOrphan()) {
				referenceValue.append(translate("details.delete.notOrphan"));
			}
			if(!reference.isOwner()) {
				if(!reference.isOrphan()) {
					referenceValue.append(", ");
				}
				referenceValue.append(translate("details.delete.notOwner"));
			}
			if(reference.isManaged()) {
				if(!reference.isOrphan() || !reference.isOwner()) {
					referenceValue.append(", ");
				}
				referenceValue.append(translate("details.delete.managed"));
			}
			referenceValue.append(")");
		}
		return referenceValue.toString();
	}
	
	private void applyDefaultToReferenceList() {
		for(ReferenceInfos reference:references) {
			String key = reference.getEntry().getKey().toString();
			boolean deletable = reference.isOwner() && reference.isOrphan() && !reference.isManaged();
			referencesEl.setEnabled(key, deletable);
			referencesEl.select(key, deletable);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		acknowledgeEl.clearError();
		if(!acknowledgeEl.isAtLeastSelected(1)) {
			acknowledgeEl.setErrorKey("details.delete.acknowledge.error");
			allOk &= false;
		}
		
		deleteReferencesEl.clearError();
		if(deleteReferencesEl.isEnabled() && deleteReferencesEl.isVisible()) {
			if(!deleteReferencesEl.isOneSelected()) {
				deleteReferencesEl.setErrorKey("form.mandatory.hover");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			if(validateFormLogic(ureq)) {
				doCompleteDelete(ureq);
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(deleteReferencesEl == source) {
			if(deleteReferencesEl.isEnabled() && deleteReferencesEl.isOneSelected()) {
				if(deleteReferencesEl.isSelected(0)) {
					//yes
					referencesEl.setVisible(true);
					applyDefaultToReferenceList();
				} else {
					//no
					referencesEl.setVisible(false);
				}
			}
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doCompleteDelete(UserRequest ureq) {
		List<ErrorList> errorList = new ArrayList<>();
		boolean allOk = deleteEntries(ureq, rows, errorList);
		
		if(allOk && deleteReferencesEl.isVisible() && deleteReferencesEl.isEnabled()
				&& deleteReferencesEl.isOneSelected() && deleteReferencesEl.isSelected(0)) {
			Map<Long,ReferenceInfos> referencesMap = new HashMap<>(); 
			for(ReferenceInfos reference:references) {
				referencesMap.put(reference.getEntry().getKey(), reference);
			}
			
			Collection<String> selectedKeys = referencesEl.getSelectedKeys();
			List<RepositoryEntry> referencesToDelete = new ArrayList<>(selectedKeys.size());
			for(String selectedRefKey:selectedKeys) {
				Long key = Long.valueOf(selectedRefKey);
				ReferenceInfos refInfos = referencesMap.get(key);
				if(refInfos != null && refInfos.isOrphan() && refInfos.isOwner() && !refInfos.isManaged()) {
					referencesToDelete.add(referencesMap.get(key).getEntry());
				}
			}
			allOk &= deleteEntries(ureq, referencesToDelete, errorList);
		}
		
		if(allOk) {
			showInfo("info.entry.deleted");
		} else {
			List<String> msgs = new ArrayList<>();
			for(ErrorList error:errorList) {
				if(StringHelper.containsNonWhitespace(error.getFirstError())) {
					msgs.add(error.getFirstError());
				}
			}
			
			if(msgs.size() == 1) {
				getWindowControl().setWarning(msgs.get(0));
			} else if(msgs.size() > 1) {
				StringBuilder sb = new StringBuilder();
				sb.append("<ul>");
				for(String msg:msgs) {
					sb.append("<li>").append(msg).append("</li>");
				}
				sb.append("</ul>");
				getWindowControl().setWarning(sb.toString());
			} else {
				showWarning("info.could.not.delete.entry");
			}
		}
	}
	
	private boolean deleteEntries(UserRequest ureq, List<RepositoryEntry> entries, List<ErrorList> errorList) {
		boolean allOk = true;
		Roles roles = ureq.getUserSession().getRoles();
		for(RepositoryEntry entry:entries) {
			ErrorList errors = repositoryService.deletePermanently(entry, getIdentity(), roles, getLocale());
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_DELETE, getClass(),
					LoggingResourceable.wrap(entry, OlatResourceableType.genRepoEntry));
			if (errors.hasErrors()) {
				allOk = false;
				errorList.add(errors);
			} else {
				EntryChangedEvent e = new EntryChangedEvent(entry, getIdentity(), Change.deleted, "delete");
				ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
			}
		}
		return allOk;
	}
	
	private static class ReferenceInfosComparator implements Comparator<ReferenceInfos> {
		
		private final Collator collator;
		
		public ReferenceInfosComparator(Collator collator) {
			this.collator = collator;
		}

		@Override
		public int compare(ReferenceInfos o1, ReferenceInfos o2) {
			if(o1 == null) return -1;
			if(o2 == null) return 1;
			
			String name1 = o1.getEntry() == null ? null : o2.getEntry().getDisplayname();
			String name2 = o2.getEntry() == null ? null : o2.getEntry().getDisplayname();
			
			if(name1 == null) return -1;
			if(name2 == null) return 1;
			return collator.compare(name1, name2);
		}
	}
}
