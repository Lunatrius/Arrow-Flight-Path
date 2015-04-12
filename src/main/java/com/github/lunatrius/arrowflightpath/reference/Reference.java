package com.github.lunatrius.arrowflightpath.reference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Reference {
    public static final String MODID = "ArrowFlightPath";
    public static final String NAME = "Arrow Flight Path";
    public static final String VERSION = "${version}";
    public static final String FORGE = "${forgeversion}";
    public static final String MINECRAFT = "${mcversion}";
    public static final String PROXY_SERVER = "com.github.lunatrius.arrowflightpath.proxy.ServerProxy";
    public static final String PROXY_CLIENT = "com.github.lunatrius.arrowflightpath.proxy.ClientProxy";

    public static Logger logger = LogManager.getLogger(Reference.MODID);
}
