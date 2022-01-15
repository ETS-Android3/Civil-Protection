package com.uoa.server.registry;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventEntryRepository extends CrudRepository<EventEntry, Long> {
    List<EventEntry> findAll();
}
