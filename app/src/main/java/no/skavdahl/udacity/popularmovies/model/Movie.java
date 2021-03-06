package no.skavdahl.udacity.popularmovies.model;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
	private final String optBackdropPath;
	private final String optSynopsis;
	private final double optPopularity;
	private final double optVoteAverage;
	private final int optVoteCount;
	private final Date optReleaseDate;

	private final boolean hasExtendedData;
	private final List<Review> optReviews;
	private final List<Video> optVideos;

	/** The popularity score to use if none is provided by the movie database. */
	public static final double DEFAULT_POPULARITY = 0.0;

	/** The vote average score to use if none is provided by the movie database. */
	public static final double DEFAULT_VOTE_AVERAGE = 0.0;

	/** The vote count score to use if none is provided by the movie database. */
	public static final int DEFAULT_VOTE_COUNT = 0;

	public Movie(
		final int movieDbId,
		final Date releaseDate,
		final String title,
		final String posterPath,
		final String backdropPath,
		final String synopsis,
		final double popularity,
		final double voteAverage,
		final int voteCount,
		final boolean hasExtendedData,
		final List<Review> reviews,
		final List<Video> videos) {

		if (title == null || title.trim().length() == 0)
			throw new IllegalArgumentException("required: title");

		this.movieDbId = movieDbId;
		this.title = title;
		this.optPosterPath = posterPath;
		this.optBackdropPath = backdropPath;
		this.optSynopsis = synopsis;
		this.optPopularity = popularity;
		this.optVoteAverage = voteAverage;
		this.optVoteCount = voteCount;
		this.optReleaseDate = releaseDate;
		this.hasExtendedData = hasExtendedData;

		if (reviews != null && !reviews.isEmpty())
			this.optReviews = Collections.unmodifiableList(reviews);
		else
			this.optReviews = Collections.emptyList();

		if (videos != null && !videos.isEmpty())
			this.optVideos = Collections.unmodifiableList(videos);
		else
			this.optVideos = Collections.emptyList();
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

	/** Returns the path suffix to a movie backdrop image. May be null. */
	public String getBackdropPath() {
		return optBackdropPath;
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
	public double getVoteAverage() {
		return optVoteAverage;
	}

	/** Returns the number of votes used to calculate its current rating. */
	public int getVoteCount() {
		return optVoteCount;
	}

	/** Returns the movie's release date. May be null. */
	public Date getReleaseDate() {
		return optReleaseDate;
	}

	/**
	 * Returns true if this movie contains extended data. This is defined as additional data
	 * that is available when the movie is looked up individually but not included when the
	 * movie is merely discovered as an element in a list.
	 */
	public boolean hasExtendedData() {
		return hasExtendedData;
	}

	/** Returns the set of reviews for this movie. May be an empty list. */
	public List<Review> getReviews() {
		return optReviews;
	}

	/** Returns the set of videos related to this movie. May be an empty list. */
	public List<Video> getVideos() {
		return optVideos;
	}

	/**
	 * Returns the (possibly empty) list of movies for a particular site.
	 *
	 * Equivalent to <code>videos.filter(v => v.site == targetSite)</code> in Scala.
	 */
	public static List<Video> filterVideosBySite(final List<Video> videoList, final String site) {
		if (TextUtils.isEmpty(site) || videoList == null)
			return Collections.emptyList();

		List<Video> result = new ArrayList<>();
		for (Video v : videoList) {
			if (site.equals(v.getSite()))
				result.add(v);
		}

		return result;
	}
}
