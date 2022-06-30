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
package org.olat.modules.video.ui.question;

import java.io.File;
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
import org.olat.core.util.Util;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.IdentifierGenerator;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilderFactory;
import org.olat.ims.qti21.ui.editor.AssessmentTestComposerController;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.model.VideoQuestionImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;

/**
 * 
 * Initial date: 4 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewQuestionItemCalloutController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private RepositoryEntry entry;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private VideoManager videoManager;
	
	public NewQuestionItemCalloutController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestComposerController.class, ureq.getLocale()));
		this.entry = entry;
		
		mainVC = createVelocityContainer("new_question");
		List<String> links = new ArrayList<>();
		addLink("new.sc", QTI21QuestionType.sc, links);
		addLink("new.mc", QTI21QuestionType.mc, links);
		
		addLink("new.kprim", QTI21QuestionType.kprim, links);
		addLink("new.match", QTI21QuestionType.match, links);
		addLink("new.matchdraganddrop", QTI21QuestionType.matchdraganddrop, links);
		addLink("new.matchtruefalse", QTI21QuestionType.matchtruefalse, links);
		
		addLink("new.fib", QTI21QuestionType.fib, links);
		addLink("new.fib.numerical", QTI21QuestionType.numerical, links);
		addLink("new.hottext", QTI21QuestionType.hottext, links);
		addLink("new.hotspot", QTI21QuestionType.hotspot, links);
		addLink("new.order", QTI21QuestionType.order, links);
		addLink("new.inlinechoice", QTI21QuestionType.inlinechoice, links);
		
		mainVC.contextPut("links", links);
		putInitialPanel(mainVC);
	}
	
	private void addLink(String name, QTI21QuestionType type, List<String> links) {
		Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
		link.setIconLeftCSS("o_icon o_icon-fw ".concat(type.getCssClass()));
		link.setUserObject(type);
		mainVC.put(name, link);
		links.add(name);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			Object uobject = link.getUserObject();
			if(uobject instanceof QTI21QuestionType) {
				doCreateNewQuestion(ureq, (QTI21QuestionType)uobject);
			}
		}
	}
	
	private void doCreateNewQuestion(UserRequest ureq, QTI21QuestionType type) {
		File assessmentDir = videoManager.getAssessmentDirectory(entry.getOlatResource());

		String itemId = IdentifierGenerator.newAsString(type.getPrefix());
		File itemDir = new File(assessmentDir, itemId);
		itemDir.mkdir();
		File itemFile = new File(itemDir, itemId + ".xml");
		
		AssessmentItemBuilder itemBuilder = AssessmentItemBuilderFactory.get(type, getLocale());
		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
		qtiService.persistAssessmentObject(itemFile, assessmentItem);
		
		VideoQuestionImpl question = new VideoQuestionImpl();
		question.setId(itemId);
		question.setTitle(assessmentItem.getTitle());
		question.setType(type.name());
		question.setQuestionRootPath(itemId);
		question.setQuestionFilename(itemFile.getName());
		fireEvent(ureq, new NewQuestionEvent(question));
	}
}
