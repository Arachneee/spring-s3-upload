package org.example.springs3upload.s3.async;

public record ImageNameAppResponse(String name) {

    public EventImage toEventImage(Event event) {
        return new EventImage(event, name);
    }
}
