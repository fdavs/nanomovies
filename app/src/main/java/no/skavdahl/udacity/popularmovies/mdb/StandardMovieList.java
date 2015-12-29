package no.skavdahl.udacity.popularmovies.mdb;

/**
 * The set of standard movie lists at themoviedb.org. Currently supported lists are:
 * <ul>
 *     <li>{@link #POPULAR} (default)</li>
 *     <li>{@link #NOW_PLAYING}</li>
 *     <li>{@link #TOP_RATED}</li>
 *     <li>{@link #UPCOMING}</li>
 *     <li>{@link #FAVORITE}</li>
 * </ul>
 */
public class StandardMovieList {

	/** The list of movies playing that have been, or are being released this week. */
	public static final String NOW_PLAYING = "now_playing";

	/** The list of popular movies on The Movie Database. */
	public static final String POPULAR = "popular";

	/** The list of top rated movies, only including movies that have 50 or more votes. */
	public static final String TOP_RATED = "top_rated";

	/** The list of upcoming movies by release date. */
	public static final String UPCOMING = "upcoming";

	/** The list of movies marked by the user as favorites. */
	public static final String FAVORITE = "favorite";

	/** The default list ({@link #POPULAR}). */
	public static final String DEFAULT = POPULAR;
}
