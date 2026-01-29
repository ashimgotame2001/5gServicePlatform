package com.service.locationservice.service;

import com.service.shared.dto.GlobalResponse;
import com.service.shared.dto.request.CreateGeofencingSubscriptionDTO;

public interface GeofencingSubscriptionService {

    /**
     * Create a geofencing subscription
     * 
     * @param request Geofencing subscription request
     * @return Subscription creation result
     */
    GlobalResponse createGeofencingSubscription(CreateGeofencingSubscriptionDTO request);

    /**
     * Get all geofencing subscriptions
     * 
     * @return List of all subscriptions
     */
    GlobalResponse getAllGeofencingSubscriptions();

    /**
     * Get geofencing subscription by ID
     * 
     * @param subscriptionId Subscription ID
     * @return Subscription details
     */
    GlobalResponse getGeofencingSubscriptionById(String subscriptionId);

    /**
     * Delete geofencing subscription by ID
     * 
     * @param subscriptionId Subscription ID
     * @return Deletion result
     */
    GlobalResponse deleteGeofencingSubscription(String subscriptionId);
}
