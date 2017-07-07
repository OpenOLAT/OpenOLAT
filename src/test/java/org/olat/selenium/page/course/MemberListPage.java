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
package org.olat.selenium.page.course;

import java.util.List;

import org.junit.Assert;
import org.olat.selenium.page.graphene.OOGraphene;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


/**
 * 
 * Initial date: 7 juil. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MemberListPage {
	
	private final WebDriver browser;
	
	public MemberListPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public MemberListPage assertOnMembers() {
		By membersBy = By.cssSelector("div.o_cmembers");
		OOGraphene.waitElement(membersBy, browser);
		return this;
	}
	
	public MemberListPage assertOnOwner(String name) {
		By ownerBy = By.xpath("//div[contains(@class,'o_sel_owners')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(ownerBy, browser);
		return this;
	}
	
	public MemberListPage assertOnNotOwner(String name) {
		By ownerBy = By.xpath("//div[contains(@class,'o_sel_owners')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		List<WebElement> ownersEl = browser.findElements(ownerBy);
		Assert.assertEquals(0, ownersEl.size());
		return this;
	}
	
	public MemberListPage assertOnCoach(String name) {
		By coachBy = By.xpath("//div[contains(@class,'o_sel_coaches')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(coachBy, browser);
		return this;
	}
	
	public MemberListPage assertOnNotCoach(String name) {
		By coachBy = By.xpath("//div[contains(@class,'o_sel_coaches')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		List<WebElement> coachEls = browser.findElements(coachBy);
		Assert.assertEquals(0, coachEls.size());
		return this;
	}
	
	public MemberListPage assertOnParticipant(String name) {
		By participantBy = By.xpath("//div[contains(@class,'o_sel_participants')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		OOGraphene.waitElement(participantBy, browser);
		return this;
	}
	
	public MemberListPage assertOnNotParticipant(String name) {
		By participantBy = By.xpath("//div[contains(@class,'o_sel_participants')]//div[@class='o_cmember_info_wrapper']/a/span[contains(text(),'" + name + "')]");
		List<WebElement> participantEls = browser.findElements(participantBy);
		Assert.assertEquals(0, participantEls.size());
		return this;
	}
}
