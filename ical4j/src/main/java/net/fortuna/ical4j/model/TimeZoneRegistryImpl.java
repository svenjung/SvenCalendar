/**
 * Copyright (c) 2012, Ben Fortuna
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * <p>
 * o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * <p>
 * o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * <p>
 * o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * <p>
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

import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.TzUrl;
import net.fortuna.ical4j.util.CompatibilityHints;
import net.fortuna.ical4j.util.Configurator;
import net.fortuna.ical4j.Logger;
import net.fortuna.ical4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * $Id$
 * <p/>
 * Created on 18/09/2005
 * <p/>
 * The default implementation of a <code>TimeZoneRegistry</code>. This implementation will search the classpath for
 * applicable VTimeZone definitions used to back the provided TimeZone instances.
 *
 * @author Ben Fortuna
 */
public class TimeZoneRegistryImpl implements TimeZoneRegistry {
    private static final Logger LOG = LoggerFactory.getLogger(TimeZoneRegistryImpl.class);

    private static final String DEFAULT_RESOURCE_PREFIX = "zoneinfo/";

    private static final Pattern TZ_ID_SUFFIX = Pattern.compile("(?<=/)[^/]*/[^/]*$");

    private static final String UPDATE_ENABLED = "net.fortuna.ical4j.timezone.update.enabled";

    private static final Map DEFAULT_TIMEZONES = new ConcurrentHashMap();

    private static final Properties ALIASES = new Properties();

    private AssetManager mAssetManager;

    private Map timezones;

    private String resourcePrefix;

    /**
     * Default constructor.
     */
    public TimeZoneRegistryImpl() {
        this(getContext(), DEFAULT_RESOURCE_PREFIX);
    }

    public TimeZoneRegistryImpl(Context context) {
        this(context, DEFAULT_RESOURCE_PREFIX);
    }

    /**
     * Creates a new instance using the specified resource prefix.
     *
     * @param resourcePrefix a prefix prepended to classpath resource lookups for default timezones
     */
    public TimeZoneRegistryImpl(Context context, final String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
        timezones = new ConcurrentHashMap();
        mAssetManager = context.getApplicationContext().getAssets();
        init();
    }

    private void init() {
        try {
            InputStream inputStream = mAssetManager.open("tz.alias");
            ALIASES.load(inputStream);
            inputStream.close();
        } catch (IOException ioe) {
            LOG.warn("Error loading timezone aliases: " + ioe.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void register(final TimeZone timezone) {
        // for now we only apply updates to included definitions by default..
        register(timezone, false);
    }

    /**
     * {@inheritDoc}
     */
    public final void register(final TimeZone timezone, boolean update) {
        if (update) {
            // load any available updates for the timezone..
            timezones.put(timezone.getID(), new TimeZone(updateDefinition(timezone.getVTimeZone())));
        } else {
            timezones.put(timezone.getID(), timezone);
        }
    }

    /**
     * {@inheritDoc}
     */
    public final void clear() {
        timezones.clear();
    }

    /**
     * {@inheritDoc}
     */
    public final TimeZone getTimeZone(final String id) {
        TimeZone timezone = (TimeZone) timezones.get(id);
        if (timezone == null) {
            timezone = (TimeZone) DEFAULT_TIMEZONES.get(id);
            if (timezone == null) {
                // if timezone not found with identifier, try loading an alias..
                final String alias = ALIASES.getProperty(id);
                if (alias != null) {
                    return getTimeZone(alias);
                } else {
                    synchronized (DEFAULT_TIMEZONES) {
                        // check again as it may be loaded now..
                        timezone = (TimeZone) DEFAULT_TIMEZONES.get(id);
                        if (timezone == null) {
                            try {
                                final VTimeZone vTimeZone = loadVTimeZone(id);
                                if (vTimeZone != null) {
                                    // XXX: temporary kludge..
                                    // ((TzId) vTimeZone.getProperties().getProperty(Property.TZID)).setValue(id);
                                    timezone = new TimeZone(vTimeZone);
                                    DEFAULT_TIMEZONES.put(timezone.getID(), timezone);
                                } else if (CompatibilityHints.isHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING)) {
                                    // strip global part of id and match on default tz..
                                    Matcher matcher = TZ_ID_SUFFIX.matcher(id);
                                    if (matcher.find()) {
                                        return getTimeZone(matcher.group());
                                    }
                                }
                            } catch (Exception e) {
                                LOG.warn("Error occurred loading VTimeZone", e);
                            }
                        }
                    }
                }
            }
        }
        return timezone;
    }

    /**
     * Loads an existing VTimeZone from the classpath corresponding to the specified Java timezone.
     */
    private VTimeZone loadVTimeZone(final String id) throws IOException, ParserException {
        InputStream inputStream = mAssetManager.open(resourcePrefix + id + ".ics");
        if (inputStream != null) {
            final CalendarBuilder builder = new CalendarBuilder();
            final Calendar calendar = builder.build(inputStream);
            final VTimeZone vTimeZone = (VTimeZone) calendar.getComponent(Component.VTIMEZONE);
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // load any available updates for the timezone.. can be explicility disabled via configuration
            if (!"false".equals(Configurator.getProperty(UPDATE_ENABLED))) {
                LOG.trace("update time zone available, but we not use");
                // return updateDefinition(vTimeZone);
            }

            return vTimeZone;
        }
        return null;
    }

    /**
     * @param vTimeZone
     * @return
     */
    private VTimeZone updateDefinition(VTimeZone vTimeZone) {
        final TzUrl tzUrl = vTimeZone.getTimeZoneUrl();
        if (tzUrl != null) {
            //kw平台 fix#307826
            InputStream inputStream = null;
            try {
                final CalendarBuilder builder = new CalendarBuilder();
                inputStream = tzUrl.getUri().toURL().openStream();
                final Calendar calendar = builder.build(inputStream);
                final VTimeZone updatedVTimeZone = (VTimeZone) calendar.getComponent(Component.VTIMEZONE);
                if (updatedVTimeZone != null) {
                    return updatedVTimeZone;
                }
            } catch (Exception e) {
                LOG.warn("Unable to retrieve updates for timezone: " + vTimeZone.getTimeZoneId().getValue(), e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return vTimeZone;
    }

    private static Context getContext() {
        try {
            Application application = (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null, (Object[]) null);
            if (application != null) {
                return application;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Application application = (Application) Class.forName("android.app.AppGlobals")
                    .getMethod("getInitialApplication").invoke(null, (Object[]) null);
            if (application != null) {
                return application;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        throw new IllegalStateException("Context is not initialed, it is recommend to init with application context.");
    }
}
