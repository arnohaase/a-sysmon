package com.ajjpj.asysmon.measure.environment;

import com.ajjpj.asysmon.util.AList;
import com.ajjpj.asysmon.util.AUUID;

/**
 * 'Environment' data describes things that potentially change, but rarely do so - more precisely, the assumption is
 *  that they (typically) do not change while an application is running.<p />
 *
 * They are structured hierarchically. The 'name' property serves as an identifier, and all elements of the list together
 *  serve are unique. The head of the list should contain the most generic segment of the name.
 *
 * @author arno
 */
public class AEnvironmentData {
    private final AUUID uuid = AUUID.createRandom();
    private final AList<String> name;
    private final String value;

    public AEnvironmentData(AList<String> name, String value) {
        this.name = name;
        this.value = value;
    }

    public AUUID getUuid() {
        return uuid;
    }

    public AList<String> getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "AEnvironmentData{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
