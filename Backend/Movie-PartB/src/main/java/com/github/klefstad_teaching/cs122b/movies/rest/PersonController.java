package com.github.klefstad_teaching.cs122b.movies.rest;

import com.github.klefstad_teaching.cs122b.core.result.MoviesResults;
import com.github.klefstad_teaching.cs122b.core.security.JWTManager;
import com.github.klefstad_teaching.cs122b.movies.Request.MovieSearchRequest;
import com.github.klefstad_teaching.cs122b.movies.Request.PersonSearchRequest;
import com.github.klefstad_teaching.cs122b.movies.Response.MovieSearchResponse;
import com.github.klefstad_teaching.cs122b.movies.Response.PersonSeachIdResponse;
import com.github.klefstad_teaching.cs122b.movies.Response.PersonSearchResponse;
import com.github.klefstad_teaching.cs122b.movies.repo.MovieRepo;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.List;

@RestController
public class PersonController
{
    private final MovieRepo repo;

    @Autowired
    public PersonController(MovieRepo repo)
    {
        this.repo = repo;
    }

    @GetMapping("/person/search")
    public ResponseEntity<PersonSearchResponse> personSearch(PersonSearchRequest personSearchRequest, @AuthenticationPrincipal SignedJWT correctUser) {
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
                .body(new PersonSearchResponse()
                        .setResult(MoviesResults.PERSONS_FOUND_WITHIN_SEARCH)
                        .setPersons(repo.personSearch(personSearchRequest, option)));
    }

    @GetMapping("/person/{personId}")
    public ResponseEntity<PersonSeachIdResponse> personSearchId(@PathVariable Long personId, PersonSearchRequest personSearchRequest, @AuthenticationPrincipal SignedJWT correctUser) {
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
                .body(new PersonSeachIdResponse()
                        .setResult(MoviesResults.PERSON_WITH_ID_FOUND)
                        .setPerson(repo.personSearchId(personId, personSearchRequest, option)));
    }
}
