package org.jivesoftware.smackx.pubsub;

import org.codehaus.jackson.org.objectweb.asm.signature.SignatureVisitor;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;

public enum PubSubElementType {
    CREATE("create", PubSubNamespace.BASIC),
    DELETE("delete", PubSubNamespace.OWNER),
    DELETE_EVENT("delete", PubSubNamespace.EVENT),
    CONFIGURE("configure", PubSubNamespace.BASIC),
    CONFIGURE_OWNER("configure", PubSubNamespace.OWNER),
    CONFIGURATION("configuration", PubSubNamespace.EVENT),
    OPTIONS("options", PubSubNamespace.BASIC),
    DEFAULT("default", PubSubNamespace.OWNER),
    ITEMS("items", PubSubNamespace.BASIC),
    ITEMS_EVENT("items", PubSubNamespace.EVENT),
    ITEM("item", PubSubNamespace.BASIC),
    ITEM_EVENT("item", PubSubNamespace.EVENT),
    PUBLISH("publish", PubSubNamespace.BASIC),
    PUBLISH_OPTIONS("publish-options", PubSubNamespace.BASIC),
    PURGE_OWNER("purge", PubSubNamespace.OWNER),
    PURGE_EVENT("purge", PubSubNamespace.EVENT),
    RETRACT("retract", PubSubNamespace.BASIC),
    AFFILIATIONS("affiliations", PubSubNamespace.BASIC),
    SUBSCRIBE("subscribe", PubSubNamespace.BASIC),
    SUBSCRIPTION("subscription", PubSubNamespace.BASIC),
    SUBSCRIPTIONS("subscriptions", PubSubNamespace.BASIC),
    UNSUBSCRIBE("unsubscribe", PubSubNamespace.BASIC);
    
    private String eName;
    private PubSubNamespace nSpace;

    private PubSubElementType(String str, PubSubNamespace pubSubNamespace) {
        this.eName = str;
        this.nSpace = pubSubNamespace;
    }

    public PubSubNamespace getNamespace() {
        return this.nSpace;
    }

    public String getElementName() {
        return this.eName;
    }

    public static PubSubElementType valueOfFromElemName(String str, String str2) {
        int lastIndexOf = str2.lastIndexOf(35);
        String substring = lastIndexOf == -1 ? null : str2.substring(lastIndexOf + 1);
        if (substring != null) {
            return valueOf((str + '_' + substring).toUpperCase());
        }
        return valueOf(str.toUpperCase().replace(SignatureVisitor.SUPER, '_'));
    }
}