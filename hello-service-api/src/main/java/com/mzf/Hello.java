package com.mzf;


import lombok.*;

import java.io.Serializable;

/**
 * @author 好大雨
 * @create 2021/9/24 21:06
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello implements Serializable {
    private String message;
    private String description;

}
