package com.uoa.server.repositories;

import com.uoa.server.models.EventModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository<EventModel, Long> {
    List<EventModel> findAll();
}