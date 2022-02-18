package com.wallet.wallet;

import org.springframework.data.repository.Repository;
import org.springframework.data.jpa.repository.Lock;
import javax.persistence.LockModeType; 

public interface CustomerRepository extends Repository<Customer,Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    void saveAndFlush(Customer c);
    Customer findById(Long id);
    void deleteById(Long id);
    boolean existsById(Long id);

    Iterable<Customer> findAll();
}