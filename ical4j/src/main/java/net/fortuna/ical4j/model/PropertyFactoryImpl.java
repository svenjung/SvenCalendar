/**
 * Copyright (c) 2012, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.ical4j.model;

import net.fortuna.ical4j.model.property.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * A factory for creating iCalendar properties. Note that if relaxed parsing is enabled (via specifying the system
 * property: icalj.parsing.relaxed=true) illegal property names are allowed.
 *
 * @author Ben Fortuna
 *         <p/>
 *         $Id$ [05-Apr-2004]
 */
public class PropertyFactoryImpl extends AbstractContentFactory<PropertyFactory> {

    private static final long serialVersionUID = -7174232004486979641L;

    /**
     * Constructor made private to prevent instantiation.
     */
    protected PropertyFactoryImpl() {

        registerFactory(Property.ACKNOWLEDGED,      new Acknowledged.Factory());
        registerFactory(Property.ACTION,            new Action.Factory());
        registerFactory(Property.ATTACH,            new Attach.Factory());
        registerFactory(Property.ATTENDEE,          new Attendee.Factory());
        registerFactory(Property.BUSYTYPE,          new BusyType.Factory());
        registerFactory(Property.CALSCALE,          new CalScale.Factory());
        registerFactory(Property.CATEGORIES,        new Categories.Factory());
        registerFactory(Property.CLASS,             new Clazz.Factory());
        registerFactory(Property.COMMENT,           new Comment.Factory());
        registerFactory(Property.COMPLETED,         new Completed.Factory());
        registerFactory(Property.CONTACT,           new Contact.Factory());
        registerFactory(Property.COUNTRY,           new Country.Factory());
        registerFactory(Property.CREATED,           new Created.Factory());
        registerFactory(Property.DESCRIPTION,       new Description.Factory());
        registerFactory(Property.DTEND,             new DtEnd.Factory());
        registerFactory(Property.DTSTAMP,           new DtStamp.Factory());
        registerFactory(Property.DTSTART,           new DtStart.Factory());
        registerFactory(Property.DUE,               new Due.Factory());
        registerFactory(Property.DURATION,          new Duration.Factory());
        registerFactory(Property.EXDATE,            new ExDate.Factory());
        registerFactory(Property.EXRULE,            new ExRule.Factory());
        registerFactory(Property.EXTENDED_ADDRESS,  new ExtendedAddress.Factory());
        registerFactory(Property.FREEBUSY,          new FreeBusy.Factory());
        registerFactory(Property.GEO,               new Geo.Factory());
        registerFactory(Property.LAST_MODIFIED,     new LastModified.Factory());
        registerFactory(Property.LOCALITY,          new Locality.Factory());
        registerFactory(Property.LOCATION,          new Location.Factory());
        registerFactory(Property.LOCATION_TYPE,     new LocationType.Factory());
        registerFactory(Property.METHOD,            new Method.Factory());
        registerFactory(Property.NAME,              new Name.Factory());
        registerFactory(Property.ORGANIZER,         new Organizer.Factory());
        registerFactory(Property.PERCENT_COMPLETE,  new PercentComplete.Factory());
        registerFactory(Property.POSTALCODE,        new Postalcode.Factory());
        registerFactory(Property.PRIORITY,          new Priority.Factory());
        registerFactory(Property.PRODID,            new ProdId.Factory());
        registerFactory(Property.RDATE,             new RDate.Factory());
        registerFactory(Property.RECURRENCE_ID,     new RecurrenceId.Factory());
        registerFactory(Property.REGION,            new Region.Factory());
        registerFactory(Property.RELATED_TO,        new RelatedTo.Factory());
        registerFactory(Property.REPEAT,            new Repeat.Factory());
        registerFactory(Property.REQUEST_STATUS,    new RequestStatus.Factory());
        registerFactory(Property.RESOURCES,         new Resources.Factory());
        registerFactory(Property.RRULE,             new RRule.Factory());
        registerFactory(Property.SEQUENCE,          new Sequence.Factory());
        registerFactory(Property.STATUS,            new Status.Factory());
        registerFactory(Property.STREET_ADDRESS,    new StreetAddress.Factory());
        registerFactory(Property.SUMMARY,           new Summary.Factory());
        registerFactory(Property.TEL,               new Tel.Factory());
        registerFactory(Property.TRANSP,            new Transp.Factory());
        registerFactory(Property.TRIGGER,           new Trigger.Factory());
        registerFactory(Property.TZID,              new TzId.Factory());
        registerFactory(Property.TZNAME,            new TzName.Factory());
        registerFactory(Property.TZOFFSETFROM,      new TzOffsetFrom.Factory());
        registerFactory(Property.TZOFFSETTO,        new TzOffsetTo.Factory());
        registerFactory(Property.TZURL,             new TzUrl.Factory());
        registerFactory(Property.UID,               new Uid.Factory());
        registerFactory(Property.URL,               new Url.Factory());
        registerFactory(Property.VERSION,           new Version.Factory());
    }

    /**
     * {@inheritDoc}
     */
    public Property createProperty(final String name) {
        final PropertyFactory factory = getFactory(name);
        if (factory != null) {
            return factory.createProperty();
        } else if (isExperimentalName(name)) {
            return new XProperty(name);
        } else if (allowIllegalNames()) {
            return new XProperty(name);
        } else {
            throw new IllegalArgumentException("Illegal property [" + name
                    + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    public Property createProperty(final String name,
                                   final ParameterList parameters, final String value)
            throws IOException, URISyntaxException, ParseException {

        final PropertyFactory factory = getFactory(name);
        if (factory != null) {
            return factory.createProperty(parameters, value);
        } else if (isExperimentalName(name)) {
            return new XProperty(name, parameters, value);
        } else if (allowIllegalNames()) {
            return new XProperty(name, parameters, value);
        } else {
            throw new IllegalArgumentException("Illegal property [" + name
                    + "]");
        }
    }

    private boolean isExperimentalName(final String name) {
        return name.startsWith(Property.EXPERIMENTAL_PREFIX)
                && name.length() > Property.EXPERIMENTAL_PREFIX.length();
    }

}
