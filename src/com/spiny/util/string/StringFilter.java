package com.spiny.util.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"rawtypes","unchecked"})
public class StringFilter {
	
	private StringFilterLayer[] layers;
	
	public StringFilter(StringFilterLayer...layers) {
		this.layers = layers;
	}
	
	public String filter(String s, Object...objectives) {
		List<?> objectSet = new ArrayList(Arrays.asList(objectives));
		for(StringFilterLayer layer : layers) {
			for(Object o : objectSet) {
				try {
					s = layer.filter(o, s);
					objectSet.remove(o);
					break;
				} catch(ClassCastException e) {
					continue;
				}
			}
		}
		return s;
	}
}
