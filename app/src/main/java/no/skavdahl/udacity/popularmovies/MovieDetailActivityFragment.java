package no.skavdahl.udacity.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import no.skavdahl.udacity.popularmovies.data.ToggleFavoriteTask;
import no.skavdahl.udacity.popularmovies.data.UpdateMovieTask;
import no.skavdahl.udacity.popularmovies.mdb.MdbJSONAdapter;
import no.skavdahl.udacity.popularmovies.model.Movie;
import no.skavdahl.udacity.popularmovies.model.Review;
import no.skavdahl.udacity.popularmovies.model.Video;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * Fragment displaying movie details.
 *
 * @author fdavs
 */
public class MovieDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private final String LOG_TAG = getClass().getSimpleName().substring(0, 23);

	private final int LOADER_ID = 0;

	private final String LOADER_ARGS_MOVIE_ID = "movieid";

	// --- view configuration ---

	private static final int MAX_VIDEOS = 10;
	private static final int MAX_REVIEWS = 10;

	// --- cursor configuration ---

	private final static String[] CURSOR_PROJECTION = new String[] {
		MovieContract.Column.MODIFIED,
		MovieContract.Column.FAVORITE,
		MovieContract.Column.JSONDATA
	};

	private final static int CURSOR_INDEX_MODIFIED = 0;
	private final static int CURSOR_INDEX_FAVORITE = 1;
	private final static int CURSOR_INDEX_MOVIE_JSON = 2;

	// --- share configuration ---

	private Movie shareMovie;
	private ShareActionProvider shareActionProvider;

	private static final String SHARE_YOUTUBE_LINK = "http://www.youtube.com/watch?v=";
	private static final String SHARE_THEMOVIEDB_LINK = "https://www.themoviedb.org/movie/";

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// register the loader
		Intent startingIntent = getActivity().getIntent();
		int movieId = startingIntent.getIntExtra(MovieDetailActivity.INTENT_EXTRA_DATA, 0);
		if (movieId == 0) {
			Log.e(LOG_TAG, "Intent extra does not specify movie id");
			getActivity().finish(); // abort the execution of this activity
			return;
		}

		Bundle loaderArgs = new Bundle();
		loaderArgs.putInt(LOADER_ARGS_MOVIE_ID, movieId);

		getLoaderManager().initLoader(LOADER_ID, loaderArgs, this);

		// enable the options menu for "share" actions
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
	                         ViewGroup container,
	                         Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_movie_detail, container, false);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_movie_details, menu);
		MenuItem shareMenuItem = menu.findItem(R.id.menu_item_share);
		shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareMenuItem);
	}

	// disable "findViewById() may return null" warning; it's true but will be caught quickly in testing
	@SuppressWarnings("ConstantConditions")
	private void bindModelToView(final Movie movie, final boolean isFavorite) {
		this.shareMovie = movie;

		final View view = getView();
		final Context context = getActivity();

		TextView movieTitleView = (TextView) view.findViewById(R.id.movie_title_textview);
		movieTitleView.setText(context.getString(R.string.movie_title, movie.getTitle()));

		TextView synopsisView = (TextView) view.findViewById(R.id.synopsis_textview);
		synopsisView.setText(
			context.getString(R.string.movie_synopsis, formatOptString(movie.getSynopsis(), "")));

		TextView releaseDateTextView = (TextView) view.findViewById(R.id.release_rate_textview);
		releaseDateTextView.setText(
			context.getString(R.string.movie_release_date, formatOptYear(movie.getReleaseDate(),
				context.getString(R.string.data_unknown))));

		TextView userRatingTextView = (TextView) view.findViewById(R.id.user_rating_textview);
		userRatingTextView.setText(
			context.getResources().getQuantityString(
				R.plurals.movie_user_rating, // id
				movie.getVoteCount(), // quantity
				movie.getVoteAverage(), movie.getVoteCount())); // format args

		ImageView posterView = (ImageView) view.findViewById(R.id.poster_imageview);
		PicassoUtils.displayPosterWithOfflineFallback(context, movie, posterView);

		ImageView backdropView = (ImageView) view.findViewById(R.id.backdrop_imageview);
		PicassoUtils.displayBackdrop(context, movie, backdropView);

		final ImageButton favoriteBtn = (ImageButton) view.findViewById(R.id.favorite_button);
		configureFavoriteBtn(favoriteBtn, movie, isFavorite);

		// videos
		ViewGroup videoContainer = (ViewGroup) view.findViewById(R.id.videos_container);
		bindVideosToView(movie, videoContainer);

		// reviews
		ViewGroup reviewsContainer = (ViewGroup) view.findViewById(R.id.review_container);
		bindReviewsToView(movie, reviewsContainer);

		// configure the share action
		configureShareIntent();
	}

	private void bindReviewsToView(Movie movie, ViewGroup container) {
		final LayoutInflater inflater = LayoutInflater.from(getActivity());

		container.removeAllViews();

		List<Review> reviewList = movie.getReviews();
		if (reviewList.isEmpty()) {
			TextView textView = (TextView) inflater.inflate(R.layout.simple_textview, container, false);
			textView.setText(R.string.no_reviews);
			container.addView(textView);
		}
		else {
			int numberToDisplay = Math.min(reviewList.size(), MAX_REVIEWS);
			for (int i = 0; i < numberToDisplay; ++i) {
				Review review = reviewList.get(i);
				View reviewView = inflater.inflate(R.layout.review_detail, container, false);

				TextView reviewTextView = (TextView) reviewView.findViewById(R.id.review_content_textview);
				reviewTextView.setText(review.getContent());

				TextView authorTextView = (TextView) reviewView.findViewById(R.id.review_author_textview);
				authorTextView.setText(review.getAuthor());

				container.addView(reviewView);
			}
		}
	}

	private void bindVideosToView(Movie movie, ViewGroup container) {
		final LayoutInflater inflater = LayoutInflater.from(getActivity());

		container.removeAllViews();

		List<Video> youtubeVideos = movie.getVideosFilterBySite(Video.SITE_YOUTUBE);
		if (youtubeVideos.isEmpty()) {
			TextView textView = (TextView) inflater.inflate(R.layout.simple_textview, container, false);
			textView.setText(R.string.no_videos);
			container.addView(textView);
		}
		else {
			int numberToDisplay = Math.min(youtubeVideos.size(), MAX_VIDEOS);
			for (int i = 0; i < numberToDisplay; ++i) {
				final Video video = youtubeVideos.get(i);

				View video_view = inflater.inflate(R.layout.trailer_detail, container, false);

				TextView titleTextView = (TextView) video_view.findViewById(R.id.video_title);
				titleTextView.setText(video.getName());

				video_view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						playVideo(video);
					}
				});

				container.addView(video_view);
			}
		}
	}

	/**
	 * Configure the favorite button, settings its on/off state and installing an appropriate
	 * click listener to toggle the favorite state.
	 */
	private void configureFavoriteBtn(ImageButton favoriteBtn, final Movie movie, final boolean isFavorite) {
		favoriteBtn.setImageResource(
			isFavorite
				? android.R.drawable.btn_star_big_off
				: android.R.drawable.btn_star_big_on);

		favoriteBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageButton favoriteBtn = (ImageButton) v;
				toggleFavorite(favoriteBtn, movie, isFavorite);
			}
		});
	}

	/**
	 * Set up the share menu with a suitable link.
	 *
	 * If the displayed movie has at least one YouTube trailer, the share action will send
	 * a link to the trailer. Otherwise, it will send a link to the movie on themoviedb.org.
	 */
	private void configureShareIntent() {
		if (shareActionProvider == null || shareMovie == null)
			return;

		// pick the first YouTube video:
		// videos.stream().filter(v -> v.site == YouTube).findFirst()
		List<Video> youtubeVideos = shareMovie.getVideosFilterBySite(Video.SITE_YOUTUBE);
		Video shareableVideo = youtubeVideos.isEmpty() ? null : youtubeVideos.get(0);
		String linkToShare = (shareableVideo != null)
			? SHARE_YOUTUBE_LINK + shareableVideo.getKey()
			: SHARE_THEMOVIEDB_LINK + shareMovie.getMovieDbId();

		String messageToFriend = getContext().getString(R.string.share_video_message, linkToShare);

		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(android.content.Intent.EXTRA_TEXT, messageToFriend);

		shareActionProvider.setShareIntent(intent);
	}

	/**
	 * Plays the given video. If YouTube is installed on the device, the video is opened
	 * in that app. Otherwise, fall back to the web browser or similar app to play the
	 * video there.
	 *
	 * @param video Reference to the video that is to be played
	 */
	private void playVideo(Video video) {
		String key = video.getKey();
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + key));
			startActivity(intent);
		}
		catch (ActivityNotFoundException ex) {
			Intent intent = new Intent(
				Intent.ACTION_VIEW,
				Uri.parse("http://www.youtube.com/watch?v=" + key));
			startActivity(intent);
		}
	}

	/**
	 * Toggles whether the currently displayed movie is one of the user's favorite movies
	 * or not.
	 *
	 * @param favoriteBtn Reference to the toggle button, whose appearance will change
	 * @param movie The movie being added or removed from the favorite movies list
	 */
	private void toggleFavorite(ImageButton favoriteBtn, Movie movie, boolean isFavorite) {
		configureFavoriteBtn(favoriteBtn, movie, !isFavorite);
		new ToggleFavoriteTask(getActivity(), movie.getMovieDbId(), !isFavorite).execute();
	}

	private String formatOptString(final String str, final String fallback) {
		return (str != null) ? str : fallback;
	}

	private String formatOptYear(final Date date, final String fallback) {
		if (date == null)
			return fallback;

		Calendar cal = GregorianCalendar.getInstance(Locale.getDefault());
		cal.setTime(date);

		return Integer.toString(cal.get(Calendar.YEAR));
	}

	// --- LoaderManager.LoaderCallback<Loader> interface ---

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle loaderArgs) {
		int movieId = loaderArgs.getInt(LOADER_ARGS_MOVIE_ID);

		Uri listMemberUri = MovieContract.buildMovieItemUri(movieId);
		return new CursorLoader(
			getActivity(),
			listMemberUri,
			CURSOR_PROJECTION,
			null, // selection
			null, // selection args
			null); // sort order
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (cursor == null || !cursor.moveToFirst())
			return; // no data to handle so just stop

		// convert the cursor data into a movie model
		// TODO ideally this deserialization should happen on a background thread
		String jsonData = cursor.getString(CURSOR_INDEX_MOVIE_JSON);
		long dataModifiedTime = cursor.getLong(CURSOR_INDEX_MODIFIED);
		boolean isFavorite = cursor.getInt(CURSOR_INDEX_FAVORITE) > 0;

		Movie movie;
		try {
			MdbJSONAdapter jsonAdapter = new MdbJSONAdapter(getResources());
			movie = jsonAdapter.toMovie(new JSONObject(jsonData));
		}
		catch (JSONException e) {
			Log.e(LOG_TAG, "Unable to deserialize movie from json", e); // TODO include more details
			return;
		}

		// update the view
		bindModelToView(movie, isFavorite);

		// evaluate the data: do we need to issue an web update?
		// we need to decide if the data is current and up-to-date or need to be refreshed
		// It is up-to-date if
		//   a) we have extended movie data (reviews, videos)
		//   b) the data are not "too old"
		// "too old" is determined by the quality of the network connection. A better and faster
		// connection makes "too old" a shorter amount of time
		// TODO include network quality in the calculation of "too old"
		final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);

		long dataAge = System.currentTimeMillis() - dataModifiedTime;
		long maxAge = BuildConfig.MOVIE_DATA_TIMEOUT;

		if (movie.hasExtendedData() && dataAge <= maxAge) {
			if (verbose) Log.v(LOG_TAG, "Movie data is up to date - no further action");
		}
		else {
			if (verbose) Log.v(LOG_TAG, "Movie data is missing or stale - updating");

			UpdateMovieTask asyncTask = new UpdateMovieTask(this.getActivity());
			asyncTask.execute(movie.getMovieDbId());
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// no action
	}

	// --- End LoaderManager.LoaderCallback<Loader> interface ---
}
