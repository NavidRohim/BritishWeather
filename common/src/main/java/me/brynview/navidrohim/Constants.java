package me.brynview.navidrohim;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Constants {

    public static final String MOD_ID = "britishweather";
    public static final String MOD_NAME = "British Weather";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static final String IP_CHECK_URL = "https://checkip.amazonaws.com";
    public static final @Nullable String USER_IP = Util.getIP();
}