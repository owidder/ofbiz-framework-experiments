/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.
 */
package org.apache.ofbiz.party.microservice.domain.repository;

import org.apache.ofbiz.party.microservice.domain.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonRepository extends JpaRepository<Person, String> {

    List<Person> findByLastName(String lastName);

    List<Person> findByFirstName(String firstName);

    List<Person> findByFirstNameAndLastName(String firstName, String lastName);

    @Query("SELECT p FROM Person p WHERE LOWER(p.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(p.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Person> searchByName(@Param("name") String name);

    @Query("SELECT p FROM Person p WHERE p.memberId = :memberId")
    List<Person> findByMemberId(@Param("memberId") String memberId);

    @Query("SELECT p FROM Person p WHERE p.gender = :gender")
    List<Person> findByGender(@Param("gender") String gender);
}
