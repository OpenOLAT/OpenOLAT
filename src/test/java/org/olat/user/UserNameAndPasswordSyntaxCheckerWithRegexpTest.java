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
package org.olat.user;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 23.08.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserNameAndPasswordSyntaxCheckerWithRegexpTest {
	
	@Test
	public void defaultPasswordCheck() {
		UserNameAndPasswordSyntaxCheckerWithRegexp checker = new UserNameAndPasswordSyntaxCheckerWithRegexp();
		Assert.assertFalse(checker.syntaxCheckOlatPassword("Kan"));
		Assert.assertTrue(checker.syntaxCheckOlatPassword("Kanu#01"));
		Assert.assertFalse(checker.syntaxCheckOlatPassword("Kan\u00FC#01"));
	}
	
	/**
	 * Min. 7 characters, one uppercase, one lowercase, one number
	 */
	@Test
	public void customPasswordCheck_upperLowerCase_number() {
		UserNameAndPasswordSyntaxCheckerWithRegexp checker = new UserNameAndPasswordSyntaxCheckerWithRegexp();
		checker.setPasswordRegExp("(?=^.{7,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$");
		
		Assert.assertTrue(checker.syntaxCheckOlatPassword("Kanu#01"));
		Assert.assertTrue(checker.syntaxCheckOlatPassword("Kanuunc1"));
		Assert.assertFalse(checker.syntaxCheckOlatPassword("Kanu#1"));//less than 7 characters
		Assert.assertFalse(checker.syntaxCheckOlatPassword("Kanuunch"));//no number	Kan\u00FC
		Assert.assertTrue(checker.syntaxCheckOlatPassword("Kan\u00FCunc1"));// Umlaut allowed
	}
	
	/**
	 * Min. 8 characters, one uppercase, one lowercase, one number, one special character
	 */
	@Test
	public void customPasswordCheck_upperLowerCase_number_special() {
		UserNameAndPasswordSyntaxCheckerWithRegexp checker = new UserNameAndPasswordSyntaxCheckerWithRegexp();
		checker.setPasswordRegExp("(?=^.{8,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z])(?=.*[$@$!%*#?&]).*$");

		Assert.assertTrue(checker.syntaxCheckOlatPassword("Kanu#010"));
		Assert.assertTrue(checker.syntaxCheckOlatPassword("?Ryomou#010"));
		
		Assert.assertFalse(checker.syntaxCheckOlatPassword("Kanuunc1"));
		Assert.assertFalse(checker.syntaxCheckOlatPassword("Kanu#10"));//less than 8 characters
		Assert.assertFalse(checker.syntaxCheckOlatPassword("Kanuunch"));//no number
		Assert.assertFalse(checker.syntaxCheckOlatPassword("kanu8#10"));
	}
}
