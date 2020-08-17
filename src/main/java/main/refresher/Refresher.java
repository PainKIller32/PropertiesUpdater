package main.refresher;

import main.annotation.Property;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Refresher {
    private static final Logger logger = Logger.getLogger(Refresher.class.getName());
    private static final String PROPERTY_DELIMITER = "=";
    private static final String GET_METHOD_PREFIX = "get";
    private static final String SET_METHOD_PREFIX = "set";

    public void doRefresh(String filePath, Object target) {
        Map<String, Object> fileProperties = getFileProperties(filePath);
        for (Field field : target.getClass().getDeclaredFields()) {
            Property property = field.getAnnotation(Property.class);
            if (property != null) {
                boolean result = false;
                if (fileProperties.containsKey(property.propertyName())) {
                    Object value = fileProperties.get(property.propertyName());
                    if (value instanceof JSONObject) {
                        result = doInnerObjectRefresh(field, value, target);
                    } else {
                        result = setFieldValue(field, value, target);
                    }
                }
                if (!result) {
                    doDefaultRefresh(field, property, target);
                }
            }
        }
    }

    private void doDefaultRefresh(Field field, Property property, Object target) {
        if (!property.defaultValue().isEmpty()) {
            setFieldValue(field, property.defaultValue(), target);
        } else {
            setFieldValue(field, null, target);
        }
    }

    private boolean doInnerObjectRefresh(Field field, Object value, Object target) {
        boolean result = false;
        Map<String, Object> values = getJsonProperties((JSONObject) value);
        Method method = getClassMethod(target.getClass(), field, GET_METHOD_PREFIX);
        if (method != null) {
            try {
                Object object = method.invoke(target, null);
                if (object == null) {
                    object = field.getType().getConstructor().newInstance();
                    setFieldValue(field, object, target);
                }
                result = setInnerFieldsValues(values, object);
                if (!result) {
                    setFieldValue(field, null, target);
                }
            } catch (Exception e) {
                String message = e.getMessage() + "\nField: " + field.getName() + ", Value: " + value;
                logger.log(Level.WARNING, message);
            }
        }
        return result;
    }

    private boolean setInnerFieldsValues(Map<String, Object> properties, Object target) {
        boolean result = false;
        for (Field field : target.getClass().getDeclaredFields()) {
            if (setFieldValue(field, properties.get(field.getName()), target)) {
                result = true;
            }
        }
        return result;
    }

    private boolean setFieldValue(Field field, Object value, Object target) {
        boolean result = false;
        try {
            if (value != null && field.getType().equals(Integer.class)) {
                value = ((Number) value).intValue();
            }
            Method method = getClassMethod(target.getClass(), field, SET_METHOD_PREFIX);
            if (method != null) {
                method.invoke(target, value);
                result = true;
            }
        } catch (Exception e) {
            String message = e.getMessage() + "\nField: " + field.getName() + ", Value: " + value;
            logger.log(Level.WARNING, message);
        }
        return result;
    }

    private Method getClassMethod(Class<?> clazz, Field field, String methodType) {
        String fieldName = field.getName();
        String methodName = methodType + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        Method method = null;
        try {
            if (methodType.equals(GET_METHOD_PREFIX)) {
                method = clazz.getMethod(methodName);
            } else {
                method = clazz.getMethod(methodName, field.getType());
            }
        } catch (NoSuchMethodException e) {
            String message = e.getMessage() + "\nField name : " + fieldName + ", method name: " + methodName;
            logger.log(Level.WARNING, message);
        }
        return method;
    }

    private Map<String, Object> getJsonProperties(JSONObject jsonObject) {
        Map<String, Object> properties = new HashMap<>();
        for (Object key : jsonObject.entrySet()) {
            Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) key;
            properties.put((String) entry.getKey(), entry.getValue());
        }
        return properties;
    }

    private Map<String, Object> getFileProperties(String path) {
        JSONParser jsonParser = new JSONParser();
        Map<String, Object> fileProperties = new HashMap<>();
        if (path != null && !path.isEmpty()) {
            try (FileReader fileReader = new FileReader(path)) {
                Scanner scanner = new Scanner(fileReader);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    int delimiterIndex = line.lastIndexOf(PROPERTY_DELIMITER);
                    if (delimiterIndex > 0) {
                        String valueName = line.substring(delimiterIndex + 1).trim();
                        if (!valueName.isEmpty()) {
                            fileProperties.put(line.substring(0, delimiterIndex).trim(), jsonParser.parse(line.substring(delimiterIndex + 1).trim()));
                        }
                    }
                }
            } catch (IOException | ParseException e) {
                logger.log(Level.WARNING, e.toString());
            }
        }
        return fileProperties;
    }
}