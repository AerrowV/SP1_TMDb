package dat.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dat.dto.MovieDTO;
import dat.dto.MovieResponseDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class FetchDanishMovies {

    private static final String apiKey = System.getenv("MOVIE_API_KEY");
    private static final String apiUrl = "https://api.themoviedb.org/3/discover/movie?include_adult=false&include_video=false&primary_release_date.gte=2020-01-01&primary_release_date.lte=2025-02-25&sort_by=popularity.desc&with_original_language=da&api_key=" + apiKey;

    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule());

    public static List<Long> fetchMovieIds() {
        List<Long> movieIds = new ArrayList<>();

        try {
            String jsonResponse = getDataFromUrl(apiUrl);

            if (jsonResponse != null) {
                MovieResponseDTO movieResponse = objectMapper.readValue(jsonResponse, MovieResponseDTO.class);

                for (MovieDTO movie : movieResponse.results) {
                    movieIds.add(movie.getId());
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
}
