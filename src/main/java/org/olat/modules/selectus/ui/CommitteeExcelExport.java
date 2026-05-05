/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import org.olat.modules.selectus.ui.committee.list.PositionCommitteeController;
import org.olat.modules.selectus.ui.components.DefaultExportTableDataModel;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  27 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CommitteeExcelExport extends DefaultExportTableDataModel<Identity> {
	
	private final Translator translator;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final Map<Identity,List<Authentication>> providers = new HashMap<>();

	private final List<Identity> secretaries;
	private final List<Identity> heads;
	private final List<Identity> exOfficios;
	
	public CommitteeExcelExport(List<Identity> members, List<Identity> secretaries, List<Identity> heads, List<Identity> exOfficios,
			Translator translator) {
		super(members);
		this.secretaries = secretaries;
		this.exOfficios = exOfficios;
		this.heads = heads;
		
		this.translator = translator;
		UserManager um = UserManager.getInstance();
		userPropertyHandlers = um.getUserPropertyHandlersFor(PositionCommitteeController.formIdentifyer, true);
		
		BaseSecurity baseSecurity = BaseSecurityManager.getInstance();
		for(Identity member:members) {
			List<Authentication> authentications = baseSecurity.getAuthentications(member);
			providers.put(member, authentications);
		}
	}
	
	@Override
	public int getColumnCount() {
		int numOfStaticFileds = Fields.values().length;
		return userPropertyHandlers.size() +  numOfStaticFileds;
	}

	@Override
	public String getFieldNameAt(int col) {
		if(col == Fields.role.ordinal()) {
			return Fields.role.name();
		} else if(col == Fields.title.ordinal()) {
			return Fields.title.name();
		} else if(col == Fields.name.ordinal()) {
			return Fields.name.name();
		} else if (col == Fields.firstName.ordinal()) {
			return Fields.firstName.name();
		} else if (col == Fields.institution.ordinal()) {
			return Fields.institution.name();
		} else if (col > Fields.institution.ordinal() && col < Fields.institution.ordinal()  + 1 + userPropertyHandlers.size()) {
			UserPropertyHandler propHandler = userPropertyHandlers.get(col - Fields.institution.ordinal() - 1);
			return propHandler.i18nColumnDescriptorLabelKey();
		} else if (col == userPropertyHandlers.size() + Fields.username.ordinal()) {
			return Fields.username.name();
		}
		return "";
	}

	@Override
	public String getHeader(int col) {
		String headerKey;
		if(col == Fields.role.ordinal()) {
			headerKey = Fields.role.key();
		} else if(col == Fields.title.ordinal()) {
			headerKey = Fields.title.key();
		} else if(col == Fields.name.ordinal()) {
			headerKey = Fields.name.key();
		} else if (col == Fields.firstName.ordinal()) {
			headerKey = Fields.firstName.key();
		} else if (col == Fields.institution.ordinal()) {
			headerKey = Fields.institution.key();
		} else if (col > Fields.institution.ordinal() && col < Fields.institution.ordinal() + 1 + userPropertyHandlers.size()) {
			UserPropertyHandler propHandler = userPropertyHandlers.get(col - Fields.institution.ordinal() - 1);
			headerKey = propHandler.i18nColumnDescriptorLabelKey();
		} else if(col == userPropertyHandlers.size() + Fields.username.ordinal()) {
			headerKey = Fields.username.key();
		} else {
			return null;
		}
		return translator.translate(headerKey);
	}

	@Override
	public Object getValueAt(int row, int col) {
		Identity member = getObject(row);
		if(col == Fields.role.ordinal()) {
			for(Identity secretary:secretaries) {
				if(member.equalsByPersistableKey(secretary)) {
					return translator.translate("role.secretary");
				}
			}
			for(Identity head:heads) {
				if(member.equalsByPersistableKey(head)) {
					return translator.translate("role.head");
				}
			}
			for(Identity exOfficio:exOfficios) {
				if(member.equalsByPersistableKey(exOfficio)) {
					return translator.translate("role.exofficio");
				}
			}
			return "";
		} else if(col == Fields.title.ordinal()) {
			User user = member.getUser();
			String title = user.getProperty("title", null);
			return "-".equals(title) ? "" : title;
		} else if(col == Fields.name.ordinal()) {
			User user = member.getUser();
			return user.getProperty(UserConstants.LASTNAME, null);
		}  else if(col == Fields.firstName.ordinal()) {
			User user = member.getUser();
			return user.getProperty(UserConstants.FIRSTNAME, null);
		} else if (col == Fields.institution.ordinal()) {
			return member.getUser().getProperty(UserConstants.INSTITUTIONALNAME, getLocale());
		} else if (col > Fields.institution.ordinal() && col < Fields.institution.ordinal() + 1 + userPropertyHandlers.size()) {
			UserPropertyHandler propHandler = userPropertyHandlers.get(col - Fields.institution.ordinal() - 1);
			return propHandler.getUserProperty(member.getUser(), getLocale());
		} else if(col == userPropertyHandlers.size() + Fields.username.ordinal()) {
			return "";
		} else {
			return member;
		}
	}
	
	private enum Fields {
		role("role"),
		title("edit.committee.title"),
		name("edit.committee.lastName"),
		firstName("edit.committee.firstName"),
		institution("edit.committee.institution"),
		username("edit.committee.username");

		private final String key;
		
		private Fields(String key) {
			this.key = key;
		}
		
		public String key() {
			return key;
		}
	}
}
