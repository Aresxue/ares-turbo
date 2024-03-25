package cn.ares.turbo.loader.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author: Ares
 * @time: 2021-12-02 20:08:00
 * @description: Map util
 * @version: JDK 1.8
 */
public class MapUtil {

  public static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

  /**
   * @author: Ares
   * @description: 合并属性到map
   * @description: Merge properties into map
   * @time: 2022-06-07 16:52:41
   * @params: [props, map] 属性，map
   * @return: void
   */
  public static <K, V> void mergePropertiesIntoMap(Properties props, Map<K, V> map) {
    if (props != null) {
      for (Enumeration<?> en = props.propertyNames(); en.hasMoreElements(); ) {
        String key = (String) en.nextElement();
        Object value = props.get(key);
        if (value == null) {
          // Allow for defaults fallback or potentially overridden accessor...
          value = props.getProperty(key);
        }
        map.put((K) key, (V) value);
      }
    }
  }

  /**
   * @author: Ares
   * @description: 以期望的元素个数创建HashMap
   * @description: New HashMap with expected size
   * @time: 2021-12-24 11:21:07
   * @params: [expectedSize] 期望的元素个数
   * @return: java.util.HashMap<K, V>
   */
  public static <K, V> HashMap<K, V> newHashMap(int expectedSize) {
    return new HashMap<>(capacity(expectedSize));
  }

  /**
   * @author: Ares
   * @description: 根据入参构建一个新映射
   * @description: Build a new map based on the input
   * @time: 2024-01-18 14:03:14
   * @params: [args] 参数
   * @return: java.util.Map<K, V> 映射
   */
  public static <K, V> Map<K, V> newMap(Object... args) {
    return newMap(null, args);
  }

  /**
   * @author: Ares
   * @description: 根据入参构建一个新的有序映射
   * @description: Build a new  linked map based on the input
   * @time: 2024-01-18 14:03:14
   * @params: [args] 参数
   * @return: java.util.Map<K, V> 映射
   */
  public static <K, V> Map<K, V> newLinkedHashMap(Object... args) {
    return newMap(LinkedHashMap.class, args);
  }

  /**
   * @author: Ares
   * @description: 以默认的元素个数创建HashMap
   * @description: New HashMap with default size
   * @time: 2022-06-07 16:54:01
   * @params: []
   * @return: java.util.HashMap<K, V>
   */
  public static <K, V> HashMap<K, V> newHashMap() {
    return new HashMap<>(16);
  }

  /**
   * @author: Ares
   * @description: 以期望的元素个数创建HashMap
   * @description: New ConcurrentMap with expected size
   * @time: 2023-05-08 11:46:35
   * @params: [expectedSize] 期望的元素个数
   * @return: java.util.concurrent.ConcurrentMap<K, V>
   */
  public static <K, V> ConcurrentMap<K, V> newConcurrentMap(int expectedSize) {
    return new ConcurrentHashMap<>(capacity(expectedSize));
  }

  /**
   * @author: Ares
   * @description: 以默认的元素个数创建HashMap
   * @description: New ConcurrentMap with default size
   * @time: 2023-05-08 11:46:35
   * @params: []
   * @return: java.util.concurrent.ConcurrentMap<K, V>
   */
  public static <K, V> ConcurrentMap<K, V> newConcurrentMap() {
    return new ConcurrentHashMap<>(16);
  }


  /**
   * @author: Ares
   * @description: 以期望的元素个数创建LinkedHashMap
   * @description: New LinkedHashMap with expected size
   * @time: 2021-12-24 11:21:07
   * @params: [expectedSize] expected size
   * @return: java.util.LinkedHashMap<K, V> out 出参
   */
  public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int expectedSize) {
    return new LinkedHashMap<>(capacity(expectedSize));
  }

  /**
   * @author: Ares
   * @description: 根据期望的容量为映射选取合适的容量
   * @description: Choose the appropriate capacity for the mapping based on the desired capacity
   * @time: 2023-12-25 17:25:59
   * @params: [expectedSize] 期望的元素容量
   * @return: int 合适的容量
   */
  public static int capacity(int expectedSize) {
    if (expectedSize < 3) {
      checkNonNegative(expectedSize, "expectedSize");
      return expectedSize + 1;
    }
    if (expectedSize < MAX_POWER_OF_TWO) {
      // This is the calculation used in JDK8 to resize when a putAll
      // happens; it seems to be the most conservative calculation we
      // can make.  0.75 is the default load factor.
      return (int) ((float) expectedSize / 0.75F + 1.0F);
    }
    return Integer.MAX_VALUE;
  }

  private static int checkNonNegative(int value, String name) {
    if (value < 0) {
      throw new IllegalArgumentException(name + " cannot be negative but was: " + value);
    }
    return value;
  }

  /**
   * @author: Ares
   * @description: 判断map是否为空
   * @description: Determine map is empty
   * @time: 2022-06-07 16:56:08
   * @params: [map] map
   * @return: boolean 是否非空
   */
  public static <K, V> boolean isEmpty(Map<K, V> map) {
    return (map == null || map.isEmpty());
  }

  /**
   * @author: Ares
   * @description: 判断map是否非空
   * @description: Determine map is not empty
   * @time: 2022-06-07 16:56:08
   * @params: [map] map
   * @return: boolean 是否非空
   */
  public static <K, V> boolean isNotEmpty(Map<K, V> map) {
    return !isEmpty(map);
  }

  /**
   * @author: Ares
   * @description: 获取映射中键对应的值
   * @description: Get value from map
   * @time: 2022-06-07 16:56:55
   * @params: [map, key] 映射, 键
   * @return: V 值
   */
  public static <K, V> V parseValue(Map<K, V> map, K key) {
    if (isEmpty(map)) {
      return null;
    }
    return map.get(key);
  }

  /**
   * @author: Ares
   * @description: 获取映射中键对应的值如果map为空返回默认值
   * @description: Get the value in the map and return the default value if the map is empty
   * @time: 2022-06-07 16:57:56
   * @params: [map, key] map, 键
   * @return: java.lang.String 字符串
   */
  public static <K, V> V parseValueOrDefault(Map<K, V> map, K key, V defaultValue) {
    if (isEmpty(map)) {
      return defaultValue;
    }
    return map.get(key);
  }


  /**
   * @author: Ares
   * @description: 根据属性创建map
   * @description: Create map from properties
   * @time: 2021-12-24 11:19:30
   * @params: [properties] properties
   * @return: java.util.Map<java.lang.String, java.lang.String> properties map
   */
  public static Map<String, String> fromProperties(Properties properties) {
    if (null == properties) {
      return Collections.emptyMap();
    }
    Map<String, String> map = newHashMap(properties.size());
    Enumeration<?> enumeration = properties.propertyNames();

    while (enumeration.hasMoreElements()) {
      String key = (String) enumeration.nextElement();
      map.put(key, properties.getProperty(key));
    }

    return map;
  }

}
