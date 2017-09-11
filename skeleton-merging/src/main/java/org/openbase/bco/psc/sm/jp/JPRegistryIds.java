package org.openbase.bco.psc.sm.jp;

/*
 * #%L
 * BCO PSC Skeleton Merging
 * %%
 * Copyright (C) 2016 - 2017 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.openbase.jps.core.AbstractJavaProperty;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.exception.JPParsingException;
import rsb.Scope;

/**
 * JavaProperty used to specify RSB Scopes(optional) and registry ids of the used Skeleton Sensors (Kinect).
 * 
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPRegistryIds extends AbstractJavaProperty<Map<Scope, String>> {
    /** The identifiers that can be used in front of the command line argument. */
    public final static String[] COMMAND_IDENTIFIERS = {"-r", "--registry-ids"};
    
    private final static String HEXA_REGEX = "[0-9a-fA-F]";
    private final static String UNIT_ID_REGEX = HEXA_REGEX+"{8}-"+HEXA_REGEX+"{4}-"+HEXA_REGEX+"{4}-"+HEXA_REGEX+"{4}-"+HEXA_REGEX+"{12}";
    private final static String FULL_REGEX = "(([^:]+):)?("+UNIT_ID_REGEX+")";
    private final static Pattern PATTERN = Pattern.compile(FULL_REGEX);
    
    private final static String KEY_VALUE_SEPARATOR = ":";
    private final static String KEY_IDENTIFIER = "SCOPE";
    private final static String VALUE_IDENTIFIER = "STRING";
   
    /** String identifying the type of the argument. */
    public final static String[] ARGUMENT_IDENTIFIERS = {"("+KEY_IDENTIFIER+KEY_VALUE_SEPARATOR+")"+VALUE_IDENTIFIER};

    /** Constructor. */
    public JPRegistryIds() {
        super(COMMAND_IDENTIFIERS);
    }
    
    @Override
    protected String[] generateArgumentIdentifiers() {
        return ARGUMENT_IDENTIFIERS;
    }

    @Override
    public String getDescription() {
        return "RSB Scopes(optional) and registry ids of the used Skeleton Sensors (Kinect). The registry should contain placement information. "
                + "One of this or the \"-f\" parameter have to be used.";
    }

    @Override
    protected Map<Scope, String> getPropertyDefaultValue() throws JPNotAvailableException {
        return Collections.emptyMap();
    }

    @Override
    protected Map<Scope, String> parse(List<String> arguments) throws Exception {
        if(arguments.stream().anyMatch(s -> !s.matches(FULL_REGEX))) 
            throw new JPParsingException("Every argument of " + COMMAND_IDENTIFIERS[1] + " has to be of type " + ARGUMENT_IDENTIFIERS[0] + 
                    " and thus match \"" + FULL_REGEX + "\".");
        return arguments.stream().map(s -> {
            Matcher m = PATTERN.matcher(s);
            m.matches();
            return new SimpleEntry<>((m.group(2) != null) ? new Scope(m.group(2)): new Scope("/"), m.group(3));
        }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
   }
}
