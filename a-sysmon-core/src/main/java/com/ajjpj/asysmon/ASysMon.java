package com.ajjpj.asysmon;

import com.ajjpj.asysmon.config.ADefaultConfigFactory;
import com.ajjpj.asysmon.config.ASysMonConfig;
import com.ajjpj.asysmon.impl.ASysMonImpl;
import com.ajjpj.asysmon.util.AFunction0;
import com.ajjpj.asysmon.util.AUnchecker;


/**
 * This class is the point of contact for an application to ASysMon. There are basically two ways to use it:
 *
 * <ul>
 *     <li> Use the static get() method to access it as a singleton. That is simple and convenient, and it is
 *          sufficient for many applications. If it is used that way, all configuration must be done through
 *          the static methods of ADefaultSysMonConfig. </li>
 *     <li> Create and manage your own instance (or instances) by calling the ASysMonImpl constructor, passing in your
 *          configuration. This is for maximum flexibility, but you lose some convenience. </li>
 * </ul>
 *
 * @author arno
 */
public class ASysMon {
    public static ASysMonApi get() {
        // this class has the sole purpose of providing really lazy init of the singleton instance
        return ASysMonInstanceHolder.INSTANCE;
    }

    /**
     * this class has the sole purpose of providing really lazy init of the singleton instance
     */
    private static class ASysMonInstanceHolder {
        public static final ASysMonApi INSTANCE = new ASysMonImpl(getConfig());

        private static ASysMonConfig getConfig() {
            return AUnchecker.executeUnchecked(new AFunction0<ASysMonConfig, Exception>() {
                @Override public ASysMonConfig apply() throws Exception {
                    return ADefaultConfigFactory.getConfigFactory().getConfig();
                }
            });
        }
    }
}

