package org.olat.modules.curriculum.ui.event;

import org.olat.core.gui.control.Event;
import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RoleEvent extends Event {

	private static final long serialVersionUID = 8782101396996802023L;

	public static final String ADD_ROLE_EVENT = "curriculum-el-add-role";
	
	private final CurriculumRoles role;
	
	public RoleEvent(CurriculumRoles role) {
		super(ADD_ROLE_EVENT);
		this.role = role;
	}
	
	public CurriculumRoles getRole() {
		return role;
	}

}
