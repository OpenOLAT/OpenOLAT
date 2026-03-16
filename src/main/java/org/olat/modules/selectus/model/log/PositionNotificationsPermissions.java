/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.log;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * 
 * Initial date: 27 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionNotificationsPermissions {
	
	private final List<Long> positionKeys = new ArrayList<>();
	private NotificationPermission[] permissions;
	
	public PositionNotificationsPermissions(NotificationPermission[] permissions) {
		this.permissions = permissions;
	}

	public List<Long> getPositionKeys() {
		return positionKeys;
	}
	
	public void addPositionKey(Long positionKey) {
		positionKeys.add(positionKey);
	}

	public NotificationPermission[] getPermissions() {
		return permissions;
	}

	public void setPermissions(NotificationPermission[] permissions) {
		this.permissions = permissions;
	}
}
