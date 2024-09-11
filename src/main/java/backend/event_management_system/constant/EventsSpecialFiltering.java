package backend.event_management_system.constant;

import backend.event_management_system.models.Events;
import backend.event_management_system.service.FilteredEvents;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EventsSpecialFiltering {

    public static Specification<Events> filterBy(FilteredEvents filteredEvents){
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("isApproved"), true));

            if (filteredEvents.getEventName() != null){
                predicates.add(criteriaBuilder.like(root.get("eventName"), "%" + filteredEvents.getEventName() + "%"));
            }
            if (filteredEvents.getEventCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("eventCategory"), filteredEvents.getEventCategory()));
            }
            if (filteredEvents.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), filteredEvents.getStartDate()));
            }
            if (filteredEvents.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), filteredEvents.getEndDate()));
            }
            if (filteredEvents.getLocation() != null) {
                predicates.add(criteriaBuilder.equal(root.get("addressLocation"), filteredEvents.getLocation()));
            }
            if (filteredEvents.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventPrice"), filteredEvents.getMinPrice()));
            }
            if (filteredEvents.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventPrice"), filteredEvents.getMaxPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
