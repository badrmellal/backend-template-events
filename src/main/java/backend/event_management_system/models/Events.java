package backend.event_management_system.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Date;


@Entity
public class Events {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String eventName;
    private String eventDescription;
    private String eventImage;
    private String eventVideo;
    private float eventPrice;
    private String eventManagerUsername;
    private Date eventDate;
    private String addressLocation;
    private String googleMapsUrl;


    public Events(Long id, String eventName, String eventDescription, String eventImage, String eventVideo, float eventPrice, String eventManagerUsername, Date eventDate, String addressLocation, String googleMapsUrl) {
        this.id = id;
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.eventImage = eventImage;
        this.eventVideo = eventVideo;
        this.eventPrice = eventPrice;
        this.eventManagerUsername = eventManagerUsername;
        this.eventDate = eventDate;
        this.addressLocation = addressLocation;
        this.googleMapsUrl = googleMapsUrl;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventImage() {
        return eventImage;
    }

    public void setEventImage(String eventImage) {
        this.eventImage = eventImage;
    }

    public String getEventVideo() {
        return eventVideo;
    }

    public void setEventVideo(String eventVideo) {
        this.eventVideo = eventVideo;
    }

    public float getEventPrice() {
        return eventPrice;
    }

    public void setEventPrice(float eventPrice) {
        this.eventPrice = eventPrice;
    }

    public String getEventManagerUsername() {
        return eventManagerUsername;
    }

    public void setEventManagerUsername(String eventManagerUsername) {
        this.eventManagerUsername = eventManagerUsername;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public String getAddressLocation() {
        return addressLocation;
    }

    public void setAddressLocation(String addressLocation) {
        this.addressLocation = addressLocation;
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl;
    }

    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }
}
