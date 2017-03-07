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
package org.olat.selenium.page.graphene;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Predicate;

/**
 * Experimental predicate which check if an element is
 * in the viewport.
 * 
 * Initial date: 7 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InViewportPredicate implements Predicate<WebDriver> {
	
	private final By by;
	
	public InViewportPredicate(By by) {
		this.by = by;
	}
	
	@Override
	public boolean apply(WebDriver driver) {
		WebElement element = driver.findElement(by);
		StringBuilder sb = new StringBuilder();
		sb.append("var rect = arguments[0].getBoundingClientRect();")
		  .append(" console.log(rect); return (")
		  .append("  rect.top >= 0 &&")
		  .append("  rect.left >= 0 &&")
		  .append("  rect.bottom <= (window.innerHeight || document.documentElement.clientHeight) &&")
		  .append("  rect.right <= (window.innerWidth || document.documentElement.clientWidth)")
		  .append(" );");
		
        Object busy = ((JavascriptExecutor)driver)
        		.executeScript(sb.toString(), element);
        return Boolean.TRUE.equals(busy);
    }
}
