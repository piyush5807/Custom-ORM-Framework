package com.hibernate.reflections;

import java.util.Collection;
import java.util.Optional;

public interface Repository<T, ID> {

     Collection<T> findAll() throws Exception;

     Optional<T> findById(ID id) throws Exception;

     void  deleteById(ID id) throws Exception;

     T save(T obj) throws Exception;

     Collection<T> saveAll(Collection<T> items) throws Exception;
}
