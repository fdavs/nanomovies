package no.skavdahl.udacity.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import no.skavdahl.udacity.popularmovies.mdb.StandardMovieList;

/**
 * Simple facade to the user preferences stored in SharedPreferences.
 *
 * @author fdavs
 */
public class UserPreferences {

	public static final String MOVIE_LIST = "list";
	public static final String SWEEP_TIME = "sweepTime";

	/** Saves the user preference for the movie list setting. */
	public static void setMovieList(final Activity activity, final String listName) {
		SharedPreferences.Editor prefs = activity.getPreferences(Context.MODE_PRIVATE).edit();
		prefs.putString(MOVIE_LIST, listName);
		prefs.apply();
	}

	/** Returns the current user preference for the movie list (discovery) setting. */
	public static String getMovieList(final Activity activity) {
		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
		if (prefs == null)
			return StandardMovieList.DEFAULT;

		return prefs.getString(MOVIE_LIST, StandardMovieList.DEFAULT);
	}

	/** Saves the time of the last database cleanup. */
	public static void setSweepTime(final Activity activity, final long time) {
		SharedPreferences.Editor prefs = activity.getPreferences(Context.MODE_PRIVATE).edit();
		prefs.putLong(SWEEP_TIME, time);
		prefs.apply();
	}

	/** Returns the time of the last database cleanup. */
	public static long getSweepTime(final Activity activity) {
		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
		if (prefs == null)
			return 0;

		return prefs.getLong(SWEEP_TIME, 0);
	}
}
