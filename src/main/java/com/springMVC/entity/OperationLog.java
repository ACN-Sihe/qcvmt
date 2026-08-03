package com.springMVC.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@SequenceGenerator(name="operationLog",sequenceName="operatorlog_seq")
@Table(name = "T_OPERATION_LOG")
public class OperationLog {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE,generator="operationLog")
    @Column(name = "OPERLOGID" , length = 7)
    private int id;

    @Column(name = "USERID" , length = 7)
    private int userid;

    @Column(name = "USERNAME" , length = 20)
    private String username;

    @Column(name = "FUNCTION" , length = 50)
    private String function;

    @Column(name = "ACTIONTYPE" , length = 10)
    private String actionType;

    @Column(name = "VALUECHANGE" , length = 300)
    private String valuechange;

    @Column(name = "TIME")
    private Date time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getValuechange() {
        return valuechange;
    }

    public void setValuechange(String valuechange) {
        this.valuechange = valuechange;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
