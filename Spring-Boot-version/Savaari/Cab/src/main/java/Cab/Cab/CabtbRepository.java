package com.Cab.Cab;

import org.springframework.data.repository.Repository;

public interface CabtbRepository extends Repository<Cabtb,Long>{
    void saveAndFlush(Cabtb c);
    Cabtb findById(Long id);
    void deleteById(Long id);
    boolean existsById(Long id);
}