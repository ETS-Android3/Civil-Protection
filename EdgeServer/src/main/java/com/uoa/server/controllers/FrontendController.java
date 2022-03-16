package com.uoa.server.controllers;

import com.uoa.server.DevicesJSONResponse;
import com.uoa.server.ServerApplication;
import com.uoa.server.services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
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
        return new DevicesJSONResponse(
                ServerApplication.androidDevices.devicesList,
                ServerApplication.iotDevices.devicesList,
                "SUCCESS");
    }

}