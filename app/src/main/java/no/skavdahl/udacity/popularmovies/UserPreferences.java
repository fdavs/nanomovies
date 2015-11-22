package no.skavdahl.udacity.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import no.skavdahl.udacity.popularmovies.mdb.DiscoveryMode;

/**
 * @author fdavs
 */
public class UserPreferences {

	public static final String DISCOVERY_MODE = "mode";

	/** Saves the user preference for the DiscoveryMode setting. */
	public static void setDiscoverModePreference(final Activity activity, final DiscoveryMode mode) {
		SharedPreferences.Editor prefs = activity.getPreferences(Context.MODE_PRIVATE).edit();
		prefs.putInt(DISCOVERY_MODE, mode.getIdentityCode());
		prefs.apply();
	}

	/** Returns the current user preference for the DiscoveryMode setting. */
	public static DiscoveryMode getDiscoveryModePreference(final Activity activity) {
		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
		int modeId = prefs.getInt(DISCOVERY_MODE, DiscoveryMode.DEFAULT.getIdentityCode());
		return DiscoveryMode.fromIdentityCode(modeId);
	}
}
