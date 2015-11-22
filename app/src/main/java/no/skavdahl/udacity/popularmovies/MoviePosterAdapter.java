package no.skavdahl.udacity.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.List;

import no.skavdahl.udacity.popularmovies.mdb.DiscoverMovies;
import no.skavdahl.udacity.popularmovies.model.Movie;

/**
 * Adapter for displaying movie posters in ListViews.
 *
 * @author fdavs
 */
public class MoviePosterAdapter extends BaseAdapter {
	private final Context context;
	private List<Movie> movies = Collections.emptyList();

	public MoviePosterAdapter(Context context) {
		this.context = context;
	}

	public void setMovies(List<Movie> movies) {
		this.movies = movies;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return movies.size();
	}

	@Override
	public Object getItem(int position) {
		return movies.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0; // TODO Implement this method
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;

		if (convertView != null) {
			imageView = (ImageView) convertView;
		}
		else {
			imageView = new ImageView(context);
			imageView.setPadding(0, 0, 0, 0);
			imageView.setAdjustViewBounds(true);
		}

		Picasso.with(context)
			.load(DiscoverMovies.getPosterThumbnailDownloadURL(movies.get(position).getPosterPath()))
				// TODO .placeholder(R.drawable.user_placeholder)
				// TODO .error(R.drawable.user_placeholder_error)
			.into(imageView);

		return imageView;
	}
}
