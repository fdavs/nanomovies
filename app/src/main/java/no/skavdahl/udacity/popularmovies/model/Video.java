package no.skavdahl.udacity.popularmovies.model;

import android.text.TextUtils;

/**
 * Represents a video or trailer related to a movie.
 *
 * @author fdavs
 */
public class Video {

	private final String key;
	private final String site;
	private final String name;

	public Video(String key, String site, String name) {
		if (TextUtils.isEmpty(key))
			throw new IllegalArgumentException("key == " + key);
		if (TextUtils.isEmpty(site))
			throw new IllegalArgumentException("site == " + site);
		if (TextUtils.isEmpty(name))
			throw new IllegalArgumentException("name == " + name);

		this.key = key;
		this.site = site;
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public String getSite() {
		return site;
	}

	public String getName() {
		return name;
	}
}
