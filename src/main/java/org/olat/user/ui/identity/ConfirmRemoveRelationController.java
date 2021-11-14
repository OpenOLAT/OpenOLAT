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
package org.olat.user.ui.identity;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmRemoveRelationController extends FormBasicController {
	
	private List<IdentityRelationRow> relationsToRemove;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private IdentityRelationshipService identityRelationsService;
	
	public ConfirmRemoveRelationController(UserRequest ureq, WindowControl wControl,
			List<IdentityRelationRow> relationsToRemove) {
		super(ureq, wControl, "confirm_remove");
		this.relationsToRemove = relationsToRemove;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String messageI18n = relationsToRemove.size() <= 1
					? "confirm.remove.relation.text.singular" : "confirm.remove.relation.text.plural";
			String message = translate(messageI18n, new String[] { Integer.toString(relationsToRemove.size())} );
			layoutCont.contextPut("msg", message);
		}

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("remove", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		int count = 0;
		for(IdentityRelationRow relationToRemove:relationsToRemove) {
			RelationRole relationRole = relationToRemove.getRelationRole();
			IdentityRef source = new IdentityRefImpl(relationToRemove.getSourceIdentity().getIdentityKey());
			IdentityRef target = new IdentityRefImpl(relationToRemove.getTargetIdentity().getIdentityKey());
			identityRelationsService.removeRelation(source, target, relationRole);
			if(++count % 20 == 0) {
				dbInstance.commit();
			}
		}
		dbInstance.commit();
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
