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
package org.olat.modules.assessment.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.QTICourseNode;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.ReferenceHistoryWithInfos;
import org.olat.modules.assessment.ui.event.ReferencesHistorySelectionEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ReferenceHistoryFilterController extends BasicController {
	
	private static final String CMD_REFERENCE = "reference";
	
	private final Dropdown referencesDropdown;
	
	private int counter = 0;
	private final QTICourseNode courseNode;
	private final RepositoryEntry referencedTestEntry;
	private final List<ReferenceHistoryWithInfos> history;
	
	@Autowired
	private QTI21Service qti21service;
	@Autowired
	private RepositoryService repositoryService;
	
	public ReferenceHistoryFilterController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, QTICourseNode courseNode, RepositoryEntry testEntry) {
		super(ureq, wControl);

		this.courseNode = courseNode;
		referencedTestEntry = courseNode.getReferencedRepositoryEntry();
		
		List<ReferenceHistoryWithInfos> historyList = qti21service.getReferenceHistoryWithInfos(courseEntry, courseNode.getIdent());
		history = historyList.stream()
				.filter(entry -> entry.runs() > 0)
				.toList();
		
		VelocityContainer mainVC = createVelocityContainer("references_history");
		
		referencesDropdown = new Dropdown("references", null, false, getTranslator());
		referencesDropdown.setTranslatedLabel(displayName(testEntry));
		referencesDropdown.setButton(true);
		referencesDropdown.setEmbbeded(true);
		referencesDropdown.setCarretIconCSS("o_icon o_icon-fw o_icon_caret");
		referencesDropdown.setElementCssClass("dropdown-menu-right");
		referencesDropdown.setIconCSS("o_icon o_icon-fw o_icon_history");
		
		Set<RepositoryEntry> deduplicate = new HashSet<>();
		
		for(ReferenceHistoryWithInfos entry:historyList) {
			if(deduplicate.contains(entry.testEntry())) continue;
			
			String id = "ref-" + (++counter);
			String displayname = displayName(entry.testEntry());
			Link refLink = LinkFactory.createLink(id, id, CMD_REFERENCE, displayname, getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
			refLink.setDomReplacementWrapperRequired(false);
			refLink.setEscapeMode(EscapeMode.none);
			refLink.setUserObject(entry);
			referencesDropdown.addComponent(refLink);
			deduplicate.add(entry.testEntry());
		}
		
		mainVC.put("references.history", referencesDropdown);
		referencesDropdown.setVisible(history.size() > 1);
		putInitialPanel(mainVC);
	}
	
	private String displayName(RepositoryEntry entry) {
		String displayname = StringHelper.escapeHtml(entry.getDisplayname());
		if(StringHelper.containsNonWhitespace(entry.getExternalRef())) {
			displayname += "<small class='mute'> \u00B7 " + StringHelper.escapeHtml(entry.getExternalRef()) + "</small>";
		}
		if(entry.equals(referencedTestEntry)) {
			displayname += " " + translate("current.reference");
		}
		return displayname;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link link && link.getUserObject() instanceof ReferenceHistoryWithInfos entry) {
			String displayName = this.displayName(entry.testEntry());
			referencesDropdown.setTranslatedLabel(displayName);
			referencesDropdown.setDirty(true);
			
			RepositoryEntry re = repositoryService.loadBy(entry.testEntry());
			fireEvent(ureq, new ReferencesHistorySelectionEvent(re, courseNode));
		}
	}
}
