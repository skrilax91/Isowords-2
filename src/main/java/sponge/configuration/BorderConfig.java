package sponge.configuration;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.LinkedHashMap;
import java.util.Map;
@ConfigSerializable
public class BorderConfig {
        private boolean enable = true;

        @Comment("List all borders available with a label and a radius")
        private Map<String, Integer> borders = new LinkedHashMap<String, Integer>(){{ put("Default", 250); put("Small", 500); put("Medium", 750); put("Large", 1000);}};


    public boolean isEnable() { return enable; }

    public Map<String, Integer> getBorders() { return borders; }
}
