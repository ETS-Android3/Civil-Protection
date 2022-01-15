package com.uoa.server.controllers;

import com.uoa.server.DevicesJSONResponse;
import com.uoa.server.IoTDevice;
import com.uoa.server.ServerApplication;
import com.uoa.server.models.EventModel;
import com.uoa.server.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class FrontendController {

    @Autowired
    EventService eventService;

    @GetMapping("/")
    public ModelAndView index () {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @GetMapping("/map")
    public ModelAndView map () {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @CrossOrigin(origins = "http://localhost:5500/")
    @GetMapping("/api/devices/")
    public DevicesJSONResponse getAllDevices () {
        DevicesJSONResponse response = new DevicesJSONResponse();
        response.setAndroidDevicesList(ServerApplication.androidDevices.devicesList);
        response.setIotDevicesList(ServerApplication.iotDevices.devicesList);
        response.setStatus("success");
        return response;
    }

    @GetMapping("/api/events/add")
    public EventModel addEvent () {
        EventModel eventModel = new EventModel("30/05/11 21:00:00", 31.2, 45.4, IoTDevice.DANGER_LEVEL_HIGH, null, null, null, null, null);
        return eventService.save(eventModel);
    }

}