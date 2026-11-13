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
package org.olat.modules.curriculum.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.figurecards.FigureCardsFactory;
import org.olat.core.gui.components.figurecards.FigureCardsItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiBusinessPathModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.Marker;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.Segment;
import org.olat.core.gui.components.progressbar.ProgressBarFactory;
import org.olat.core.gui.components.progressbar.ProgressBarRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dashboard.DashboardUIFactory;
import org.olat.core.gui.control.generic.dashboard.TableWidgetController;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumElementDetailsController;
import org.olat.modules.curriculum.ui.event.ActivateEvent;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.ParticipantsAvailability;
import org.olat.resource.accesscontrol.ParticipantsAvailability.ParticipantsAvailabilityNum;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.user.PortraitSize;
import org.olat.user.PortraitUser;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.olat.user.UsersPortraitsComponent;
import org.olat.user.UsersPortraitsComponent.PortraitItemsTag;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 30 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersWidgetController extends TableWidgetController implements FlexiTableComponentDelegate {
	
	private static final int MAX_PORTRAITS = 5;

	private FigureCardsItem figuresEl;
	private FormLink participantsLink;
	private FormLink toConfirmLink;
	private FormLink detailsLink;
	private FlexiTableElement tableEl;
	private MembersDataModel dataModel;

	private final CurriculumElement curriculumElement;
	private CurriculumElementInfos curriculumElementInfos;
	private final String businessPath;

	@Autowired
	private ACService acService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private UserPortraitService userPortraitService;

	public MembersWidgetController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CurriculumElementDetailsController.class, getLocale(), getTranslator()));
		businessPath = "[CurriculumAdmin:0][Implementations:0][CurriculumElement:" + curriculumElement.getKey() + "]";
		this.curriculumElement = curriculumElement;
		initForm(ureq);
		loadModel(ureq);
	}

	private CurriculumElementInfos loadInformations(CurriculumElement curriculumElement) {
		CurriculumElementInfosSearchParams searchParams = CurriculumElementInfosSearchParams.searchElements(null, List.of(curriculumElement));
		List<CurriculumElementInfos> elements = curriculumService.getCurriculumElementsWithInfos(searchParams);
		if(elements == null || elements.isEmpty()) {
			return new CurriculumElementInfos(curriculumElement, null, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l, 0l);
		}
		return elements.get(0);
	}

	@Override
	protected String getTitle() {
		return "<i class=\"o_icon o_icon_membersmanagement\"> </i> " + translate("curriculum.members");
	}

	@Override
	protected String createTopArea(FormLayoutContainer widgetCont) {
		figuresEl = FigureCardsFactory.createItem("figures", widgetCont);
		figuresEl.setElementCssClass("o_curriculum_members_figure");
		participantsLink = FigureCardsFactory.createCardLink("participants", "participants", widgetCont);
		setUrl(participantsLink, "[Members:0]", businessPath + "[Members:0]");
		figuresEl.setCards(List.of(participantsLink));
		return figuresEl.getComponent().getComponentName();
	}

	@Override
	protected String createTable(FormLayoutContainer widgetCont) {
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		dataModel = new MembersDataModel(tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 10, false, getTranslator(), widgetCont);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("members_widget_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withIconCss("o_icon_group")
				.withMessageTranslated(translate("widget.members.empty"))
				.build());
		return tableEl.getComponent().getComponentName();
	}

	@Override
	protected List<String> createFooterButtons(FormLayoutContainer widgetCont) {
		toConfirmLink = uifactory.addFormLink("to.confirm", "to.confirm", "", null, widgetCont, Link.BUTTON + Link.NONTRANSLATED);
		toConfirmLink.setPrimary(true);
		toConfirmLink.getComponent().setAriaRole(Link.ARIA_ROLE_LINK);
		setUrl(toConfirmLink, "[Members:0][Pending:0][All:0]", businessPath + "[Members:0][Pending:0][All:0]");
		
		detailsLink = DashboardUIFactory.createDetailsLink(widgetCont);
		setUrl(detailsLink, "[Members:0]", businessPath + "[Members:0]");
		return List.of(toConfirmLink.getComponent().getComponentName(), detailsLink.getComponent().getComponentName());
	}

	@Override
	public boolean isRowClickEnabled() {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		if (rowObject instanceof MemberRow memberRow && memberRow.portraits() != null) {
			return List.of(memberRow.portraits());
		}
		return List.of();
	}

	public void loadModel(UserRequest ureq) {
		curriculumElementInfos = loadInformations(curriculumElement);
		updateFigure();
		updateRows(ureq);
		
		long numPending = curriculumElementInfos.numOfPending();
		toConfirmLink.setI18nKey(translate("widget.members.to.confirm", format(numPending)));
		toConfirmLink.setVisible(numPending > 0);
	}

	private void updateFigure() {
		CurriculumElement element = curriculumElementInfos.curriculumElement();
		long max = element.getMaxParticipants() == null? 0: element.getMaxParticipants().longValue();
		long min = element.getMinParticipants() == null? 0: element.getMinParticipants().longValue();
		long numActive = curriculumElementInfos.numOfParticipants();
		long numPending = curriculumElementInfos.numOfPending();
		long total = numActive + numPending;
		boolean withMax = max > 0;
		boolean minNotReached = min > 0 && total < min;
		
		String figure = withMax? format(total) + "/" + format(max): format(total);
		StringBuilder sb = new StringBuilder();
		sb.append(FigureCardsFactory.createCardText(null, null, figure, translate("curriculum.element.participants")));
		
		ParticipantsAvailabilityNum availabilityNum = withMax? acService.getParticipantsAvailability(max, total, true): null;
		String status = null;
		String statusCss = null;
		if (minNotReached) {
			status = translate("participants.to.minimum", format(min - total));
			statusCss = "o_curriculum_members_status_minimum";
		} else if (availabilityNum != null && availabilityNum.availability() == ParticipantsAvailability.overbooked) {
			status = translate("widget.members.overbooked", format(total - max));
			statusCss = "o_curriculum_members_status_overbooked";
		} else if (availabilityNum != null && availabilityNum.availability() == ParticipantsAvailability.fullyBooked) {
			status = translate("filter.occupancy.status.fully.booked");
			statusCss = "o_curriculum_members_status_fullybooked";
		} else if (availabilityNum != null) {
			status = translateCount(availabilityNum.numAvailable(), "widget.members.seats.left");
		}
		if (status != null) {
			sb.append("<span class='o_curriculum_members_status");
			if (statusCss != null) {
				sb.append(" ").append(statusCss);
			}
			sb.append("'>").append(StringHelper.escapeHtml(status)).append("</span>");
		}
		
		if (withMax) {
			sb.append("<span class='o_curriculum_members_bar'>");
			sb.append(renderBar(max, min, numActive, numPending, minNotReached));
			sb.append("</span>");
		}
		participantsLink.setI18nKey(sb.toString());
		participantsLink.setElementCssClass(withMax? "o_figure_card o_curriculum_members_with_bar": "o_figure_card");
	}

	private String renderBar(long max, long min, long numActive, long numPending, boolean minNotReached) {
		long total = numActive + numPending;
		ProgressBar progressBar = ProgressBarFactory.createProgressBar("members_bar", getLocale());
		progressBar.setMax(Math.max(max, minNotReached? min: total));
		progressBar.setRenderSize(RenderSize.small);
		progressBar.setAriaHidden(true);
		
		List<Segment> segments = new ArrayList<>(2);
		segments.add(new Segment(numActive, BarColor.success, "o_members_widget_bar_active", translate("widget.members.bar.active")));
		segments.add(new Segment(numPending, BarColor.warning, translate("widget.members.bar.pending")));
		List<Marker> markers = new ArrayList<>(2);
		if (minNotReached) {
			markers.add(new Marker(min, BarColor.neutral, translate("widget.members.bar.minimum")));
		}
		if (total > max) {
			markers.add(new Marker(max, BarColor.danger, translate("widget.members.bar.maximum")));
		}
		progressBar.setSegments(segments);
		progressBar.setMarkers(markers);
		
		StringOutput target = new StringOutput(512);
		ProgressBarRenderer.renderProgressBar(target, progressBar, null);
		return target.toString();
	}

	private void updateRows(UserRequest ureq) {
		CurriculumElement element = curriculumElementInfos.curriculumElement();
		SearchMemberParameters params = new SearchMemberParameters(element);
		params.setRoles(List.of(CurriculumRoles.coach, CurriculumRoles.mastercoach, CurriculumRoles.owner, CurriculumRoles.curriculumelementowner));
		List<CurriculumMember> staff = curriculumService.getCurriculumElementsMembers(params);
		
		long numActive = curriculumElementInfos.numOfParticipants();
		long numPending = curriculumElementInfos.numOfPending();
		List<MemberRow> rows = new ArrayList<>(6);
		if (numActive > 0 || numPending > 0 || !staff.isEmpty()) {
			List<Identity> participants = numActive > 0
					? curriculumService.getMembersIdentity(element, CurriculumRoles.participant, MAX_PORTRAITS)
					: List.of();
			rows.add(new MemberRow(createPortraits(ureq, "users_active", participants, numActive), getStateCss(numActive, "o_members_widget_state_active"), "o_icon_check",
					translateCount(numActive, "widget.members.active"), "[Members:0][Active:0][Participant:0]"));
			
			if (numPending > 0 || isPendingPossible(element)) {
				List<Identity> pending = numPending > 0
						? acService.getReservations(List.of(element.getResource())).stream().map(ResourceReservation::getIdentity).distinct().limit(MAX_PORTRAITS).toList()
						: List.of();
				rows.add(new MemberRow(createPortraits(ureq, "users_pending", pending, numPending), getStateCss(numPending, "o_members_widget_state_pending"), "o_membership_status_pending",
						translateCount(numPending, "widget.members.pending"), "[Members:0][Pending:0][All:0]"));
			}
			
			addStaffRow(ureq, rows, staff, CurriculumRoles.coach, "widget.members.coach", "Coach");
			addStaffRow(ureq, rows, staff, CurriculumRoles.mastercoach, "widget.members.master.coach", "MasterCoach");
			addStaffRow(ureq, rows, staff, CurriculumRoles.owner, "widget.members.course.owner", "Owner");
			addStaffRow(ureq, rows, staff, CurriculumRoles.curriculumelementowner, "widget.members.element.owner", CurriculumRoles.curriculumelementowner.name());
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private boolean isPendingPossible(CurriculumElement element) {
		return acService.getOffers(element, true, false, null, false, null, null).stream()
				.anyMatch(Offer::isConfirmationByManagerRequired);
	}

	private void addStaffRow(UserRequest ureq, List<MemberRow> rows, List<CurriculumMember> staff, CurriculumRoles role,
			String i18nKey, String filter) {
		List<Identity> identities = staff.stream()
				.filter(member -> role.name().equals(member.getRole()))
				.map(CurriculumMember::getIdentity)
				.distinct()
				.toList();
		if (!identities.isEmpty()) {
			rows.add(new MemberRow(createPortraits(ureq, "users_" + role.name(), identities, identities.size()), null, null,
					translateCount(identities.size(), i18nKey), "[Members:0][Active:0][" + filter + ":0]"));
		}
	}

	private UsersPortraitsComponent createPortraits(UserRequest ureq, String name, List<Identity> identities, long numUsers) {
		if (identities.isEmpty()) {
			return null;
		}
		List<PortraitUser> portraitUsers = userPortraitService.createPortraitUsers(getLocale(), identities);
		UsersPortraitsComponent portraitsCmp = UserPortraitFactory.createUsersPortraits(ureq, name, null);
		portraitsCmp.setSize(PortraitSize.small);
		portraitsCmp.setMaxUsersVisible(MAX_PORTRAITS);
		portraitsCmp.setPortraitItemsTag(PortraitItemsTag.div);
		portraitsCmp.setUsers(portraitUsers);
		portraitsCmp.setNumUsers(numUsers);
		return portraitsCmp;
	}

	private String getStateCss(long count, String stateCss) {
		return count == 0? "o_members_widget_state_muted": stateCss;
	}

	private String translateCount(long count, String i18nKey) {
		return translate(count == 1? i18nKey + ".single": i18nKey, format(count));
	}

	private String format(long value) {
		return String.valueOf(value);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se) {
				fireActivateEvent(ureq, dataModel.getObject(se.getIndex()).businessPath());
			}
		} else if (source instanceof FormLink link && link.getUserObject() instanceof String path) {
			fireActivateEvent(ureq, path);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void fireActivateEvent(UserRequest ureq, String path) {
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromString(path);
		fireEvent(ureq, new ActivateEvent(entries));
	}

	public record MemberRow(UsersPortraitsComponent portraits, String stateCss, String iconCss, String label, String businessPath) {
		
		public String getPortraitsName() {
			return portraits != null? portraits.getComponentName(): null;
		}
	}

	private class MembersDataModel extends DefaultFlexiTableDataModel<MemberRow> implements FlexiBusinessPathModel {
		
		public MembersDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			return getObject(row);
		}

		@Override
		public String getUrl(Component source, Object object, String action) {
			return object instanceof MemberRow memberRow
					? BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString(businessPath + memberRow.businessPath())
					: null;
		}
	}
}
