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
package org.olat.selenium.page.course;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * To pilot the course node selection UI.
 * 
 * Initial date: 31 oct. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeSelectionPage {
	
	private final WebDriver browser;
	
	public CourseNodeSelectionPage(WebDriver browser) {
		this.browser = browser;
	}
	
	/**
	 * Check in the author table if the participant has selected a specific node.
	 * 
	 * @param user The participant
	 * @return Itself
	 */
	public CourseNodeSelectionPage assertOnCourseNodeSelectedBy(UserVO user) {
		By listBy = By.xpath("//div[contains(@class,'o_segments_content')]//table//td/a[text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(listBy, browser);
		return this;
	}
	
	/**
	 * For authors: open the details in the table.
	 * 
	 * @param user The participant
	 * @return Itself
	 */
	public CourseNodeSelectionPage openDetails(UserVO user) {
		By openBy = By.xpath("//div[contains(@class,'o_segments_content')]//table//tr[td/a[text()[contains(.,'" + user.getFirstName() + "')]]]/td/div/a[i[contains(@class,'o_icon_table_details_expand')]]");
		OOGraphene.waitElement(openBy, browser).click();
		
		By detailsBy = By.xpath("//div[contains(@class,'o_table_row_details_container')]//div[contains(@class,'o_cns_participant_selections')]//div[contains(@class,'o_user_info_profile_name')][text()[contains(.,'" + user.getFirstName() + "')]]");
		OOGraphene.waitElement(detailsBy, browser);
		return this;
	}
	
	/**
	 * Asserts the node was selected in the details of the specified participant.
	 * 
	 * @param user The participant
	 * @param nodeTitle The selected node
	 * @return Itself
	 */
	public CourseNodeSelectionPage assertOnDetailsSelectedNode(UserVO user, String nodeTitle) {
		By detailsBy = By.xpath("//div[contains(@class,'o_table_row_details_container')]//div[@class='o_cns_participant_selections'][div/div/div/div/div/div[@class='o_user_info_profile_name'][text()[contains(.,'" + user.getFirstName() + "')]]]//table//tr[td/span[text()[contains(.,'" + nodeTitle + "')]]]/td/div/span[contains(@class,'o_lp_done')]");
		OOGraphene.waitElement(detailsBy, browser);
		return this;
	}
	
	/**
	 * Asserts the participant can select a course element.
	 * 
	 * @return Itself
	 */
	public CourseNodeSelectionPage assertOnCourseNodeSelection() {
		By listBy = By.cssSelector(".o_cns_selection_table .o_cns_selection_row a.btn-primary");
		OOGraphene.waitElement(listBy, browser);
		return this;
	}
	
	/**
	 * Selects a course element.
	 * 
	 * @param nodeTitle The course element to select
	 * @return Itself
	 */
	public CourseNodeSelectionPage selectCourseNode(String nodeTitle) {
		By selectBy = By.xpath("//div[contains(@class,'o_cns_selection_table')]//div[contains(@class,'o_cns_selection_row')]//div[@class='o_text_block_items'][div/h4/a/span[text()[contains(.,'" + nodeTitle + "')]]]//a[contains(@class,'btn-primary')]");
		OOGraphene.waitElement(selectBy, browser).click();
		
		OOGraphene.waitModalDialog(browser);
		
		By confirmBy = By.cssSelector("dialog.dialog fieldset.o_sel_confirm_form a.btn.o_sel_confirm");
		OOGraphene.waitElement(confirmBy, browser).click();
		
		OOGraphene.waitModalDialogDisappears(browser);
		return this;
	}

}
