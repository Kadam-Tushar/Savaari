package com.Cab.Cab;

import org.springframework.data.repository.Repository;

public interface CabCommitedRepository extends Repository<CabCommited,Long>{
    void saveAndFlush(CabCommited c);
    CabCommited findById(Long id);
    void deleteById(Long id);
    boolean existsById(Long id);
}