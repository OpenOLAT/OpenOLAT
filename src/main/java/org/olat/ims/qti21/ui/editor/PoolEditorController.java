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

import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.ims.qti21.ui.editor.events.DetachFromPoolEvent;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;

/**
 * 
 * Initial date: 23.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PoolEditorController extends FormBasicController {
	
	private FormLink identifierLink;
	private FormLink masterIdentifierLink;
	private FormLink copyButton;

	private final QuestionItem originalItem;
	private final QuestionItem masterItem;
	private final AssessmentItemRef itemRef;
	private final ManifestMetadataBuilder metadataBuilder;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private QPoolService qpoolService;
	
	public PoolEditorController(UserRequest ureq, WindowControl wControl,
			AssessmentItemRef itemRef, ManifestMetadataBuilder metadataBuilder, QPoolInformations poolInformations) {
		super(ureq, wControl);
		this.itemRef = itemRef;
		originalItem = poolInformations.getOriginalItem();
		masterItem = poolInformations.getMasterItem();
		this.metadataBuilder = metadataBuilder;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean copy = originalItem == null && masterItem != null;
		if(copy) {
			setFormWarning("warning.copy.from.pool");
		}

		String identifier = metadataBuilder.getOpenOLATMetadataIdentifier();
		if(originalItem == null) {
			uifactory.addStaticTextElement("general.identifier", identifier, formLayout);
		} else {
			identifierLink = uifactory.addFormLink("general.identifier", identifier, translate("general.identifier"), formLayout, Link.NONTRANSLATED);
		}
		String masterIdentifier = metadataBuilder.getOpenOLATMetadataMasterIdentifier();
		if(masterItem == null) {
			uifactory.addStaticTextElement("general.master.identifier", masterIdentifier, formLayout);
		} else {
			masterIdentifierLink = uifactory.addFormLink("general.master.identifier", masterIdentifier, translate("general.master.identifier"), formLayout, Link.NONTRANSLATED);
		}

		//Rights owners
		List<Identity> authors = null;
		if(originalItem != null) {
			authors = qpoolService.getAuthors(originalItem);
		} else if(masterItem != null) {
			authors = qpoolService.getAuthors(masterItem);
		}
		if(authors != null && !authors.isEmpty()) {
			String author = userManager.getUserDisplayName(authors.get(0));
			uifactory.addStaticTextElement("rights.owners", author, formLayout);
			for(int i=1; i<authors.size(); i++) {
				author = userManager.getUserDisplayName(authors.get(i));
				uifactory.addStaticTextElement("rightss.owners_" + i, null, author, formLayout);
			}
		}

		Date copiedAt = metadataBuilder.getOpenOLATMetadataCopiedAt();
		String copiedAtStr = Formatter.getInstance(getLocale()).formatDateAndTime(copiedAt);
		uifactory.addStaticTextElement("copy.at", copiedAtStr, formLayout);

		String version = metadataBuilder.getLifecycleVersion();
		uifactory.addStaticTextElement("lifecycle.version", version, formLayout);
		
		copyButton = uifactory.addFormLink("copy.qpool.question", formLayout, Link.BUTTON);
		copyButton.setVisible(!copy);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(identifierLink == source) {
			doOpenQuestion(ureq, originalItem);
		} else if(masterIdentifierLink == source) {
			doOpenQuestion(ureq, masterItem);
		} else if(copyButton == source) {
			doCopy(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenQuestion(UserRequest ureq, QuestionItem item) {
		String businessPath = "[QPool:0][QuestionItem:" + item.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doCopy(UserRequest ureq) {
		fireEvent(ureq, new DetachFromPoolEvent(itemRef));
	}
}
