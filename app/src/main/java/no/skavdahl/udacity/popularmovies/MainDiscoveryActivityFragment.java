package no.skavdahl.udacity.popularmovies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONException;

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;
import no.skavdahl.udacity.popularmovies.mdb.DiscoveryMode;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * The main Movie Discovery fragment.
 *
 * @author fdavs
 */
public class MainDiscoveryActivityFragment extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();

	private MoviePosterAdapter viewAdapter;

	// It is recommended to keep a local reference to this ChangeListener to avoid
	// garbage collection -- see the Android API docs at http://goo.gl/0yFyTy
	// (SharedPreferences#registerOnSharedPreferenceChangeListener)
	@SuppressWarnings({"FieldCanBeLocal", "unused"})
	private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;

	public MainDiscoveryActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
	    setHasOptionsMenu(true);

	    final View view = inflater.inflate(R.layout.fragment_main_discovery, container, false);

	    // Configure the grid display of movie posters
	    GridView posterGrid = (GridView) view.findViewById(R.id.poster_grid);

	    // -- how many posters to display on each row
	    int posterViewWidth = calculateOptimalColumnWidth();
	    posterGrid.setColumnWidth(posterViewWidth);

	    // -- how to display movie posters
	    posterGrid.setAdapter(viewAdapter = new MoviePosterAdapter(getContext(), posterViewWidth));

	    // -- what should happen when a movie poster is clicked
		posterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Movie selectedMovie = (Movie) viewAdapter.getItem(position);
				openMovieDetailsActivity(selectedMovie);
			}
		});

	    return view;
    }

	/**
	 * Calculates the optimal column width based on the width of the display and an ideal
	 * poster width of approximately one inch.
	 *
	 * @return tne optimal column width in pixels.
	 */
	private int calculateOptimalColumnWidth() {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		double widthInches = ((double)dm.widthPixels) / dm.xdpi;
		int numCols = (int) Math.round(widthInches);

		return dm.widthPixels / numCols;
	}

	/**
	 * Starts the Movie Details activity for the given movie.
	 *
	 * @param movie The movie for which to show details.
	 */
	private void openMovieDetailsActivity(final Movie movie) {
		String movieData;
		try {
			DiscoverMoviesJSONAdapter adapter = new DiscoverMoviesJSONAdapter(null);
			movieData = adapter.toJSONString(movie);
		}
		catch (JSONException e) {
			// this error should never occur so this is mostly verifying an assertion
			Log.e(LOG_TAG, "Unable to convert data to JSON", e);
			return; // abort
		}

		Intent openMovieDetailsIntent = new Intent(getContext(), MovieDetailActivity.class);
		openMovieDetailsIntent.putExtra("movie", movieData);

		Activity contextActivity = MainDiscoveryActivityFragment.this.getActivity();
		if (openMovieDetailsIntent.resolveActivity(contextActivity.getPackageManager()) != null)
			startActivity(openMovieDetailsIntent);
	}

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_discovery, menu);

	    configureOptionsMenu(menu);

	    getActivity().getPreferences(Context.MODE_PRIVATE).registerOnSharedPreferenceChangeListener(
		    prefChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			    @Override
			    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					if (!UserPreferences.DISCOVERY_MODE.equals(key))
						return;

				    configureOptionsMenu(menu);
				    refreshMovies();
			    }
		    });
    }

	private void configureOptionsMenu(final Menu menu) {
		// ensure that the discovery mode menu options are checked appropriately
		DiscoveryMode discoveryMode = UserPreferences.getDiscoveryModePreference(getActivity());

		menu.findItem(R.id.action_popular_movies).setChecked(discoveryMode == DiscoveryMode.POPULAR_MOVIES);
		menu.findItem(R.id.action_high_rated_movies).setChecked(discoveryMode == DiscoveryMode.HIGH_RATED_MOVIES);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

	    switch (id) {
		    case R.id.action_popular_movies:
			    UserPreferences.setDiscoverModePreference(getActivity(), DiscoveryMode.POPULAR_MOVIES);
			    return true;
		    case R.id.action_high_rated_movies:
			    UserPreferences.setDiscoverModePreference(getActivity(), DiscoveryMode.HIGH_RATED_MOVIES);
			    return true;
		    case R.id.action_refresh:
                refreshMovies();
                return true;
		    default:
			    return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onStart() {
		super.onStart();
		refreshMovies();
	}

	/**
     * Issues or re-issues the current query to themoviedb.org and updates the display with the
     * new results from the query.
     */
    private void refreshMovies() {
        // verify that we have the permission to perform this operation
        int permissionCheck = ContextCompat.checkSelfPermission(
            this.getActivity(),
            Manifest.permission.INTERNET);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.w(LOG_TAG, "Operation canceled: permission denied by user: " + Manifest.permission.INTERNET);
            // TODO We should inform the user that the operation is aborted due to missing permission
            //      hence it is clear why the screen doesn't update
            return;
        }

        // permission granted, go ahead with the operation
	    DiscoveryMode mode = UserPreferences.getDiscoveryModePreference(getActivity());
	    String apiKey = getString(R.string.movie_api_key);
        DiscoverMoviesTask task = new DiscoverMoviesTask(mode, apiKey, getResources(), viewAdapter);
        task.execute();
    }
}
