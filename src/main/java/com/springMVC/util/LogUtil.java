package com.springMVC.util;

import com.springMVC.entity.OperationLog;
import com.springMVC.entity.User;

import java.util.Date;

public class LogUtil {

    public enum Function {
        USER_MANAGER("User Manager"),
        COLOR_CONFIGURATION("Color Configuration"),
        VESSEL_BAY_CONFIGURATION("Vessel Bay Configuration"),
        VESSEL_MANAGER("Vessel Manager"),
        VESSEL_REFUEL_CONFIGURATION("Vessel Refuel Configuration"),
        VESSEL_REFUEL_BAY_ROW_CONFIGURATION("Vessel Refuel Bay Row Configuration");

        private final String function;

        public String getFunction() {
            return function;
        }

        Function(String function) {
            this.function = function;
        }
    }

    public enum ActionType {
        SAVE("Save"),
        UPDATE("Update"),
        DELETE("Delete");

        private final String actionType;

        public String getActionType() {
            return actionType;
        }

        ActionType(String actionType) {
            this.actionType = actionType;
        }
    }

    public static OperationLog buildOperationLog(User user, Function function, ActionType actionType, String oldValue, String newValue) {
        OperationLog log = new OperationLog();
        log.setUserid(user.getId());
        log.setUsername(user.getUsername());
        log.setFunction(function.getFunction());
        log.setActionType(actionType.getActionType());
        String valueChange = (oldValue != null ? oldValue : "null") + "->" + (newValue != null ? newValue : "null");
        log.setValuechange(valueChange);
        log.setTime(new Date());
        return log;
    }
}
