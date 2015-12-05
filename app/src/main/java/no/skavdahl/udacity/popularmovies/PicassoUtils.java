package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.mdb.Request;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * A Picasso Callback that displays a generated movie poster if the real poster cannot
 * be accessed on the server.
 *
 * @author fdavs
 */
public class PicassoUtils {
	public static void displayWithFallback(Context context, Movie movie, ImageView targetView, com.squareup.picasso.Callback callback) {
		if (movie.getPosterPath() != null) {
			Picasso.with(context)
				.load(Request.getPosterThumbnailDownloadURL(movie.getPosterPath(), DiscoverMovies.POSTER_SIZE_PIXELS.x))
				.error(OfflinePoster.forMovie(movie))
				.into(targetView, callback);
		}
		else
			targetView.setImageDrawable(OfflinePoster.forMovie(movie));
	}
}