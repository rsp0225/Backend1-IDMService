package com.github.klefstad_teaching.cs122b.movies.rest;

import com.github.klefstad_teaching.cs122b.core.result.MoviesResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.github.klefstad_teaching.cs122b.movies.Request.MovieSearchRequest;
import com.github.klefstad_teaching.cs122b.movies.Response.MovieSearchResponse;
import com.github.klefstad_teaching.cs122b.movies.repo.MovieRepo;
import com.github.klefstad_teaching.cs122b.movies.util.Validate;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.html.Option;
import java.text.ParseException;
import java.util.List;

@RestController
public class MovieController
{
    private final MovieRepo repo;
    private final Validate validate;

    @Autowired
    public MovieController(MovieRepo repo, Validate validate)
    {
        this.repo = repo;
        this.validate = validate;
    }

    @GetMapping("/movie/search")
    public ResponseEntity<MovieSearchResponse> movieSearch(@AuthenticationPrincipal SignedJWT correctUser, MovieSearchRequest movieSearchRequest) {
        List<String> roles;
        try {
            roles = correctUser.getJWTClaimsSet().getStringListClaim(JWTManager.CLAIM_ROLES);
        }
        catch (ParseException exc) {
            System.out.println(exc);
            return null;
        }

        boolean option = false;
        if(roles.contains("ADMIN") || roles.contains("EMPLOYEE")){
            option = true;

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MovieSearchResponse()
                        .setResult(MoviesResults.MOVIES_FOUND_WITHIN_SEARCH)
                        .setMovies(repo.movieSearch(movieSearchRequest, option)));
    }
}
