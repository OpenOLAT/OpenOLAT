package org.olat.user.restapi;

import org.olat.core.id.Roles;

public class RolesVO {
	
	private boolean olatAdmin = false;
	private boolean userManager = false;
	private boolean groupManager = false;
	private boolean author = false;
	private boolean guestOnly = false;
	private boolean institutionalResourceManager = false;
	private boolean invitee = false;

	public RolesVO() {
		//for JAXB
	}

	public RolesVO(Roles roles) {
		this.olatAdmin = roles.isOLATAdmin();
		this.groupManager = roles.isGroupManager();
		this.userManager = roles.isUserManager();
		this.author = roles.isAuthor();
		this.guestOnly = roles.isGuestOnly();
		this.institutionalResourceManager = roles.isInstitutionalResourceManager();
		this.invitee = roles.isInvitee();
	}
	
	public Roles toRoles() {
		return new Roles(olatAdmin, userManager, groupManager, author, guestOnly, institutionalResourceManager, invitee);
	}

	public boolean isOlatAdmin() {
		return olatAdmin;
	}

	public void setOlatAdmin(boolean olatAdmin) {
		this.olatAdmin = olatAdmin;
	}

	public boolean isUserManager() {
		return userManager;
	}

	public void setUserManager(boolean userManager) {
		this.userManager = userManager;
	}

	public boolean isGroupManager() {
		return groupManager;
	}

	public void setGroupManager(boolean groupManager) {
		this.groupManager = groupManager;
	}

	public boolean isAuthor() {
		return author;
	}

	public void setAuthor(boolean author) {
		this.author = author;
	}

	public boolean isGuestOnly() {
		return guestOnly;
	}

	public void setGuestOnly(boolean guestOnly) {
		this.guestOnly = guestOnly;
	}

	public boolean isInstitutionalResourceManager() {
		return institutionalResourceManager;
	}

	public void setInstitutionalResourceManager(boolean institutionalResourceManager) {
		this.institutionalResourceManager = institutionalResourceManager;
	}

	public boolean isInvitee() {
		return invitee;
	}

	public void setInvitee(boolean invitee) {
		this.invitee = invitee;
	}

}
