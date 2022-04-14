package com.linjicong.stream.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author linjicong
 * @date 2022-04-13 9:26
 */
@Data
@AllArgsConstructor
public class TestUser {
    private String username;
    private Long deptId;
    private Double score;
    private Integer winningCount;
    private Long count;
}
