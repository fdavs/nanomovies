package no.skavdahl.udacity.popularmovies;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Packages all unit tests.
 *
 * @author fdavs
 */
public class FullTestSuite extends TestSuite {
	public static Test suite() {
		return new TestSuiteBuilder(FullTestSuite.class)
			.includeAllPackagesUnderHere().build();
	}

	public FullTestSuite() {
		super();
	}
}
