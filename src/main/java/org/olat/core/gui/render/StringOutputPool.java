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
package org.olat.core.gui.render;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * the pool is based on the implementation of Slobodan Celenkovic:<br/>
 * http://www.jroller.com/slobodan/date/20050309<br/>
 * 
 * Initial date: 10.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StringOutputPool {

	/** Max pool size */
  protected final static int MAX_STR_BUILDER_POOL_SIZE = 50;

  /** General purpose pool for StringBuilder instances. */
  private static List<Reference<StringOutput>> stringBuilders = new ArrayList<>(MAX_STR_BUILDER_POOL_SIZE+1);

  /** Auxiliary pool for small size builders. It is a simple sparse array with
      elements not sorted. Buffers are simply placed in and taken out. In
      general, when storing buffers preference is given to large buffers. */
  private static final StringOutput auxStringBuilders[] =
    new StringOutput[ 10 ];


  /** Allocates a string builder instance using recycling. Doesn't guarantee
      that the returned object will have the requested size!
      @param size (optional) builder size
      @return builder instance */
  public static StringOutput allocStringBuilder(final int size)  {
    assert size > 0;
    if ( size < 4096 ) {        // Smaller buffer? -> use auxiliary pool
      synchronized ( auxStringBuilders ) {
        for(int index=0; index < auxStringBuilders.length; index++)
          if ((auxStringBuilders[index] != null) &&
              (auxStringBuilders[index].capacity() >= size)) {
          	StringOutput res = auxStringBuilders[ index ];
            auxStringBuilders[ index ] = null;
            return res;
          }
      }
      return new StringOutput(size);
    } else {// Bigger buffers? -> use primary pool
      synchronized ( stringBuilders ) { // pool lookup
        if ( stringBuilders.isEmpty())  {
        	return new StringOutput(size);
        }

        StringOutput res =null;
        for (Iterator<Reference<StringOutput>> seq=stringBuilders.iterator(); seq.hasNext(); ) {
          res = seq.next().get();
          if ( res == null ) {
          	seq.remove(); // no longer available, cleanup
          }  else if ( res.capacity() >= size ) {
            seq.remove();
            return res;
          }
        } // for
      } // sync
    } // else

    return new StringOutput(size);
  } // allocStringBuilder
  
  /** Supplies object instance for recycling. The object could have been
  allocated using any means. Caller must not continue using it after this
  method call.
  @param obj discarded builder instance */
	public static void free(final StringOutput obj) {
		if ( obj.capacity() < 4096 ) { // Smaller buffer? -> use auxiliary pool
		  synchronized ( auxStringBuilders ) {
		    int minIndex =-1, minSize = Integer.MAX_VALUE, size =0;
		    for(int index=0; index < auxStringBuilders.length; index++) {
		      if ( auxStringBuilders[index] == null ) {
		        obj.setLength(0);
		        auxStringBuilders[ index ] = obj;
		        return;
		      }
		
		      if ((size= auxStringBuilders[index].capacity()) < minSize) {
		        minIndex = index;
		        minSize = size;
		      }
		    }
		
		    if ( minSize < obj.capacity() ) {
		      obj.setLength(0);
		      auxStringBuilders[minIndex] = obj;
		    }
		  }
		} else if(obj.capacity() < 250000) {
			// Bigger buffers? -> use primary pool, too big, don't return it to prevent memory leak
		  synchronized ( stringBuilders ) {
	      obj.setLength(0);
		    stringBuilders.add(0, new SoftReference<>(obj));
		    if ( stringBuilders.size() > MAX_STR_BUILDER_POOL_SIZE ) {
		      stringBuilders.remove(MAX_STR_BUILDER_POOL_SIZE);
		    }
		  }
		}
	} // free
	
	/** Frees the string builder and returns its contents before cleanup.
	  @param obj buffer to free
	  @return buffer's contents before cleanup */
	public static String freePop(final StringOutput obj) {
		String res = obj.toString();
		free( obj );
		return res;
	}
}