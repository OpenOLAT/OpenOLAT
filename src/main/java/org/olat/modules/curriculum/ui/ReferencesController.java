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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.event.SelectReferenceEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferencesController extends BasicController {
	
	private int counter = 0;
	
	@Autowired
	private CurriculumService curriculumService;
		
	public ReferencesController(UserRequest ureq, WindowControl wControl, Translator translator, CurriculumElement element) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), translator));
		VelocityContainer mainVC = createVelocityContainer("references");

		List<RepositoryEntry> refs = curriculumService.getRepositoryEntries(element);

		List<String> refLinks = new ArrayList<>(refs.size());
		for(RepositoryEntry ref:refs) {
			String name = "ref-" + (++counter);
			Link refLink = LinkFactory.createLink(name, "reference", getTranslator(), mainVC, this, Link.NONTRANSLATED);
			String label =  label(ref);
			refLink.setCustomDisplayText(label);
			refLink.setUserObject(ref);
			refLink.setIconLeftCSS("o_icon o_icon-fw " + RepositoyUIFactory.getIconCssClass(ref));
			refLinks.add(name);
		}
		mainVC.contextPut("referenceLinks", refLinks);
		
		putInitialPanel(mainVC);
	}
	
	private String label(RepositoryEntry ref) {
		StringBuilder sb = new StringBuilder(128);
		sb.append(StringHelper.escapeHtml(ref.getDisplayname()));
		if(StringHelper.containsNonWhitespace(ref.getExternalRef())) {
			sb.append(" <small>").append(StringHelper.escapeHtml(ref.getExternalRef())).append("</small>");
		}
		
		RepositoryEntryStatusEnum status = ref.getEntryStatus();
		sb.append(" <span class='o_labeled_light o_repo_status_").append(status.name())
		  .append("' title=\"").append(StringHelper.escapeHtml(translate("status." + status.name() + ".desc"))).append("\">")
		  .append("<i class='o_icon o_icon-fw o_icon_repo_status_").append(status.name()).append("'> </i></span>");
		return sb.toString();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			if("reference".equals(link.getCommand())) {
				RepositoryEntryRef uobject = (RepositoryEntryRef)link.getUserObject();
				fireEvent(ureq, new SelectReferenceEvent(uobject));
			}
		}
	}
}
