package com.sandbox.services.db.mysql.enumeration;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Getter
public enum DataSourceEnum {
    /**
     *
     */
    DS0("ds0", "数据源0"),

    DS1("ds1", "数据源1"),

    DS0SLAVE0("ds0slave0", "数据源0的备用0"),

    DS1SLAVE0("ds1slave0", "数据源1的备用0");

    private String value;
    private String description;

    DataSourceEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static String getDescriptionByValue(String value) {
        return Arrays.stream(values()).filter(x -> x.getValue().equals(value)).findFirst()
                .map(DataSourceEnum::getDescription).orElseThrow(() -> new NoSuchElementException("没有找到对应的枚举！"));
    }

    public static List<DataSourceEnum> getList() {
        return Arrays.stream(DataSourceEnum.values()).collect(Collectors.toList());
    }
}
