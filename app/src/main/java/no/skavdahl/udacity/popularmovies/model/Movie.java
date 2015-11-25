package no.skavdahl.udacity.popularmovies.model;

import java.util.Date;
import java.util.Random;

/**
 * Represents a movie.
 *
 * With the exception of ID and Title, one should not assume that any field is populated
 * with a meaningful value. These values will often come from an external server and there
 * are no guarantees that the record is in any way complete.
 *
 * @author fdavs
 */
public class Movie {
	private final int movieDbId;
	private final String title;
	private final String optPosterPath;
	private final String optSynopsis;
	private final double optPopularity;
	private final double optUserRating;
	private final Date optReleaseDate;
	private final int fallbackColor;

	/** The popularity score to use if none is provided by the movie database. */
	public static final double DEFAULT_POPULARITY = 0.0;

	/** The user rating score to use if none is provided by the movie database. */
	public static final double DEFAULT_USER_RATING = 0.0;

	public Movie(
		final int movieDbId,
		final Date releaseDate,
		final String title,
		final String posterPath,
		final String synopsis,
		final double popularity,
		final double userRating,
		final int fallbackColor) {

		if (title == null || title.trim().length() == 0)
			throw new IllegalArgumentException("required: title");

		this.movieDbId = movieDbId;
		this.title = title;
		this.optPosterPath = posterPath;
		this.optSynopsis = synopsis;
		this.optPopularity = popularity;
		this.optUserRating = userRating;
		this.optReleaseDate = releaseDate;
		this.fallbackColor = fallbackColor;
	}

	/** Returns the integer identifier for this movie at themoviedb.org */
	public int getMovieDbId() {
		return movieDbId;
	}

	/** Returns the title of the movie. The title is guaranteed to be not null. */
	public String getTitle() {
		return title;
	}

	/** Returns the path suffix to the movie poster. May be null. */
	public String getPosterPath() {
		return optPosterPath;
	}

	/** Returns a brief synopsis of the movie. May be null. */
	public String getSynopsis() {
		return optSynopsis;
	}

	/** Returns the movie's current popularity index. */
	public double getPopularity() {
		return optPopularity;
	}

	/** Returns the movie's current average rating. */
	public double getUserRating() {
		return optUserRating;
	}

	/** Returns the movie's release date. May be null. */
	public Date getReleaseDate() {
		return optReleaseDate;
	}

	/**
	 * Returns a random color code that can be used to represent the movie in the
	 * event that a true movie poster is absent.
 	 */
	public int getFallbackColorCode() { return fallbackColor; }
}
