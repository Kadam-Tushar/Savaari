package com.RideService.RideService;

import org.springframework.data.repository.Repository;

public interface DummyRepository extends Repository<Dummy,Boolean>{
    Dummy findById(Boolean id);
    void saveAndFlush(Dummy d);
}