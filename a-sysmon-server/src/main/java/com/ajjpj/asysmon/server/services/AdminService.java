package com.ajjpj.asysmon.server.services;


import com.ajjpj.asysmon.server.storage.ScalarMetaData;
import com.ajjpj.asysmon.server.util.AOption;
import com.ajjpj.asysmon.server.util.json.ListWrapper;

import java.util.List;

/**
 * @author arno
 */
public interface AdminService {
    ListWrapper<String> getMonitoredApplicationNames();

    public AOption<ScalarMetaData> get(String name);
}
