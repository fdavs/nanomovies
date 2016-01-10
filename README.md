This is an implementation of the Popular Movies project that is part of the
Android Nanodegree program offered by Udacity.

themoviedb.org API Key
======================

Prior to compilation, you must provide a valid API key for themoviedb
service.

  * Add the following line to the file `$HOME/.gradle/gradle.properties` (create the file if necessary)

        themoviedb_api_key = "<API KEY>"
          
  
  * Replace the string `<API KEY>` with your API key.

Implementation notes
====================

I have purposefully implemented all features (data binding, content providers) by hand rather than to use external libraries, in an effort to better learn how Android works under the hood.

TODO
====

Basic requirements:
  - [x] ~~Feature: Allow user to view movie trailer (in the YouTube app or web browser)~~
  - [x] ~~Feature: Allow user to read reviews~~
  - [x] ~~Feature: Allow user to bookmark a movie as a personal favorite~~
  - [x] ~~Feature: Allow user to view his/her personal favorites~~
  - [x] ~~Optimize for tablet~~

Requirements for the "Exceeds specifications" distinction:
  - [x] ~~Exceeds feature: App persists favorite movie details using a database~~
    - [x] ~~Store detailed info for movies that have been explored offline~~
    - [x] ~~Store image data in database or on the file system~~
    - [x] ~~Purge old data from the database periodically~~
  - [x] ~~Exceeds feature: App displays favorite movie details even when offline~~
  - [x] ~~Exceeds feature: App uses a contentProvider to populate favorite movie details. (It is allowed to use a library to generate the content provider rather than to build one by hand)~~
  - [x] ~~Exceeds feature: Movie Details View includes an action bar item that allows the user to share the first trailer video URL from the list of trailers~~
  - [x] ~~Exceeds feature: App uses a share Intent to expose the external youtube URL for the trailer~~

Refactorings and other improvements:
  - [x] ~~Refactor: Move themoviedb API key from app.properties to the global Gradle properties~~
  - [x] ~~Bonus: Enable endless scrolling~~
    - [x] ~~Refactor: Replace GridView with RecycleView~~
  - [ ] Bonus: App icon
  - [ ] Bonus: Default poster image when none can be downloaded from themoviedb.org (replace the somewhat ugly generated color-based poster image)
  - [ ] Bonus: Display genre list and actors in the movie details view
    - [ ] Bonus: Store enumerations (genre lists, actor names etc) in a database, refresh on app start
  - [ ] Bonus: Support the local language with english as fallback
    - [ ] Include original title in the movie detail activity
  - [ ] Possible: change movie list by tapping the list name, opening a list menu (removing these options from the "settings" menu)
        See http://developer.android.com/training/appbar/action-views.html
  - [ ] Possible: Move the favorite button to the action bar or to a floating action button
  - [ ] Possible: Expose the favorite movies as searchable through the content provider
  - [ ] Possible: consider alternative JSON serialization frameworks (like [gson] [gson])
  - [ ] Possible: review other popular android libraries at [appbrain.com] [appbrain], like [guava] [guava]

Simplifications:

  - [ ] Refactor: Utilize [Butterknife] [butterknife] for view binding (ref. stage 1 feedback)
  - [ ] Refactor: Utilize [Retrofit] [retrofit] or [OkHttp] [okhttp] for REST services (ref. stage 1 feedback)
  - [x] ~~Replace the homemade favorite icon with a standard icon ("favorite" and "favorite border", or "star" and "star border" -- see https://design.google.com/icons/)~~
  - [ ] Show a rating bar for the movie's vote score

  [appbrain]: http://www.appbrain.com/stats/libraries/dev
  [butterknife]: http://jakewharton.github.io/butterknife/
  [gson]: https://github.com/google/gson
  [guava]: https://github.com/google/guava
  [retrofit]: http://square.github.io/retrofit/
  [okhttp]: https://github.com/square/okhttp

References for stage 2
======================
  * Sunshine part 4 and 5
  * [Stage 2 Implementation Guide] [implguide]
  * [Stage 2 Evaluation Rubbric] [evalguide]

  [implguide]: https://docs.google.com/document/d/1ZlN1fUsCSKuInLECcJkslIqvpKlP7jWL2TP9m6UiA6I/pub?embedded=true
  [evalguide]: https://docs.google.com/document/d/11JDnp_WTNGcIm_gs1raroUuDyxo9H_WsQxnpeozMov4/pub?embedded=true

Notes
=====

API calls
---------

This program does not use the `/discover/movie` API call at themoviedb as described in the "Network API implementaion" project evaluation document. I prefer instead `/movie/latest` and related API calls, for two reasons.

First, this is the simplest way to get an intelligent (also taking into account the number of votes) response for the top rated movies.

Second, I think the optimal solution shows the same lists of movies that are displayed when we navigate to themoviedb.org website and choose Movies > Popular/Top Rated/etc. It is possible to get the same lists by using `/discover/movie` with suitable combinations of parameters like `vote_cont.gte` and `sort_by`, but this makes the app vulnerable to tweaks when themoviedb updates its algorithms.


