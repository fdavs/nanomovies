package no.skavdahl.udacity.popularmovies.data;

import android.test.AndroidTestCase;

/**
 * Tests features of the MovieContract class.
 *
 * @author fdavs
 */
public class MovieContractTest extends AndroidTestCase {

	/*public void setUp() {
	}
	
	public void tearDown() {
	}*/

	public void testContentAuthorityMatchesPackageName() {
		// I include this test case to catch move and rename refactorings
		// without corresponding updates of the content authority string
		String authority = MovieContract.CONTENT_AUTHORITY;
		assertTrue(getClass().getPackage().getName().startsWith(authority));
	}
}
