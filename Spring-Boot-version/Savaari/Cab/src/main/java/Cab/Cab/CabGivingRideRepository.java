package com.Cab.Cab;

import org.springframework.data.repository.Repository;

public interface CabGivingRideRepository extends Repository<CabGivingRide,Long>{
    void saveAndFlush(CabGivingRide c);
    CabGivingRide findById(Long id);
    void deleteById(Long id);
    boolean existsById(Long id);
}