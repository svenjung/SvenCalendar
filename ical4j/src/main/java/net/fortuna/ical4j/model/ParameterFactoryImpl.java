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

import net.fortuna.ical4j.model.parameter.*;

import java.net.URISyntaxException;

/**
 * A factory for creating iCalendar parameters.
 * <p/>
 * $Id $
 * <p/>
 * [05-Apr-2004]
 *
 * @author Ben Fortuna
 */
public class ParameterFactoryImpl extends AbstractContentFactory<ParameterFactory> {

    private static final long serialVersionUID = -4034423507432249165L;

    protected ParameterFactoryImpl() {
        registerFactory(Parameter.ABBREV,           new Abbrev.Factory());
        registerFactory(Parameter.ALTREP,           new AltRep.Factory());
        registerFactory(Parameter.CN,               new Cn.Factory());
        registerFactory(Parameter.CUTYPE,           new CuType.Factory());
        registerFactory(Parameter.DELEGATED_FROM,   new DelegatedFrom.Factory());
        registerFactory(Parameter.DELEGATED_TO,     new DelegatedTo.Factory());
        registerFactory(Parameter.DIR,              new Dir.Factory());
        registerFactory(Parameter.ENCODING,         new Encoding.Factory());
        registerFactory(Parameter.FBTYPE,           new FbType.Factory());
        registerFactory(Parameter.FMTTYPE,          new FmtType.Factory());
        registerFactory(Parameter.LANGUAGE,         new Language.Factory());
        registerFactory(Parameter.MEMBER,           new Member.Factory());
        registerFactory(Parameter.PARTSTAT,         new PartStat.Factory());
        registerFactory(Parameter.RANGE,            new Range.Factory());
        registerFactory(Parameter.RELATED,          new Related.Factory());
        registerFactory(Parameter.ROLE,             new Role.Factory());
        registerFactory(Parameter.RSVP,             new Rsvp.Factory());
        registerFactory(Parameter.SCHEDULE_AGENT,   new ScheduleAgent.Factory());
        registerFactory(Parameter.SCHEDULE_STATUS,  new ScheduleStatus.Factory());
        registerFactory(Parameter.SENT_BY,          new SentBy.Factory());
        registerFactory(Parameter.TYPE,             new Type.Factory());
        registerFactory(Parameter.TZID,             new TzId.Factory());
        registerFactory(Parameter.VALUE,            new Value.Factory());
        registerFactory(Parameter.VVENUE,           new Vvenue.Factory());
    }

    /**
     * Creates a parameter.
     *
     * @param name  name of the parameter
     * @param value a parameter value
     * @return a component
     * @throws URISyntaxException thrown when the specified string is not a valid representation of a URI for selected
     *                            parameters
     */
    public Parameter createParameter(final String name, final String value)
            throws URISyntaxException {
        final ParameterFactory factory = getFactory(name);
        Parameter parameter;
        if (factory != null) {
            parameter = factory.createParameter(value);
        } else if (isExperimentalName(name)) {
            parameter = new XParameter(name, value);
        } else if (allowIllegalNames()) {
            parameter = new XParameter(name, value);
        } else {
            throw new IllegalArgumentException(String.format("Unsupported parameter name: %s", name));
        }
        return parameter;
    }

    private boolean isExperimentalName(final String name) {
        return name.startsWith(Parameter.EXPERIMENTAL_PREFIX)
                && name.length() > Parameter.EXPERIMENTAL_PREFIX.length();
    }

}
