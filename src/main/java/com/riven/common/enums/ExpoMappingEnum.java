package com.riven.common.enums;


import com.riven.common.dto.AddTaskDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Getter
public enum  ExpoMappingEnum {

    PERSONAL("personal",1001,10010, Arrays.asList("personal@example.com")),
    INDIVIDUAL("individual",1002,10020, Arrays.asList("individual@example.com")),
    PRIVATE("private",1003,10030, Arrays.asList("private@example.com")),
    ;

    private String referType;

    private Integer  appId;

    private Integer cmdId;

    private List<String> notifyList;




    public static List<String> getNotifyListByPullParam(Object pullParam){

        if (pullParam instanceof AddTaskDTO){

            AddTaskDTO  addTaskDTO  = (AddTaskDTO)pullParam;
            String referType = addTaskDTO.getReferType();
            ExpoMappingEnum[] values = ExpoMappingEnum.values();
            for (ExpoMappingEnum value : values) {
                if (value.referType.equals(referType)){
                   return value.notifyList;
                }
            }
        }


        throw new RuntimeException("未能找到匹配的expo接口,pullParam格式异常");
    }

}