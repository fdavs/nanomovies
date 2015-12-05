package no.skavdahl.udacity.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import no.skavdahl.udacity.popularmovies.mdb.StandardMovieList;

/**
 * @author fdavs
 */
public class UserPreferences {

	public static final String MOVIE_LIST = "list";

	/** Saves the user preference for the movie list (discovery) setting. */
	public static void setDiscoveryPreference(final Activity activity, final StandardMovieList mode) {
		SharedPreferences.Editor prefs = activity.getPreferences(Context.MODE_PRIVATE).edit();
		prefs.putString(MOVIE_LIST, mode.getListName());
		prefs.apply();
	}

	/** Returns the current user preference for the movie list (discovery) setting. */
	public static StandardMovieList getDiscoveryPreference(final Activity activity) {
		SharedPreferences prefs = activity.getPreferences(Context.MODE_PRIVATE);
		String listName = prefs.getString(MOVIE_LIST, StandardMovieList.DEFAULT.getListName());
		return StandardMovieList.fromListName(listName);
	}
}
