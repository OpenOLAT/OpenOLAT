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
package org.olat.modules.reminder.restapi;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.model.ReminderRuleImpl;

/**
 * 
 * Initial date: 25 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "reminderRuleVO")
public class ReminderRuleVO {
	
	private String type;
	private String leftOperand;
	private String operator;
	private String rightOperand;
	private String rightUnit;
	
	public ReminderRuleVO() {
		//
	}
	
	public static ReminderRuleVO valueOf(ReminderRule rule) {
		ReminderRuleVO vo = new ReminderRuleVO();
		vo.setType(rule.getType());
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl ruleImpl = (ReminderRuleImpl)rule;
			vo.setLeftOperand(ruleImpl.getLeftOperand());
			vo.setOperator(ruleImpl.getOperator());
			vo.setRightOperand(ruleImpl.getRightOperand());
			vo.setRightUnit(ruleImpl.getRightUnit());
		}
		return vo;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getLeftOperand() {
		return leftOperand;
	}
	
	public void setLeftOperand(String leftOperand) {
		this.leftOperand = leftOperand;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public void setOperator(String operator) {
		this.operator = operator;
	}
	
	public String getRightOperand() {
		return rightOperand;
	}
	
	public void setRightOperand(String rightOperand) {
		this.rightOperand = rightOperand;
	}
	
	public String getRightUnit() {
		return rightUnit;
	}
	
	public void setRightUnit(String rightUnit) {
		this.rightUnit = rightUnit;
	}
}
