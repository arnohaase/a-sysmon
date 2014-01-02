package com.ajjpj.asysmon.config.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author arno
 */
public class APresentationMenuEntry {
    public final String label;
    public final List<APresentationPageDefinition> pageDefinitions;

    public APresentationMenuEntry(String label, List<APresentationPageDefinition> presentationPageDefinitions) {
        this.label = label;
        this.pageDefinitions = Collections.unmodifiableList(new ArrayList<APresentationPageDefinition>(presentationPageDefinitions));
    }
}
