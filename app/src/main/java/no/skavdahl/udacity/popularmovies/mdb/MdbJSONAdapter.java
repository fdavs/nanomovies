package no.skavdahl.udacity.popularmovies.mdb;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import no.skavdahl.udacity.popularmovies.R;
import no.skavdahl.udacity.popularmovies.model.Movie;
import no.skavdahl.udacity.popularmovies.model.Review;
import no.skavdahl.udacity.popularmovies.model.Video;
import no.skavdahl.udacity.utils.JSONAdapter;

/**
 * Parser for JSON data that is returned from a discover movies request to themoviedb.org.
 *
 * @author fdavs
 */
public class MdbJSONAdapter extends JSONAdapter {

	private final String LOG_TAG = getClass().getSimpleName();

	// --- attribute names in JSON responses from the server ---

	@SuppressWarnings("FieldCanBeLocal")
	private static final String JSON_RESULTS = "results";

	private static final String JSON_MOVIE_ID = "id";
	private static final String JSON_MOVIE_TITLE = "title";
	private static final String JSON_MOVIE_POSTER_PATH = "poster_path";
	private static final String JSON_MOVIE_BACKDROP_PATH = "backdrop_path";
	private static final String JSON_MOVIE_SYNOPSIS = "overview";
	private static final String JSON_MOVIE_POPULARITY = "popularity";
	private static final String JSON_MOVIE_VOTE_AVERAGE = "vote_average";
	private static final String JSON_MOVIE_VOTE_COUNT = "vote_count";
	private static final String JSON_MOVIE_RELEASE_DATE = "release_date";
	private static final String JSON_MOVIE_GENRES = "genre_ids";

	private static final String JSON_MOVIE_VIDEOS  = "videos";
	private static final String JSON_VIDEOS_KEY  = "key";
	private static final String JSON_VIDEOS_SITE  = "site";
	private static final String JSON_VIDEOS_NAME  = "name";

	private static final String JSON_MOVIE_REVIEWS  = "reviews";
	private static final String JSON_REVIEW_ID  = "id";
	private static final String JSON_REVIEW_AUTHOR  = "author";
	private static final String JSON_REVIEW_CONTENT  = "content";

	private static final String JSON_EXTENDED_DATA = "extended_data";

	// --- class properties ---

	private final Resources resources;
	private final Random rnd;

	/**
	 * Initialize a movies JSON adapter.
	 *
	 * @param resources Access to the color resources for default movie posters. May be
	 *                  <code>null</code> in which case random colors will be used instead.
	 */
	public MdbJSONAdapter(final @Nullable Resources resources) {
		this.resources = resources;
		rnd = new Random();
	}

	/**
	 * Parses a JSON-formatted string, which is expected to comply with the expected output
	 * from a movies discovery request to themoviedb.org.
	 *
	 * @param jsonString A JSON-formatted string
	 *
	 * @return a list of movies
	 */
	public List<Movie> getMoviesList(String jsonString) throws JSONException {
		JSONObject obj = new JSONObject(jsonString);
		JSONArray results = obj.getJSONArray(JSON_RESULTS);
		int numMovies = results.length();

		List<Movie> movies = new ArrayList<>(numMovies);
		for (int i = 0; i < numMovies; ++i) {
			JSONObject movieJson = null;
			try {
				movieJson = results.getJSONObject(i);
				Movie movie = toMovie(movieJson);

				if (movie.getTitle() != null) // require a title
					movies.add(movie);
			}
			catch (JSONException e) {
				// log the error (for diagnostics) but otherwise just continue without
				// this particular movie in the result collection
				Log.w(LOG_TAG, "Error parsing JSON response for movie: " + movieJson, e);
			}
		}
		return movies;
	}

	/**
	 * Converts a JSON object to a Movie model.
	 *
	 * @param obj a JSON object which must have all the attributes expected for a Movie.
	 *
	 * @return a movie object with the same values as the JSON object.
	 *
	 * @throws JSONException if unable to convert
	 */
	public Movie toMovie(JSONObject obj) throws JSONException {
		int id = obj.getInt(JSON_MOVIE_ID);
		Date releaseDate = getOptDate(obj, JSON_MOVIE_RELEASE_DATE);
		String title = getOptString(obj, JSON_MOVIE_TITLE);
		String posterPath = getOptString(obj, JSON_MOVIE_POSTER_PATH);
		String backdropPath = getOptString(obj, JSON_MOVIE_BACKDROP_PATH);
		String synopsis = getOptString(obj, JSON_MOVIE_SYNOPSIS);
		double popularity = obj.optDouble(JSON_MOVIE_POPULARITY, Movie.DEFAULT_POPULARITY);
		double voteAverage = obj.optDouble(JSON_MOVIE_VOTE_AVERAGE, Movie.DEFAULT_VOTE_AVERAGE);
		int voteCount = obj.optInt(JSON_MOVIE_VOTE_COUNT, Movie.DEFAULT_VOTE_COUNT);
		List<Integer> genreList = getOptIntArray(obj.optJSONArray(JSON_MOVIE_GENRES));
		int fallbackColor = generateColorCode(title);

		boolean hasExtendedData = obj.has(JSON_MOVIE_REVIEWS);

		List<Video> videos = toVideoList(obj, JSON_MOVIE_VIDEOS);
		List<Review> reviews = toReviewList(obj, JSON_MOVIE_REVIEWS);

		return new Movie(id, releaseDate, title, posterPath, backdropPath, synopsis, popularity, voteAverage, voteCount, genreList, hasExtendedData, reviews, videos, fallbackColor);
	}

	private List<Video> toVideoList(JSONObject obj, String key) throws JSONException {
		JSONArray videoArray = obj.optJSONArray(key);
		if (videoArray != null)
			return toVideoList(videoArray);

		JSONObject videoContainer = obj.optJSONObject(key);
		if (videoContainer != null)
			return toVideoList(videoContainer);

		return Collections.emptyList();
	}

	/**
	 * Converts a JSON object to a list of video models.
	 *
	 * @param videoContainer A JSON object that adheres to the syntax emitted by themoviedb.com
	 *                       service /movie/id/videos
	 *
	 * @link http://docs.themoviedb.apiary.io/#reference/movies/movieidvideos/get
	 */
	private List<Video> toVideoList(@NonNull JSONObject videoContainer) throws JSONException {
		return toVideoList(videoContainer.getJSONArray(JSON_RESULTS));
	}

	/**
	 * Converts a JSON object to a list of video models.
	 *
	 * @param videoArray An array of JSON objects that adheres to the syntax emitted
	 *                   by themoviedb.com service /movie/id/videos
	 *
	 * @link http://docs.themoviedb.apiary.io/#reference/movies/movieidvideos/get
	 */
	private List<Video> toVideoList(@NonNull JSONArray videoArray) throws JSONException {
		int count = videoArray.length();
		List<Video> result = new ArrayList<>(count);

		for (int i = 0; i < count; ++i)
			result.add(toVideo(videoArray.getJSONObject(i)));

		return result;
	}

	private Video toVideo(@NonNull JSONObject obj) throws JSONException {
		String key = obj.getString(JSON_VIDEOS_KEY);
		String site = obj.getString(JSON_VIDEOS_SITE);
		String name = obj.getString(JSON_VIDEOS_NAME);

		return new Video(key, site, name);
	}

	private List<Review> toReviewList(JSONObject obj, String key) throws JSONException {
		JSONArray reviewArray = obj.optJSONArray(key);
		if (reviewArray != null)
			return toReviewList(reviewArray);

		JSONObject reviewContainer = obj.optJSONObject(key);
		if (reviewContainer != null)
			return toReviewList(reviewContainer);

		return Collections.emptyList();
	}

	/**
	 * Converts a JSON object to a list of video reviews.
	 *
	 * @param reviewContainer A JSON object that adheres to the syntax emitted by themoviedb.com
	 *                        service /movie/id/reviews
	 *
	 * @link http://docs.themoviedb.apiary.io/#reference/movies/movieidreviews/get
	 */
	private List<Review> toReviewList(@NonNull JSONObject reviewContainer) throws JSONException {
		return toReviewList(reviewContainer.getJSONArray(JSON_RESULTS));
	}

	/**
	 * Converts a JSON object to a list of video models.
	 *
	 * @param reviewArray An array of JSON objects that adheres to the syntax emitted
	 *                    by themoviedb.com service /movie/id/reviews
	 *
	 * @link http://docs.themoviedb.apiary.io/#reference/movies/movieidreviews/get
	 */
	private List<Review> toReviewList(@NonNull JSONArray reviewArray) throws JSONException {
		int count = reviewArray.length();
		List<Review> result = new ArrayList<>(count);

		for (int i = 0; i < count; ++i)
			result.add(toReview(reviewArray.getJSONObject(i)));

		return result;
	}

	private Review toReview(@NonNull JSONObject obj) throws JSONException {
		String id = obj.getString(JSON_REVIEW_ID);
		String author = obj.getString(JSON_REVIEW_AUTHOR);
		String content = obj.getString(JSON_REVIEW_CONTENT);

		return new Review(id, author, content);
	}

	/**
	 * Generates a color code associated with the given movie title. The title is used
	 * as a base for the calculation in order to generate a reproducible result (the same
	 * color is generated every time). The generated number will be in the range 0xFF000000
	 * through 0xFFFFFFFF and should be interpreted as four bytes representing the alpha,
	 * R, G and B components respectively.
	 *
	 * @param title The movie title from which to start the calculation.
	 *
	 * @return a 4-byte value that can be interpreted as an ARGB color.
	 */
	private int generateColorCode(String title) {
		// if we have access to the color resources, choose a color from Android's
		// Material Design palette.
		if (resources != null) {
			int groupIndex = R.array.mdcolor_500;
			TypedArray group = resources.obtainTypedArray(groupIndex);

			try {
				int index = Math.abs(title.hashCode() % group.length());
				return group.getColor(index, Color.WHITE);
			}
			finally {
				group.recycle();
			}
		}

		// otherwise -- primarily in unit tests in which 'resources' are absent --
		// just choose a random color
		else return 0xFF000000 | rnd.nextInt(0x01000000);
	}


	/**
	 * Converts a Movie model object to a JSON-formatted string.
	 *
	 * @param movie the Movie object to convert to JSON. <code>null</code> not allowed.
	 *
	 * @return a JSON string containing the attributes for movies.
	 *
	 * @throws JSONException if unable to convert
	 */
	public String toJSONString(Movie movie) throws JSONException {
		JSONObject obj = new JSONObject();

		// put a marker (here in front) that allows us to quickly decide whether this movie
		// contains extended movie data, such as reviews and video, which are not included
		// in a basic discovery or list request
		if (movie.hasExtendedData())
			obj.put(JSON_EXTENDED_DATA, true);

		obj.put(JSON_MOVIE_ID, movie.getMovieDbId());
		obj.put(JSON_MOVIE_TITLE, movie.getTitle());
		obj.put(JSON_MOVIE_POSTER_PATH, movie.getPosterPath());
		obj.put(JSON_MOVIE_BACKDROP_PATH, movie.getBackdropPath());
		obj.put(JSON_MOVIE_SYNOPSIS, movie.getSynopsis());
		obj.put(JSON_MOVIE_POPULARITY, movie.getPopularity());
		obj.put(JSON_MOVIE_VOTE_AVERAGE, movie.getVoteAverage());
		obj.put(JSON_MOVIE_VOTE_COUNT, movie.getVoteCount());
		putOptDate(obj, JSON_MOVIE_RELEASE_DATE, movie.getReleaseDate());
		putOptArray(obj, JSON_MOVIE_GENRES, movie.getGenres());

		if (movie.hasExtendedData()) {
			obj.put(JSON_MOVIE_REVIEWS, toReviewJSONArray(movie.getReviews()));
			obj.put(JSON_MOVIE_VIDEOS, toVideoJSONArray(movie.getVideos()));
		}

		return obj.toString();
	}

	private JSONArray toReviewJSONArray(List<Review> reviews) throws JSONException {
		JSONArray arr = new JSONArray();
		for (Review review : reviews) {
			JSONObject obj = new JSONObject();
			obj.put(JSON_REVIEW_ID, review.getId());
			obj.put(JSON_REVIEW_AUTHOR, review.getAuthor());
			obj.put(JSON_REVIEW_CONTENT, review.getContent());
			arr.put(obj);
		}
		return arr;
	}

	private JSONArray toVideoJSONArray(List<Video> videos) throws JSONException {
		JSONArray arr = new JSONArray();
		for (Video video : videos) {
			JSONObject obj = new JSONObject();
			obj.put(JSON_VIDEOS_KEY, video.getKey());
			obj.put(JSON_VIDEOS_SITE, video.getSite());
			obj.put(JSON_VIDEOS_NAME, video.getName());
			arr.put(obj);
		}
		return arr;
	}

	/**
	 * Returns true if the given json string contains extended movie data.
	 * For a definition of "extended", see {@link Movie#hasExtendedData()}.
	 */
	public static boolean containsExtendedData(String json) {
		// Note that without parsing the json and reading the actual attribute names,
		// the test as implemented below can theoretically match an attribute *value* and
		// return an incorrect result. It is unlikely, though, and much faster than parsing.
		return json.contains(JSON_EXTENDED_DATA);
	}
}
