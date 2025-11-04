package com.riven.common.service;

import com.riven.common.dto.AddTaskDTO;
import com.riven.common.entity.SubscriptionInfo;
import com.riven.common.enums.CodeEnum;
import com.riven.common.enums.OperType;
import com.riven.common.enums.SubscriptionType;
import com.riven.common.mapper.SubscriptionInfoMapper;
import com.riven.common.utils.SpringUtil;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class NotifyService {

    @Autowired

    private SubscriptionInfoMapper subscriptionInfoMapper;

    @Autowired
    private GlobalTaskManagerService globalTaskManagerService;

    public void  addTask(AddTaskDTO add){
        add.validate(OperType.ADD);
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo();
        fillSubscriptionInfoWithAddDTO(add, subscriptionInfo);
        subscriptionInfo.setCreatedAt(new Date());
        int insert = subscriptionInfoMapper.insert(subscriptionInfo);
        if(SubscriptionType.EMAIL.getCode().equals(add.getSubscriptionType())){
            globalTaskManagerService.addTask(subscriptionInfo);
        }


    }



    public void  updateTask(AddTaskDTO update){
        update.validate(OperType.UPDATE);
        SubscriptionInfo subscriptionInfo = subscriptionInfoMapper.selectByPrimaryKey(update.getTaskId());


        if (subscriptionInfo == null){
            throw new RuntimeException("更新主键ID不存在,请先新增");
        }
        if ( !Objects.equals(update.getUserId(),subscriptionInfo.getUserId())){
            throw new RuntimeException("更新者用户信息不一致,无权限更新");
        }

        fillSubscriptionInfoWithAddDTO(update,subscriptionInfo);
        subscriptionInfoMapper.updateByPrimaryKeySelective(subscriptionInfo);

        if(SubscriptionType.EMAIL.getCode().equals(update.getSubscriptionType())){
            //之前是不发邮件,现在是发邮件
            if (SubscriptionType.DEFAULT.getCode().equals(subscriptionInfo.getSubscriptionType())){
                globalTaskManagerService.addTask(subscriptionInfo);
            }else if (SubscriptionType.EMAIL.getCode().equals(subscriptionInfo.getSubscriptionType())){
                //之前就是发邮件,现在还发  就先移除再添加
                globalTaskManagerService.cancelTask(subscriptionInfo);
                globalTaskManagerService.addTask(subscriptionInfo);

            }


        }
    }

    @Transactional
    public Integer  deleteTask(AddTaskDTO delete){
        delete.validate(OperType.DELETE);
        SubscriptionInfo subscriptionInfo = subscriptionInfoMapper.selectByPrimaryKey(delete.getTaskId());
        if (Objects.isNull(subscriptionInfo)){
            return 0;
        }
        if (!Objects.equals(subscriptionInfo.getUserId(),delete.getUserId()) && !(SpringUtil.getProfile().contains("test"))){
            throw new RuntimeException("用户id和任务id无法匹配");
        }
        // 检查订阅类型是否有
        if (CodeEnum.isValidCode(SubscriptionType.class, subscriptionInfo.getSubscriptionType())) {

            if (SubscriptionType.EMAIL.getCode().equals(subscriptionInfo.getSubscriptionType())){
               globalTaskManagerService.cancelTask(subscriptionInfo);
            }
        }

        return subscriptionInfoMapper.deleteByPrimaryKey(delete.getTaskId());


    }

    public List<AddTaskDTO> selectTask(AddTaskDTO select){
        select.validate(OperType.SELECT);
        List<SubscriptionInfo> subscriptionInfos = new ArrayList<>();
        if (Objects.nonNull(select.getTaskId())){
            subscriptionInfos = subscriptionInfoMapper.selectAllById(select.getTaskId());
        }else if (Objects.nonNull(select.getUserId())){
            subscriptionInfos = subscriptionInfoMapper.selectAllByUserId(select.getUserId());
        }
        List<AddTaskDTO> addTaskDTOS = new ArrayList<>();
        for (SubscriptionInfo find : subscriptionInfos) {
            AddTaskDTO addTaskDTO = AddTaskDTO.convert(find);
            addTaskDTOS.add(addTaskDTO);
        }

        return addTaskDTOS;
    }




    private void fillSubscriptionInfoWithAddDTO(AddTaskDTO add, SubscriptionInfo subscriptionInfo) {
        subscriptionInfo.setUserId(add.getUserId());
        subscriptionInfo.setSendType(add.getSendType());
        String cron = add.getCron();
        if (StringUtils.isEmpty(cron) && StringUtils.isNotEmpty(add.getSpecDateCron()) ){
              cron = AddTaskDTO.timeToCronExpression(add.getSpecDateCron());
        }
        subscriptionInfo.setCronExpression(cron);
        subscriptionInfo.setPersonalParam(add.getPersonalParam());
        subscriptionInfo.setSubscriptionType(add.getSubscriptionType());
        subscriptionInfo.setUpdatedAt(new Date());
        subscriptionInfo.setReferType(add.getReferType());
        subscriptionInfo.setClientId(SpringUtil.getProfile());
        subscriptionInfo.setCcEmail(add.getCcEmail());
        subscriptionInfo.setToEmail(add.getToEmail());
        subscriptionInfo.setFromEmail(add.getFromEmail());
    }
}