package com.fintory.domain.alarm.service;

import com.fintory.domain.alarm.dto.AlarmStatusRequest;
import com.fintory.domain.alarm.dto.AlarmStatusResponse;
import com.fintory.domain.alarm.model.NotificationType;
import com.fintory.domain.child.model.Child;


public interface AlarmService {

     void saveToken(Child child, String token);

     void pushMessage(Long childId,NotificationType notificationType,
                            String title,
                            String body);

     void deleteToken(String token);

     void setStatus(Child child, AlarmStatusRequest request);

     AlarmStatusResponse getStatus(Child child);
}
