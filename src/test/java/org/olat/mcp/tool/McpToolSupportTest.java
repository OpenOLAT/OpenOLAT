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
package org.olat.mcp.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.id.Identity;
import org.olat.core.id.User;

/**
 * Unit tests of the argument helpers of {@link McpToolSupport}: the text
 * cleaning of the UI text fields and the tag lists.
 *
 * Initial date: 13 Oct 2026<br>
 * @author gnaegi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class McpToolSupportTest {

	@Test
	public void string_trimmedAndCleaned() {
		Map<String,Object> arguments = Map.of("text", "  a\u0000b\u0007c\td  ", "blank", " \u0000 ", "emoji", "x 😀");
		// as AbstractTextElement.setValue: trim, then remove the characters XML 1.0 does not allow
		Assert.assertEquals("abc\td", McpToolSupport.string(arguments, "text", null));
		Assert.assertEquals("default", McpToolSupport.string(arguments, "blank", "default"));
		Assert.assertEquals("default", McpToolSupport.string(arguments, "absent", "default"));
		Assert.assertEquals("x 😀", McpToolSupport.string(arguments, "emoji", null));
	}

	@Test
	public void stringUntrimmed_keepsWhiteSpace() {
		Map<String,Object> arguments = Map.of("text", "  # Title\u0000\n\n", "blank", "  ");
		Assert.assertEquals("  # Title\n\n", McpToolSupport.stringUntrimmed(arguments, "text", null));
		Assert.assertNull(McpToolSupport.stringUntrimmed(arguments, "blank", null));
		Assert.assertNull(McpToolSupport.stringUntrimmed(arguments, "absent", null));
	}

	@Test
	public void strings_trimmedAndCleaned() {
		Map<String,Object> arguments = Map.of("list", Arrays.asList(" a\u0001 ", "\u0000", null, " ", "b"), "single", " c\u0000 ");
		assertThat(McpToolSupport.strings(arguments, "list")).containsExactly("a", "b");
		assertThat(McpToolSupport.strings(arguments, "single")).containsExactly("c");
		assertThat(McpToolSupport.strings(arguments, "absent")).isEmpty();
	}

	@Test
	public void tags_deduplicatedCaseInsensitive() {
		Map<String,Object> arguments = Map.of("tags", List.of(" Review ", "review", "REVIEW", "x", "", "\u0000", "X", "urgent"));
		// the first spelling wins, as TagSelectionImpl keeps the first
		assertThat(McpToolSupport.tags(arguments, "tags", 3, 64)).containsExactly("Review", "x", "urgent");
		assertThat(McpToolSupport.tags(arguments, "absent", 3, 64)).isEmpty();
	}

	@Test
	public void tags_limits() {
		// the item limit counts the tags after the de-duplication
		Map<String,Object> duplicates = Map.of("tags", List.of("a", "A", "b"));
		assertThat(McpToolSupport.tags(duplicates, "tags", 2, 64)).containsExactly("a", "b");

		Map<String,Object> tooMany = Map.of("tags", IntStream.range(0, 3).mapToObj(i -> "t" + i).toList());
		try {
			McpToolSupport.tags(tooMany, "tags", 2, 64);
			Assert.fail("Three tags are more than two");
		} catch (McpInvalidArgumentException e) {
			assertThat(e.getMessage()).contains("tags has more than 2");
		}

		Map<String,Object> tooLong = Map.of("tags", List.of("x".repeat(65)));
		try {
			McpToolSupport.tags(tooLong, "tags", 2, 64);
			Assert.fail("A tag of 65 characters is longer than 64");
		} catch (McpInvalidArgumentException e) {
			assertThat(e.getMessage()).contains("tags is longer than 64");
		}
	}

	@Test
	public void matchTags_existingSpelling() {
		List<String> matched = McpToolSupport.matchTags(List.of("urgent", "review", "New"), List.of("Urgent", "Review", "other"));
		assertThat(matched).containsExactly("Urgent", "Review", "New");
		assertThat(McpToolSupport.matchTags(List.of("a"), List.of())).containsExactly("a");
	}

	@Test
	public void enumValues_namesOfTheConstants() {
		List<GroupRoles> allowed = List.of(GroupRoles.participant, GroupRoles.coach);
		assertThat(McpToolSupport.names(allowed)).containsExactly("participant", "coach");
		assertThat(McpToolSupport.names(GroupRoles.owner, GroupRoles.waiting)).containsExactly("owner", "waiting");
		assertThat(McpToolSupport.enumValue(allowed, "coach")).isEqualTo(GroupRoles.coach);
		// a constant of the enum that the argument does not accept, the wrong case and null
		assertThat(McpToolSupport.enumValue(allowed, "owner")).isNull();
		assertThat(McpToolSupport.enumValue(allowed, "Coach")).isNull();
		assertThat(McpToolSupport.enumValue(allowed, null)).isNull();
		assertThat(McpToolSupport.unknownValue("role", "owner", allowed))
			.isEqualTo("Unknown role 'owner'. Use one of: participant, coach.");
	}

	@Test
	public void resolveIdentities_displayName() {
		Identity anna = identity(1l, "anna", "Anna", "Muster");
		Identity twin1 = identity(2l, "twin1", "Alex", "Twin");
		Identity twin2 = identity(3l, "twin2", "Alex", "Twin");
		List<Identity> candidates = List.of(anna, twin1, twin2);
		BiPredicate<Identity,String> byLogin = (candidate, value) -> value.equalsIgnoreCase(candidate.getName());

		assertThat(McpToolSupport.resolveIdentities("assignee", "this ToDo", List.of("ANNA MUSTER", "twin1", "anna"), candidates, byLogin))
				.containsExactly(anna, twin1);
		McpInvalidArgumentException ambiguous = Assert.assertThrows(McpInvalidArgumentException.class,
				() -> McpToolSupport.resolveIdentities("assignee", "this ToDo", List.of("Alex Twin"), candidates, byLogin));
		assertThat(ambiguous.getMessage()).startsWith("Alex Twin is ambiguous");
		McpInvalidArgumentException unknown = Assert.assertThrows(McpInvalidArgumentException.class,
				() -> McpToolSupport.resolveIdentities("assignee", "this ToDo", List.of("Muster"), candidates, byLogin));
		Assert.assertEquals("Muster is not a valid assignee for this ToDo.", unknown.getMessage());
	}

	private static Identity identity(Long key, String name, String firstName, String lastName) {
		User user = mock(User.class);
		when(user.getFirstName()).thenReturn(firstName);
		when(user.getLastName()).thenReturn(lastName);
		Identity identity = mock(Identity.class);
		when(identity.getKey()).thenReturn(key);
		when(identity.getName()).thenReturn(name);
		when(identity.getUser()).thenReturn(user);
		return identity;
	}
}
