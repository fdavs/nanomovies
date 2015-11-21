package no.skavdahl.udacity.popularmovies.mdb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * Parser for JSON data that is returned from a discover movies request to themoviedb.org.
 *
 * @author fdavs
 */
public class DiscoverMoviesJSONAdapter extends JSONAdapter {

	private final String JSON_DISCOVER_MOVIE_RESULTS = "results";
	private final String JSON_MOVIE_ID = "id";
	private final String JSON_MOVIE_TITLE = "title";
	private final String JSON_MOVIE_POSTER_PATH = "poster_path";
	private final String JSON_MOVIE_SYNOPSIS = "overview";
	private final String JSON_MOVIE_POPULARITY = "popularity";
	private final String JSON_MOVIE_USER_RATING = "vote_average";
	private final String JSON_MOVIE_RELEASE_DATE = "release_date";

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
			movies.add(toMovie(results.getJSONObject(i)));
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
		Date releaseDate = getDate(obj, JSON_MOVIE_RELEASE_DATE);
		String title = obj.getString(JSON_MOVIE_TITLE);
		String posterPath = obj.getString(JSON_MOVIE_POSTER_PATH);
		String synopsis = obj.getString(JSON_MOVIE_SYNOPSIS);
		double popularity = obj.getDouble(JSON_MOVIE_POPULARITY);
		double userRating = obj.getDouble(JSON_MOVIE_USER_RATING);

		return new Movie(id, releaseDate, title, posterPath, synopsis, popularity, userRating);
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
		putDate(obj, JSON_MOVIE_RELEASE_DATE, movie.getReleaseDate());
		obj.put(JSON_MOVIE_TITLE, movie.getTitle());
		obj.put(JSON_MOVIE_POSTER_PATH, movie.getPosterPath());
		obj.put(JSON_MOVIE_SYNOPSIS, movie.getSynopsis());
		obj.put(JSON_MOVIE_POPULARITY, movie.getPopularity());
		obj.put(JSON_MOVIE_USER_RATING, movie.getUserRating());
		return obj.toString();
	}
}
