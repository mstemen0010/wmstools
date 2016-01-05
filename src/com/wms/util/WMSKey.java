package com.wms.util;

import com.wms.logos.iframe.IFrameInterface;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author mstemen
 */
public class WMSKey
{
    IFrameInterface.IDomainKey domainID;
    Class interfacePart;
    String name;
    static HashSet<WMSKey> allKeys = new HashSet<WMSKey>();
    private WMSKey(IFrameInterface.IDomainKey type, Class mod)
    {
        this.domainID = type;
        this.interfacePart = mod;
    }
    /**
     * 
     * @param type
     * @param mod
     * @return IKey
     */
    public static WMSKey makeKey(IFrameInterface.IDomainKey type, Class mod)
    {
        
        WMSKey key = findKey(type, mod);
        if (key == null) {
            key = new WMSKey(type, mod);
            allKeys.add(key);
        }
        return key;
    }
    /**
     * 
     * @param type
     * @param mod
     * @return IKey
     */
    public static WMSKey getKey(IFrameInterface.IDomainKey type, Class mod)
    {        return makeKey(type, mod);    }
    private static WMSKey findKey(IFrameInterface.IDomainKey type, Class mod)
    {
        Iterator<WMSKey> i = allKeys.iterator();
        WMSKey k = null;
        while (i.hasNext()) {
            k = i.next();
            if (k.domainID.equals(type) && k.interfacePart.equals(mod)) {
                break;
            }
        }
        return k;
    }
}
