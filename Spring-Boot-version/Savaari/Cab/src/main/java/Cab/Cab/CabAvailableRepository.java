package com.Cab.Cab;

import org.springframework.data.repository.Repository;

public interface CabAvailableRepository extends Repository<CabAvailable,Long>{
    void saveAndFlush(CabAvailable c);
    CabAvailable findById(Long id);
    void deleteById(Long id);
    boolean existsById(Long id);
}