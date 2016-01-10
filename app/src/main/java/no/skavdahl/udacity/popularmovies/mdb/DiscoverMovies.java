package no.skavdahl.udacity.popularmovies.mdb;

import java.io.IOException;

import no.skavdahl.udacity.utils.WebApiClient;

/**
 * Encapsulates /discover/movie network queries performed against themoviedb.org.
 *
 * @author fdavs
 */
public class DiscoverMovies extends WebApiClient {

	/**
	 * Submits a movie discovery request to themoviedb.org. The query is performed
	 * synchronously. The response is returned as a raw JSON string.
	 *
	 * @param apiKey The API key necessary to perform a query at themoviedb.org
	 * @param listName Which movie list to query. This must be one of the standard movie
	 *                 list names.
	 * @param page Desired page number (starting with 1) of the response
	 *
	 * @return the JSON string response
	 *
	 * @throws IOException if the query failed or returned a non-sensible result
	 */
	public String discoverStandardMovies(String apiKey, String listName, int page) throws IOException {
		String endpoint = Request.getStandardMovieListURL(apiKey, listName, page);
		return executeQuery(endpoint);
	}

	/**
	 * Submits a movie details request to themoviedb.org. The query is performed
	 * synchronously. The response is returned as a raw JSON string.
	 *
	 * @param apiKey The API key necessary to perform a query at themoviedb.org
	 * @param movieId Which movie to query.
	 *
	 * @return the JSON string response
	 *
	 * @throws IOException if the query failed or returned a non-sensible result
	 */
	public String getMovieDetails(String apiKey, int movieId) throws IOException {
		String endpoint = Request.getMovieURL(apiKey, movieId);
		return executeQuery(endpoint);
	}
}
