# Movie Streaming App

A Java AWT application for browsing, searching, and streaming movies using the TMDb API.

## Features

- **Browse Movies**: View popular, top-rated, and latest movies
- **Advanced Search**: Search movies by title with filters
- **Filtering Options**: Filter movies by genre, year, and rating
- **Sort Options**: Sort movies by popularity, rating, release date, or title
- **Movie Details**: View comprehensive movie information including:
  - Title, year, rating, runtime
  - Cast and director information
  - Movie overview
  - Trailer links
  - Similar movie recommendations
- **Streaming**: Watch movies by opening streams in your default browser
- **Trailers**: Watch movie trailers on YouTube

## Requirements

- Java 8 or higher
- Internet connection for TMDb API access

## Setup and Running

1. Download the project
2. Make sure you have the required libraries:
   - Run `download-gson.bat` to download the Gson library (Windows)
   - For Linux/Mac users, download Gson v2.10.1 and place it in the `lib/` folder
3. Compile the project:
   - Windows: Run `build.bat`
   - Linux/Mac: Run `build.sh`
4. Choose option 1 to run the application

## Usage

1. **Login**: Use any username/password for demonstration purposes
2. **Browse Movies**: The home screen shows popular movies
3. **Search**: Enter a movie title in the search box and click "Search"
4. **Filter**: Use the dropdown menus to filter movies by genre, year, and rating
5. **Sort**: Use the "Sort By" dropdown to change how movies are ordered
6. **Movie Details**: Click on a movie card or the "Details" button to view detailed information
7. **Play Movie**: Click "Watch Now" on the details page or the "Play" button on a movie card
8. **Watch Trailer**: If available, click "Watch Trailer" to watch on YouTube
9. **Similar Movies**: Scroll to the bottom of a movie's detail page to find similar movie recommendations

## Architecture

The application uses a simple but effective architecture:

- Java AWT for the user interface
- TMDb API for movie data
- Gson for JSON parsing
- WebPlayer approach for streaming via vidsrc.to

## Credits

- Movie data provided by [The Movie Database (TMDb)](https://www.themoviedb.org/)
- This product uses the TMDb API but is not endorsed or certified by TMDb
- Streaming links provided by vidsrc.to

## License

This project is intended for educational purposes only. Movie streaming functionality should be used in accordance with copyright laws in your region.

## Note

The application doesn't store any movie files locally. All streaming is done via external websites in a web browser.
