package no.skavdahl.udacity.popularmovies.data;

import android.test.AndroidTestCase;

/**
 * Tests features of the MovieContracts class.
 *
 * @author fdavs
 */
public class MovieContractsTest extends AndroidTestCase {

	/**
	 * Verify that the content authority matches the package name. I include this
	 * test case to protect against move and rename refactorings without corresponding
	 * updates to the content authority string.
	 */
	public void testContentAuthorityMatchesPackageName() {
		String authority = MovieContracts.CONTENT_AUTHORITY;
		assertTrue(getClass().getPackage().getName().startsWith(authority));
	}
}
