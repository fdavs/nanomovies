package no.skavdahl.udacity.popularmovies.mdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.json.JSONException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * Unit tests for the DiscoverMoviesJSONAdapter class.
 *
 * @author fdavs
 */
public class DiscoverMoviesJSONAdapterTest {

	private String discoveryResultJSON;

	@Before
	public void setup() throws IOException {
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream("/discover_movie.json")));

			StringBuilder buf = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				buf.append(line).append('\n');
			}

			discoveryResultJSON = buf.toString();
		}
		finally {
			if (reader != null) {
				try {
					reader.close();
				}
				catch (IOException e) {
					// ignore
				}
			}
		}
	}

	@After
	public void cleanup() {
		discoveryResultJSON = null;
	}


	private List<Movie> fromJSON(String json) throws JSONException {
		DiscoverMoviesJSONAdapter adapter = new DiscoverMoviesJSONAdapter();
		return adapter.getMoviesList(json);
	}

	/** Verifies that one movie is correctly parsed from the JSON string. */
	@Test
	public void testCorrectMovieAttributes() throws JSONException {
		List<Movie> movies = fromJSON(discoveryResultJSON);
		Movie movie = movies.get(0);

		assertThat(movie.getMovieDbId()).isEqualTo(206647);
		assertThat(movie.getTitle()).isEqualTo("Spectre");
		assertThat(movie.getPosterPath()).isEqualTo("/1n9D32o30XOHMdMWuIT4AaA5ruI.jpg");
		assertThat(movie.getSynopsis())
			.startsWith("A cryptic message")
			.endsWith("truth behind SPECTRE.");
		assertThat(movie.getPopularity()).isCloseTo(54.146108, within(0.000001));
	}

	/** Verifies that a collection of movies are correctly parsed from the JSON string. */
	@Test
	public void testCorrectNumberOfMoviesInResult() throws JSONException {
		DiscoverMoviesJSONAdapter adapter = new DiscoverMoviesJSONAdapter();
		List<Movie> movies = adapter.getMoviesList(discoveryResultJSON);

		assertThat(movies).hasSize(20);
		assertThat(movies.get(0).getMovieDbId()).isEqualTo(206647);
		assertThat(movies.get(movies.size() - 1).getMovieDbId()).isEqualTo(274854);
	}

}
