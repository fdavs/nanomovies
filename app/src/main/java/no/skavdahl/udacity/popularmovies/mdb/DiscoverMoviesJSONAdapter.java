package no.skavdahl.udacity.popularmovies.mdb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * Parser for JSON data that is returned from a discover movies request to themoviedb.org.
 *
 * @author fdavs
 */
public class DiscoverMoviesJSONAdapter {

	private final String JSON_DISCOVER_MOVIE_RESULTS = "results";
	private final String JSON_MOVIE_ID = "id";
	private final String JSON_MOVIE_TITLE = "title";
	private final String JSON_MOVIE_POSTER_PATH = "poster_path";
	private final String JSON_MOVIE_SYNOPSIS = "overview";
	private final String JSON_MOVIE_POPULARITY = "popularity";

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

	public Movie toMovie(JSONObject obj) throws JSONException {
		int id = obj.getInt(JSON_MOVIE_ID);
		String title = obj.getString(JSON_MOVIE_TITLE);
		String posterPath = obj.getString(JSON_MOVIE_POSTER_PATH);
		String synopsis = obj.getString(JSON_MOVIE_SYNOPSIS);
		double popularity = obj.getDouble(JSON_MOVIE_POPULARITY);

		return new Movie(id, title, posterPath, synopsis, popularity);
	}
}
