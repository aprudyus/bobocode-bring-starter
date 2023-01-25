package com.hoverla.bring.context;

import com.hoverla.bring.annotation.Autowired;
import com.hoverla.bring.annotation.Bean;
import com.hoverla.bring.exception.NoSuchBeanException;
import com.hoverla.bring.exception.NoUniqueBeanException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ApplicationContextImpl implements ApplicationContext {
    private Map<String, Object> beans = new ConcurrentHashMap<>();

    public ApplicationContextImpl(String... basePackages) {
        Reflections beanScanner = new Reflections((Object[]) basePackages);
        Set<Class<?>> beanClasses = beanScanner.getTypesAnnotatedWith(Bean.class, true);

        if (beanClasses.isEmpty()) {
            return;
        }

        initBeans(beanClasses);
        postProcess();
    }

    @SneakyThrows
    private void initBeans(Set<Class<?>> beanClasses) {
        for (Class<?> beanType: beanClasses) {
            String beanName = resolveBeanName(beanType);
            Object instance = beanType.getConstructor().newInstance();
            beans.put(beanName, instance);
        }
    }

    @SneakyThrows
    private void postProcess() {
        Collection<Object> beanInstances = beans.values();

        for (Object beanInstance: beanInstances) {
            for (Field field: beanInstance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    field.set(beanInstance, getBean(field.getType()));
                }
            }
        }

    }

    private String resolveBeanName(Class<?> type) {
        String beanName = type.getAnnotation(Bean.class).value();
        return StringUtils.isNotBlank(beanName) ? beanName : resolveBeanName(type.getSimpleName());
    }

    private String resolveBeanName(String typeName) {
        return typeName.substring(0, 1).toLowerCase() + typeName.substring(1);
    }

    @Override
    public <T> T getBean(Class<T> beanType) {
        Map<String, T> beanMap = getAllBeans(beanType);
        if (beanMap.isEmpty()) {
            throw new NoSuchBeanException();
        }

        if (beanMap.size() > 1) {
            throw new NoUniqueBeanException();
        }

        return beanMap.values().stream().toList().get(0);
    }

    @Override
    public <T> T getBean(String name, Class<T> beanType) {
        Object potentialBean = beans.get(name);

        if (potentialBean != null && beanType.isAssignableFrom(potentialBean.getClass())) {
            return beanType.cast(potentialBean);
        }

        throw new NoSuchBeanException();
    }

    @Override
    public <T> Map<String, T> getAllBeans(Class<T> beanType) {
        return beans.entrySet().stream()
            .filter(potentialBean -> beanType.isAssignableFrom(potentialBean.getValue().getClass()))
            .collect(Collectors.toMap(Map.Entry::getKey, bean -> beanType.cast(bean.getValue())));
    }
}
