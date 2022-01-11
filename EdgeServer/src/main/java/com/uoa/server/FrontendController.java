package com.uoa.server;

import com.uoa.server.registry.EventEntry;
import com.uoa.server.registry.EventEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class FrontendController {

    @Autowired
    EventEntryService eventEntryService;

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
    public EventEntry addEvent () {
        EventEntry eventEntry = new EventEntry("30/05/11 21:00:00", 31.2, 45.4, IoTDevice.DANGER_LEVEL_HIGH, null, null, null, null, null);
        return eventEntryService.save(eventEntry);
    }

}