/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.util.vfs;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Description:<br>
 * TODO: Felix Jost Class Description for Test
 * 
 * <P>
 * Initial Date:  23.06.2005 <br>
 *
 * @author Felix Jost
 */
public class VFSTestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		VFSTestMain t = new VFSTestMain();
		t.doit();
	}

	/**
	 * 
	 */
	private void doit() {
		VFSContainer cont = new LocalFolderImpl(new File("c:/temp/aaa"), null);
		stats(cont);
		//dump(cont, 0);

		VFSItem q = cont.resolve("/");
		//dump(q,0);
		if (q instanceof VFSContainer) {
			VFSContainer qc = (VFSContainer) q;
			List items = cont.getItems();
			items.size();
		}
		
		
		VFSItem r = cont.resolve("/testfile.bla");
		VFSItem s = cont.resolve("/echopoint/FontChooser.class");
		
		// -------------------------------------------------------
		VFSContainer cc2 = new LocalFolderImpl(new File("c:/temp/in"), null);		
		MergeSource c2 = new MergeSource(null, null);
		c2.addContainersChildren(cont, true);
		c2.addContainersChildren(cc2, false);
		VFSItem q2 = c2.resolve("/");
		VFSItem r2 = c2.resolve("/testfile.bla");
		VFSItem s2 = c2.resolve("/echopoint");
		VFSItem s21 = s2.resolve("/FontChooser.class");
		VFSItem s22 = s2.resolve("/");
		VFSItem s23 = s2.resolve("/glabab/blub.txt");
		
		
		
		
		VFSItem t2 = c2.resolve("/echopoint/FontChooser.class");
		
		VFSItem u2 = c2.resolve("/demo.zip");
		VFSItem v2 = c2.resolve("/subin");
		VFSItem w2 = c2.resolve("/subin/xep.bat");
			
		
		// -------------------------------------------------------
		
		VFSContainer cc3 = new LocalFolderImpl(new File("c:/temp/in"), null);
		NamedContainerImpl ccc3 = new NamedContainerImpl("virtual1", cc3);
		MergeSource c3 = new MergeSource(null, null);
		c3.addContainersChildren(cont, true);
		c3.addContainersChildren(ccc3, false);
		VFSItem q3 = c3.resolve("/");
		VFSItem r3 = c3.resolve("/testfile.bla");
		VFSItem s3 = c3.resolve("/echopoint");
		VFSItem t3 = c3.resolve("/echopoint/FontChooser.class");
		
		VFSItem x3 = c3.resolve("/virtual1");
		VFSItem y3 = c3.resolve("/virtual12");
		VFSItem z3 = c3.resolve("/virtual12/aaa");
		VFSItem u3 = c3.resolve("/virtual1/demo.zip");
		VFSItem v3 = c3.resolve("/virtual1/subin");
		VFSItem w3 = c3.resolve("/virtual1/subin/xep.bat");
		
		int i = 1;

/*		
		VFSContainer c2 = new LocalFolderImpl(new File("c:/temp/f2"));
		stats(c2);
		
		VFSContainer named = new NamedContainerImpl("namedcontainer", c2, true);
		stats(named);
		
		MergeSource m = new MergeSource(cont, new VFSContainer[] { named});
		stats(m);

		VFSItem q = m.resolveFile("/bookmarks.html");
		VFSItem a = m.resolveFile("/ifa/sammlung.zip");
		VFSItem d = m.resolveFile("/ifa");
		VFSItem b = m.resolveFile("/namedcontainer/zzz/zzztest.txt");
		VFSItem f = m.resolveFile("/namedcontainer");
		VFSItem g = m.resolveFile("/namedcontainer/zzz");
		VFSItem e = m.resolveFile("/");
		
		
		//c2.copyFrom(cont);
		
		dump(m, 0);
		
		
	*/	
		//VFSContainer del = new LocalFolderImpl(new File("c:/temp/f2/felix/ifa"));
		//del.delete();
		
		/*try {
			StreamedImpl st = new StreamedImpl("testit.html",new FileInputStream("c:/temp/felix/bookmarks.html"));
			c2.copyFrom(st);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	

	/**
	 * @param it
	 */
	private void stats(VFSItem it) {
		boolean copyFrom = it.canCopy() == VFSConstants.YES;
		boolean del = (it.canDelete() == VFSConstants.YES);
		boolean ren = (it.canRename() == VFSConstants.YES);
		boolean copyTo = it.canWrite() == VFSConstants.YES;
		
		String name = it.getName();
		
		System.out.println(name +" cfrom "+copyFrom+" del "+del+" ren "+ren+" cTo "+copyTo);
		
	}

	private void dump(VFSItem item, int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append("     ");
		}
		sb.append("name of container:" +item.getName());
		if (item instanceof VFSContainer) {
			sb.append("/");
		}
		System.out.println(sb.toString());

		if (item instanceof VFSContainer) {
			VFSContainer cont = (VFSContainer) item;
			List items = cont.getItems();
			Collections.sort(items, new Comparator() {
				public int compare(Object a, Object b) {
					VFSItem va = (VFSItem)a;
					VFSItem vb = (VFSItem)b;
					boolean ac = (va instanceof VFSContainer);
					boolean bc = (vb instanceof VFSContainer);
					if (ac && !bc) return -1;
					if (!ac && bc) return 1;
					return va.getName().compareTo(vb.getName());
				}});
			
			for (Iterator iter = items.iterator(); iter.hasNext();) {
				VFSItem chditem = (VFSItem) iter.next();
				dump(chditem, indent+1);
			}
		}
	}
}

