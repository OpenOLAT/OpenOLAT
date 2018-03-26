package org.olat.user.ui.admin;

import java.util.Collections;
import java.util.List;

import org.olat.admin.user.ExtendedIdentitiesTableDataModel;
import org.olat.admin.user.UserAdminController;
import org.olat.admin.user.UsermanagerUserSearchController;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.admin.UserSearchTableModel.UserCols;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSearchTableController extends FormBasicController {
	
	private static final String USER_PROPS_ID = ExtendedIdentitiesTableDataModel.class.getCanonicalName();
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private UserSearchTableModel tableModel;
	private TooledStackedPanel stackPanel;
	
	private UserAdminController userAdminCtr;
	
	private final boolean vCard;
	private final boolean isAdministrativeUser;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public UserSearchTableController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, boolean vCard) {
		super(ureq, wControl, "search_table");
		this.vCard = vCard;
		this.stackPanel = stackPanel;
		setTranslator(Util.createPackageTranslator(UsermanagerUserSearchController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.id, "select"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.username, "select"));
		}
		
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, "select", true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}
		
		if(vCard) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.vcard", translate("table.identity.vcard"), "vcard"));
		}
		
		tableModel = new UserSearchTableModel(new EmptyDataSource(), columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmtpyTableMessageKey("error.no.user.found");
		tableEl.setExportEnabled(true);
	}
	
	public void loadModel(SearchIdentityParams params) {
		UserSearchDataSource dataSource = new UserSearchDataSource(params, userPropertyHandlers, getLocale());
		tableModel.setSource(dataSource);
		tableEl.reset(true, true, true);
	}
	
	public void loadModel(List<Identity> identityList) {
		IdentityListDataSource dataSource = new IdentityListDataSource(identityList, userPropertyHandlers, getLocale());
		tableModel.setSource(dataSource);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent te = (SelectionEvent) event;
				String cmd = te.getCommand();
				UserPropertiesRow userRow = tableModel.getObject(te.getIndex());
				if("select".equals(cmd)) {
					doSelectIdentity(ureq, userRow);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelectIdentity(UserRequest ureq, UserPropertiesRow userRow) {
		removeAsListenerAndDispose(userAdminCtr);
		
		Identity identity = securityManager.loadIdentityByKey(userRow.getIdentityKey());

		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = addToHistory(ureq, ores, null);

		userAdminCtr = new UserAdminController(ureq, bwControl, identity);
		userAdminCtr.setBackButtonEnabled(false);
		listenTo(userAdminCtr);

		String fullName = userManager.getUserDisplayName(identity);
		stackPanel.pushController(fullName, userAdminCtr);
	}
	
	private final class EmptyDataSource implements FlexiTableDataSourceDelegate<UserPropertiesRow> {

		@Override
		public int getRowCount() {
			return 0;
		}

		@Override
		public List<UserPropertiesRow> reload(List<UserPropertiesRow> rows) {
			return Collections.emptyList();
		}

		@Override
		public ResultInfos<UserPropertiesRow> getRows(String query, List<FlexiTableFilter> filters,
				List<String> condQueries, int firstResult, int maxResults, SortKey... orderBy) {
			return new DefaultResultInfos<>();
		}
	}
}
