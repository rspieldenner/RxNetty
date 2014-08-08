package io.reactivex.netty.metrics;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.netty.metrics.MetricsEvent.MetricEventType;

/**
 * @author Nitesh Kant
 */
public class EventInvocationsStore<E extends Enum<E> & MetricEventType> implements MetricEventsListener<MetricsEvent<E>> {

    private final EnumMap<E, Integer> eventTypeVsInvocations;
    private final EnumMap<E, List<String>> eventTypeVsInvalidInvocations;

    public EventInvocationsStore(Class<E> type) {
        eventTypeVsInvocations = new EnumMap<E, Integer>(type);
        eventTypeVsInvalidInvocations = new EnumMap<E, List<String>>(type);
    }

    public EnumMap<E, Integer> getEventTypeVsInvocations() {
        return eventTypeVsInvocations;
    }

    public EnumMap<E, List<String>> getEventTypeVsInvalidInvocations() {
        return eventTypeVsInvalidInvocations;
    }

    @Override
    public void onEvent(MetricsEvent<E> event, long duration, TimeUnit timeUnit, Throwable throwable, Object value) {
        E type = event.getType();
        boolean isError = type.isError();
        boolean isTimed = type.isTimed();
        Class<?> optionalDataType = type.getOptionalDataType();

        Integer existing = eventTypeVsInvocations.get(type);
        if (null == existing) {
            eventTypeVsInvocations.put(type, 1);
        } else {
            eventTypeVsInvocations.put(type, existing + 1);
        }

        List<String> errors = eventTypeVsInvalidInvocations.get(type);
        if (null == errors) {
            errors = new ArrayList<String>();
        }

        if (isError && NO_ERROR == throwable) {
            errors.add("No error passed.");
        } else if (!isError && NO_ERROR != throwable) {
            errors.add("Error passed for non-error event.");
        }

        if (isTimed) {
            if (NO_DURATION == duration) {
                errors.add("No duration provided for timed event.");
            }

            if (NO_TIME_UNIT == timeUnit) {
                errors.add("No time unit provided for timed event.");
            }
        } else {
            if (NO_DURATION != duration) {
                errors.add("Duration provided for non-timed event.");
            }
            if (NO_TIME_UNIT != timeUnit) {
                errors.add("Timeunit provided for non-timed event.");
            }
        }

        if (optionalDataType != null && optionalDataType != Void.class) {
            if (NO_VALUE == value) {
                errors.add("No value provided for the event.");
            } else if (value.getClass() != type.getOptionalDataType()) {
                errors.add("Invalid value provided. Expected: " + type.getOptionalDataType() + ", got: "
                        + value.getClass());
            }
        }

        if (!errors.isEmpty()) {
            eventTypeVsInvalidInvocations.put(type, errors);
        }
    }

    @Override
    public void onCompleted() {
    }

    @Override
    public void onSubscribe() {
    }
}
