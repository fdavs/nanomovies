package no.skavdahl.udacity.popularmovies.mdb;

/**
 * The set of standard movie lists at themoviedb.org. Currently supported lists are:
 * <ul>
 *     <li>{@link #POPULAR} (default)</li>
 *     <li>{@link #NOW_PLAYING}</li>
 *     <li>{@link #TOP_RATED}</li>
 *     <li>{@link #UPCOMING}</li>
 * </ul>
 */
public enum StandardMovieList {

	/** The list of movies playing that have been, or are being released this week. */
	NOW_PLAYING("now_playing"),

	/** The list of popular movies on The Movie Database. */
	POPULAR("popular"),

	/** The list of top rated movies, only including movies that have 50 or more votes. */
	TOP_RATED("top_rated"),

	/** The list of upcoming movies by release date. */
	UPCOMING("upcoming");

	/** The default movie list. */
	public static StandardMovieList DEFAULT = POPULAR;

	/** List name, such as it appears in themoviedb.org API. */
	private final String listName;

	StandardMovieList(final String listName) {
		this.listName = listName;
	}

	public String getListName() {
		return listName;
	}

	public static StandardMovieList fromListName(final String listName) {
		if (listName != null) {
			for (StandardMovieList list : StandardMovieList.values()) {
				if (listName.equals(list.getListName()))
					return list;
			}
		}
		return DEFAULT;
	}
}
