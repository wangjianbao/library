/* Copyright c 2005-2012.
 * Licensed under GNU  LESSER General Public License, Version 3.
 * http://www.gnu.org/licenses
 */
package org.beangle.commons.meta;

import org.testng.annotations.Test;

@Test
public class BeangleVersionTest {

	public void testGetVersion() {
		System.out.println(BeangleVersion.getName());
	}
}