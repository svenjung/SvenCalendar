package net.fortuna.ical4j.transform.rfc5545;

import net.fortuna.ical4j.model.property.Attendee;

import java.net.URI;
import java.net.URISyntaxException;

public class AttendeePropertyRule implements Rfc5545PropertyRule<Attendee> {

    private static final String MAILTO = "mailto";
    private static final String APOSTROPHE = "'";
    private static final int MIN_LENGTH = 3;

    @Override
    public void applyTo(Attendee element) {
        if (element == null) {
            return;
        }
        URI calAddress = element.getCalAddress();
        if (calAddress == null) {
            return;
        }
        String scheme = calAddress.getScheme();
        if (scheme != null && (scheme.startsWith(MAILTO) || scheme.startsWith(MAILTO.toUpperCase()))) {
            String part = calAddress.getSchemeSpecificPart();
            if (part != null && part.length() >= MIN_LENGTH && part.startsWith(APOSTROPHE)
                    && part.endsWith(APOSTROPHE)) {
                String newPart = part.substring(1, part.length() - 1);
                safelySetNewValue(element, newPart);
            }
        }
    }

    private static void safelySetNewValue(Attendee element, String newPart) {
        try {
            element.setValue(MAILTO + ":" + newPart);
        } catch (URISyntaxException e) {

        }
    }

    @Override
    public Class<Attendee> getSupportedType() {
        return Attendee.class;
    }

}
