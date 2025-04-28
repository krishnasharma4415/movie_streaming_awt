import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.ImageIcon;
import com.google.gson.*;

public class MovieDatabase {
    private static final String TMDB_API_KEY = "18ba17d49c4c3a8233c350b395968ddd";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final Gson gson = new Gson();
    private static Map<Integer, String> genreMap = new HashMap<>();

    public static void initialize() {
        System.out.println("Initializing MovieDatabase - fetching genres from TMDb API");
        fetchGenres();
    }

    public static Map<Integer, String> getGenreIdToNameMap() {
        if (genreMap.isEmpty()) {
            fetchGenres();
        }
        return genreMap;
    }

    public static List<Map<String, Object>> fetchGenres() {
        List<Map<String, Object>> genres = new ArrayList<>();

        try {
            String response = makeApiCall(TMDB_BASE_URL + "/genre/movie/list");
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            JsonArray genresArray = jsonObject.getAsJsonArray("genres");

            genreMap.clear(); // Clear existing map

            for (JsonElement genreElement : genresArray) {
                JsonObject genreObj = genreElement.getAsJsonObject();
                Integer id = genreObj.get("id").getAsInt();
                String name = genreObj.get("name").getAsString();

                Map<String, Object> genre = new HashMap<>();
                genre.put("id", id);
                genre.put("name", name);
                genres.add(genre);

                genreMap.put(id, name);
            }
        } catch (Exception e) {
            System.out.println("Error fetching genres: " + e.getMessage());
        }

        return genres;
    }

    public static List<Map<String, Object>> getGenres() {
        List<Map<String, Object>> genres = new ArrayList<>();

        for (Map.Entry<Integer, String> entry : genreMap.entrySet()) {
            Map<String, Object> genre = new HashMap<>();
            genre.put("id", entry.getKey());
            genre.put("name", entry.getValue());
            genres.add(genre);
        }

        // Sort by genre name
        genres.sort((g1, g2) -> ((String) g1.get("name")).compareTo((String) g2.get("name")));

        return genres;
    }

    public static List<Map<String, Object>> searchMovies(String query) {
        return searchMovies(query, null, null, null, null);
    }

    public static List<Map<String, Object>> searchMovies(String query, Integer genreId, String year, Double minRating,
            String sortBy) {
        try {
            StringBuilder urlBuilder = new StringBuilder(TMDB_BASE_URL);
            urlBuilder.append("/search/movie?api_key=").append(TMDB_API_KEY);
            urlBuilder.append("&query=").append(URLEncoder.encode(query, "UTF-8"));

            // Add additional filters
            if (genreId != null) {
                urlBuilder.append("&with_genres=").append(genreId);
            }

            if (year != null && !year.equals("All Years")) {
                urlBuilder.append("&primary_release_year=").append(year);
            }

            if (minRating != null) {
                urlBuilder.append("&vote_average.gte=").append(minRating);
            }

            if (sortBy != null) {
                urlBuilder.append("&sort_by=").append(sortBy);
            }

            String response = makeApiCall(urlBuilder.toString());
            return parseMoviesResponse(response);
        } catch (Exception e) {
            System.out.println("Error searching TMDb: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static List<Map<String, Object>> getPopularMovies() {
        return getPopularMovies(null, null, null, null);
    }

    public static List<Map<String, Object>> getPopularMovies(Integer genreId, String year, Double minRating,
            String sortBy) {
        try {
            StringBuilder urlBuilder = new StringBuilder(TMDB_BASE_URL);

            // Choose endpoint based on sort criteria
            if (sortBy != null) {
                if (sortBy.equals("popularity.desc")) {
                    urlBuilder.append("/movie/popular");
                } else if (sortBy.equals("vote_average.desc")) {
                    urlBuilder.append("/movie/top_rated");
                } else if (sortBy.equals("release_date.desc")) {
                    urlBuilder.append("/movie/now_playing");
                } else {
                    urlBuilder.append("/movie/popular"); // Default to popular
                }
            } else {
                urlBuilder.append("/movie/popular"); // Default to popular
            }

            urlBuilder.append("?api_key=").append(TMDB_API_KEY);

            // Add additional filters
            if (genreId != null) {
                urlBuilder.append("&with_genres=").append(genreId);
            }

            if (year != null && !year.equals("All Years")) {
                urlBuilder.append("&primary_release_year=").append(year);
            }

            if (minRating != null) {
                urlBuilder.append("&vote_average.gte=").append(minRating);
            }

            String response = makeApiCall(urlBuilder.toString());
            return parseMoviesResponse(response);
        } catch (Exception e) {
            System.out.println("Error fetching popular movies: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static List<Map<String, Object>> discoverMovies(Integer genreId, String year, Double minRating,
            String sortBy) {
        try {
            StringBuilder urlBuilder = new StringBuilder(TMDB_BASE_URL);
            urlBuilder.append("/discover/movie?api_key=").append(TMDB_API_KEY);

            // Add filters
            if (genreId != null) {
                urlBuilder.append("&with_genres=").append(genreId);
            }

            if (year != null && !year.equals("All Years")) {
                urlBuilder.append("&primary_release_year=").append(year);
            }

            if (minRating != null) {
                urlBuilder.append("&vote_average.gte=").append(minRating);
            }

            if (sortBy != null) {
                urlBuilder.append("&sort_by=").append(sortBy);
            } else {
                urlBuilder.append("&sort_by=popularity.desc"); // Default sort
            }

            String response = makeApiCall(urlBuilder.toString());
            return parseMoviesResponse(response);
        } catch (Exception e) {
            System.out.println("Error discovering movies: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static Map<String, Object> getMovieDetails(int tmdbId) {
        try {
            String urlStr = TMDB_BASE_URL + "/movie/" + tmdbId + "?api_key=" + TMDB_API_KEY
                    + "&append_to_response=credits,videos";
            String response = makeApiCall(urlStr);

            JsonObject movieObj = JsonParser.parseString(response).getAsJsonObject();
            Map<String, Object> movie = new HashMap<>();

            // Basic details
            movie.put("tmdb_id", movieObj.get("id").getAsInt());
            movie.put("title", movieObj.has("title") ? movieObj.get("title").getAsString() : "Unknown");
            movie.put("overview", movieObj.has("overview") ? movieObj.get("overview").getAsString() : "");

            // Release date and year
            if (movieObj.has("release_date") && !movieObj.get("release_date").isJsonNull()) {
                String releaseDate = movieObj.get("release_date").getAsString();
                movie.put("release_date", releaseDate);
                if (releaseDate != null && releaseDate.length() >= 4) {
                    movie.put("year", releaseDate.split("-")[0]);
                } else {
                    movie.put("year", "Unknown");
                }
            } else {
                movie.put("release_date", "");
                movie.put("year", "Unknown");
            }

            // Rating
            if (movieObj.has("vote_average") && !movieObj.get("vote_average").isJsonNull()) {
                movie.put("vote_average", movieObj.get("vote_average").getAsDouble());
            } else {
                movie.put("vote_average", 0.0);
            }

            // Poster
            if (movieObj.has("poster_path") && !movieObj.get("poster_path").isJsonNull()) {
                movie.put("poster_path", TMDB_IMAGE_BASE_URL + movieObj.get("poster_path").getAsString());
            } else {
                movie.put("poster_path", "");
            }

            // Runtime
            if (movieObj.has("runtime") && !movieObj.get("runtime").isJsonNull()) {
                movie.put("runtime", movieObj.get("runtime").getAsInt());
            }

            // Genres
            if (movieObj.has("genres")) {
                JsonArray genresArray = movieObj.getAsJsonArray("genres");
                StringBuilder genresStr = new StringBuilder();
                for (int i = 0; i < genresArray.size(); i++) {
                    JsonObject genreObj = genresArray.get(i).getAsJsonObject();
                    if (i > 0)
                        genresStr.append(", ");
                    genresStr.append(genreObj.get("name").getAsString());
                }
                movie.put("genre", genresStr.toString());
            }

            // Cast
            if (movieObj.has("credits") && movieObj.getAsJsonObject("credits").has("cast")) {
                JsonArray castArray = movieObj.getAsJsonObject("credits").getAsJsonArray("cast");
                StringBuilder castStr = new StringBuilder();
                int count = Math.min(5, castArray.size()); // Get top 5 cast members
                for (int i = 0; i < count; i++) {
                    JsonObject castObj = castArray.get(i).getAsJsonObject();
                    if (i > 0)
                        castStr.append(", ");
                    castStr.append(castObj.get("name").getAsString());
                }
                movie.put("cast", castStr.toString());
            }

            // Director
            if (movieObj.has("credits") && movieObj.getAsJsonObject("credits").has("crew")) {
                JsonArray crewArray = movieObj.getAsJsonObject("credits").getAsJsonArray("crew");
                for (JsonElement crewElement : crewArray) {
                    JsonObject crewObj = crewElement.getAsJsonObject();
                    if (crewObj.has("job") && crewObj.get("job").getAsString().equals("Director")) {
                        movie.put("director", crewObj.get("name").getAsString());
                        break;
                    }
                }
            }

            // Trailer
            if (movieObj.has("videos") && movieObj.getAsJsonObject("videos").has("results")) {
                JsonArray videosArray = movieObj.getAsJsonObject("videos").getAsJsonArray("results");
                for (JsonElement videoElement : videosArray) {
                    JsonObject videoObj = videoElement.getAsJsonObject();
                    if (videoObj.has("type") && videoObj.get("type").getAsString().equals("Trailer") &&
                            videoObj.has("site") && videoObj.get("site").getAsString().equals("YouTube")) {
                        movie.put("trailer_key", videoObj.get("key").getAsString());
                        break;
                    }
                }
            }

            return movie;
        } catch (Exception e) {
            System.out.println("Error getting movie details: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static List<Map<String, Object>> getSimilarMovies(int tmdbId) {
        try {
            String urlStr = TMDB_BASE_URL + "/movie/" + tmdbId + "/similar?api_key=" + TMDB_API_KEY;
            String response = makeApiCall(urlStr);
            return parseMoviesResponse(response);
        } catch (Exception e) {
            System.out.println("Error getting similar movies: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static ImageIcon fetchPosterImage(String posterUrl) {
        if (posterUrl == null || posterUrl.isEmpty()) {
            return null;
        }

        try {
            URL url = new URL(posterUrl);
            return new ImageIcon(url);
        } catch (Exception e) {
            System.out.println("Error fetching poster image: " + e.getMessage());
            return null;
        }
    }

    public static String getStreamLink(int tmdbId) {
        // Use vidsrc.to for streaming the movie
        return "https://vidsrc.to/embed/movie/" + tmdbId;
    }

    public static String getTrailerUrl(String youtubeKey) {
        if (youtubeKey == null || youtubeKey.isEmpty()) {
            return null;
        }
        return "https://www.youtube.com/watch?v=" + youtubeKey;
    }

    private static String makeApiCall(String urlStr) throws IOException {
        int maxRetries = 3;
        int retryDelayMs = 1000;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    return response.toString();
                } else if (responseCode == 429) {
                    // Rate limit hit, wait and retry
                    if (attempt < maxRetries) {
                        Thread.sleep(retryDelayMs * attempt);
                        continue;
                    }
                }

                throw new IOException("HTTP error code: " + responseCode);
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    throw new IOException("Failed after " + maxRetries + " attempts: " + e.getMessage());
                }
                try {
                    Thread.sleep(retryDelayMs * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Operation interrupted", ie);
                }
            }
        }

        throw new IOException("Failed to make API call after " + maxRetries + " attempts");
    }

    private static List<Map<String, Object>> parseMoviesResponse(String jsonResponse) {
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray resultsArray = jsonObject.getAsJsonArray("results");

            for (JsonElement movieElement : resultsArray) {
                JsonObject movieObj = movieElement.getAsJsonObject();
                Map<String, Object> movie = new HashMap<>();

                // Basic details
                movie.put("tmdb_id", movieObj.get("id").getAsInt());
                movie.put("title", movieObj.has("title") ? movieObj.get("title").getAsString() : "Unknown");
                movie.put("overview", movieObj.has("overview") ? movieObj.get("overview").getAsString() : "");

                // Release date and year
                if (movieObj.has("release_date") && !movieObj.get("release_date").isJsonNull() &&
                        !movieObj.get("release_date").getAsString().isEmpty()) {
                    String releaseDate = movieObj.get("release_date").getAsString();
                    movie.put("release_date", releaseDate);
                    if (releaseDate.length() >= 4) {
                        movie.put("year", releaseDate.split("-")[0]);
                    } else {
                        movie.put("year", "Unknown");
                    }
                } else {
                    movie.put("release_date", "");
                    movie.put("year", "Unknown");
                }

                // Rating
                if (movieObj.has("vote_average") && !movieObj.get("vote_average").isJsonNull()) {
                    movie.put("vote_average", movieObj.get("vote_average").getAsDouble());
                } else {
                    movie.put("vote_average", 0.0);
                }

                // Poster
                if (movieObj.has("poster_path") && !movieObj.get("poster_path").isJsonNull() &&
                        movieObj.get("poster_path").getAsString() != null) {
                    movie.put("poster_path", TMDB_IMAGE_BASE_URL + movieObj.get("poster_path").getAsString());
                } else {
                    movie.put("poster_path", "");
                }

                // Genres
                if (movieObj.has("genre_ids")) {
                    JsonArray genreIdsArray = movieObj.getAsJsonArray("genre_ids");
                    StringBuilder genresStr = new StringBuilder();
                    for (int i = 0; i < genreIdsArray.size(); i++) {
                        int genreId = genreIdsArray.get(i).getAsInt();
                        String genreName = genreMap.containsKey(genreId) ? genreMap.get(genreId) : "";

                        if (!genreName.isEmpty()) {
                            if (genresStr.length() > 0)
                                genresStr.append(", ");
                            genresStr.append(genreName);
                        }
                    }
                    movie.put("genre", genresStr.toString());
                }

                results.add(movie);
            }
        } catch (Exception e) {
            System.out.println("Error parsing movies response: " + e.getMessage());
        }

        return results;
    }
}