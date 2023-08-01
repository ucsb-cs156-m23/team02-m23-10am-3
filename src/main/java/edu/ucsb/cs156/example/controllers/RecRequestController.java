package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.RecRequest;
import edu.ucsb.cs156.example.entities.UCSBDiningCommons;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecRequestRepository;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsRepository;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import javax.validation.Valid;

@Tag(name = "RecRequest")
@RequestMapping("/api/recommendationrequest")
@RestController
@Slf4j
public class RecRequestController extends ApiController {

    @Autowired
    RecRequestRepository recRequestRepository;
    UCSBDiningCommonsRepository ucsbDiningCommonsRepository;

    @Operation(summary= "List all recommendation requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<RecRequest> allRecommendations() {
        Iterable<RecRequest> recommendations = recRequestRepository.findAll();
        return recommendations;
    }

    @Operation(summary= "Get a single recommendation")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public RecRequest getById(
            @Parameter(name="id") @RequestParam Long id) {
        RecRequest recommendation = recRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(RecRequest.class, id));

        return recommendation;
    }

    @Operation(summary= "Create a new recommendation")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public RecRequest postCommons(
        @Parameter(name="requesterEmail", description="person' email who is requesting a recommendation", example="ryanhe@ucsb.edu") @RequestParam String requesterEmail,
        @Parameter(name="professorEmail", description="professor's email who you want a recommendation from", example="phtcon@ucsb.edu") @RequestParam String professorEmail,
        @Parameter(name="explanation", description="what you need the recommendation for and some other details", example="For BS/MS program") @RequestParam String explanation,
        @Parameter(name="dateRequested", description="The date the recommendation was requestion in ISO-8601", example="2022-01-03T00:00:00") @RequestParam LocalDateTime dateRequested,
        @Parameter(name="dateNeeded", description="The date of when the recommendation is needed by in ISO-8601", example="2022-01-03T00:00:00") @RequestParam LocalDateTime dateNeeded,
        @Parameter(name="done", description="If the recommendation has been sent", example="true") @RequestParam boolean done
        )
        {

        RecRequest recommendation = new RecRequest();
        recommendation.setRequesterEmail(requesterEmail);
        recommendation.setProfessorEmail(professorEmail);
        recommendation.setExplanation(explanation);
        recommendation.setDateRequested(dateRequested);
        recommendation.setDateNeeded(dateNeeded);
        recommendation.setDone(done);

        RecRequest savedRecommendation = recRequestRepository.save(recommendation);

        return savedRecommendation;
    }

    // @Operation(summary= "Delete a UCSBDiningCommons")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @DeleteMapping("")
    // public Object deleteCommons(
    //         @Parameter(name="code") @RequestParam String code) {
    //     UCSBDiningCommons commons = ucsbDiningCommonsRepository.findById(code)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommons.class, code));

    //     ucsbDiningCommonsRepository.delete(commons);
    //     return genericMessage("UCSBDiningCommons with id %s deleted".formatted(code));
    // }

    // @Operation(summary= "Update a single commons")
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    // @PutMapping("")
    // public UCSBDiningCommons updateCommons(
    //         @Parameter(name="code") @RequestParam String code,
    //         @RequestBody @Valid UCSBDiningCommons incoming) {

    //     UCSBDiningCommons commons = ucsbDiningCommonsRepository.findById(code)
    //             .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommons.class, code));


    //     commons.setName(incoming.getName());  
    //     commons.setHasSackMeal(incoming.getHasSackMeal());
    //     commons.setHasTakeOutMeal(incoming.getHasTakeOutMeal());
    //     commons.setHasDiningCam(incoming.getHasDiningCam());
    //     commons.setLatitude(incoming.getLatitude());
    //     commons.setLongitude(incoming.getLongitude());

    //     ucsbDiningCommonsRepository.save(commons);

    //     return commons;
    // }
}

