package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import no.skavdahl.udacity.popularmovies.mdb.Request;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * A collection of Picasso utilities.
 *
 * @author fdavs
 */
public class PicassoUtils {

	/**
	 * Attempts to load a movie poster thumbnail. If the poster image cannot be loaded,
	 * a poster image is generated and displayed instead.
	 *
	 * @param context The Android context
	 * @param movie The movie whose poster image to load
	 * @param targetView The view that will display the image once it has been loaded
	 */
	public static void displayPosterWithOfflineFallback(Context context, Movie movie, ImageView targetView) {
		if (movie.getPosterPath() != null) {
			int posterWidth = (int) context.getResources().getDimension(R.dimen.poster_width);
			Picasso.with(context)
				.load(Request.getPosterThumbnailDownloadURL(movie.getPosterPath(), posterWidth))
				.error(OfflinePoster.forMovie(context, movie))
				.into(targetView);
		}
		else
			targetView.setImageDrawable(OfflinePoster.forMovie(context, movie));
	}

	/**
	 * Attempts to load a movie backdrop image.
	 *
	 * @param context The Android context
	 * @param movie The movie whose poster image to load
	 * @param targetView The view that will display the image once it has been loaded
	 */
	public static void displayBackdrop(Context context, Movie movie, ImageView targetView) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();

		String path = movie.getBackdropPath();
		if (path == null)
			path = movie.getPosterPath();

		if (path == null)
			return; // don't display anything
			// TODO consider displaying an alternative layout if there is no backdrop

		Picasso.with(context)
			.load(Request.getImageDownloadURL(Request.ImageType.BACKDROP, path, dm.widthPixels))
			.into(targetView);
	}
}