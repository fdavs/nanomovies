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
	private final static String BUNDLE_MOVIES = "movies";
	private final static String BUNDLE_MOVIES_LOADTIME = "movies.time";
	private final static String BUNDLE_SCROLL_INDEX = "scroll.index";

	/** How long to keep movie data before they should be refreshed. */
	private final static long MAX_MOVIES_AGE = 1000 * 60*60; // 1 hour in millis

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


	/**
	 * Returns true if there are movie data available and they are not expired
	 * (older than the limit set by {@link #MAX_MOVIES_AGE}
	 */
	private boolean areMovieDataCurrent() {
		return true; // TODO reimplement
		/*
		List<Movie> movies = viewAdapter.getMovies();
		if (movies.isEmpty())
			return false;

		// there are movie data available so there will also be a non-null load time
		return areMovieDataCurrent(viewAdapter.getMovieLoadTime().getTime());
		*/
	}

	private boolean areMovieDataCurrent(long movieLoadTime) {
		return true; // TODO reimplement
		//return System.currentTimeMillis() - movieLoadTime < MAX_MOVIES_AGE;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (areMovieDataCurrent()) {
			saveGridScrollPosition(outState);
			// TODO saveMovieData(outState);
		}

		super.onSaveInstanceState(outState);
	}

	private void saveGridScrollPosition(Bundle outState) {
		outState.putInt(BUNDLE_SCROLL_INDEX, posterGrid.getFirstVisiblePosition());
	}

	/*
	private void saveMovieData(Bundle outState) {
		// The basic idea is to "bundle" the movie data into a parcel and save that
		// parcel in the outState bundle. This requires that Movie implements Parcelable.
		// Since it's just a little data, we already have a mechanism to serialize and
		// deserialize movies (json), and because in the long term I wish to save movie
		// data in a small, local database, I choose to use the existing json serialization
		// for now.

		List<Movie> movies = viewAdapter.getMovies();
		Date movieLoadTime = viewAdapter.getMovieLoadTime();
		//assert !movies.isEmpty() && movieLoadTime != null;

		MdbJSONAdapter jsonAdapter = new MdbJSONAdapter(getResources());

		ArrayList<String> bundle = new ArrayList<>(movies.size());
		for (Movie m : movies) {
			try {
				bundle.add(jsonAdapter.toJSONString(m));
			}
			catch (JSONException e) {
				// this error should never occur so this is mostly verifying an assertion
				Log.e(LOG_TAG, "Unable to convert to JSON: " + m, e);
			}
		}

		outState.putStringArrayList(BUNDLE_MOVIES, bundle);
		outState.putLong(BUNDLE_MOVIES_LOADTIME, movieLoadTime.getTime());
	}*/

	@Override
	public void onActivityCreated(Bundle inState) {
		super.onActivityCreated(inState);

		// initialize the loader
		getLoaderManager().initLoader(LOADER_ID, null, this);

		/*
		if (inState != null) {
			long movieLoadTime = inState.getLong(BUNDLE_MOVIES_LOADTIME);
			if (areMovieDataCurrent(movieLoadTime)) {
				restoreMovieData(inState);
				restoreGridScrollPosition(inState);
				return;
			}
		}

		refreshMovies();
		*/
	}

	private void restoreGridScrollPosition(Bundle inState) {
		int position = inState.getInt(BUNDLE_SCROLL_INDEX);
		posterGrid.setSelection(position);
	}

	/*
	private void restoreMovieData(Bundle inState) {
		ArrayList<String> bundle = inState.getStringArrayList(BUNDLE_MOVIES);
		long moviesLoadTime = inState.getLong(BUNDLE_MOVIES_LOADTIME);

		if (bundle == null || moviesLoadTime == 0)
			return;

		List<Movie> movies = new ArrayList<>(bundle.size());
		MdbJSONAdapter jsonAdapter = new MdbJSONAdapter(getResources());

		for (String json : bundle) {
			try {
				JSONObject obj = new JSONObject(json);
				movies.add(jsonAdapter.toMovie(obj));
			}
			catch (JSONException e) {
				// this error should never occur so this is mostly verifying an assertion
				Log.e(LOG_TAG, "Unable to convert from JSON: " + json, e);
			}
		}

		viewAdapter.setMovies(movies, new Date(moviesLoadTime));
	} */

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
		// Sort order:  Ascending, by date.
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

		/*Cursor cursor = getActivity().getContentResolver().query(listMemberUri,
			new String[]{
				PopularMoviesContract.MovieContract.Column._ID, // required by the CursorAdapter
				PopularMoviesContract.MovieContract.Column.JSONDATA}, // projection
			null, // selection
			null, // selectionArgs
			sortOrder);

		int adapterFlags = 0;

		return
		//posterGrid.setAdapter(viewAdapter = new MoviePosterAdapter(getContext(), cursor, adapterFlags, posterViewWidth));
		*/
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		viewAdapter.swapCursor(cursor);
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
	    StandardMovieList movieList = UserPreferences.getDiscoveryPreference(getActivity());
	    String apiKey = BuildConfig.THEMOVIEDB_API_KEY;

	    // TODO reimplement using the loader
	    getLoaderManager().restartLoader(LOADER_ID, null, this);
	    /*
        DiscoverMoviesTask task = new DiscoverMoviesTask(movieList, apiKey, getActivity(),
	        new DiscoverMoviesTask.Listener() {
			    @Override
			    public void onDownloadSuccess(List<Movie> movies) {
				    if (Log.isLoggable(LOG_TAG, Log.DEBUG))
				        Log.d(LOG_TAG, "Movie data successfully downloaded from server (" + movies.size() + " movies downloaded)");

				    viewAdapter.setMovies(movies, new Date());
			    }

			    @Override
			    public void onNetworkFailure() {
				    showFailureDialog(R.string.no_network_try_again);
			    }
		    }
        );
        task.execute();
        */
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
