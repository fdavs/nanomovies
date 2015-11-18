package no.skavdahl.udacity.popularmovies.model;

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

	public Movie(int movieDbId, String title, String posterPath, String synopsis, double popularity) {
		this.movieDbId = movieDbId;
		this.title = title;
		this.posterPath = posterPath;
		this.synopsis = synopsis;
		this.popularity = popularity;
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
}
