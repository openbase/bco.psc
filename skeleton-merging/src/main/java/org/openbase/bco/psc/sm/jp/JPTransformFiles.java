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

import java.io.File;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.openbase.jps.core.AbstractJavaProperty;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.exception.JPParsingException;
import org.openbase.jps.exception.JPValidationException;
import org.openbase.jps.tools.FileHandler;
import org.openbase.jps.tools.FileHandler.FileType;
import rsb.Scope;

/**
 * JavaProperty used to specify RSB scopes and files containing the placement information of the used Skeleton Sensors (Kinect).
 * 
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class JPTransformFiles extends AbstractJavaProperty<Map<Scope, File>>{
    /** The identifiers that can be used in front of the command line argument. */
    public final static String[] COMMAND_IDENTIFIERS = {"-f", "--transform-files"};
    
    private final static String FULL_REGEX = "(([^:]+):)?([^:]+)";
    private final static Pattern PATTERN = Pattern.compile(FULL_REGEX);
    
    private final static FileHandler.ExistenceHandling EXISTENCE_HANDLING = FileHandler.ExistenceHandling.CanExist;
    private final static FileHandler.AutoMode AUTO_CREATE_MODE = FileHandler.AutoMode.Off;
    
    private final static String KEY_VALUE_SEPARATOR = ":";
    private final static String KEY_IDENTIFIER = "SCOPE";
    private final static String VALUE_IDENTIFIER = "FILE";  
   
    /** String identifying the type of the argument. */
    public final static String[] ARGUMENT_IDENTIFIERS = {"("+KEY_IDENTIFIER+KEY_VALUE_SEPARATOR+")"+VALUE_IDENTIFIER};
    
    /** Constructor. */
    public JPTransformFiles() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    protected Map<Scope, File> getPropertyDefaultValue() throws JPNotAvailableException {
        return Collections.emptyMap();
    }

    @Override
    public String getDescription() {
        return "RSB scopes and files containing the placement information of the used Skeleton Sensors (Kinect). "
                + "One of this or the \"-r\" parameter have to be used.";
    }

    @Override
    protected String[] generateArgumentIdentifiers() {
        return ARGUMENT_IDENTIFIERS;
    }

    @Override
    protected Map<Scope, File> parse(List<String> arguments) throws Exception {
        if(arguments.stream().anyMatch(s -> !s.matches(FULL_REGEX))) 
            throw new JPParsingException("Every argument of " + COMMAND_IDENTIFIERS[1] + " has to be of type " + ARGUMENT_IDENTIFIERS[0] + 
                    " and thus match \"" + FULL_REGEX + "\".");
        Map<Scope, File> result = arguments.stream().map(s -> {
            Matcher m = PATTERN.matcher(s);
            m.matches();
            return new AbstractMap.SimpleEntry<>((m.group(2) != null) ? new Scope(m.group(2)): new Scope("/"), new File(m.group(3)));
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return result;
    }
    
    @Override
    public void validate() throws JPValidationException {
        try {
            for(File f : getValue().values()){
                FileHandler.handle(f, FileType.File, EXISTENCE_HANDLING, AUTO_CREATE_MODE);
            }
        } catch (Exception ex) {
            throw new JPValidationException("There was an invalid file in " + getValue() + "!", ex);
        }
    }
}
