package no.skavdahl.udacity.popularmovies.data;

import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * Tests features of the MovieProvider class. This class is based on the similar
 * test case in the Sunshine project.
 *
 * @author fdavs
 */
public class MovieProviderTest extends AndroidTestCase {

	/** Verifies that the Movie content provider is registered correctly. */
	public void testProviderRegistration() {
		// verify that the provider is registered
		ProviderInfo providerInfo = null;
		try {
			PackageManager pm = mContext.getPackageManager();
			ComponentName componentName = new ComponentName(
				mContext.getPackageName(),
				MovieProvider.class.getName());
			providerInfo = pm.getProviderInfo(componentName, 0);
		}
		catch (PackageManager.NameNotFoundException e) {
			fail("Content provider is not registered (" + e.getMessage() + ")");
		}

		// verify that the registered provider's authority matches
		// the authority specified in the Contract
		assertEquals(
			"Content provider registered with incorrect authority",
			providerInfo.authority, PopularMoviesContract.CONTENT_AUTHORITY);
	}

	/** Verifies that all URI match constants are unique */
	public void testUriMatchConstantsAreUnique() {
		Set<Integer> s = new HashSet<>();
		s.add(MovieProvider.LIST_DIRECTORY);
		s.add(MovieProvider.LIST_ITEM);
		s.add(MovieProvider.LIST_MEMBER_DIRECTORY);
		s.add(MovieProvider.LIST_MEMBER_ITEM);
		s.add(MovieProvider.MOVIE_DIRECTORY);
		s.add(MovieProvider.MOVIE_ITEM);

		assertEquals(6, s.size());
	}

}
