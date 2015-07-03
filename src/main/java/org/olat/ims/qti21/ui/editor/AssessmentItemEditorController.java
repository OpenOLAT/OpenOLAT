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
package org.olat.ims.qti21.ui.editor;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.ui.AssessmentItemDisplayController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;

/**
 * 
 * Initial date: 22.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemEditorController extends BasicController {

	private final ResolvedAssessmentItem resolvedAssessmentItem;
	
	private final TabbedPane tabbedPane;
	private final VelocityContainer mainVC;
	
	private FormBasicController itemEditor;
	private AssessmentItemDisplayController displayCtrl;
	
	@Autowired
	private QtiSerializer qtiSerializer;
	
	public AssessmentItemEditorController(UserRequest ureq, WindowControl wControl, RepositoryEntry testEntry,
			ResolvedAssessmentItem resolvedAssessmentItem, AssessmentItemRef itemRef, File unzippedDirectory) {
		super(ureq, wControl);
		this.resolvedAssessmentItem = resolvedAssessmentItem;
		
		mainVC = createVelocityContainer("assessment_item_editor");
		tabbedPane = new TabbedPane("itemTabs", getLocale());
		tabbedPane.addListener(this);
		mainVC.put("tabbedpane", tabbedPane);

		initItemEditor(ureq);
		
		displayCtrl = new AssessmentItemDisplayController(ureq, getWindowControl(), testEntry, resolvedAssessmentItem, itemRef, unzippedDirectory);
		listenTo(displayCtrl);
		tabbedPane.addTab("Preview", displayCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void initItemEditor(UserRequest ureq) {
		AssessmentItem item = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
		if(QTI21Constants.TOOLNAME.equals(item.getToolName())) {
			//we have create this one
			List<Interaction> interactions = item.getItemBody().findInteractions();
			
			boolean choice = false;
			boolean unkown = false;
			
			if(interactions != null && interactions.size() > 0) {
				for(Interaction interaction: interactions) {
					if(interaction instanceof ChoiceInteraction) {
						choice = true;
					} else {
						unkown = true;
					}	
				}	
			}
			
			if(choice && !unkown) {
				itemEditor = new SingleChoiceEditorController(ureq, getWindowControl());
				listenTo(itemEditor);
				tabbedPane.addTab("Choice", itemEditor.getInitialComponent());
			} else if(unkown) {
				initItemCreatedByUnkownEditor(ureq);
			}
		} else {
			initItemCreatedByUnkownEditor(ureq);
		}
	}
	
	private void initItemCreatedByUnkownEditor(UserRequest ureq) {
		itemEditor = new UnkownItemEditorController(ureq, getWindowControl());
		listenTo(itemEditor);
		tabbedPane.addTab("Unkown", itemEditor.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof AssessmentItemEvent) {
			if(event == AssessmentItemEvent.CHANGED_EVENT) {
				doSaveAssessmentItem();
			}
			
		}
		super.event(ureq, source, event);
	}

	private void doSaveAssessmentItem() {
		URI itemUri = resolvedAssessmentItem.getItemLookup().getSystemId();
		File itemFile = new File(itemUri);
		AssessmentItem assessmentItem = resolvedAssessmentItem.getItemLookup().getRootNodeHolder().getRootNode();
		
		try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(assessmentItem, out);	
		} catch(Exception e) {
			logError("", e);
			showError("serialize.error");
		}
	}
}