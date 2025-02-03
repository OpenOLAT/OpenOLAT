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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 juil. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteCurriculumElementController extends ConfirmationController {
	
	private MultipleSelectionElement notifyEl;
	
	private final List<Long> membersKeys;
	private final List<RepositoryEntry> references;
	private final List<CurriculumElement> descendants;
	private final CurriculumElement curriculumElement;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CurriculumService curriculumService;
	
	public ConfirmDeleteCurriculumElementController(UserRequest ureq, WindowControl wControl,
			String message, String confirmation, String confirmButton,
			CurriculumElement curriculumElement, List<CurriculumElement> descendants) {
		super(ureq, wControl, message, confirmation, confirmButton, ButtonType.danger, false);
		setTranslator(Util.createPackageTranslator(ConfirmationController.class, getLocale(), getTranslator()));
		
		this.curriculumElement = curriculumElement;
		this.descendants = descendants.stream()
				.filter(element -> element.getElementStatus() != CurriculumElementStatus.deleted)
				.filter(element -> !element.equals(curriculumElement))
				.toList();
		references = new ArrayList<>(new HashSet<>(curriculumService.getRepositoryEntriesWithDescendants(curriculumElement)));
		
		List<CurriculumElementRef> elements = new ArrayList<>();
		elements.add(curriculumElement);
		elements.addAll(descendants);
		membersKeys = curriculumService.getMemberKeys(elements, CurriculumRoles.participant.name(), CurriculumRoles.coach.name(), CurriculumRoles.owner.name(),
				 CurriculumRoles.mastercoach.name(), CurriculumRoles.curriculumelementowner.name());
		
		initForm(ureq);
	}
	
	@Override
	protected void initFormElements(FormLayoutContainer confirmCont) {
		StringBuilder impact = new StringBuilder();
		impact.append("<ul class='o_static_list'>");
		if(!descendants.isEmpty()) {
			impact.append("<li>")
			      .append(translate("curriculums.elements.bulk.delete.impacts.descendants", String.valueOf(descendants.size())))
			      .append("</li>");
		}
		if(!references.isEmpty()) {
			impact.append("<li>")
		      .append(translate("curriculums.elements.bulk.delete.impacts.entries", String.valueOf(references.size())))
		      .append("</li>");
		}
		if(!membersKeys.isEmpty()) {
			impact.append("<li>")
		          .append(translate("curriculums.elements.bulk.delete.impacts.members", String.valueOf(membersKeys.size())))
		          .append("</li>");
		}
		impact.append("</ul>");

		StaticTextElement impactEl = uifactory.addStaticTextElement("curriculums.elements.bulk.delete.impacts", impact.toString(), confirmCont);
		impactEl.setDomWrapperElement(DomWrapperElement.div);
		impactEl.setVisible(impact.length() > 10);
		
		if(!membersKeys.isEmpty()) {
			notifyEl = uifactory.addCheckboxesHorizontal("curriculums.elements.bulk.delete.notify", "curriculums.elements.bulk.delete.notify", confirmCont,
					new String[] { "notify" }, new String[] { translate("curriculums.elements.bulk.delete.notify.val") });
		}
	}
	
	@Override
	protected void doAction(UserRequest ureq) {
		boolean notify = notifyEl != null && notifyEl.isAtLeastSelected(1);
		CurriculumElement rootElement = curriculumService.getImplementationOf(curriculumElement);
		curriculumService.deleteSoftlyCurriculumElement(curriculumElement, getIdentity(), notify);
		dbInstance.commitAndCloseSession();
		curriculumService.numberRootCurriculumElement(rootElement);
		dbInstance.commitAndCloseSession();
		
		super.doAction(ureq);
	}
}
