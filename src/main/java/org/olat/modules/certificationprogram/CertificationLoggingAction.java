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
package org.olat.modules.certificationprogram;

import java.lang.reflect.Field;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionObject;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ActionVerb;
import org.olat.core.logging.activity.BaseLoggingAction;
import org.olat.core.logging.activity.CrudAction;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ResourceableTypeList;
import org.olat.core.logging.activity.StringResourceableType;

/**
 * 
 * Initial date: 17 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationLoggingAction extends BaseLoggingAction {

	private static final Logger log = Tracing.createLoggerFor(CertificationLoggingAction.class);
	
	public static final ILoggingAction CERTIFICATE_ISSUED = 
			new CertificationLoggingAction(ActionType.statistic, CrudAction.create, ActionVerb.add, ActionObject.certificate).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.certificationProgram).addMandatory(StringResourceableType.targetIdentity));
	public static final ILoggingAction CERTIFICATE_REVOKED = 
			new CertificationLoggingAction(ActionType.statistic, CrudAction.delete, ActionVerb.revoke, ActionObject.certificate).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.certificationProgram).addMandatory(StringResourceableType.targetIdentity));
	public static final ILoggingAction CERTIFICATE_REMOVED = 
			new CertificationLoggingAction(ActionType.statistic, CrudAction.delete, ActionVerb.remove, ActionObject.certificate).setTypeList(
					new ResourceableTypeList().addMandatory(OlatResourceableType.certificationProgram).addMandatory(StringResourceableType.targetIdentity));

	
	/**
	 * This static constructor's only use is to set the javaFieldIdForDebug
	 * on all of the LoggingActions defined in this class.
	 * <p>
	 * This is used to simplify debugging - as it allows to issue (technical) log
	 * statements where the name of the LoggingAction Field is written.
	 */
	static {
		Field[] fields = CertificationLoggingAction.class.getDeclaredFields();
		if (fields!=null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				if (field.getType()==CertificationLoggingAction.class) {
					try {
						CertificationLoggingAction aLoggingAction = (CertificationLoggingAction)field.get(null);
						aLoggingAction.setJavaFieldIdForDebug(field.getName());
					} catch (IllegalArgumentException | IllegalAccessException e) {
						log.error("", e);
					}
				}
			}
		}
	}
	
	/**
	 * Simple wrapper calling super<init>
	 * @see BaseLoggingAction#BaseLoggingAction(ActionType, CrudAction, ActionVerb, String)
	 */
	CertificationLoggingAction(ActionType resourceActionType, CrudAction action, ActionVerb actionVerb, ActionObject actionObject) {
		super(resourceActionType, action, actionVerb, actionObject.name());
	}
}
