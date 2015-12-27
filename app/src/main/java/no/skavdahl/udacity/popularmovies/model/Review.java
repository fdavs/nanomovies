package no.skavdahl.udacity.popularmovies.model;

import android.text.TextUtils;

/**
 * Represents a review of a movie.
 *
 * @author fdavs
 */
public class Review {

	private final String id;
	private final String author;
	private final String content;

	public Review(String id, String author, String content) {
		// If API level >= 19 it's possible to use java.util.Objects#requireNonNull
		if (TextUtils.isEmpty(id))
			throw new IllegalArgumentException("id = " + id);

		if (TextUtils.isEmpty(author))
			throw new IllegalArgumentException("author == " + author);

		if (TextUtils.isEmpty(content))
			throw new IllegalArgumentException("content == " + content);

		this.id = id;
		this.author = author;
		this.content = content;
	}

	public String getId() {
		return id;
	}

	public String getAuthor() {
		return author;
	}

	public String getContent() {
		return content;
	}
}
