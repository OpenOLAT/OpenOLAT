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
package org.olat.course.nodes.card2brain;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.card2brain.Card2BrainModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.04.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Card2BrainPeekViewController extends BasicController {
	
	private final static String EVENT_RUN = "run";
	
	private Panel main;
	private VelocityContainer container;
	
	private final Long repositoryEntryKey;
	private final String nodeId;
	private final String flashcardAlias;
	
	@Autowired
	private Card2BrainModule card2BrainModule;

	public Card2BrainPeekViewController(UserRequest ureq, WindowControl wControl, Long repositoryEntryKey, String nodeId, String flashcardAlias) {
		super(ureq, wControl);

		this.flashcardAlias = flashcardAlias;
		this.repositoryEntryKey = repositoryEntryKey;
		this.nodeId = nodeId;
		
		main = new Panel("card2brainPanel");
		runPeekview();
		putInitialPanel(main);
	}

	private void runPeekview() {
		container = createVelocityContainer("peekview");

		String src = String.format(card2BrainModule.getPeekViewUrl(), flashcardAlias);
		container.contextPut("src", src);
		container.contextPut("run", EVENT_RUN);
		
		main.setContent(container);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == container && EVENT_RUN.equals(event.getCommand())) {
			String businessPath = "[RepositoryEntry:" + repositoryEntryKey + "][CourseNode:" + nodeId + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());	
		}
	}
}
