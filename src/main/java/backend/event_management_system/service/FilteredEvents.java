package backend.event_management_system.service;

import java.util.Date;

public class FilteredEvents {

    private String eventName;
    private String eventCategory;
    private Date startDate;
    private Date endDate;
    private String location;
    private Double minPrice;
    private Double maxPrice;

    public FilteredEvents(String eventName, String eventCategory, Date startDate, Date endDate, String location, Double minPrice, Double maxPrice) {
        this.eventName = eventName;
        this.eventCategory = eventCategory;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }
}
