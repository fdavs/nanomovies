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
	 * @param list Which movie list to query.
	 *
	 * @return the JSON string response
	 *
	 * @throws IOException if the query failed or returned a non-sensible result
	 */
	public String discoverMovies(String apiKey, StandardMovieList list) throws IOException {
		String endpoint = Request.getStandardMovieListURL(apiKey, list.getListName(), 1);
		return executeQuery(endpoint);
	}
}
