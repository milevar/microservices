package com.appdeveloperblog.store.ProductsService.command.rest;

import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.springboot.autoconfig.EventProcessingAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/management")
public class EventsReplayContoller {

    @Autowired
    private EventProcessingConfiguration eventProcessingConfiguration;

    @PostMapping("/eventProcessor/{processorName/reset}")
    public ResponseEntity<String> replayEvents(@PathVariable String processorName) {
       Optional<TrackingEventProcessor> trackingEventProcessor =
               eventProcessingConfiguration.eventProcessor(processorName, TrackingEventProcessor.class);
       if (trackingEventProcessor.isPresent()) {
           TrackingEventProcessor eventProcessor = trackingEventProcessor.get();
           eventProcessor.shutDown();
           eventProcessor.resetTokens();
           eventProcessor.start();

           return ResponseEntity.ok()
                   .body(String.format("The event Processor with a name [%] has been reset", processorName));

       } else {

           return ResponseEntity.badRequest()
                   .body(String.format("The event Processor with a name [%] is not a tracking event processor"
                           + " Only tracking event processor is supported " , processorName));
       }
    }
}
