package com.linjicong.stream.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author linjicong
 * {@code @date} 2022-04-13 9:13
 */
public class FunctionalUtil {


    /**
     * 根据单个字段进行排序, 排序对象可以是实现了Comparable接口的对象
     *
     * @param list      集合
     * @param keySelect 字段选择
     * @param desc      true: 降序, false: 升序
     * @param <T>       输入类型
     */
    public static <T, U extends Comparable<U>> void sort(List<T> list, boolean desc, Function<? super T, ? extends U> keySelect) {
        notEmptyCondition(list);
        sortList(list, desc, keySelect);
    }

    /**
     * 根据多个字段进行排序, 排序字段的选择必须为数字, 排序方式为, 字段1 + 字段2 + 字段... 总和进行排序
     *
     * @param list      list集合
     * @param desc      是否降序
     * @param keySelect 字段选择
     * @param <T>       输入类型
     */
    @SafeVarargs
    public static <T> void sort(List<T> list, boolean desc, Function<? super T, ? extends Number>... keySelect) {
        notEmptyCondition(list);
        sortList(list, desc, keySelect);
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
     * 精准排序, 限制数字类型, 支持多字段排序, 多字段排序为多个字段的总和后进行排序, 泛型约定了只能为数字.
     *
     * @param list      list集合
     * @param desc      是否降序
     * @param keySelect 排序字段选择, 不定长参数可以选择多个字段进行排序
     * @param <T>       输入类型
     */
    @SafeVarargs
    private static <T> void sortList(List<T> list, boolean desc, Function<? super T, ? extends Number>... keySelect) {
        list.sort(Comparator.comparingDouble(s -> {
            double sum = 0;
            for (Function<? super T, ? extends Number> function : keySelect) {
                sum += function.apply(s).doubleValue();
            }
            return desc ? -(sum) : sum;
        }));
    }

    /**
     * 从List中查找最大值
     *
     * @param list      集合
     * @param keySelect 字段选择
     * @param <T>       输入类型
     * @param <U>       比较字段类型
     * @return 结果
     */
    public static <T, U extends Comparable<U>> T findMaximum(List<T> list, Function<? super T, U> keySelect) {
        notEmptyCondition(list);
        return doFindMaxOrMin(list, keySelect, true);
    }

    /**
     * 从List中查找最小值
     *
     * @param list      集合
     * @param keySelect 字段选择
     * @param <T>       输入类型
     * @param <U>       比较字段类型
     * @return 结果
     */
    public static <T, U extends Comparable<U>> T findMinimum(List<T> list, Function<? super T, U> keySelect) {
        notEmptyCondition(list);
        return doFindMaxOrMin(list, keySelect, false);
    }

    /**
     * 根据字段查询集合最大值/最小值
     *
     * @param list      list集合
     * @param keySelect 字段选择
     * @param max       true: 最大值, false: 最小值
     * @param <T>       输入类型
     * @param <U>       比较类型
     * @return 结果
     */
    private static <T, U extends Comparable<U>> T doFindMaxOrMin(List<T> list, Function<? super T, U> keySelect, boolean max) {
        //函数返回的是Optional类, 如果没有的话就抛异常吧...e.e
        return max ? list.stream().max(Comparator.comparing(keySelect)).orElseThrow(() -> new RuntimeException("没有找到最大值")) : list.stream().min(Comparator.comparing(keySelect)).orElseThrow(() -> new RuntimeException("没有找到最小值"));
    }

    /**
     * 将List某个字段合并, 总数/平均值
     *
     * @param list             集合
     * @param keySelect        字段选择, 不定长参数, 可以选择多个字段进行合并
     * @param mergingOperation 合并操作, 总数 / 平均数
     * @return 结果
     */
    @SafeVarargs
    public static <T> Double mergingListResult(List<T> list, MergingOperation mergingOperation, Function<? super T, ? extends Number>... keySelect) {
        notEmptyCondition(list);
        return doMergingListResult(list, mergingOperation, keySelect);
    }

    /**
     * 线性表List, 根据字段计算总和/平均值
     *
     * @param list             list
     * @param keySelect        字段选择, 不定长参数, 可以选择多个字段进行合并
     * @param mergingOperation 合并操作, 总数 / 平均数
     * @return 结果
     */
    @SafeVarargs
    private static <T> Double doMergingListResult(List<T> list, MergingOperation mergingOperation, Function<? super T, ? extends Number>... keySelect) {
        //计算总数逻辑
        double sum = 0;
        for (Function<? super T, ? extends Number> keyFunction : keySelect) {
            sum += list.stream().mapToDouble(s -> keyFunction.apply(s).doubleValue()).sum();
        }
        switch (mergingOperation) {
            //如果是总数合并的话就直接返回
            case SUMMING:
                return sum;
            //要平均数的话就除以list.size()
            case AVERAGING:
                return sum / list.size();
            default:
                throw new RuntimeException("俺也不知道为啥会走到这个分支");
        }
    }

    /**
     * List统计数据, 包括最小值, 最大值, 平均值等等, 详情查看类{@link java.util.DoubleSummaryStatistics}
     *
     * @param list      集合
     * @param keySelect 统计字段
     * @param <T>       统计结果类型
     * @return 统计结果
     */
    public static <T> DoubleSummaryStatistics getDoubleSummaryStatistics(List<T> list, Function<? super T, ? extends Number> keySelect) {
        notEmptyCondition(list);
        return list.stream().collect(Collectors.summarizingDouble(s -> keySelect.apply(s).doubleValue()));
    }

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
        return list.stream().collect(Collectors.groupingBy(classifier, Collectors.mapping(mappingFunction, Collectors.toList())));
    }

    /**
     * 可选返回字段函数式分组 + 排序字段选择
     *
     * @param list            列表
     * @param classifier      分组字段选择
     * @param mappingFunction 返回字段
     * @param keySelect       排序字段选择
     * @param desc            是否降序
     * @param <T>             输入类型
     * @param <K>             key类型
     * @param <U>             最后返回的元素类型
     * @return 最后返回Map<K, List < U>
     */
    public static <T, K, U, C extends Comparable<C>> Map<K, List<U>> customizingFieldGroupingBy(List<T> list, Function<? super T, ? extends K> classifier, Function<? super T, ? extends U> mappingFunction, boolean desc, Function<? super T, ? extends C> keySelect) {
        notEmptyCondition(list);
        sortList(list, desc, keySelect);
        return list.stream().collect(Collectors.groupingBy(classifier, Collectors.mapping(mappingFunction, Collectors.toList())));
    }

    /**
     * 分组后合并操作
     *
     * @param list              集合List
     * @param classifier        分组字段
     * @param mergingClassifier 合并字段选择, 支持多字段选择, 限制只能是数字 Integer/Long/Double
     * @param <T>               输入类型
     * @param <K>               key类型
     * @return Map<Long, ? extends Number>
     */
    @SafeVarargs
    public static <T, K> Map<K, Double> mergingResultGroupingBy(List<T> list, Function<? super T, ? extends K> classifier, MergingOperation mergingOperation, Function<? super T, ? extends Number>... mergingClassifier) {
        notEmptyCondition(list);
        return mergingGroupingBy(list, classifier, mergingOperation, mergingClassifier);
    }

    /**
     * 合并操作, 分组后计算总值/平均值
     *
     * @param list              集合
     * @param classifier        分组字段选择
     * @param mergingClassifier 不定长参数, 选择多个合并字段.
     * @param mergingOperation  合并操作符
     * @param <T>               输入类型
     * @param <K>               key类型
     * @return 合并结果
     */
    @SafeVarargs
    private static <T, K> Map<K, Double> mergingGroupingBy(List<T> list, Function<? super T, ? extends K> classifier, MergingOperation mergingOperation, Function<? super T, ? extends Number>... mergingClassifier) {
        switch (mergingOperation) {
            //求总和
            case SUMMING:
                return list.stream().collect(Collectors.groupingBy(classifier, Collectors.summingDouble(s -> {
                    //这里的逻辑是这样的, 循环多个函数相加出总和进行合并
                    double sum = 0;
                    for (Function<? super T, ? extends Number> function : mergingClassifier) {
                        sum += function.apply(s).doubleValue();
                    }
                    return sum;
                })));
            //求平均值
            case AVERAGING:
                return list.stream().collect(Collectors.groupingBy(classifier, Collectors.averagingDouble(s -> {
                    //循环多个函数, 计算出总和后求平均值
                    double sum = 0;
                    for (Function<? super T, ? extends Number> function : mergingClassifier) {
                        sum += function.apply(s).doubleValue();
                    }
                    return sum;
                })));
            default:
                return null;
        }
    }

    /**
     * 分组后自定义结果集
     *
     * @param list             集合
     * @param classifier       分组字段
     * @param finisherFunction 整合函数, 传入一个函数函数的参数为Map<K, List<T>, 返回值为泛型R
     * @param <T>              集合内元素类型
     * @param <K>              Key类型
     * @param <R>              返回值
     * @return R
     * <p>
     * example:
     * int function(Map<K, List<T>);
     * customizingResultSetGroupingBy(list, Obj::getField, this::function);
     */
    public static <T, K, R> R customizingResultSetGroupingBy(List<T> list, Function<? super T, ? extends K> classifier, Function<Map<K, List<T>>, R> finisherFunction) {
        notEmptyCondition(list);
        return list.stream().collect(Collectors.collectingAndThen(Collectors.groupingBy(classifier), finisherFunction));
    }

    /**
     * 分组后统计分组后的数量
     *
     * @param list       list集合
     * @param classifier 分组字段
     * @param <T>        输入类型
     * @param <K>        Key类型
     * @return Map<K, Long> Long为每组的数量
     */
    public static <T, K> Map<K, Long> countListGroupingBy(List<T> list, Function<? super T, ? extends K> classifier) {
        notEmptyCondition(list);
        return list.stream().collect(Collectors.groupingBy(classifier, Collectors.counting()));
    }

    /**
     * 将List转换为Map, 如果有Hash冲突将会抛出异常
     *
     * @param list        list集合
     * @param keySelect   key选择
     * @param valueSelect value选择
     * @param <T>         输入类型
     * @param <K>         key类型
     * @param <V>         value类型
     * @return Map<K, V>
     */
    public static <T, K, V> Map<K, V> listToMap(List<T> list, Function<? super T, ? extends K> keySelect, Function<? super T, ? extends V> valueSelect) {
        notEmptyCondition(list);
        return list.stream().collect(Collectors.toMap(keySelect, valueSelect));
    }

    /**
     * list转其他集合
     *
     * @param list               list集合
     * @param collectionSupplier 集合实现类提供者
     * @param <T>                输入类型
     * @param <R>                返回类型
     * @return R
     */
    public static <T, R extends Collection<T>> R listToCollection(List<T> list, Supplier<R> collectionSupplier) {
        notEmptyCondition(list);
        return list.stream().collect(Collectors.toCollection(collectionSupplier));
    }

    /**
     * 对象数组转集合
     *
     * @param array              数组
     * @param collectionSupplier 集合实现类提供者
     * @param <T>                输入类型
     * @param <R>                返回类型
     * @return R
     */
    public static <T, R extends Collection<T>> R arrayToCollection(T[] array, Supplier<R> collectionSupplier) {
        Objects.requireNonNull(array);
        return Arrays.stream(array).collect(Collectors.toCollection(collectionSupplier));
    }

    public static <T> T[] collectionToArray(List<T> list,T[] a) {
        return list.toArray(a);
    }

    /**
     * List根据某个字段去重
     *
     * @param list      list集合
     * @param keySelect 去重字段选择
     * @param <T>       输入类型
     * @param <K>       去重字段类型
     * @return 结果
     */
    public static <T, K> List<T> distinctByField(List<T> list, Function<? super T, ? extends K> keySelect) {
        notEmptyCondition(list);
        /* 这里去重的逻辑大概是这样: 将List转换为Map, Key的话为需要去重的字段, value的话为T本身
        toMap函数如果出现Hash冲突没处理的话默认是会报错的, 这里第三个参数是mergeFunction, 就是用来
        处理Hash冲突的, 这里的处理方式是这样的: 如果出现了Hash冲突就将原本的保留, 冲突的丢弃, 最终将
        map的values放入一个新的List并返回.
        */
        return new ArrayList<>(list.stream().collect(Collectors.toMap(keySelect, (T o) -> o, (p, n) -> p)).values());
    }

    /**
     * 排序Map, 根据key进行排序
     *
     * @param map  map集合
     * @param desc 是否降序
     * @param <K>  key类型
     * @param <T>  value类型
     * @return 用LinkedHashMap保证顺序
     */
    public static <K extends Comparable<K>, T> Map<K, T> sortMapByKey(Map<K, T> map, boolean desc) {
        notEmptyCondition(map);
        return doSortMapByKey(map, desc);
    }

    /**
     * 排序Map, 根据key进行排序
     *
     * @param map  map集合
     * @param desc 是否降序
     * @param <K>  key类型
     * @param <T>  value类型
     * @return 用LinkedHashMap保证顺序
     */
    private static <K extends Comparable<K>, T> Map<K, T> doSortMapByKey(Map<K, T> map, boolean desc) {
        LinkedHashMap<K, T> linkedHashMap = new LinkedHashMap<>();
        if (desc) {
            map.entrySet().stream().sorted(Map.Entry.<K, T>comparingByKey().reversed()).forEachOrdered(e -> linkedHashMap.put(e.getKey(), e.getValue()));
        } else {
            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(e -> linkedHashMap.put(e.getKey(), e.getValue()));
        }
        return linkedHashMap;
    }

    /**
     * 排序Map根据值进行排序
     *
     * @param map       map集合
     * @param keySelect 值的选择函数
     * @param desc      是否降序
     * @param <K>       key类型
     * @param <T>       元素类型
     * @return 用LinkedHashMap保证顺序
     */
    public static <K, T, U extends Comparable<U>> Map<K, T> sortMapByValue(Map<K, T> map, boolean desc, Function<? super T, ? extends U> keySelect) {
        notEmptyCondition(map);
        return doSortMapByValue(map, keySelect, desc);
    }

    /**
     * 排序Map根据值进行排序
     *
     * @param map       map集合
     * @param keySelect 值的选择函数
     * @param desc      是否降序
     * @param <K>       key类型
     * @param <T>       元素类型
     * @return 用LinkedHashMap保证顺序
     */
    private static <K, T, U extends Comparable<U>> Map<K, T> doSortMapByValue(Map<K, T> map, Function<? super T, ? extends U> keySelect, boolean desc) {
        LinkedHashMap<K, T> linkedHashMap = new LinkedHashMap<>();
        if (desc) {
            map.entrySet().stream().sorted(Map.Entry.<K, T>comparingByValue(Comparator.comparing(keySelect)).reversed()).forEachOrdered(e -> linkedHashMap.put(e.getKey(), e.getValue()));
        } else {
            map.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparing(keySelect))).forEachOrdered(e -> linkedHashMap.put(e.getKey(), e.getValue()));
        }
        return linkedHashMap;
    }

    public static void notEmptyCondition(List<?> list) {
        if (list == null || list.size() == 0) throw new NullPointerException("list is null or size is 0");
    }

    public static void notEmptyCondition(Map<?, ?> map) {
        if (map == null || map.size() == 0) throw new NullPointerException("list is null or size is 0");
    }

    public static void main(String[] args) {
        ArrayList<TestUser> list = new ArrayList<TestUser>() {{
            add(new TestUser("用户1", 4L, 120.0, 11, 20L));
            add(new TestUser("用户2", 4L, 110.0, 12, 20L));
            add(new TestUser("用户3", 1L, 130.0, 13, 200L));
            add(new TestUser("用户4", 1L, 150.0, 14, 20L));
        }};
        // 根据winningCount降序进行排序
        FunctionalUtil.sort(list, true, TestUser::getWinningCount);

        System.out.println("winningCount降序排序: " + list);

        // 根据score和count降序进行排序
        FunctionalUtil.sort(list, true, TestUser::getScore, TestUser::getCount);
        System.out.println("score和Count降序排序: " + list);

        //根据TestUser的DeptId进行分组, 这里返回的Key为DeptId, value为List<TestUser>
        Map<Long, List<TestUser>> result = FunctionalUtil.simpleGroupingBy(list, TestUser::getDeptId);
        System.out.println("result = " + result);

        //根据TestUser的DeptId进行分组, 并对List进行排序, 排序字段为TestUser的winningCount字段
        Map<Long, List<TestUser>> result2 = FunctionalUtil.simpleGroupingBy(list, TestUser::getDeptId, true, TestUser::getWinningCount);
        System.out.println("result2 = " + result2);

        //根据deptId字段进行分组, 选择score字段进行返回
        Map<Long, List<Double>> resultMap = FunctionalUtil.customizingFieldGroupingBy(list, TestUser::getDeptId, TestUser::getScore);
        System.out.println("resultMap = " + resultMap);

        //根据deptId字段进行分组, 选择score字段进行返回, 按照score降序进行排序
        Map<Long, List<Double>> resultMap2 = FunctionalUtil.customizingFieldGroupingBy(list, TestUser::getDeptId, TestUser::getScore, true, TestUser::getScore);
        System.out.println("resultMap2 = " + resultMap2);

        // 根据deptId进行分组后, 将count 和 winningCount进行求和
        Map<Long, Double> resultMap3 = FunctionalUtil.mergingResultGroupingBy(list, TestUser::getDeptId, MergingOperation.SUMMING,
                //不定长参数, 因此可以传多个: Function<? super T, ? extends Number>... mergingClassifier
                //但是只能传数字, 其他类型的话会飘红, 比如TestUser::getUsername
                TestUser::getCount, TestUser::getWinningCount);
        System.out.println("resultMap = " + resultMap3);

        // 根据deptId进行分组后, 将count 和 winningCount进行求和后计算平均值
        Map<Long, Double> resultMap4 = FunctionalUtil.mergingResultGroupingBy(list, TestUser::getDeptId, MergingOperation.AVERAGING,
                //不定长参数, 因此可以传多个: Function<? super T, ? extends Number>... mergingClassifier
                //但是只能传数字, 其他类型的话会飘红, 比如TestUser::getUsername
                TestUser::getCount, TestUser::getWinningCount);
        System.out.println("resultMap2 = " + resultMap4);

        // 分组后的结果使用finisher方法处理, 返回的结果由finisher方法决定
        Double result5 = FunctionalUtil.customizingResultSetGroupingBy(list, TestUser::getDeptId, (Map<Long, List<TestUser>> map) -> 0.0);
        System.out.println(result5); //控制台打印: 0.0

        Map<Long, Long> result6 = FunctionalUtil.countListGroupingBy(list, TestUser::getDeptId);
        System.out.println("result = " + result6);

        // 查找Score最大的TestUser
        TestUser maximum = FunctionalUtil.findMaximum(list, TestUser::getScore);
        System.out.println("maximum = " + maximum);

        // 查找Score最小的TestUser
        TestUser minimum = FunctionalUtil.findMinimum(list, TestUser::getScore);
        System.out.println("minimum = " + minimum);

        Double mergingListResult = FunctionalUtil.mergingListResult(list, MergingOperation.SUMMING, TestUser::getScore);
        Double mergingListResult2 = FunctionalUtil.mergingListResult(list, MergingOperation.SUMMING, TestUser::getScore, TestUser::getWinningCount); //可选多个参数进行合并
        System.out.println("result = " + mergingListResult);
        System.out.println("result2 = " + mergingListResult2);

        DoubleSummaryStatistics getDoubleSummaryStatistics = FunctionalUtil.getDoubleSummaryStatistics(list, TestUser::getScore);
        System.out.println("result = " + getDoubleSummaryStatistics);

        Map<String, TestUser> listToMap = FunctionalUtil.listToMap(list, TestUser::getUsername, s -> s);
        System.out.println("result = " + listToMap);

        //将List转换为LinkedList
        LinkedList<TestUser> testUsers = FunctionalUtil.listToCollection(list, LinkedList::new);
        //将List转换为ArrayDeque
        ArrayDeque<TestUser> arrayDeque = FunctionalUtil.listToCollection(list, ArrayDeque::new);
        System.out.println("testUsers = " + testUsers);
        System.out.println("arrayDeque = " + arrayDeque);

        List<TestUser> distinctByField = FunctionalUtil.distinctByField(list, TestUser::getUsername);
        System.out.println("result = " + distinctByField);

        Map<Long, List<TestUser>> simpleGroupingBy = FunctionalUtil.simpleGroupingBy(list, TestUser::getCount);
        System.err.println("未排序之前result = " + simpleGroupingBy);

        result = FunctionalUtil.sortMapByKey(simpleGroupingBy, true);
        System.err.println("对key降序排序result = " + result);

        Map<Double, TestUser> listToMap2 = FunctionalUtil.listToMap(list, TestUser::getScore, s -> s);
        System.err.println("未排序之前result2 = " + listToMap2);

        listToMap2 = FunctionalUtil.sortMapByValue(listToMap2, true, TestUser::getScore);
        System.err.println("对Score降序排序之后result2 = " + listToMap2);

        TestUser[] array = FunctionalUtil.collectionToArray(list, new TestUser[list.size()]);
        System.out.println("array = " + Arrays.toString(array));

        List<TestUser> arrayToCollection = FunctionalUtil.arrayToCollection(array, ArrayList::new);
        System.out.println("arrayToCollection = " + arrayToCollection);
    }

}
