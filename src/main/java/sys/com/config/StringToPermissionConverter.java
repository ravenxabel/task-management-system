package sys.com.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import sys.com.model.Permission;

@Component
public class StringToPermissionConverter implements Converter<String, Permission> {

    @Override
    public Permission convert(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        try {
            return Permission.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid permission: " + source);
        }
    }
}
