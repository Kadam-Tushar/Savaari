package com.RideService.RideService;

import org.springframework.data.repository.Repository;

public interface RidetbRepository extends Repository<Ridetb,Long>{
    Ridetb saveAndFlush(Ridetb c);
    Ridetb findById(Long id);
    void deleteById(Long id);
    boolean existsById(Long id);
}