package no.skavdahl.udacity.popularmovies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

import java.util.List;

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMoviesJSONAdapter;
import no.skavdahl.udacity.popularmovies.mdb.StandardMovieList;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * The main Movie Discovery fragment.
 *
 * @author fdavs
 */
public class MainDiscoveryActivityFragment extends Fragment {

    private final String LOG_TAG = getClass().getSimpleName();

	private GridView posterGrid;
	private MoviePosterAdapter viewAdapter;

	// It is recommended to keep a local reference to this ChangeListener to avoid
	// garbage collection -- see the Android API docs at http://goo.gl/0yFyTy
	// (SharedPreferences#registerOnSharedPreferenceChangeListener)
	private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;

	private final static String PREF_SCROLL_INDEX = "scroll.index";
	private final static String PREF_SCROLL_OFFSET = "scroll.offset";

	/**
	 * Local copy of saved instance state between onCreateView and the successful
	 * async loading of movie posters.
	 */
	private Bundle savedInstanceState;

	public MainDiscoveryActivityFragment() {
		Log.i(LOG_TAG, "MainDiscoveryActivityFragment created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
	    setHasOptionsMenu(true);

	    final View view = inflater.inflate(R.layout.fragment_main_discovery, container, false);

	    // Configure the grid display of movie posters
	    posterGrid = (GridView) view.findViewById(R.id.poster_grid);

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

	    // -- scroll to the previously saved position in the list
	    this.savedInstanceState = savedInstanceState;

	    return view;
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		saveGridScrollPosition(outState);
		super.onSaveInstanceState(outState);
	}

	private void saveGridScrollPosition(Bundle outState) {
		int index = posterGrid.getFirstVisiblePosition();

		int offset = 0;
		if (index < posterGrid.getChildCount()) { // it's possible that the list is empty
			final View first = posterGrid.getChildAt(index);
			if (null != first)
				offset = first.getTop();
		}

		Log.d(LOG_TAG, "GRID POSITION index=" + index + " offset=" + offset);

		outState.putInt(PREF_SCROLL_INDEX, index);
		outState.putInt(PREF_SCROLL_OFFSET, offset);
	}

	private void restoreGridScrollPosition(Bundle savedInstanceState) {
		if (savedInstanceState == null)
			return;

		int index = savedInstanceState.getInt(PREF_SCROLL_INDEX);
		posterGrid.setSelection(index + 1);

		int offset = savedInstanceState.getInt(PREF_SCROLL_OFFSET);
		posterGrid.scrollBy(0, -offset);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		if (prefChangeListener != null) {
			getActivity()
				.getPreferences(Context.MODE_PRIVATE)
				.unregisterOnSharedPreferenceChangeListener(prefChangeListener);
			prefChangeListener = null;
		}
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
				    if (!UserPreferences.MOVIE_LIST.equals(key))
					    return;

				    configureOptionsMenu(menu);
				    refreshMovies();
			    }
		    });
    }

	private void configureOptionsMenu(final Menu menu) {
		// ensure that the movie list radio buttons are checked appropriately
		// NOTE It is possible to store selected menu item id directly in the UserPreferences
		// avoiding the need to map between menu item ids and movie list names. However,
		// I'm not sure how stable a menu item id is. Will it survive an upgrade to a
		// newer version of the app? Saving the movie list name seems more stable and safer
		// (not to mention readable).
		StandardMovieList selectedMovieList = UserPreferences.getDiscoveryPreference(getActivity());
		int menuItem = mapMovieListToMenuItem(selectedMovieList);
		if (menuItem > 0)
			menu.findItem(menuItem).setChecked(true);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	    StandardMovieList movieList = mapMenuItemToMovieList(item.getItemId());
		if (movieList != null) {
			UserPreferences.setDiscoveryPreference(getActivity(), movieList);
			return true;
		}
		else return super.onOptionsItemSelected(item);
    }

	private int mapMovieListToMenuItem(StandardMovieList movieList) {
		switch (movieList) {
			case POPULAR:
				return R.id.action_popular_movies;
			case TOP_RATED:
				return R.id.action_top_rated_movies;
			case NOW_PLAYING:
				return R.id.action_new_movies;
			case UPCOMING:
				return R.id.action_upcoming_movies;
			default:
				return 0;
		}
	}

	private StandardMovieList mapMenuItemToMovieList(int menuItemId) {
		switch (menuItemId) {
			case R.id.action_popular_movies:
				return StandardMovieList.POPULAR;
			case R.id.action_top_rated_movies:
				return StandardMovieList.TOP_RATED;
			case R.id.action_new_movies:
				return StandardMovieList.NOW_PLAYING;
			case R.id.action_upcoming_movies:
				return StandardMovieList.UPCOMING;
			default:
				return null;
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

            // Inform the user that the operation is aborted due to missing permission
            // so it is clear why the screen doesn't update
	        showFailureDialog(R.string.no_permission_try_again);

            return;
        }

        // permission granted, go ahead with the operation
	    StandardMovieList movieList = UserPreferences.getDiscoveryPreference(getActivity());
	    String apiKey = getString(R.string.movie_api_key);
        DiscoverMoviesTask task = new DiscoverMoviesTask(movieList, apiKey, getActivity(),
	        new DiscoverMoviesTask.Listener() {
			    @Override
			    public void onDownloadSuccess(List<Movie> movies) {
				    viewAdapter.setMovies(movies);

				    // restore previously scroll position (if any)
				    if (savedInstanceState != null) {
					    restoreGridScrollPosition(savedInstanceState);
					    savedInstanceState = null;
				    }
			    }

			    @Override
			    public void onNetworkFailure() {
				    showFailureDialog(R.string.no_network_try_again);
			    }
		    }
        );
        task.execute();
    }

	/**
	 * Displays a dialog informing the user that the refreshMovies() operation failed
	 * with a critical error (network down or required permission denied). The user gets the
	 * choice between retrying the operation og closing the activity.
	 */
	private void showFailureDialog(int messageId) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
		alertDialogBuilder.setMessage(messageId);

		alertDialogBuilder.setPositiveButton(R.string.try_again, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				refreshMovies();
			}
		});
		alertDialogBuilder.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				getActivity().finish();
			}
		});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
}
