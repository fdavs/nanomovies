This is an implementation of the Popular Movies project that is part of the
Android Nanodegree program offered by Udacity.

themoviedb.org API Key
======================

Prior to compilation, you must provide a valid API key for themoviedb
service.

  * rename the file `app.properties.example` to `app.properties`
  * paste your API key into this file

Notes
=====

API calls
---------

This program does not use the `/discover/movie` API call at themoviedb as described in the "Network API implementaion" project evaluation document. I prefer instead `/movie/latest` and related API calls, for two reasons. 

First, this is the simplest way to get an intelligent (also taking into account the number of votes) response for the top rated movies.

Second, I think the optimal solution shows the same lists of movies that are displayed when we navigate to themoviedb.org website and choose Movies > Popular/Top Rated/etc. It is possible to get the same lists by using `/discover/movie` with suitable combinations of parameters like `vote_cont.gte` and `sort_by`, but this makes the app vulnerable to tweaks when themoviedb updates its algorithms.


