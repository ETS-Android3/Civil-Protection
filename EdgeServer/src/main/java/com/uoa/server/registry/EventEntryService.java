package com.uoa.server.registry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventEntryService {

    @Autowired
    EventEntryRepository repository;

    public EventEntry save(EventEntry eventEntry) {
        return repository.save(eventEntry);
    }

    public List<EventEntry> findAll() {
        return repository.findAll();
    }

}
