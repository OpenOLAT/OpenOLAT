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
package org.olat.user.ui.organisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.junit.Test;
import org.olat.core.id.Organisation;

/**
 *
 * Initial date: 10 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationRowComparatorTest {

	private final OrganisationRowComparator sut = new OrganisationRowComparator(Locale.GERMAN);

	@Test
	public void shouldBeAntisymmetricWithDuplicateRootNames() {
		OrganisationRow root1 = row(1L, "Ununique");
		OrganisationRow root2 = row(2L, "Ununique");
		OrganisationRow child = row(3L, "Child");
		child.setParent(root1);
		List<OrganisationRow> rows = List.of(root1, root2, child);

		assertAntisymmetric(rows);
	}

	@Test
	public void shouldBeTransitiveWithDuplicateRootNames() {
		OrganisationRow root1 = row(1L, "Ununique");
		OrganisationRow root2 = row(2L, "Ununique");
		OrganisationRow child = row(3L, "Child");
		child.setParent(root1);
		List<OrganisationRow> rows = List.of(root1, root2, child);

		assertTransitive(rows);
	}

	@Test
	public void shouldBeTransitiveWithDuplicateSiblingNames() {
		OrganisationRow parent = row(1L, "Parent");
		OrganisationRow sibling1 = row(2L, "Ununique");
		sibling1.setParent(parent);
		OrganisationRow sibling2 = row(3L, "Ununique");
		sibling2.setParent(parent);
		OrganisationRow grandchild = row(4L, "Grandchild");
		grandchild.setParent(sibling1);
		List<OrganisationRow> rows = List.of(parent, sibling1, sibling2, grandchild);

		assertTransitive(rows);
	}

	private OrganisationRow row(Long key, String displayName) {
		Organisation organisation = mock(Organisation.class);
		when(organisation.getKey()).thenReturn(key);
		when(organisation.getDisplayName()).thenReturn(displayName);
		return new OrganisationRow(organisation, null);
	}

	private void assertAntisymmetric(List<OrganisationRow> rows) {
		for(OrganisationRow a : rows) {
			for(OrganisationRow b : rows) {
				int ab = Integer.signum(sut.compare(a, b));
				int ba = Integer.signum(sut.compare(b, a));
				assertThat(ab).as("compare(%s,%s) vs compare(%s,%s)", a, b, b, a).isEqualTo(-ba);
			}
		}
	}

	private void assertTransitive(List<OrganisationRow> rows) {
		for(OrganisationRow a : rows) {
			for(OrganisationRow b : rows) {
				for(OrganisationRow c : rows) {
					int ab = Integer.signum(sut.compare(a, b));
					int bc = Integer.signum(sut.compare(b, c));
					int ac = Integer.signum(sut.compare(a, c));
					if(ab == 0 && bc == 0) {
						assertThat(ac).as("compare(%s,%s)=0 and compare(%s,%s)=0 but compare(%s,%s)", a, b, b, c, a, c).isZero();
					} else if(ab <= 0 && bc <= 0) {
						assertThat(ac).as("compare(%s,%s)<=0 and compare(%s,%s)<=0 but compare(%s,%s)", a, b, b, c, a, c).isLessThanOrEqualTo(0);
					}
				}
			}
		}
	}

}
