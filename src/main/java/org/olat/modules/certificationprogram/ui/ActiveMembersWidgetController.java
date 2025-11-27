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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.indicators.IndicatorsFactory;
import org.olat.core.gui.components.indicators.IndicatorsItem;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.model.CertificationProgramActiveMemberStatistics;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters.OrderBy;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters.Type;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberWithInfos;
import org.olat.user.PortraitSize;
import org.olat.user.PortraitUser;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitComponent;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ActiveMembersWidgetController extends TableWidgetController implements FlexiTableComponentDelegate {

	private static final String CMD_OPEN = "open";

	private IndicatorsItem indicatorsEl;
	private FormLink showAllLink;
	private FormLink indicatorActiveLink;
	private FormLink indicatorCertifiedLink;
	private FormLink expiringSoonLink;
	private FormLink inRecertificationLink;
	private FlexiTableElement tableEl;
	private MembersTableModel dataModel;
	
	private CertificationProgram certificationProgram;
	private final String membersAreaBusinessPath;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public ActiveMembersWidgetController(UserRequest ureq, WindowControl wControl, CertificationProgram certificationProgram) {
		super(ureq, wControl);
		this.certificationProgram = certificationProgram;
		membersAreaBusinessPath = "[CurriculumAdmin:0][Certification:0][CertificationProgram:" + certificationProgram.getKey() + "][Members:0]";

		initForm(ureq);
	}

	@Override
	protected String getTitle() {
		return "<i class=\"o_icon o_icon_activate\"> </i> " + translate("active.members");
	}

	@Override
	protected String getTableTitle() {
		return translate("certified");
	}

	@Override
	protected String createIndicators(FormLayoutContainer widgetCont) {
		indicatorsEl = IndicatorsFactory.createItem("indicators", widgetCont);
		
		indicatorActiveLink = IndicatorsFactory.createIndicatorFormLink("active", CMD_OPEN, "", "", widgetCont);
		setUrl(indicatorActiveLink, membersAreaBusinessPath + "[Active:0][All:0]");
		indicatorsEl.setKeyIndicator(indicatorActiveLink);
		
		List<FormItem> focusIndicators = new ArrayList<>(4);
		indicatorCertifiedLink = IndicatorsFactory.createIndicatorFormLink("certified", CMD_OPEN, "", "", widgetCont);
		setUrl(indicatorCertifiedLink, membersAreaBusinessPath + "[Active:0][Certified:0]");
		focusIndicators.add(indicatorCertifiedLink);
		
		if(certificationProgram.isValidityEnabled()) {
			expiringSoonLink = IndicatorsFactory.createIndicatorFormLink("expiring.soon", CMD_OPEN, "", "", widgetCont);
			setUrl(expiringSoonLink, membersAreaBusinessPath + "[Active:0][ExpiringSoon:0]");
			focusIndicators.add(expiringSoonLink);
		}
		
		if(certificationProgram.isRecertificationEnabled()) {
			inRecertificationLink = IndicatorsFactory.createIndicatorFormLink("certifying", CMD_OPEN, "", "", widgetCont);
			setUrl(inRecertificationLink, membersAreaBusinessPath + "[Active:0][InRecertification:0]");
			focusIndicators.add(inRecertificationLink);
		}
		
		indicatorsEl.setFocusIndicatorsItems(focusIndicators);
		return indicatorsEl.getComponent().getComponentName();
	}
	
	private void setUrl(FormLink link, String businessPath) {
		link.setUserObject(businessPath);
		String url = BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString(businessPath);
		link.setUrl(url);
	}
	
	public void reload(UserRequest ureq) {
		updateIndicators(ureq);
		updateTable(ureq);
	}
	
	private void updateIndicators(UserRequest ureq) {
		CertificationProgramActiveMemberStatistics stats = certificationProgramService
				.getCertificationProgramActiveMembersStatistics(certificationProgram, ureq.getRequestTimestamp());
				
		indicatorActiveLink.setI18nKey(IndicatorsFactory.createLinkText(
				translate("widget.active"),
				Long.toString(stats.active())));
		
		indicatorCertifiedLink.setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_course_widget_icon o_icon_certification_certified\"></i> " + translate("widget.certified"),
				Long.toString(stats.certified())));

		if(certificationProgram.isValidityEnabled()) {
			expiringSoonLink.setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_course_widget_icon o_icon_recertification_warning\"></i> " + translate("widget.expiring.soon"),
				Long.toString(stats.expiringSoon())));
		}

		if(certificationProgram.isRecertificationEnabled()) {
			inRecertificationLink.setI18nKey(IndicatorsFactory.createLinkText(
				"<i class=\"o_icon o_course_widget_icon o_icon_certification_certifying\"></i> " + translate("widget.in.recertification"),
				Long.toString(stats.inRecertification())));
		}
	}
	
	private void updateTable(UserRequest ureq) {
		final boolean hasCreditPoints = certificationProgram.hasCreditPoints();
		final boolean hasValidity = certificationProgram.isValidityEnabled();
		
		CertificationProgramMemberSearchParameters searchParams = new CertificationProgramMemberSearchParameters(certificationProgram);
		searchParams.setType(Type.CERTIFIED);
		if(certificationProgram.isValidityEnabled()) {
			searchParams.setOrderBy(OrderBy.NEXTRECERTIFICATIONDATE, true);
		} else {
			searchParams.setOrderBy(OrderBy.CREATIONDATE, false);
		}
		List<CertificationProgramMemberWithInfos> memberList = certificationProgramService.getMembers(searchParams,
				ureq.getRequestTimestamp(), 5);
		
		List<MemberRow> rows = new ArrayList<>();
		for(CertificationProgramMemberWithInfos member:memberList) {
			String balance = hasCreditPoints && member.wallet() != null
					? CertificationHelper.creditPointsToString(member.wallet().getBalance(), certificationProgram.getCreditPointSystem())
					: null;
			Date validUntil = hasValidity
					? member.certificate().getNextRecertificationDate()
					: null;
			
			String fullName = userManager.getUserDisplayName(member.identity());

			PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), member.identity());
			UserPortraitComponent portraitComp = UserPortraitFactory.createUserPortrait("portrait_" + member.identity().getKey(), (VelocityContainer)null, getLocale());
			portraitComp.setSize(PortraitSize.small);
			portraitComp.setPortraitUser(portraitUser);

			rows.add(new MemberRow(fullName, balance, validUntil, portraitComp));
		}
		
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected String createTable(FormLayoutContainer widgetCont) {
		// No table view => No columns needed.
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel portraitCol = new DefaultFlexiColumnModel(MemberCols.portrait);
		portraitCol.setHeaderLabel("\u00A0");
		portraitCol.setHeaderTooltip(translate("table.header.portrait"));
		tableColumnModel.addFlexiColumnModel(portraitCol);
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.member));
		if(certificationProgram.hasCreditPoints()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.balance));
		}
		if(certificationProgram.isValidityEnabled()) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.validUntil,
				new DateFlexiCellRenderer(getLocale())));
		}
		
		dataModel = new MembersTableModel(tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 5, true, getTranslator(), widgetCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		tableEl.setRendererType(FlexiTableRendererType.classic);
		VelocityContainer rowVC = createVelocityContainer("member_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		return tableEl.getComponent().getComponentName();
	}
	
	@Override
	public boolean isRowClickEnabled() {
		return false;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>(1);
		if(rowObject instanceof MemberRow mRow) {
			if(mRow.portraitCmp() != null) {
				components.add(mRow.portraitCmp());
			}
		}
		return components;
	}

	@Override
	protected String createShowAll(FormLayoutContainer widgetCont) {
		showAllLink = createShowAllLink(widgetCont);
		setUrl(showAllLink, membersAreaBusinessPath + "[Active:0][All:0]");
		return showAllLink.getComponent().getComponentName();
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(showAllLink == source) {
			doOpen(ureq, (String)showAllLink.getUserObject());
		} else if(source instanceof FormLink link) {
			if (CMD_OPEN.equals(link.getCmd()) && link.getUserObject() instanceof String businessPath) {
				doOpen(ureq, businessPath);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpen(UserRequest ureq, String businessPath) {
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	public static record MemberRow(String fullName, String balance, Date validUntil, UserPortraitComponent portraitCmp) {
		//
	}
	
	private static class MembersTableModel extends DefaultFlexiTableDataModel<MemberRow> {
		
		private static final MemberCols[] COLS = MemberCols.values();
		
		public MembersTableModel(FlexiTableColumnModel tableColumnModel) {
			super(tableColumnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			MemberRow memberRow = getObject(row);
			return switch(COLS[col]) {
				case portrait -> memberRow.portraitCmp();
				case member -> memberRow.fullName();
				case balance -> memberRow.balance();
				case validUntil -> memberRow.validUntil();
				default -> "ERROR";
			};
		}
	}
	
	public enum MemberCols implements FlexiSortableColumnDef {
		portrait("table.header.portrait"),
		member("table.header.member"),
		validUntil("table.header.valid.until"),
		balance("table.header.balance");

		private final String i18nKey;
		
		private MemberCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return this != portrait;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
