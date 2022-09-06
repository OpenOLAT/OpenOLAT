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
package org.olat.modules.portfolio.ui.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.Invitation;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.ui.InvitationURLController;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.AssessedBinder;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.modules.portfolio.ui.shared.InviteeBindersAdminDataModel.BinderAdminCols;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageDisplayController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InviteeBindersAdminController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private InviteeBindersAdminDataModel tableModel;
	
	private int counter = 0;
	private final Identity invitee;
	
	private CloseableModalController cmc;
	private InvitationURLController invitationUrlCtrl;
	private CloseableCalloutWindowController urlCalloutCtrl;
	private HomePageDisplayController homePageDisplayCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private InvitationService invitationService;
	
	public InviteeBindersAdminController(UserRequest ureq, WindowControl wControl, Identity invitee) {
		super(ureq, wControl, "invitee_binders_admin");
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));
		this.invitee = invitee;
		
		initForm(ureq);
		loadModel(null);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BinderAdminCols.binderKey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BinderAdminCols.binderOwner, "owner"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BinderAdminCols.binderName));
		
		DefaultFlexiColumnModel courseCol = new DefaultFlexiColumnModel(true, false,
				BinderAdminCols.courseName.i18nHeaderKey(), BinderAdminCols.courseName.iconHeader(), BinderAdminCols.courseName.ordinal(),
				"course", BinderAdminCols.courseName.sortable(), BinderAdminCols.courseName.sortKey(), FlexiColumnModel.ALIGNMENT_LEFT,
				new CourseNameRenderer());
		columnsModel.addFlexiColumnModel(courseCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BinderAdminCols.invitationDate,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BinderAdminCols.invitationLink));
		
		tableModel = new InviteeBindersAdminDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
	}
	
	private void loadModel(String searchString) {
		List<Invitation> invitations = invitationService.findInvitations(invitee);
		Map<Group,Invitation> groupToInvitations = invitations.stream()
				.filter(invitation -> invitation.getBaseGroup() != null)
				.collect(Collectors.toMap(Invitation::getBaseGroup, inv -> inv, (u, v) -> u));

		List<AssessedBinder> assessedBinders = portfolioService.searchSharedBindersWith(invitee, searchString);
		List<InviteeBinderAdminRow> rows = new ArrayList<>(assessedBinders.size());
		for(AssessedBinder assessedBinder:assessedBinders) {
			Invitation invitation = groupToInvitations.get(assessedBinder.getBaseGroup());
			rows.add(forgeRow(assessedBinder, invitation));
		}
		tableModel.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}
	
	private InviteeBinderAdminRow forgeRow(AssessedBinder assessedBinder, Invitation invitation) {
		String ownerFullname = userManager.getUserDisplayName(assessedBinder.getAssessedIdentity());
		InviteeBinderAdminRow row = new InviteeBinderAdminRow(assessedBinder, ownerFullname, invitation);
		if(invitation != null) {
			FormLink invitationLink = uifactory.addFormLink("invitation_" + (++counter), "invitation", "", null, flc, Link.LINK | Link.NONTRANSLATED);
			invitationLink.setIconLeftCSS("o_icon o_icon_link o_icon-fw");
			invitationLink.setTitle(translate("invitation.link.long"));
			row.setInvitationLink(invitationLink);
			invitationLink.setUserObject(row);	
		}
		return row;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(invitationUrlCtrl == source) {
			if(urlCalloutCtrl != null) {
				urlCalloutCtrl.deactivate();
			}
			cleanUp();
		} else if(homePageDisplayCtrl == source) {
			if(cmc != null) {
				cmc.deactivate();
			}
			cleanUp();
		} else if(urlCalloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(homePageDisplayCtrl);
		removeAsListenerAndDispose(invitationUrlCtrl);
		removeAsListenerAndDispose(urlCalloutCtrl);
		removeAsListenerAndDispose(cmc);
		homePageDisplayCtrl = null;
		invitationUrlCtrl = null;
		urlCalloutCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				InviteeBinderAdminRow row = tableModel.getObject(se.getIndex());
				if("owner".equals(cmd)) {
					doSelectUser(ureq, row);
				} else if("course".equals(cmd)) {
					doSelectCourse(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				loadModel(se.getSearch());
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("invitation".equals(link.getCmd()) && link.getUserObject() instanceof InviteeBinderAdminRow) {
				InviteeBinderAdminRow row = (InviteeBinderAdminRow)link.getUserObject();
				doOpenInvitationLink(ureq, link.getFormDispatchId(), row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelectUser(UserRequest ureq, InviteeBinderAdminRow row) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getOwnerKey());
		homePageDisplayCtrl = new HomePageDisplayController(ureq, getWindowControl(), assessedIdentity, new HomePageConfig());
		listenTo(homePageDisplayCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), 
				homePageDisplayCtrl.getInitialComponent(), true, row.getOwnerFullname());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSelectCourse(UserRequest ureq, InviteeBinderAdminRow row) {
		String businessPath = "[RepositoryEntry:" + row.getCourseKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doOpenInvitationLink(UserRequest ureq, String elementId, InviteeBinderAdminRow row) {
		String url = invitationService.toUrl(row.getInvitation());
		invitationUrlCtrl = new InvitationURLController(ureq, getWindowControl(), url);
		listenTo(invitationUrlCtrl);

		String title = translate("invitation.link.long");
		urlCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				invitationUrlCtrl.getInitialComponent(), elementId, title, true, "");
		listenTo(urlCalloutCtrl);
		urlCalloutCtrl.activate();
	}
	
	private static class CourseNameRenderer extends StaticFlexiCellRenderer {
		
		public CourseNameRenderer() {
			super("course", new TextFlexiCellRenderer());
			setIconLeftCSS("o_icon o_CourseModule_icon o_icon-fw");
		}

		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if(cellValue != null) {
				super.render(renderer, target, cellValue, row, source, ubu, translator);
			}
		}
	}
}
