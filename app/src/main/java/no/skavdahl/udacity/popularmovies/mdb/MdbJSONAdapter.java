package no.skavdahl.udacity.popularmovies.mdb;

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

	private static final String LOG_TAG = MdbJSONAdapter.class.getSimpleName();

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

	private static final String JSON_MOVIE_VIDEOS  = "videos";
	private static final String JSON_VIDEOS_KEY  = "key";
	private static final String JSON_VIDEOS_SITE  = "site";
	private static final String JSON_VIDEOS_NAME  = "name";

	private static final String JSON_MOVIE_REVIEWS  = "reviews";
	private static final String JSON_REVIEW_ID  = "id";
	private static final String JSON_REVIEW_AUTHOR  = "author";
	private static final String JSON_REVIEW_CONTENT  = "content";

	// --- class properties ---

	/**
	 * Parses a JSON-formatted string, which is expected to comply with the expected output
	 * from a movies discovery request to themoviedb.org.
	 *
	 * @param jsonString A JSON-formatted string
	 *
	 * @return a list of movies
	 */
	public static List<Movie> getMoviesList(String jsonString) throws JSONException {
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
	public static Movie toMovie(JSONObject obj) throws JSONException {
		int id = obj.getInt(JSON_MOVIE_ID);
		Date releaseDate = getOptDate(obj, JSON_MOVIE_RELEASE_DATE);
		String title = getOptString(obj, JSON_MOVIE_TITLE);
		String posterPath = getOptString(obj, JSON_MOVIE_POSTER_PATH);
		String backdropPath = getOptString(obj, JSON_MOVIE_BACKDROP_PATH);
		String synopsis = getOptString(obj, JSON_MOVIE_SYNOPSIS);
		double popularity = obj.optDouble(JSON_MOVIE_POPULARITY, Movie.DEFAULT_POPULARITY);
		double voteAverage = obj.optDouble(JSON_MOVIE_VOTE_AVERAGE, Movie.DEFAULT_VOTE_AVERAGE);
		int voteCount = obj.optInt(JSON_MOVIE_VOTE_COUNT, Movie.DEFAULT_VOTE_COUNT);

		boolean hasExtendedData = obj.has(JSON_MOVIE_REVIEWS);

		List<Video> videos = toVideoList(obj, JSON_MOVIE_VIDEOS);
		List<Review> reviews = toReviewList(obj, JSON_MOVIE_REVIEWS);

		return new Movie(id, releaseDate, title, posterPath, backdropPath, synopsis, popularity, voteAverage, voteCount, hasExtendedData, reviews, videos);
	}

	private static List<Video> toVideoList(JSONObject obj, String key) throws JSONException {
		JSONArray videoArray = obj.optJSONArray(key);
		if (videoArray != null)
			return toVideoList(videoArray);

		JSONObject videoContainer = obj.optJSONObject(key);
		if (videoContainer != null)
			return toVideoList(videoContainer);

		return Collections.emptyList();
	}

	/**
	 * Converts a JSON string to a list of video models.
	 *
	 * @param videoArrayStr A String that is a JSON array of video objects.
	 *
	 * @see <a href="http://docs.themoviedb.apiary.io/#reference/movies/movieidvideos/get">themoviedb reference</a>
	 */
	public static @NonNull List<Video> toVideoList(@Nullable String videoArrayStr) {
		try {
			if (videoArrayStr != null)
				return toVideoList(new JSONArray(videoArrayStr));
		}
		catch (JSONException e) {
			Log.w(LOG_TAG, "JSON error", e);
		}

		return Collections.emptyList();
	}

	private static List<Video> toVideoList(@NonNull JSONObject videoContainer) throws JSONException {
		return toVideoList(videoContainer.getJSONArray(JSON_RESULTS));
	}

	private static List<Video> toVideoList(@NonNull JSONArray videoArray) throws JSONException {
		int count = videoArray.length();
		List<Video> result = new ArrayList<>(count);

		for (int i = 0; i < count; ++i)
			result.add(toVideo(videoArray.getJSONObject(i)));

		return result;
	}

	private static Video toVideo(@NonNull JSONObject obj) throws JSONException {
		String key = obj.getString(JSON_VIDEOS_KEY);
		String site = obj.getString(JSON_VIDEOS_SITE);
		String name = obj.getString(JSON_VIDEOS_NAME);

		return new Video(key, site, name);
	}

	private static List<Review> toReviewList(JSONObject obj, String key) throws JSONException {
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
	 * @param reviewArrayStr A string that is a JSON array of reviews objects, as emitted by
	 *                       themoviedb.com service /movie/id/reviews
	 *
	 * @see <a href="http://docs.themoviedb.apiary.io/#reference/movies/movieidreviews/get">themoviedb reference</a>
	 */
	public static @NonNull List<Review> toReviewList(@Nullable String reviewArrayStr) {
		try {
			if (reviewArrayStr != null)
				return toReviewList(new JSONArray(reviewArrayStr));
		}
		catch (JSONException e) {
			Log.w(LOG_TAG, "JSON error", e);
		}

		return Collections.emptyList();
	}

	private static List<Review> toReviewList(@NonNull JSONObject reviewContainer) throws JSONException {
		return toReviewList(reviewContainer.getJSONArray(JSON_RESULTS));
	}

	private static List<Review> toReviewList(@NonNull JSONArray reviewArray) throws JSONException {
		int count = reviewArray.length();
		List<Review> result = new ArrayList<>(count);

		for (int i = 0; i < count; ++i)
			result.add(toReview(reviewArray.getJSONObject(i)));

		return result;
	}

	private static Review toReview(@NonNull JSONObject obj) throws JSONException {
		String id = obj.getString(JSON_REVIEW_ID);
		String author = obj.getString(JSON_REVIEW_AUTHOR);
		String content = obj.getString(JSON_REVIEW_CONTENT);

		return new Review(id, author, content);
	}

	public static String toReviewJSONString(List<Review> reviews) {
		try {
			JSONArray arr = new JSONArray();
			if (reviews != null) {
				for (Review review : reviews) {
					JSONObject obj = new JSONObject();
					obj.put(JSON_REVIEW_ID, review.getId());
					obj.put(JSON_REVIEW_AUTHOR, review.getAuthor());
					obj.put(JSON_REVIEW_CONTENT, review.getContent());
					arr.put(obj);
				}
			}
			return arr.toString();
		}
		catch (JSONException e) {
			Log.e(LOG_TAG, "JSON error", e);
			return null;
		}
	}

	public static String toVideoJSONString(List<Video> videos) {
		try {
			JSONArray arr = new JSONArray();
			if (videos != null) {
				for (Video video : videos) {
					JSONObject obj = new JSONObject();
					obj.put(JSON_VIDEOS_KEY, video.getKey());
					obj.put(JSON_VIDEOS_SITE, video.getSite());
					obj.put(JSON_VIDEOS_NAME, video.getName());
					arr.put(obj);
				}
			}
			return arr.toString();
		}
		catch (JSONException e) {
			Log.e(LOG_TAG, "JSON error", e);
			return null;
		}
	}
}
