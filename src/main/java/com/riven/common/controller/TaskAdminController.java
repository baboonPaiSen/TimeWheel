package com.riven.common.controller;

import com.riven.common.config.ScheduleConfig;
import com.riven.common.dto.AddTaskDTO;
import com.riven.common.enums.CodeEnum;
import com.riven.common.enums.ReferType;
import com.riven.common.service.NotifyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/task")
public class TaskAdminController {


    private static final Logger log = LoggerFactory.getLogger(TaskAdminController.class);
    @Autowired
    private NotifyService notifyService;


    @Autowired
    private ScheduleConfig scheduleConfig;



    @PostMapping("/add")
    public Result<String> addTask(@RequestBody AddTaskDTO addTaskDTO){
        notifyService.addTask(addTaskDTO);
        return Result.ofSuccess(addTaskDTO.getTraceId());
    }


    @PostMapping("/select")
    public Result<List<AddTaskDTO>> select(@RequestBody AddTaskDTO addTaskDTO){
        String referType = addTaskDTO.getReferType();
        if (referType == null || referType.isEmpty() || !CodeEnum.isValidCode(ReferType.class,referType)) {
            log.warn("业务方简称不能为空");
        }
        List<AddTaskDTO> addTaskDTOS = notifyService.selectTask(addTaskDTO);
        return Result.ofSuccess(addTaskDTOS);
    }


    @PostMapping("/update")
    public Result<String> update(@RequestBody AddTaskDTO addTaskDTO){

        notifyService.updateTask(addTaskDTO);
        return Result.ofSuccess(addTaskDTO.getTraceId());
    }


    @PostMapping("/delete")
    public Result<String> delete(@RequestBody AddTaskDTO addTaskDTO)
    {
        Integer integer = notifyService.deleteTask(addTaskDTO);
        return Result.ofSuccess(addTaskDTO.getTraceId());
    }


}
