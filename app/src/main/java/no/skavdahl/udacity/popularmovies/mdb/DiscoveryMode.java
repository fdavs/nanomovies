package no.skavdahl.udacity.popularmovies.mdb;

/**
 * Determines how to discover movies. Currently supported modes are:
 * <ul>
 *     <li>{@link #POPULAR_MOVIES}</li>
 *     <li>{@link #HIGH_RATED_MOVIES}</li>
 * </ul>
 */
public enum DiscoveryMode {

	/** Discover movies by their popularity index. */
	POPULAR_MOVIES(0),

	/** Discover movies by their rating score. */
	HIGH_RATED_MOVIES(1),

	/** Discover movies by their release date. */
	NEW_MOVIES(2);

	/** The default discovery mode. */
	public static DiscoveryMode DEFAULT = POPULAR_MOVIES;

	private final int idcode;

	DiscoveryMode(final int idcode) {
		this.idcode = idcode;
	}

	public int getIdentityCode() {
		return idcode;
	}

	public static DiscoveryMode fromIdentityCode(final int idcode) {
		for (DiscoveryMode mode : DiscoveryMode.values()) {
			if (mode.getIdentityCode() == idcode)
				return mode;
		}
		return null;
	}
}
