import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.ImageIcon;
import com.google.gson.*;

public class MovieDatabase {
    // Using both authentication methods for backward compatibility
    private static final String TMDB_API_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiIxOGJhMTdkNDljNGMzYTgyMzNjMzUwYjM5NTk2OGRkZCIsIm5iZiI6MTc0NTc4MTc4MS44NzUsInN1YiI6IjY4MGU4NDE1YmFhYzFjNWE3ZDgxMzczMSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.PEBvE3zWRfg4k6Ekx74duoUMErhNUTxz2mUn8MMkZ-U";
    private static final String TMDB_API_KEY = "18ba17d49c4c3a8233c350b395968ddd";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    private static final Gson gson = new Gson();
    private static Map<Integer, String> genreMap = new HashMap<>();

    public static void initialize() {
        System.out.println("Initializing MovieDatabase - fetching genres from TMDb API");

        // Ensure genres are fetched and loaded correctly
        try {
            List<Map<String, Object>> genres = fetchGenres();
            System.out.println("Successfully loaded " + genres.size() + " genres");

            // Verify genreMap is populated
            if (genreMap.isEmpty()) {
                System.out.println("WARNING: Genre map is empty after initialization!");
                // Fallback - directly populate some common genres
                populateFallbackGenres();
            }
        } catch (Exception e) {
            System.err.println("ERROR initializing genres: " + e.getMessage());
            e.printStackTrace();

            // Fallback - directly populate some common genres
            populateFallbackGenres();
        }
    }

    private static void populateFallbackGenres() {
        genreMap.put(28, "Action");
        genreMap.put(12, "Adventure");
        genreMap.put(16, "Animation");
        genreMap.put(35, "Comedy");
        genreMap.put(80, "Crime");
        genreMap.put(99, "Documentary");
        genreMap.put(18, "Drama");
        genreMap.put(10751, "Family");
        genreMap.put(14, "Fantasy");
        genreMap.put(36, "History");
        genreMap.put(27, "Horror");
        genreMap.put(10402, "Music");
        genreMap.put(9648, "Mystery");
        genreMap.put(10749, "Romance");
        genreMap.put(878, "Science Fiction");
        genreMap.put(10770, "TV Movie");
        genreMap.put(53, "Thriller");
        genreMap.put(10752, "War");
        genreMap.put(37, "Western");
        System.out.println("Added fallback genres: " + genreMap.size());
    }

    public static Map<Integer, String> getGenreIdToNameMap() {
        if (genreMap.isEmpty()) {
            fetchGenres();
        }
        return genreMap;
    }

    public static List<Map<String, Object>> fetchGenres() {
        List<Map<String, Object>> genres = new ArrayList<>();

        System.out.println("Fetching genres from TMDB API...");

        try {
            String endpoint = TMDB_BASE_URL + "/genre/movie/list";
            System.out.println("Genre endpoint: " + endpoint);

            // Make API call with detailed error handling
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            // Set Authorization header with Bearer token
            conn.setRequestProperty("Authorization", "Bearer " + TMDB_API_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");

            System.out.println("Connecting to genre API...");
            int responseCode = conn.getResponseCode();
            System.out.println("Genre API response code: " + responseCode);

            if (responseCode == 200) {
                // Read the response
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the response
                String jsonResponse = response.toString();
                System.out.println("Genre API response: "
                        + jsonResponse.substring(0, Math.min(jsonResponse.length(), 100)) + "...");

                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                JsonArray genresArray = jsonObject.getAsJsonArray("genres");

                System.out.println("Found " + genresArray.size() + " genres");
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
                    System.out.println("Added genre: " + id + " = " + name);
                }
            } else {
                // Handle error response
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    errorResponse.append(line);
                }
                reader.close();
                System.out.println("Genre API error response: " + errorResponse.toString());

                throw new IOException("HTTP error code: " + responseCode + " - " + errorResponse.toString());
            }
        } catch (Exception e) {
            System.out.println("Error fetching genres: " + e.getMessage());
            e.printStackTrace();

            // If we have no genres at all, add fallback genres
            if (genreMap.isEmpty()) {
                System.out.println("Using fallback genres due to API error");
                populateFallbackGenres();
            }
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
            urlBuilder.append("/search/movie");

            // Add query parameters
            urlBuilder.append("?query=").append(URLEncoder.encode(query, "UTF-8"));

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

            // Add parameters if necessary
            boolean hasParam = false;

            // Add additional filters
            if (genreId != null) {
                urlBuilder.append(hasParam ? "&" : "?").append("with_genres=").append(genreId);
                hasParam = true;
            }

            if (year != null && !year.equals("All Years")) {
                urlBuilder.append(hasParam ? "&" : "?").append("primary_release_year=").append(year);
                hasParam = true;
            }

            if (minRating != null) {
                urlBuilder.append(hasParam ? "&" : "?").append("vote_average.gte=").append(minRating);
                hasParam = true;
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
            urlBuilder.append("/discover/movie");

            // Start with params
            boolean hasParam = false;

            // Add filters
            if (genreId != null) {
                urlBuilder.append(hasParam ? "&" : "?").append("with_genres=").append(genreId);
                hasParam = true;
            }

            if (year != null && !year.equals("All Years")) {
                urlBuilder.append(hasParam ? "&" : "?").append("primary_release_year=").append(year);
                hasParam = true;
            }

            if (minRating != null) {
                urlBuilder.append(hasParam ? "&" : "?").append("vote_average.gte=").append(minRating);
                hasParam = true;
            }

            if (sortBy != null) {
                urlBuilder.append(hasParam ? "&" : "?").append("sort_by=").append(sortBy);
                hasParam = true;
            } else {
                urlBuilder.append(hasParam ? "&" : "?").append("sort_by=popularity.desc"); // Default sort
                hasParam = true;
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
            // Request comprehensive details with a single API call
            String urlStr = TMDB_BASE_URL + "/movie/" + tmdbId + "?append_to_response=credits,videos,recommendations";
            String response = makeApiCall(urlStr);

            JsonObject movieObj = JsonParser.parseString(response).getAsJsonObject();
            Map<String, Object> movie = new HashMap<>();

            // Basic details
            movie.put("tmdb_id", movieObj.get("id").getAsInt());
            movie.put("title", movieObj.has("title") ? movieObj.get("title").getAsString() : "Unknown");
            movie.put("overview",
                    movieObj.has("overview") ? movieObj.get("overview").getAsString() : "No overview available");

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

            // Poster and backdrop
            if (movieObj.has("poster_path") && !movieObj.get("poster_path").isJsonNull()) {
                movie.put("poster_path", TMDB_IMAGE_BASE_URL + movieObj.get("poster_path").getAsString());
            } else {
                movie.put("poster_path", "");
            }

            if (movieObj.has("backdrop_path") && !movieObj.get("backdrop_path").isJsonNull()) {
                movie.put("backdrop_path", TMDB_IMAGE_BASE_URL + movieObj.get("backdrop_path").getAsString());
            } else {
                movie.put("backdrop_path", "");
            }

            // Runtime
            if (movieObj.has("runtime") && !movieObj.get("runtime").isJsonNull()) {
                int runtime = movieObj.get("runtime").getAsInt();
                movie.put("runtime", runtime);

                // Format runtime as hours and minutes
                int hours = runtime / 60;
                int minutes = runtime % 60;
                String runtimeFormatted = (hours > 0 ? hours + "h " : "") + minutes + "m";
                movie.put("runtime_formatted", runtimeFormatted);
            } else {
                movie.put("runtime", 0);
                movie.put("runtime_formatted", "Unknown");
            }

            // Genres
            if (movieObj.has("genres")) {
                JsonArray genresArray = movieObj.getAsJsonArray("genres");
                StringBuilder genresStr = new StringBuilder();
                List<String> genresList = new ArrayList<>();

                for (int i = 0; i < genresArray.size(); i++) {
                    JsonObject genreObj = genresArray.get(i).getAsJsonObject();
                    String genreName = genreObj.get("name").getAsString();
                    genresList.add(genreName);

                    if (i > 0)
                        genresStr.append(", ");
                    genresStr.append(genreName);
                }
                movie.put("genre", genresStr.toString());
                movie.put("genres_list", genresList);
            } else {
                movie.put("genre", "Unknown");
                movie.put("genres_list", new ArrayList<String>());
            }

            // Cast
            if (movieObj.has("credits") && movieObj.getAsJsonObject("credits").has("cast")) {
                JsonArray castArray = movieObj.getAsJsonObject("credits").getAsJsonArray("cast");
                StringBuilder castStr = new StringBuilder();
                List<Map<String, Object>> castList = new ArrayList<>();

                int count = Math.min(8, castArray.size()); // Get top 8 cast members
                for (int i = 0; i < count; i++) {
                    JsonObject castObj = castArray.get(i).getAsJsonObject();
                    String name = castObj.get("name").getAsString();
                    String character = castObj.has("character") ? castObj.get("character").getAsString() : "";
                    String profilePath = castObj.has("profile_path") && !castObj.get("profile_path").isJsonNull()
                            ? TMDB_IMAGE_BASE_URL + castObj.get("profile_path").getAsString()
                            : "";

                    Map<String, Object> castMember = new HashMap<>();
                    castMember.put("name", name);
                    castMember.put("character", character);
                    castMember.put("profile_path", profilePath);
                    castList.add(castMember);

                    if (i > 0)
                        castStr.append(", ");
                    castStr.append(name);
                }
                movie.put("cast", castStr.toString());
                movie.put("cast_list", castList);
            } else {
                movie.put("cast", "Unknown");
                movie.put("cast_list", new ArrayList<>());
            }

            // Director and crew
            if (movieObj.has("credits") && movieObj.getAsJsonObject("credits").has("crew")) {
                JsonArray crewArray = movieObj.getAsJsonObject("credits").getAsJsonArray("crew");
                Map<String, List<String>> crewByDepartment = new HashMap<>();

                for (JsonElement crewElement : crewArray) {
                    JsonObject crewObj = crewElement.getAsJsonObject();
                    String job = crewObj.has("job") ? crewObj.get("job").getAsString() : "";
                    String name = crewObj.get("name").getAsString();
                    String department = crewObj.has("department") ? crewObj.get("department").getAsString() : "";

                    if (job.equals("Director")) {
                        movie.put("director", name);
                    }

                    if (!department.isEmpty()) {
                        crewByDepartment.computeIfAbsent(department, k -> new ArrayList<>())
                                .add(name + " (" + job + ")");
                    }
                }

                movie.put("crew_by_department", crewByDepartment);

                if (!movie.containsKey("director")) {
                    movie.put("director", "Unknown");
                }
            } else {
                movie.put("director", "Unknown");
                movie.put("crew_by_department", new HashMap<>());
            }

            // Trailer
            if (movieObj.has("videos") && movieObj.getAsJsonObject("videos").has("results")) {
                JsonArray videosArray = movieObj.getAsJsonObject("videos").getAsJsonArray("results");
                List<Map<String, Object>> videosList = new ArrayList<>();

                // First find an official trailer
                String trailerKey = null;
                for (JsonElement videoElement : videosArray) {
                    JsonObject videoObj = videoElement.getAsJsonObject();
                    if (videoObj.has("type") && videoObj.get("type").getAsString().equals("Trailer") &&
                            videoObj.has("site") && videoObj.get("site").getAsString().equals("YouTube") &&
                            videoObj.has("official") && videoObj.get("official").getAsBoolean()) {
                        trailerKey = videoObj.get("key").getAsString();
                        break;
                    }
                }

                // If no official trailer, try any trailer
                if (trailerKey == null) {
                    for (JsonElement videoElement : videosArray) {
                        JsonObject videoObj = videoElement.getAsJsonObject();
                        if (videoObj.has("type") && videoObj.get("type").getAsString().equals("Trailer") &&
                                videoObj.has("site") && videoObj.get("site").getAsString().equals("YouTube")) {
                            trailerKey = videoObj.get("key").getAsString();
                            break;
                        }
                    }
                }

                // Collect all videos
                for (JsonElement videoElement : videosArray) {
                    JsonObject videoObj = videoElement.getAsJsonObject();
                    if (videoObj.has("site") && videoObj.get("site").getAsString().equals("YouTube")) {
                        Map<String, Object> video = new HashMap<>();
                        video.put("key", videoObj.get("key").getAsString());
                        video.put("name", videoObj.get("name").getAsString());
                        video.put("type", videoObj.get("type").getAsString());
                        video.put("official",
                                videoObj.has("official") ? videoObj.get("official").getAsBoolean() : false);
                        videosList.add(video);
                    }
                }

                movie.put("trailer_key", trailerKey);
                movie.put("videos", videosList);
            }

            // Similar movies (from recommendations)
            if (movieObj.has("recommendations") && movieObj.getAsJsonObject("recommendations").has("results")) {
                JsonArray recommendationsArray = movieObj.getAsJsonObject("recommendations").getAsJsonArray("results");
                List<Map<String, Object>> similarMovies = new ArrayList<>();

                int maxSimilarMovies = Math.min(6, recommendationsArray.size());
                for (int i = 0; i < maxSimilarMovies; i++) {
                    JsonObject recObj = recommendationsArray.get(i).getAsJsonObject();
                    Map<String, Object> similarMovie = new HashMap<>();

                    similarMovie.put("tmdb_id", recObj.get("id").getAsInt());
                    similarMovie.put("title", recObj.has("title") ? recObj.get("title").getAsString() : "Unknown");

                    if (recObj.has("poster_path") && !recObj.get("poster_path").isJsonNull()) {
                        similarMovie.put("poster_path", TMDB_IMAGE_BASE_URL + recObj.get("poster_path").getAsString());
                    } else {
                        similarMovie.put("poster_path", "");
                    }

                    similarMovies.add(similarMovie);
                }

                movie.put("similar_movies", similarMovies);
            } else {
                movie.put("similar_movies", new ArrayList<>());
            }

            return movie;
        } catch (Exception e) {
            System.out.println("Error getting movie details: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static List<Map<String, Object>> getSimilarMovies(int tmdbId) {
        try {
            String urlStr = TMDB_BASE_URL + "/movie/" + tmdbId + "/similar";
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
                conn.setConnectTimeout(10000); // Increased timeout to 10 seconds
                conn.setReadTimeout(10000); // Increased timeout to 10 seconds

                // Set Authorization header with Bearer token instead of using API key in URL
                conn.setRequestProperty("Authorization", "Bearer " + TMDB_API_TOKEN);
                conn.setRequestProperty("Content-Type", "application/json");

                System.out.println("Making API call to: " + urlStr);

                int responseCode = conn.getResponseCode();
                System.out.println("Response code: " + responseCode);

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
                    System.out.println("Rate limit hit, waiting to retry...");
                    if (attempt < maxRetries) {
                        Thread.sleep(retryDelayMs * attempt);
                        continue;
                    }
                } else {
                    // Try to get error message from response
                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        reader.close();
                        System.out.println("Error response: " + errorResponse.toString());
                    } catch (Exception e) {
                        System.out.println("Could not read error response: " + e.getMessage());
                    }
                }

                throw new IOException("HTTP error code: " + responseCode);
            } catch (Exception e) {
                System.out.println("Error in attempt " + attempt + ": " + e.getMessage());
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