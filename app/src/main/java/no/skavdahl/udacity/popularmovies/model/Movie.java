package no.skavdahl.udacity.popularmovies.model;

import java.util.Date;

/**
 * Represents a movie.
 *
 * @author fdavs
 */
public class Movie {
	private final int movieDbId;
	private final String title;
	private final String posterPath;
	private final String synopsis;
	private final double popularity;
	private final double userRating;
	private final Date releaseDate;

	public Movie(int movieDbId, Date releaseDate, String title, String posterPath, String synopsis, double popularity, double userRating) {
		this.movieDbId = movieDbId;
		this.title = title;
		this.posterPath = posterPath;
		this.synopsis = synopsis;
		this.popularity = popularity;
		this.userRating = userRating;
		this.releaseDate = releaseDate;
	}

	/** Returns the integer identifier for this movie at themoviedb.org */
	public int getMovieDbId() {
		return movieDbId;
	}

	/** Returns the title of the movie. */
	public String getTitle() {
		return title;
	}

	/** Returns the path suffix to the movie poster. */
	public String getPosterPath() {
		return posterPath;
	}

	/** Returns a brief synopsis of the movie. */
	public String getSynopsis() {
		return synopsis;
	}

	/** Returns the movie's current popularity index. */
	public double getPopularity() {
		return popularity;
	}

	/** Returns the movie's current average rating */
	public double getUserRating() {
		return userRating;
	}

	/** Rethrns the movie's release date. */
	public Date getReleaseDate() {
		return releaseDate;
	}
}
