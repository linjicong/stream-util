package com.linjicong.stream.util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author linjicong
 * @date 2022-04-13 9:13
 */
public class StreamUtil {
    /**
     * 简单分组
     *
     * @param list       集合
     * @param classifier 分组字段选择函数
     * @param <T>        输入类型
     * @param <K>        Key的类型
     * @return 最后返回Map<K, List < T>
     */
    public static <T, K> Map<K, List<T>> simpleGroupingBy(List<T> list, Function<? super T, ? extends K> classifier) {
        notEmptyCondition(list);
        return list.stream().collect(Collectors.groupingBy(classifier));
    }
    /**
     * 基本排序操作, 这里约定了排序字段的选择必须是实现了Comparable接口的.
     *
     * @param list      集合
     * @param desc      是否降序
     * @param keySelect 排序字段选择函数
     * @param <T>       输入类型
     */
    private static <T, U extends Comparable<U>> void sortList(List<T> list, boolean desc, Function<? super T, ? extends U> keySelect) {
        // 这里如果是降序的话, 就将List进行反转
        if (desc) {
            list.sort(Comparator.comparing(keySelect));
            Collections.reverse(list); //反转List
        } else {
            list.sort(Comparator.comparing(keySelect));
        }
    }
    /**
     * 简单分组 + 排序字段的选择
     *
     * @param list       集合
     * @param classifier 分组字段
     * @param keySelect  排序字段选择
     * @param desc       是否降序
     * @param <T>        输入类型
     * @param <K>        key的类型
     * @return 最后返回Map<K, List < T> List为排序之后的List
     */
    public static <T, K, C extends Comparable<C>> Map<K, List<T>> simpleGroupingBy(List<T> list, Function<? super T, ? extends K> classifier, boolean desc, Function<? super T, ? extends C> keySelect) {
        notEmptyCondition(list);
        sortList(list, desc, keySelect);
        return list.stream().collect(Collectors.groupingBy(classifier));
    }
    /**
     * 可选返回字段函数式分组
     *
     * @param list            列表
     * @param classifier      分组字段选择
     * @param mappingFunction 返回字段
     * @param <T>             输入类型
     * @param <K>             key类型
     * @param <U>             最后返回的元素类型
     * @return 最后返回Map<K, List < U>
     */
    public static <T, K, U> Map<K, List<U>> customizingFieldGroupingBy(List<T> list, Function<? super T, ? extends K> classifier, Function<? super T, ? extends U> mappingFunction) {
        notEmptyCondition(list);
        return list.stream()
                .collect(Collectors.groupingBy(classifier, Collectors.mapping(mappingFunction, Collectors.toList())));
    }
    /**
     * 可选返回字段函数式分组
     *
     * @param list            列表
     * @param classifier      分组字段选择
     * @param mappingFunction 返回字段
     * @param <T>             输入类型
     * @param <K>             key类型
     * @param <U>             最后返回的元素类型
     * @return 最后返回Map<K, List < U>
     */
    public static <T, K, U, C extends Comparable<C>> Map<K, List<U>> customizingFieldGroupingBy(List<T> list, Function<? super T, ? extends K> classifier, Function<? super T, ? extends U> mappingFunction, boolean desc, Function<? super T, ? extends C> keySelect) {
        notEmptyCondition(list);
        sortList(list, desc, keySelect);
        return list.stream()
                .collect(Collectors.groupingBy(classifier, Collectors.mapping(mappingFunction, Collectors.toList())));
    }

    public static void notEmptyCondition(List<?> list){
        if(list == null || list.size() == 0) throw new NullPointerException("list is null or size is 0");
    }
    public static void notEmptyCondition(Map<?,?> map){
        if(map == null || map.size() == 0) throw new NullPointerException("list is null or size is 0");
    }

    public static void main(String[] args) {
        ArrayList<TestUser> list = new ArrayList<TestUser>() {{
            add(new TestUser("用户1", 4L, 120.0, 11, 20L));
            add(new TestUser("用户2", 4L, 110.0, 12, 20L));
            add(new TestUser("用户3", 1L, 130.0, 13, 200L));
            add(new TestUser("用户4", 1L, 150.0, 14, 20L));
        }};
        //根据TestUser的DeptId进行分组, 这里返回的Key为DeptId, value为List<TestUser>
        Map<Long, List<TestUser>> result = StreamUtil.simpleGroupingBy(list, TestUser::getDeptId);
        System.err.println("result = " + result);

        //根据TestUser的DeptId进行分组, 并对List进行排序, 排序字段为TestUser的winningCount字段
        Map<Long, List<TestUser>> result2 = StreamUtil.simpleGroupingBy(list, TestUser::getDeptId, true, TestUser::getWinningCount);
        System.err.println("result2 = " + result2);

        //根据deptId字段进行分组, 选择score字段进行返回
        Map<Long, List<Double>> resultMap = StreamUtil.customizingFieldGroupingBy(list, TestUser::getDeptId, TestUser::getScore);
        System.err.println("resultMap = " + resultMap);

        //根据deptId字段进行分组, 选择score字段进行返回, 按照score降序进行排序
        Map<Long, List<Double>> resultMap2 = StreamUtil.customizingFieldGroupingBy(list, TestUser::getDeptId, TestUser::getScore, true, TestUser::getScore);
        System.err.println("resultMap2 = " + resultMap2);
    }
}
