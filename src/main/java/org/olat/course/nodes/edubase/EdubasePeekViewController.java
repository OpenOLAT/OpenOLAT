/**
// * <a href="http://www.openolat.org">
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
package org.olat.course.nodes.edubase;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlsite.OlatCmdEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.EdubaseCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.edubase.BookSection;
import org.olat.modules.edubase.EdubaseManager;
import org.olat.modules.edubase.model.PositionComparator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 21.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdubasePeekViewController extends BasicController {

	private static final String PEEKVIEW_CONTAINER = "peekview";
	private final static int NUMBER_BOOK_SECTION_DESC_DISABLED = 4;

	private final String nodeId;
	
	@Autowired
	private EdubaseManager edubaseManager;

	public EdubasePeekViewController(UserRequest ureq, WindowControl wControl, ModuleConfiguration modulConfiguration,
			String nodeId) {
		super(ureq, wControl);
		this.nodeId = nodeId;

		Component peekviewContainer = createPeekviewComponent(modulConfiguration);
		putInitialPanel(peekviewContainer);
	}

	private Component createPeekviewComponent(ModuleConfiguration modulConfiguration) {
		VelocityContainer container = createVelocityContainer(PEEKVIEW_CONTAINER);;

		// BookSections
		List<BookSection> bookSections =
				modulConfiguration.getList(EdubaseCourseNode.CONFIG_BOOK_SECTIONS, BookSection.class).stream()
				.map(bs -> edubaseManager.appendCoverUrl(bs))
				.sorted(new PositionComparator())
				.limit(NUMBER_BOOK_SECTION_DESC_DISABLED)
				.collect(Collectors.toList());
		container.contextPut("bookSections", bookSections);

		// Add a link to show all BookSections (go to node)
		Link allItemsLink = LinkFactory.createLink("peekview.allItemsLink", container, this);
		allItemsLink.setIconRightCSS("o_icon o_icon_start");
		allItemsLink.setCustomEnabledLinkCSS("pull-right");

		return container;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link) source;
			if ("peekview.allItemsLink".equals(link.getCommand())) {
				fireEvent(ureq, new OlatCmdEvent(OlatCmdEvent.GOTONODE_CMD, nodeId));
			}
		}
	}
}
