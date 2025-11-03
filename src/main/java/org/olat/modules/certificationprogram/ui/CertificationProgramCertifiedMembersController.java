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
package org.olat.modules.certificationprogram.ui;

import java.math.BigDecimal;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters.Type;
import org.olat.modules.certificationprogram.ui.CertificationProgramMembersTableModel.CertificationProgramMembersCols;
import org.olat.modules.certificationprogram.ui.component.NextRecertificationInDaysFlexiCellRenderer;
import org.olat.modules.certificationprogram.ui.component.WalletBalanceCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramCertifiedMembersController extends AbstractCertificationProgramMembersController {
	
	protected static final String VALID_TAB_ID = "Valid";
	protected static final String PAUSED_TAB_ID = "Paused";
	protected static final String EXPIRED_TAB_ID = "Expired";
	protected static final String RECERTIFIED_ID = "Recertified";
	protected static final String EXPIRING_SOON_ID = "ExpiringSoon";
	protected static final String INSUFFICIENT_CREDIT_POINTS_ID = "InsufficientCreditPoints";
	
	private FormLink addMemberButton;
	
	private ToolsController	toolsCtrl;
	private CloseableModalController cmc;
	private AddProgramMemberController addMemberCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ConfirmRenewController renewConfirmationCtrl;
	private ConfirmRevokeController revokeConfirmationCtrl;
	
	@Autowired
	private DB dbInstance;
	
	public CertificationProgramCertifiedMembersController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CertificationProgram certificationProgram, CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl, toolbarPanel, certificationProgram, secCallback);
		
		initForm(ureq);
		tableEl.setSelectedFilterTab(ureq, allTab);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(secCallback.canAddMember()) {
			addMemberButton = uifactory.addFormLink("add.member", formLayout, Link.BUTTON);
			addMemberButton.setIconLeftCSS("o_icon o_icon_add_member");
		}
		super.initForm(formLayout, listener, ureq);
	}

	@Override
	protected void initColumns(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificationProgramMembersCols.nextRecertificationDays,
				new NextRecertificationInDaysFlexiCellRenderer(getTranslator())));
		
		DefaultFlexiColumnModel balanceColModel = new DefaultFlexiColumnModel(CertificationProgramMembersCols.walletBalance,
				new WalletBalanceCellRenderer(certificationProgram.getCreditPointSystem()));
		balanceColModel.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(balanceColModel);
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(CertificationStatus.VALID.name(), translate("filter.valid")));
		statusValues.add(SelectionValues.entry(CertificationStatus.EXPIRED.name(), translate("filter.expired")));
		statusValues.add(SelectionValues.entry(CertificationStatus.PAUSED.name(), translate("filter.paused")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_STATUS, statusValues, true);
		filters.add(statusFilter);
		
		filters.add(new FlexiTableDateRangeFilter(translate("filter.expiration.date"), FILTER_EXPIRATION, true, false,
				getLocale()));
		
		SelectionValues expireSoonValues = new SelectionValues();
		expireSoonValues.add(SelectionValues.entry(FILTER_EXPIRE_SOON, translate("filter.expiring.soon")));
		FlexiTableOneClickSelectionFilter withContentFilter = new FlexiTableOneClickSelectionFilter(translate("filter.expiring.soon"),
				FILTER_EXPIRE_SOON, expireSoonValues, true);
		filters.add(withContentFilter);
		
		SelectionValues notEnoughCreditPointsValues = new SelectionValues();
		notEnoughCreditPointsValues.add(SelectionValues.entry(FILTER_NOT_ENOUGH_CREDIT_POINTS, translate("filter.not.enough.creditpoints")));
		FlexiTableOneClickSelectionFilter notEnoughCreditPointsFilter = new FlexiTableOneClickSelectionFilter(translate("filter.not.enough.creditpoints"),
				FILTER_NOT_ENOUGH_CREDIT_POINTS, notEnoughCreditPointsValues, true);
		filters.add(notEnoughCreditPointsFilter);
		
		SelectionValues recertifiedValues = new SelectionValues();
		recertifiedValues.add(SelectionValues.entry(FILTER_RECERTIFIED, translate("filter.recertified")));
		FlexiTableOneClickSelectionFilter recertifiedFilter = new FlexiTableOneClickSelectionFilter(translate("filter.recertified"),
				FILTER_RECERTIFIED, recertifiedValues, false);
		filters.add(recertifiedFilter);
	}

	@Override
	protected void initFiltersPresets(List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab validTab = FlexiFiltersTabFactory.tabWithImplicitFilters(VALID_TAB_ID, translate("filter.valid"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, CertificationStatus.VALID.name())));
		tabs.add(validTab);
		
		FlexiFiltersTab expiredTab = FlexiFiltersTabFactory.tabWithImplicitFilters(EXPIRED_TAB_ID, translate("filter.expired"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, CertificationStatus.EXPIRED.name())));
		tabs.add(expiredTab);
		
		FlexiFiltersTab pausedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PAUSED_TAB_ID, translate("filter.paused"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, CertificationStatus.PAUSED.name())));
		tabs.add(pausedTab);
		
		FlexiFiltersTab expiringSoonTab = FlexiFiltersTabFactory.tabWithImplicitFilters(EXPIRING_SOON_ID, translate("filter.expiring.soon"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, CertificationStatus.VALID.name()),
						FlexiTableFilterValue.valueOf(FILTER_EXPIRE_SOON, FILTER_EXPIRE_SOON)));
		tabs.add(expiringSoonTab);
		
		FlexiFiltersTab creditPointsTab = FlexiFiltersTabFactory.tabWithImplicitFilters(INSUFFICIENT_CREDIT_POINTS_ID, translate("filter.insufficient.credit.points"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_NOT_ENOUGH_CREDIT_POINTS, FILTER_NOT_ENOUGH_CREDIT_POINTS)));
		tabs.add(creditPointsTab);
		
		FlexiFiltersTab recertifiedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RECERTIFIED_ID, translate("filter.recertified"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_RECERTIFIED, FILTER_RECERTIFIED)));
		tabs.add(recertifiedTab);
	}
	
	@Override
	protected CertificationProgramMemberSearchParameters getSearchParams() {
		CertificationProgramMemberSearchParameters searchParams = new CertificationProgramMemberSearchParameters(certificationProgram);
		searchParams.setType(Type.CERTIFIED);
		return searchParams;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(calloutCtrl == source) {
        	cleanUp();
        } else if(toolsCtrl == source) {
        	if(event == Event.CLOSE_EVENT) {
        		calloutCtrl.deactivate();
        		cleanUp();
        	}
        } else if(renewConfirmationCtrl == source) {
        	if(event == Event.DONE_EVENT) {
        		loadModel(ureq);
        	}
        	cmc.deactivate();
        	cleanUp();
        } else if(revokeConfirmationCtrl == source || addMemberCtrl == source) {
        	if(event == Event.DONE_EVENT) {
        		loadModel(ureq);
        		fireEvent(ureq, Event.CHANGED_EVENT);
        	}
        	cmc.deactivate();
        	cleanUp();
        } else if(cmc == source) {
        	cleanUp();
        }
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(revokeConfirmationCtrl);
		removeAsListenerAndDispose(renewConfirmationCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		revokeConfirmationCtrl = null;
		renewConfirmationCtrl = null;
		calloutCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addMemberButton == source) {
			doAddMember(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAddMember(UserRequest ureq) {
		List<Long> currentMembers = tableModel.getIdentitiesKeys();
		addMemberCtrl = new AddProgramMemberController(ureq, getWindowControl(), certificationProgram, currentMembers);
		listenTo(addMemberCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), addMemberCtrl.getInitialComponent(),
				true, translate("renew.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmRenew(UserRequest ureq, CertificationProgramMemberRow row) {
		BigDecimal balance = row.getWalletBalance();
		BigDecimal price = certificationProgram.getCreditPoints();
		Identity certifiedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		
		if((balance == null && price == null)
				|| (price != null && BigDecimal.ZERO.compareTo(price) == 0)
				|| (price != null && balance != null && price.compareTo(balance) <= 0)) {
			
			renewConfirmationCtrl = new ConfirmRenewController(ureq, getWindowControl(), 
					translate("renew.confirm.text", CertificationHelper.creditPoints(price)),
					null,
					translate("renew"), certificationProgram, certifiedIdentity);
			renewConfirmationCtrl.setUserObject(row);
			listenTo(renewConfirmationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"), renewConfirmationCtrl.getInitialComponent(),
					true, translate("renew.confirm.title"), true);
			listenTo(cmc);
			cmc.activate();
		} else {
			showWarning("warning.renew.balance.insufficient");
		}
	}

	private void doConfirmRevoke(UserRequest ureq, CertificationProgramMemberRow row) {
		Identity certifiedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		revokeConfirmationCtrl = new ConfirmRevokeController(ureq, getWindowControl(), 
				translate("revoke.confirm.text"),
				null,
				translate("revoke"), certificationProgram, certifiedIdentity);
		revokeConfirmationCtrl.setUserObject(row);
		listenTo(revokeConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), revokeConfirmationCtrl.getInitialComponent(),
				true, translate("revoke.confirm.title"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doPause(UserRequest ureq, CertificationProgramMemberRow row) {
		IdentityRef certifiedIdentity = new IdentityRefImpl(row.getIdentityKey());
		certificationProgramService.pauseRecertification(certificationProgram, certifiedIdentity, getIdentity());
		getLogger().info("Pause certificate of {} in certification program {}", certifiedIdentity.getKey(), certificationProgram.getKey());
		dbInstance.commit();
		showInfo("info.recertification.paused");
		loadModel(ureq);
	}
	
	private void doContinue(UserRequest ureq, CertificationProgramMemberRow row) {
		IdentityRef certifiedIdentity = new IdentityRefImpl(row.getIdentityKey());
		certificationProgramService.continueRecertification(certificationProgram, certifiedIdentity, getIdentity());
		getLogger().info("Continue certificate of {} in certification program {}", certifiedIdentity.getKey(), certificationProgram.getKey());
		dbInstance.commit();
		showInfo("info.recertification.continue");
		loadModel(ureq);
	}

	@Override
	protected void doOpenTools(UserRequest ureq, CertificationProgramMemberRow row, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {

		private Link pauseLink;
		private Link renewLink;
		private Link revokeLink;
		private Link continueLink;
		private final Link contactLink;
		
		private final CertificationProgramMemberRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CertificationProgramMemberRow row) {
			super(ureq, wControl);
			this.row = row;
			final CertificationStatus status = row.getStatus();
			
			VelocityContainer mainVC = createVelocityContainer("tool_members");
			
			contactLink = LinkFactory.createLink("contact", "contact", getTranslator(), mainVC, this, Link.LINK);
			contactLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
			
			if(certificationProgram.isRecertificationEnabled()
					&& (certificationProgram.isPrematureRecertificationByUserEnabled() || status == CertificationStatus.EXPIRED)) {
				renewLink = LinkFactory.createLink("renew", "renew", getTranslator(), mainVC, this, Link.LINK);
				renewLink.setIconLeftCSS("o_icon o_icon-fw o_icon_recycle");
			}
			
			if(status != CertificationStatus.REVOKED && status != CertificationStatus.ARCHIVED) {
				revokeLink = LinkFactory.createLink("revoke", "revoke", getTranslator(), mainVC, this, Link.LINK);
				revokeLink.setIconLeftCSS("o_icon o_icon-fw o_icon_certification_status_revoked");
			}
			
			if(status != CertificationStatus.PAUSED) {
				pauseLink = LinkFactory.createLink("pause", "pause", getTranslator(), mainVC, this, Link.LINK);
				pauseLink.setIconLeftCSS("o_icon o_icon-fw o_icon_certification_status_paused");
			}

			if(status == CertificationStatus.PAUSED) {
				continueLink = LinkFactory.createLink("continue", "continue", getTranslator(), mainVC, this, Link.LINK);
				continueLink.setIconLeftCSS("o_icon o_icon-fw o_icon_play");
			}
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if(contactLink == source) {
				doOpenContact(ureq, row);
			} else if(renewLink == source) {
				doConfirmRenew(ureq, row);
			} else if(revokeLink == source) {
				doConfirmRevoke(ureq, row);
			} else if(pauseLink == source) {
				doPause(ureq, row);
			} else if(continueLink == source) {
				doContinue(ureq, row);
			}
		}
	}
}
