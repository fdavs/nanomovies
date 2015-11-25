package no.skavdahl.udacity.popularmovies;

import android.widget.ImageView;

import no.skavdahl.udacity.popularmovies.model.Movie;

public abstract class OfflinePosterCallback implements com.squareup.picasso.Callback {

	private final Movie movie;
	private final ImageView targetView;

	public OfflinePosterCallback(final Movie movie, final ImageView posterView) {
		this.movie = movie;
		this.targetView = posterView;
	}

	public Movie getMovie() {
		return movie;
	}

	public ImageView getTargetView() {
		return targetView;
	}

	@Override
	public void onError() {
		displayOfflinePoster();
	}

	protected void displayOfflinePoster() {
		OfflinePoster.forMovie(movie);
	}
}