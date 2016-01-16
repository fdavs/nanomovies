package no.skavdahl.udacity.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;

import com.squareup.picasso.Picasso;

import java.io.File;

import no.skavdahl.udacity.popularmovies.BuildConfig;
import no.skavdahl.udacity.popularmovies.R;
import no.skavdahl.udacity.popularmovies.mdb.Request;
import no.skavdahl.udacity.popularmovies.mdb.StandardMovieList;
import no.skavdahl.udacity.utils.picasso.FileTarget;

import static no.skavdahl.udacity.popularmovies.data.PopularMoviesContract.*;

/**
 * Toggles whether a movie is a favorite or not.
 *
 * A movie becomes a favorite by being added to the favorite movies list.
 *
 * @author fdavs
 */
public class ToggleFavoriteTask extends AsyncTask<Void, Void, Void> {

	public static final String LOG_TAG = ToggleFavoriteTask.class.getSimpleName();

	private final boolean verbose = BuildConfig.DEBUG && Log.isLoggable(LOG_TAG, Log.VERBOSE);

	private final Context context;
	private final boolean makeFavorite;
	private final int movieId;

	private final String currentPosterPath;
	private String newPosterPath;

	private final String currentBackdropPath;
	private String newBackdropPath;

	/**
	 * Helper class to help coordinate the download of the necessary images
	 * and trigger the database update async task when the downloads are finished.
	 *
	 * <p><b>Note.</b> Although this object coordinates operations happening on different threads,
	 * it is not (and does not to be) thread-safe. All methods should only from the UI
	 * thread.</p>
	 */
	private static class Coordinator {
		public static final int IMAGETYPE_POSTER = 1;
		public static final int IMAGETYPE_BACKDROP = 2;

		private final ToggleFavoriteTask task;

		private boolean posterDownloaded = false;
		private boolean backdropDownloaded = false;

		public Coordinator(final ToggleFavoriteTask task) {
			this.task = task;
		}

		/**
		 * Schedule a background download of an image.
		 */
		public void scheduleDownload(
			final Context context,
			final int imageType,
			final String baseImagePath,
			final String downloadUrl) {

			Picasso
				.with(context)
				.load(downloadUrl)
				.into(new FileTarget(context, baseImagePath) {
					@Override
					public void onSuccess(String newImagePath) {
						downloadComplete(imageType, newImagePath);
					}

					@Override
					public void onBitmapFailed(Drawable errorDrawable) {
						downloadComplete(imageType, null);
					}
				});
		}

		/**
		 * Called when an image has finished downloading and stored on local storage
		 * (either successfully or ending with failure).
		 *
		 * @param imageType Identifies the kind of image that was downloaded.
		 *                  {@link #IMAGETYPE_POSTER} or {@link #IMAGETYPE_BACKDROP}.
		 * @param newPath If the download succeeded, this is the path to the image file on disk.
		 *                If the download fail, this will be <tt>null</tt>.
		 */
		public void downloadComplete(final int imageType, final String newPath) {
			if (imageType == IMAGETYPE_POSTER) {
				posterDownloaded = true;
				task.setNewPosterPath(toLocalFilePath(newPath));
			}
			else if (imageType == IMAGETYPE_BACKDROP) {
				backdropDownloaded = true;
				task.setNewBackdropPath(toLocalFilePath(newPath));
			}

			if (allDownloadsCompleted())
				task.execute();
		}

		/** Returns true when all downloads have completed. */
		private boolean allDownloadsCompleted() {
			return posterDownloaded && backdropDownloaded;
		}
	}

	/**
	 * Start a background task to add the given movie to the list of favorite movies.
	 *
	 * @param context Context for background execution
	 * @param movieId The movie to add to the list of favorites
	 * @param posterPath The current poster path for the movie
	 * @param backdropPath The current backdrop path for the movie
	 */
	// TODO remove the image paths from the method signature
	public static void addToFavorites(final Context context, final int movieId, final String posterPath, String backdropPath) {

		// Use Picasso to cache the poster image and the backdrop image to disk.
		// It's beneficial to use Picasso because it likely already have the images
		// cached in memory, preventing an unnecessary network download operation.
		//
		// Problem: Picasso manages its own background operations and must be set up from
		// the main thread.

		final ToggleFavoriteTask task = new ToggleFavoriteTask(context, movieId, posterPath, backdropPath, true);
		final Coordinator coordinator = new Coordinator(task);

		// initiate download of poster path (if there is one)
		if (posterPath == null)
			coordinator.downloadComplete(Coordinator.IMAGETYPE_POSTER, null);
		else {
			int posterWidth = (int) context.getResources().getDimension(R.dimen.poster_width);
			String downloadUrl = Request.getPosterThumbnailDownloadURL(posterPath, posterWidth);
			coordinator.scheduleDownload(context, Coordinator.IMAGETYPE_POSTER, posterPath, downloadUrl);
		}

		// initiate download of backdrop path (if there is one)
		if (backdropPath == null)
			coordinator.downloadComplete(Coordinator.IMAGETYPE_BACKDROP, null);
		else {
			DisplayMetrics dm = context.getResources().getDisplayMetrics();
			String downloadUrl = Request.getImageDownloadURL(Request.ImageType.BACKDROP, backdropPath, dm.widthPixels);
			coordinator.scheduleDownload(context, Coordinator.IMAGETYPE_BACKDROP, backdropPath, downloadUrl);
		}

		// when the downloads initiated above have completed, the coordinator will
		// initiate the background job to update the database accordingly and complete
		// the operation
	}

	/**
	 * Start a background task to remove the given movie from the list of favorite movies.
	 *
	 * @param context Context for background execution
	 * @param movieId The movie to remove from the list of favorites
	 * @param posterPath The current poster path for the movie
	 * @param backdropPath The current backdrop path for the movie
	 */
	// TODO remove the image paths from the method signature
	public static void removeFromFavorites(final Context context, final int movieId, final String posterPath, String backdropPath) {
		final ToggleFavoriteTask task = new ToggleFavoriteTask(context, movieId, posterPath, backdropPath, false);

		if (posterPath != null)
			task.setNewPosterPath(toOnlinePath(posterPath));

		if (backdropPath != null)
			task.setNewBackdropPath(toOnlinePath(backdropPath));

		task.execute();
	}

	/**
	 * Takes a filename of the form "file:///path/to/file.jpg" and removes scheme, path
	 * and extension, leaving the base filename "/file.jpg".
	 */
	private static String toOnlinePath(String path) {
		int pathSepPos = path.lastIndexOf(File.separator);
		return path.substring(pathSepPos);
	}

	/**
	 * Converts a path to a local file path starting with "file:///"
	 */
	private static String toLocalFilePath(String path) {
		return "file://" + path;
	}


	public void setNewPosterPath(final String newPath) {
		this.newPosterPath = newPath;
	}

	public void setNewBackdropPath(final String newPath) {
		this.newBackdropPath = newPath;
	}

	/**
	 * Create a new task to add or remove a movie from the list of favorite.
	 *
	 * <p>Do not make a task directly but use {@link #addToFavorites} or
	 * {@link #removeFromFavorites}.</p>
	 *
	 * @param movieId The movie to modify
	 * @param currentPosterPath The current poster path (if any -- may be <tt>null</tt>)
	 * @param currentBackdropPath The current backdrop path (if any -- may be <tt>null</tt>)
	 * @param makeFavorite If <tt>true</tt>, add the movie to the list of favorite movies.
	 *                     If <tt>false</tt>, remove it from the list.
	 */
	private ToggleFavoriteTask(
		final Context context,
		final int movieId,
		final String currentPosterPath,
		final String currentBackdropPath,
		final boolean makeFavorite) {

		this.context = context;
		this.movieId = movieId;
		this.currentPosterPath = currentPosterPath;
		this.currentBackdropPath = currentBackdropPath;
		this.makeFavorite = makeFavorite;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Uri listMemberUri = ListContract.buildListMemberItemUri(StandardMovieList.FAVORITE, movieId);

		if (makeFavorite)
			context.getContentResolver().insert(listMemberUri, null);
		else {
			context.getContentResolver().delete(listMemberUri, null, null);

			tryDeleteFile(currentPosterPath);
			tryDeleteFile(currentBackdropPath);
		}

		updateImagePathsInDb(movieId, newPosterPath, newBackdropPath);

		if (verbose) {
			if (newPosterPath != null)
				Log.v(LOG_TAG, "Updated database, changed posterPath from " + currentPosterPath + " to " + newPosterPath);

			if (newBackdropPath != null)
				Log.v(LOG_TAG, "Updated database, changed backdropPath from " + currentBackdropPath + " to " + newBackdropPath);
		}

		return null;
	}

	private void updateImagePathsInDb(final int movieId, final String newPosterPath, final String newBackdropPath) {
		Uri movieUri = MovieContract.buildMovieItemUri(movieId);
		ContentValues values = new ContentValues();

		if (newPosterPath != null)
			values.put(MovieContract.Column.POSTER_PATH, newPosterPath);

		if (newBackdropPath != null)
			values.put(MovieContract.Column.BACKDROP_PATH, newBackdropPath);

		if (values.size() > 0)
			context.getContentResolver().update(
				movieUri,
				values,
				MovieContract.Column._ID + " = ?",
				new String[]{ Integer.toString(movieId) });
	}

	/**
	 * Attempts to delete a file; failures are logged but otherwise ignored.
	 *
	 * @param path Absolute path to the file to be deleted
	 */
	private void tryDeleteFile(String path) {
		File f = new File(path);
		if (f.exists()) {
			if (f.delete()) {
				if (verbose) Log.v(LOG_TAG, "Deleted file: " + path);
			}
			else
				Log.w(LOG_TAG, "Failed to delete file: " + path);
		}
	}
}
