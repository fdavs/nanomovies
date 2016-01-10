package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import no.skavdahl.udacity.popularmovies.mdb.Request;

/**
 * A collection of Picasso utilities.
 *
 * @author fdavs
 */
public class PicassoUtils {

	/**
	 * Attempts to load a movie poster thumbnail. If the poster image cannot be loaded,
	 * a poster image is generated and displayed instead, showing the movie title.
	 *
	 * @param context The Android context
	 * @param posterPath The path to the poster image to load
	 * @param title The title of the movie
	 * @param targetView The view that will display the image once it has been loaded
	 */
	public static void displayPosterWithOfflineFallback(Context context, String posterPath, String title, ImageView targetView) {
		Drawable offlinePoster = OfflinePoster.forMovie(context, title);

		if (posterPath == null)
			targetView.setImageDrawable(offlinePoster);
		else {
			if (!isLocalFile(posterPath)) {
				int posterWidth = (int) context.getResources().getDimension(R.dimen.poster_width);
				posterPath = Request.getPosterThumbnailDownloadURL(posterPath, posterWidth);
			}

			Picasso.with(context).load(posterPath).error(offlinePoster).into(targetView);
		}
	}

	/**
	 * Attempts to load a movie backdrop image.
	 *
	 * @param context The Android context
	 * @param backdropPath The path to the backdrop image, if any (may be null)
	 * @param posterPath The path to the poster image, if any (may be null)
	 * @param targetView The view that will display the image once it has been loaded
	 */
	public static void displayBackdrop(Context context, String backdropPath, String posterPath, ImageView targetView) {

		String path = (backdropPath != null) ? backdropPath : posterPath;
		if (path == null)
			return; // don't display anything
			// TODO consider displaying an alternative layout if there is no backdrop

		if (!isLocalFile(path)) {
			DisplayMetrics dm = context.getResources().getDisplayMetrics();
			path = Request.getImageDownloadURL(Request.ImageType.BACKDROP, path, dm.widthPixels);
		}

		Picasso.with(context).load(path).into(targetView);
	}

	private static boolean isLocalFile(String path) {
		return path != null && path.startsWith("file://");
	}
}