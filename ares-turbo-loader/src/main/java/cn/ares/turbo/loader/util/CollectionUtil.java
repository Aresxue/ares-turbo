package cn.ares.turbo.loader.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author: Ares
 * @time: 2021-10-25 18:59:00
 * @description: Collection util
 * @version: JDK 1.8
 */
public class CollectionUtil {

  /**
   * @author: Ares
   * @description: 集合是否为空
   * @description: Collection is empty
   * @time: 2022-06-07 17:06:43
   * @params: [collection] 集合
   * @return: boolean 是否为空
   */
  public static <T> boolean isEmpty(Collection<T> collection) {
    return (collection == null || collection.isEmpty());
  }

  /**
   * @author: Ares
   * @description: 集合是否非空
   * @description: Collection is not empty
   * @time: 2022-06-07 17:06:43
   * @params: [collection] 集合
   * @return: boolean 是否非空
   */
  public static <T> boolean isNotEmpty(Collection<T> collection) {
    return !isEmpty(collection);
  }

  /**
   * @author: Ares
   * @description: 以期望的元素个数创建HashSet
   * @description: New HashSet with expected size
   * @time: 2022-06-07 17:09:07
   * @params: [expectedSize] 期望的元素个数
   * @return: java.util.HashSet<E> 不可重复集合
   */
  public static <E> HashSet<E> newHashSet(int expectedSize) {
    return new HashSet<>(MapUtil.capacity(expectedSize));
  }

  /**
   * @author: Ares
   * @description: 以默认的元素个数创建不可重复集合
   * @description: New HashSet with default size
   * @time: 2022-06-07 17:09:35
   * @params: []
   * @return: java.util.HashSet<E>
   */
  public static <E> HashSet<E> newHashSet() {
    return newHashSet(16);
  }

  /**
   * @author: Ares
   * @description: 集合是否包含元素
   * @description: Collection contains element
   * @time: 2022-06-07 17:09:53
   * @params: [collection, element] 集合，元素
   * @return: boolean 是否包含
   */
  public static <T> boolean containsInstance(Collection<T> collection, Object element) {
    if (collection != null) {
      for (Object candidate : collection) {
        if (candidate == element) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * @author: Ares
   * @description: 来源集合包含候选集合的任意元素
   * @description: The source set contains any element of the candidate set
   * @time: 2022-06-07 17:13:35
   * @params: [source, candidates] 来源集合，候选集合
   * @return: boolean 是否包含
   */
  public static <T> boolean containsAny(Collection<T> source, Collection<T> candidates) {
    return findFirstMatch(source, candidates) != null;
  }

  /**
   * @author: Ares
   * @description: 获取集合第一个元素
   * @description: Get first element for collection
   * @time: 2023-10-27 16:28:30
   * @params: [collection] 集合
   * @return: T 首个元素
   */
  public static <T> T random(Collection<T> collection) {
    if (isEmpty(collection)) {
      return null;
    }
    if (collection instanceof List) {
      List<T> list = (List<T>) collection;
      if (list.size() == 1) {
        return list.get(0);
      }
      int index = ThreadLocalRandom.current().nextInt(list.size());
      return list.get(index);
    } else {
      if (collection.size() == 1) {
        return new ArrayList<>(collection).get(0);
      }
      int index = ThreadLocalRandom.current().nextInt(collection.size());
      for (T t : collection) {
        if (index <= 0) {
          return t;
        }
        index--;
      }
      return null;
    }
  }


  /**
   * @author: Ares
   * @description: 元素数组转为数组链表
   * @description: Convert element array to ArrayList
   * @time: 2022-06-07 17:18:38
   * @params: [elements] 元素数组
   * @return: java.util.ArrayList<E>
   */
  @SafeVarargs
  public static <E> ArrayList<E> newArrayList(E... elements) {
    if (null == elements) {
      throw new NullPointerException();
    }
    int arraySize = elements.length;
    ArrayList<E> list = newArrayList(arraySize);
    Collections.addAll(list, elements);
    return list;
  }

  /**
   * @author: Ares
   * @description: 以指定的元素大小创建数组链表
   * @description: Create an array linked list with the specified element size
   * @time: 2024-03-25 13:00:41
   * @params: [arraySize] 数组大小
   * @return: java.util.ArrayList<E> 数组链表
   */
  public static <E> ArrayList<E> newArrayList(int arraySize) {
    return new ArrayList<>(suitableCapacity(arraySize));
  }

  /**
   * @author: Ares
   * @description: 元素数组转为链表
   * @description: Convert element array to list
   * @time: 2023-12-07 17:18:38
   * @params: [elements] 元素数组
   * @return: java.util.List<E>
   */
  @SafeVarargs
  public static <E> List<E> asList(E... elements) {
    return newArrayList(elements);
  }

  /**
   * @author: Ares
   * @description: 元素数组转为非线性链表
   * @description: Convert element array to linked list
   * @time: 2023-12-07 17:18:38
   * @params: [elements] 元素数组
   * @return: java.util.LinkedList<E>
   */
  @SafeVarargs
  public static <E> LinkedList<E> asLinkedList(E... elements) {
    if (null == elements) {
      throw new NullPointerException();
    }
    return new LinkedList<>(Arrays.asList(elements));
  }

  /**
   * @author: Ares
   * @description: 元素数组转为数组链表
   * @description: Convert element array to ArrayList
   * @time: 2022-06-07 17:18:38
   * @params: [elements] 元素数组
   * @return: java.util.ArrayList<E>
   */
  public static <E> ArrayList<E> newListArray(E[] elements) {
    if (null == elements) {
      throw new NullPointerException();
    }
    return newArrayList(elements);
  }

  /**
   * @author: Ares
   * @description: 根据数组大小获取合适的数组链表大小
   * @description: Get suitable capacity for ArrayList by array size
   * @time: 2022-06-07 17:19:47
   * @params: [arraySize] in 入参
   * @return: int out 出参
   */
  public static int suitableCapacity(int arraySize) {
    return IntegerUtil.saturatedCast(5 + arraySize + arraySize / 10);
  }


  /**
   * @author: Ares
   * @description: 寻找两个集合的第一个匹配元素
   * @description: Find the first matching element of two collection
   * @time: 2022-06-07 17:14:17
   * @params: [source, candidates] 来源集合，候选集合
   * @return: E 元素
   */
  public static <SOURCE, E> E findFirstMatch(Collection<SOURCE> source, Collection<E> candidates) {
    if (isEmpty(source) || isEmpty(candidates)) {
      return null;
    }
    for (Object candidate : candidates) {
      if (source.contains(candidate)) {
        return (E) candidate;
      }
    }
    return null;
  }

  /**
   * @author: Ares
   * @description: 寻找指定类型的元素
   * @description: Find value from collection by type
   * @time: 2022-06-07 17:21:26
   * @params: [collection, type] 集合，类型
   * @return: T 元素
   */
  public static <T> T findValueOfType(Collection<T> collection, Class<T> type) {
    if (isEmpty(collection)) {
      return null;
    }
    T value = null;
    for (T element : collection) {
      if (type == null || type.isInstance(element)) {
        if (value != null) {
          // More than one value found... no clear single value.
          return null;
        }
        value = element;
      }
    }
    return value;
  }

  /**
   * @author: Ares
   * @description: 只包含唯一元素
   * @description: Only contains one element
   * @time: 2022-06-07 17:22:27
   * @params: [collection] 集合
   * @return: boolean 是否
   */
  public static <T> boolean hasUniqueObject(Collection<T> collection) {
    if (isEmpty(collection)) {
      return false;
    }
    boolean hasCandidate = false;
    Object candidate = null;
    for (Object elem : collection) {
      if (!hasCandidate) {
        hasCandidate = true;
        candidate = elem;
      } else if (candidate != elem) {
        return false;
      }
    }
    return true;
  }

  /**
   * @author: Ares
   * @description: 寻找通用元素类型
   * @description: Find common element type
   * @time: 2022-06-07 17:24:56
   * @params: [collection] 集合
   * @return: java.lang.Class<?> 类型
   */
  public static <T> Class<?> findCommonElementType(Collection<T> collection) {
    if (isEmpty(collection)) {
      return null;
    }
    Class<?> candidate = null;
    for (Object val : collection) {
      if (val != null) {
        if (candidate == null) {
          candidate = val.getClass();
        } else if (candidate != val.getClass()) {
          return null;
        }
      }
    }
    return candidate;
  }

  /**
   * @author: Ares
   * @description: 获取set第一个元素
   * @description: Get first element for set
   * @time: 2022-06-07 17:25:44
   * @params: [set]
   * @return: T 首个元素
   */
  public static <T> T firstElement(Set<T> set) {
    if (isEmpty(set)) {
      return null;
    }
    if (set instanceof SortedSet) {
      return ((SortedSet<T>) set).first();
    }

    Iterator<T> iterator = set.iterator();
    T first = null;
    if (iterator.hasNext()) {
      first = iterator.next();
    }
    return first;
  }

  /**
   * @author: Ares
   * @description: 获取列表第一个元素
   * @description: Get first element for list
   * @time: 2022-06-07 17:25:44
   * @params: [list] 列表
   * @return: T 首个元素
   */
  public static <T> T firstElement(List<T> list) {
    if (isEmpty(list)) {
      return null;
    }
    return list.get(0);
  }

  /**
   * @author: Ares
   * @description: 获取集合第一个元素
   * @description: Get first element for collection
   * @time: 2023-10-27 16:28:30
   * @params: [collection] 集合
   * @return: T 首个元素
   */
  public static <T> T firstElement(Collection<T> collection) {
    if (isEmpty(collection)) {
      return null;
    }
    if (collection instanceof Set) {
      return firstElement((Set<T>) collection);
    } else if (collection instanceof List) {
      return firstElement((List<T>) collection);
    } else {
      Iterator<T> iterator = collection.iterator();
      T first = null;
      if (iterator.hasNext()) {
        first = iterator.next();
      }
      return first;
    }
  }

  /**
   * @author: Ares
   * @description: 获取set最后一个元素
   * @description: Get last element for set
   * @time: 2022-06-07 17:25:44
   * @params: [set]
   * @return: T 最后一个元素
   */
  public static <T> T lastElement(Set<T> set) {
    if (isEmpty(set)) {
      return null;
    }
    if (set instanceof SortedSet) {
      return ((SortedSet<T>) set).last();
    }

    // Full iteration necessary...
    Iterator<T> it = set.iterator();
    T last = null;
    while (it.hasNext()) {
      last = it.next();
    }
    return last;
  }

  /**
   * @author: Ares
   * @description: 获取列表最后一个元素
   * @description: Get last element for list
   * @time: 2022-06-07 17:25:44
   * @params: [list] 列表
   * @return: T 最后一个元素
   */
  public static <T> T lastElement(List<T> list) {
    if (isEmpty(list)) {
      return null;
    }
    return list.get(list.size() - 1);
  }


  /**
   * @author: Ares
   * @description: 获取集合最后一个元素
   * @description: Get last element for collection
   * @time: 2023-10-27 16:28:30
   * @params: [collection] 集合
   * @return: T 最后一个元素
   */
  public static <T> T lastElement(Collection<T> collection) {
    if (isEmpty(collection)) {
      return null;
    }
    if (collection instanceof Set) {
      return lastElement((Set<T>) collection);
    } else if (collection instanceof List) {
      return lastElement((List<T>) collection);
    } else {
      Iterator<T> iterator = collection.iterator();
      T last = null;
      while (iterator.hasNext()) {
        last = iterator.next();
      }
      return last;
    }
  }

  /**
   * @author: Ares
   * @description: Enumeration to array
   * @time: 2022-06-07 17:27:26
   * @params: [enumeration, array]
   * @return: A[] 数组
   */
  public static <A, E extends A> A[] toArray(Enumeration<E> enumeration, A[] array) {
    ArrayList<A> elements = new ArrayList<>();
    while (enumeration.hasMoreElements()) {
      elements.add(enumeration.nextElement());
    }
    return elements.toArray(array);
  }

  /**
   * @author: Ares
   * @description: 数组转为不可重复集合
   * @description: Convert array to set
   * @time: 2022-06-08 11:02:52
   * @params: [elements] 元素数组
   * @return: java.util.Set<E>
   */
  @SafeVarargs
  public static <E> Set<E> asSet(E... elements) {
    if (null == elements) {
      throw new NullPointerException();
    }
    Set<E> set = newHashSet(elements.length);
    set.addAll(Arrays.asList(elements));
    return set;
  }

  /**
   * @author: Ares
   * @description: 数组转为有序的不可重复集合
   * @description: Convert array to linked set
   * @time: 2023-05-08 13:21:07
   * @params: [elements] 元素数组
   * @return: java.util.Set<E> out 出参
   */
  public static <E> LinkedHashSet<E> asLinkedHashSet(E... elements) {
    if (null == elements) {
      throw new NullPointerException();
    }
    return new LinkedHashSet<>(Arrays.asList(elements));
  }


  private static class EnumerationIterator<E> implements Iterator<E> {

    private final Enumeration<E> enumeration;

    public EnumerationIterator(Enumeration<E> enumeration) {
      this.enumeration = enumeration;
    }

    @Override
    public boolean hasNext() {
      return this.enumeration.hasMoreElements();
    }

    @Override
    public E next() {
      return this.enumeration.nextElement();
    }

    @Override
    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException("Not supported");
    }
  }

}
