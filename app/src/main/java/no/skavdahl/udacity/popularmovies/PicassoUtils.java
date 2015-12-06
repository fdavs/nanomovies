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
			int posterWidth = (int) context.getResources().getDimension(R.dimen.poster_width);
			Picasso.with(context)
				.load(Request.getPosterThumbnailDownloadURL(movie.getPosterPath(), posterWidth))
				.error(OfflinePoster.forMovie(context, movie))
				.into(targetView, callback);
		}
		else
			targetView.setImageDrawable(OfflinePoster.forMovie(context, movie));
	}
}