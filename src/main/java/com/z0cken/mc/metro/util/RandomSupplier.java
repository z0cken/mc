package com.z0cken.mc.metro.util;

import java.util.Map;
import java.util.function.Supplier;

public class RandomSupplier<T> implements Supplier<T> {

    private Map<T, Double> map;

    public RandomSupplier(Map<T, Double> map) {
        this.map = map;
        double sum = map.values().stream().mapToDouble(Double::doubleValue).sum();
        map.replaceAll((key, val) -> val / sum);
    }

    @Override
    public T get() {
        double p = Math.random();
        double sum = 0.0;
        for(Map.Entry<T, Double> entry : map.entrySet()) {
            sum += entry.getValue();
            if(p <= sum) return entry.getKey();
        }
        return null;
    }
}