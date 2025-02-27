package dat.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dat.dto.*;
import dat.entities.Actor;
import dat.entities.Director;
import dat.entities.Genre;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

import static dat.services.DTOMapper.*;

public class FetchDanishMovies {

    private static final String apiKey = System.getenv("MOVIE_API_KEY");
    private static final String apiUrl = "https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&primary_release_date.gte=2020-01-01&primary_release_date.lte=2025-02-25&sort_by=popularity.desc&with_original_language=da&api_key=" + apiKey;

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    public static List<Long> fetchMovieIds() {
        List<Long> movieIds = new ArrayList<>();
        String urlWithFirstPage = apiUrl + "&page=1";

        try {
            String firstPageResponse = getDataFromUrl(urlWithFirstPage);
            if (firstPageResponse == null) {
                return movieIds;
            }

            MovieResponseDTO firstMovieResponse =
                    objectMapper.readValue(firstPageResponse, MovieResponseDTO.class);

            for (MovieDTO movie : firstMovieResponse.results) {
                movieIds.add(movie.getId());
            }

            int totalPages = firstMovieResponse.total_pages;

            for (int page = 2; page <= totalPages; page++) {
                String urlWithPage = apiUrl + "&page=" + page;
                String jsonResponse = getDataFromUrl(urlWithPage);

                if (jsonResponse != null) {
                    MovieResponseDTO movieResponse =
                            objectMapper.readValue(jsonResponse, MovieResponseDTO.class);
                    for (MovieDTO movie : movieResponse.results) {
                        movieIds.add(movie.getId());
                    }
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return movieIds;
    }

    private static String getDataFromUrl(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                System.out.println("GET request failed. Status code: " + response.statusCode());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MovieDTO fetchMovieDetails(Long movieId) {
        String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;

        try {
            String jsonResponse = getDataFromUrl(url);
            if (jsonResponse != null) {
                return objectMapper.readValue(jsonResponse, MovieDTO.class);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Actor> fetchActorDetails(Long movieId) {
        String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;

        try {
            String jsonResponse = getDataFromUrl(url);
            ActorResponseDTO response = objectMapper.readValue(jsonResponse, ActorResponseDTO.class);
            List<Actor> actors = new ArrayList<>();
            for (ActorDTO actorDTO : response.getActors()) {
                actors.add(actorToEntity(actorDTO));
            }
            return actors;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Collection<Genre> fetchGenreDetails(Long movieId) {
        String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;

        try {
            String jsonResponse = getDataFromUrl(url);
            if (jsonResponse != null) {
                GenreResponseDTO response = objectMapper.readValue(jsonResponse, GenreResponseDTO.class);
                Collection<Genre> genres = new HashSet<>();
                for (GenreDTO genreDTO : response.getGenres()) {
                    genres.add(genreToEntity(genreDTO));
                }
                return genres;
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static Director fetchDirectorDetails(Long movieId) {
        String url = "https://api.themoviedb.org/3/movie/" + movieId + "?api_key=" + apiKey;

        try {
            String jsonResponse = getDataFromUrl(url);
            if (jsonResponse != null) {
                DirectorResponseDTO response = objectMapper.readValue(jsonResponse, DirectorResponseDTO.class);
                DirectorDTO directorDTO = response.getDirector();
                return directorToEntity(directorDTO);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void printAllMovieDetails() {
        List<Long> movieIds = fetchMovieIds();

        for (Long movieId : movieIds) {
            MovieDTO movie = fetchMovieDetails(movieId);
            if (movie != null) {
                System.out.println("Movie ID: " + movie.getId());
                System.out.println("Title: " + movie.getTitle());
                System.out.println("Release Date: " + movie.getReleaseDate());
                System.out.println("Overview: " + movie.getOverview());
                System.out.println("-----------------------------");
            }
        }
    }
}
