package org.telegram.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SubscriptionService {

    private final Map<Long, String> subscriptions = new HashMap<>();

    public void addSubscription(String subscriptionDetails) {
        long id = generateId();
        subscriptions.put(id, subscriptionDetails);
    }

    public String getSubscriptionStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Всего абонементов: ").append(subscriptions.size()).append("\n");
        for (Map.Entry<Long, String> entry : subscriptions.entrySet()) {
            stats.append("ID: ").append(entry.getKey()).append(", Детали: ").append(entry.getValue()).append("\n");
        }
        return stats.toString();
    }

    private long generateId() {
        return subscriptions.size() + 1;
    }
}
