package no.skavdahl.udacity.popularmovies.mdb;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import no.skavdahl.udacity.popularmovies.R;
import no.skavdahl.udacity.popularmovies.model.Movie;
import no.skavdahl.udacity.utils.JSONAdapter;

/**
 * Parser for JSON data that is returned from a discover movies request to themoviedb.org.
 *
 * @author fdavs
 */
public class DiscoverMoviesJSONAdapter extends JSONAdapter {

	private final String LOG_TAG = getClass().getSimpleName();

	// --- attribute names in JSON responses from the server ---

	@SuppressWarnings("FieldCanBeLocal")
	private static final String JSON_DISCOVER_MOVIE_RESULTS = "results";

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

	// --- class properties ---

	private final Resources resources;
	private final Random rnd;

	/**
	 * Initialize a movies JSON adapter.
	 *
	 * @param resources Access to the color resources for default movie posters. May be
	 *                  <code>null</code> in which case random colors will be used instead.
	 */
	public DiscoverMoviesJSONAdapter(final Resources resources) {
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
		JSONArray results = obj.getJSONArray(JSON_DISCOVER_MOVIE_RESULTS);
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

		return new Movie(id, releaseDate, title, posterPath, backdropPath, synopsis, popularity, voteAverage, voteCount, genreList, fallbackColor);
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

		return obj.toString();
	}
}
