package com.uoa.server.services;

import com.uoa.server.models.EventModel;
import com.uoa.server.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    @Autowired
    EventRepository repository;

    public EventModel save(EventModel eventModel) {
        return repository.save(eventModel);
    }

    public List<EventModel> findAll() {
        return repository.findAll();
    }

}