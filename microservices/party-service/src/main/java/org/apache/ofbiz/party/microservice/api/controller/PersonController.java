package org.apache.ofbiz.party.microservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ofbiz.party.microservice.api.dto.CreatePersonRequest;
import org.apache.ofbiz.party.microservice.api.dto.PersonDto;
import org.apache.ofbiz.party.microservice.application.service.PersonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
@Tag(name = "Persons", description = "Person management API")
public class PersonController {

    private final PersonService personService;

    @GetMapping
    @Operation(summary = "Get all persons", description = "Returns a list of all persons")
    public ResponseEntity<List<PersonDto>> getAll() {
        return ResponseEntity.ok(personService.findAll());
    }

    @GetMapping("/{partyId}")
    @Operation(summary = "Get person by ID", description = "Returns a single person by party ID")
    @ApiResponse(responseCode = "200", description = "Person found")
    @ApiResponse(responseCode = "404", description = "Person not found")
    public ResponseEntity<PersonDto> getById(
            @Parameter(description = "Party ID") @PathVariable String partyId) {
        return ResponseEntity.ok(personService.findById(partyId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search persons by name", description = "Search persons by first or last name")
    public ResponseEntity<List<PersonDto>> searchByName(
            @Parameter(description = "Name to search for") @RequestParam String name) {
        return ResponseEntity.ok(personService.searchByName(name));
    }

    @GetMapping("/by-lastname/{lastName}")
    @Operation(summary = "Find persons by last name", description = "Returns all persons with the given last name")
    public ResponseEntity<List<PersonDto>> getByLastName(
            @Parameter(description = "Last name") @PathVariable String lastName) {
        return ResponseEntity.ok(personService.findByLastName(lastName));
    }

    @PostMapping
    @Operation(summary = "Create a new person", description = "Creates a new person and returns the created entity")
    @ApiResponse(responseCode = "201", description = "Person created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<PersonDto> create(
            @Valid @RequestBody CreatePersonRequest request) {
        PersonDto created = personService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{partyId}")
    @Operation(summary = "Update a person", description = "Updates an existing person")
    @ApiResponse(responseCode = "200", description = "Person updated successfully")
    @ApiResponse(responseCode = "404", description = "Person not found")
    public ResponseEntity<PersonDto> update(
            @Parameter(description = "Party ID") @PathVariable String partyId,
            @Valid @RequestBody CreatePersonRequest request) {
        return ResponseEntity.ok(personService.update(partyId, request));
    }

    @DeleteMapping("/{partyId}")
    @Operation(summary = "Delete a person", description = "Deletes a person by party ID")
    @ApiResponse(responseCode = "204", description = "Person deleted successfully")
    @ApiResponse(responseCode = "404", description = "Person not found")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Party ID") @PathVariable String partyId) {
        personService.delete(partyId);
        return ResponseEntity.noContent().build();
    }
}
