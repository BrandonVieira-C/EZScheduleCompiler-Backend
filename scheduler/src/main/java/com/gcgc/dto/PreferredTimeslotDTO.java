package com.gcgc.dto;

import com.gcgc.entity.Employee;

public class PreferredTimeslotDTO {
    private Employee employee;
    private String timeslot;
    private Integer prefLevel;

    public Employee getEmployee() {
        return employee;
    }
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
    public String getTimeslot() {
        return timeslot;
    }
    public void setTimeslot(String timeslot) {
        this.timeslot = timeslot;
    }
    public Integer getPrefLevel() {
        return prefLevel;
    }
    public void setPrefLevel(Integer prefLevel) {
        this.prefLevel = prefLevel;
    }
    
}
