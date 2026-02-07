package org.apache.ofbiz.party.microservice.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ofbiz.party.microservice.api.dto.CreatePartyGroupRequest;
import org.apache.ofbiz.party.microservice.api.dto.PartyGroupDto;
import org.apache.ofbiz.party.microservice.application.service.PartyGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/party-groups")
@RequiredArgsConstructor
@Tag(name = "Party Groups", description = "Party Group (Organization) management API")
public class PartyGroupController {

    private final PartyGroupService partyGroupService;

    @GetMapping
    @Operation(summary = "Get all party groups", description = "Returns a list of all party groups/organizations")
    public ResponseEntity<List<PartyGroupDto>> getAll() {
        return ResponseEntity.ok(partyGroupService.findAll());
    }

    @GetMapping("/{partyId}")
    @Operation(summary = "Get party group by ID", description = "Returns a single party group by party ID")
    @ApiResponse(responseCode = "200", description = "Party group found")
    @ApiResponse(responseCode = "404", description = "Party group not found")
    public ResponseEntity<PartyGroupDto> getById(
            @Parameter(description = "Party ID") @PathVariable String partyId) {
        return ResponseEntity.ok(partyGroupService.findById(partyId));
    }

    @GetMapping("/search")
    @Operation(summary = "Search party groups by name", description = "Search party groups by group name")
    public ResponseEntity<List<PartyGroupDto>> searchByName(
            @Parameter(description = "Name to search for") @RequestParam String name) {
        return ResponseEntity.ok(partyGroupService.searchByGroupName(name));
    }

    @PostMapping
    @Operation(summary = "Create a new party group", description = "Creates a new party group/organization")
    @ApiResponse(responseCode = "201", description = "Party group created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<PartyGroupDto> create(
            @Valid @RequestBody CreatePartyGroupRequest request) {
        PartyGroupDto created = partyGroupService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{partyId}")
    @Operation(summary = "Update a party group", description = "Updates an existing party group")
    @ApiResponse(responseCode = "200", description = "Party group updated successfully")
    @ApiResponse(responseCode = "404", description = "Party group not found")
    public ResponseEntity<PartyGroupDto> update(
            @Parameter(description = "Party ID") @PathVariable String partyId,
            @Valid @RequestBody CreatePartyGroupRequest request) {
        return ResponseEntity.ok(partyGroupService.update(partyId, request));
    }

    @DeleteMapping("/{partyId}")
    @Operation(summary = "Delete a party group", description = "Deletes a party group by party ID")
    @ApiResponse(responseCode = "204", description = "Party group deleted successfully")
    @ApiResponse(responseCode = "404", description = "Party group not found")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Party ID") @PathVariable String partyId) {
        partyGroupService.delete(partyId);
        return ResponseEntity.noContent().build();
    }
}
