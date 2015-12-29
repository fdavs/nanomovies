package no.skavdahl.udacity.popularmovies;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import no.skavdahl.udacity.popularmovies.data.PopularMoviesContract;
import no.skavdahl.udacity.popularmovies.data.UpdateMovieListTask;
import no.skavdahl.udacity.popularmovies.mdb.StandardMovieList;

/**
 * The main Movie Discovery fragment.
 *
 * @author fdavs
 */
public class MainDiscoveryActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	// Log tags must be <= 23 characters
	// see Log.isLoggable() throws description
    private final String LOG_TAG = getClass().getSimpleName().substring(0, 23);

	private GridView posterGrid;
	private MoviePosterAdapter viewAdapter;

	private final int LOADER_ID = 0;

	// It is recommended to keep a local reference to this ChangeListener to avoid
	// garbage collection -- see the Android API docs at http://goo.gl/0yFyTy
	// (SharedPreferences#registerOnSharedPreferenceChangeListener)
	private SharedPreferences.OnSharedPreferenceChangeListener prefChangeListener;

	// --- saved instance state ---

	private Bundle savedInstanceState;

	private final static String BUNDLE_SCROLL_INDEX = "scroll.index";

	// --- cursor configuration ---

	private final static String[] CURSOR_PROJECTION = new String[] {
		PopularMoviesContract.MovieContract.Column._ID,
		PopularMoviesContract.MovieContract.Column.JSONDATA
	};

	private final static int CURSOR_INDEX_MOVIE_ID = 0;
	private final static int CURSOR_INDEX_MOVIE_JSON = 1;

	public MainDiscoveryActivityFragment() {
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		posterGrid.setAdapter(viewAdapter = new MoviePosterAdapter(getContext(), null, 0, posterViewWidth));

	    // -- what should happen when a movie poster is clicked
		posterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Cursor cursor = (Cursor) viewAdapter.getItem(position);
				if (cursor != null) {
					int movieId = cursor.getInt(CURSOR_INDEX_MOVIE_ID);
					openMovieDetailsActivity(movieId);
				}
			}
		});

	    return view;
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		saveGridScrollPosition(outState);
		super.onSaveInstanceState(outState);
	}

	private void saveGridScrollPosition(Bundle outState) {
		outState.putInt(BUNDLE_SCROLL_INDEX, posterGrid.getFirstVisiblePosition());
	}

	@Override
	public void onActivityCreated(Bundle inState) {
		super.onActivityCreated(inState);

		// save so we can restore scroll position once the movie list has been loaded
		savedInstanceState = inState;

		// TODO purge old data (outdated list contents and unreferenced movies)

		// initialize the loader
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}

	private void restoreGridScrollPosition(Bundle inState) {
		int position = inState.getInt(BUNDLE_SCROLL_INDEX);
		posterGrid.setSelection(position);
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
	 * @param movieId The movie for which to show details.
	 */
	private void openMovieDetailsActivity(final int movieId) {
		Intent openMovieDetailsIntent = new Intent(getContext(), MovieDetailActivity.class);
		openMovieDetailsIntent.putExtra(MovieDetailActivity.INTENT_EXTRA_DATA, movieId);

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

		// set activity title
		int titleId = mapMovieListToTitle(selectedMovieList);
		getActivity().setTitle(titleId);
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

	private int mapMovieListToTitle(StandardMovieList movieList) {
		switch (movieList) {
			case POPULAR:
				return R.string.action_popular_movies;
			case TOP_RATED:
				return R.string.action_top_rated_movies;
			case NOW_PLAYING:
				return R.string.action_new_movies;
			case UPCOMING:
				return R.string.action_upcoming_movies;
			default:
				return R.string.app_name;
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

	// --- LoaderManager.LoaderCallback<Loader> interface ---

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String listName = "popular"; // TODO read from preferences
		String sortOrder =
			PopularMoviesContract.ListMembershipContract.Column.PAGE + " ASC, " +
				PopularMoviesContract.ListMembershipContract.Column.POSITION + " ASC";

		Uri listMemberUri = PopularMoviesContract.ListContract.buildListMemberDirectoryUri(listName);

		return new CursorLoader(
			getContext(),
			listMemberUri,
			CURSOR_PROJECTION,
			null, // selection
			null, // selectionArgs
			sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		viewAdapter.swapCursor(cursor);

		if (savedInstanceState != null) {
			restoreGridScrollPosition(savedInstanceState);
			savedInstanceState = null;
		}

		// determine whether we should update the list
		// TODO determine whether to update the list from the server

		if (cursor.getCount() > 0)
			return; // database query returned results so we're done

		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);

		if (verbose) Log.v(LOG_TAG, "Database query returned empty result: scheduling update");

		String listName = "popular"; // TODO read from settings
		int page = 1; // TODO read from... somewhere

		UpdateMovieListTask updateTask = new UpdateMovieListTask(getActivity());
		updateTask.execute(new UpdateMovieListTask.Input(listName, page));
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		viewAdapter.swapCursor(null);
	}

	// --- End LoaderManager.LoaderCallback<Loader> interface ---

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
	    getLoaderManager().restartLoader(LOADER_ID, null, this);
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
