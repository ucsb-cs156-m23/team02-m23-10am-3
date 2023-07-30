package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.RecRequest;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RecRequestRepository extends CrudRepository<RecRequest, Long> {
    
}