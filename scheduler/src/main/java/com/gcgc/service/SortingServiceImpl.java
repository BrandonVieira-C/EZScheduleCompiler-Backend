package com.gcgc.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import javax.persistence.Entity;

import com.gcgc.dto.EmployeeDTO;
import com.gcgc.dto.PreferredShiftsDTO;
import com.gcgc.dto.PreferredTimeslotDTO;
import com.gcgc.dto.ShiftDTO;
import com.gcgc.entity.Employee;
import com.gcgc.entity.PreferredShifts;
import com.gcgc.entity.PreferredTimeslot;
import com.gcgc.entity.Shift;
import com.gcgc.exception.SchedulerException;
import com.gcgc.repository.EmployeeRepository;
import com.gcgc.repository.PreferredShiftsRepository;
import com.gcgc.repository.PreferredTimeslotRepository;
import com.gcgc.repository.ShiftRepository;

import org.springframework.beans.factory.annotation.Autowired;

public class SortingServiceImpl {

    @Autowired
    EmployeeRepository employeeRepo;

    @Autowired
    ShiftRepository shiftRepo;

    @Autowired
    PreferredShiftsRepository preferredShiftRepo;

    @Autowired
    PreferredTimeslotRepository preferredTimeslotRepo;

    @Autowired
    ShiftService shiftService;

    public String schedulePartTimeEmployees() throws SchedulerException {

        // get all the employees

        List<Employee> employeeEntityList = (List<Employee>) employeeRepo.getEmployeesInOrderOfSeniority();
        if (employeeEntityList.isEmpty()) {
            throw new SchedulerException("Did not retrieve any employees");
        }

        // convert the list of entities into queue of dtos

        List<EmployeeDTO> employeeDTOList = new LinkedList<>();
        Queue<PreferredShiftsDTO> queuePreferredShiftsDTOList = new LinkedList<>();
        Queue<PreferredTimeslotDTO> queuePreferredTimeslotDTOList = new LinkedList<>();
        Queue<EmployeeDTO> queueOfEmployeeDTO = new LinkedList<>();
        for (Employee employeeEntity : employeeEntityList) {
            EmployeeDTO employeeDTO = new EmployeeDTO();
            employeeDTO.setEmpNo(employeeEntity.getEmpNo());
            employeeDTO.setFirstName(employeeEntity.getFirstName());
            employeeDTO.setFullTime(employeeEntity.getFullTime());
            employeeDTO.setHireDate(employeeEntity.getHireDate());
            employeeDTO.setLastName(employeeEntity.getLastName());
            queueOfEmployeeDTO.add(employeeDTO);

            //convert this entitys list preferred shifts into queue of dto
            queuePreferredShiftsDTOList = getEmployeePreferredShiftsAsQueue(employeeDTO.getEmpNo());
        
            // convert this entitys list of timeslots into queue of dto
            List<PreferredTimeslot> preferredTimeSlotEntityList = preferredTimeslotRepo.getEmployeesPreferredTimeslotsInOrder(employeeEntity.getEmpNo());
            for (PreferredTimeslot preferredTimeslotEntity : preferredTimeSlotEntityList) {
                PreferredTimeslotDTO preferredTimeslotDTO = new PreferredTimeslotDTO();
                preferredTimeslotDTO.setTimeslot(preferredTimeslotEntity.getTimeslot());
                preferredTimeslotDTO.setPrefLevel(preferredTimeslotEntity.getPrefLevel());
                queuePreferredTimeslotDTOList.add(preferredTimeslotDTO);
            }
            employeeDTOList.add(employeeDTO);
        }

        // get the top most senior employee

        EmployeeDTO currentEmployee = new EmployeeDTO();

        // get this employees most preferredShift
        PreferredShiftsDTO nextPreferredShift = new PreferredShiftsDTO();

        // now we have the most senior employee and their most preferred shift
        // we need to iterate through the available shifts

        List<ShiftDTO> shiftDTOList = shiftService.getAllShifts();

        while (true) {
            if (shiftDTOList.isEmpty()) {
                break;
            }
            currentEmployee = queueOfEmployeeDTO.remove();
            if (currentEmployee.getShiftsGiven() == 4) {
                continue;
            } else {
                Integer empNo = currentEmployee.getEmpNo();
                queuePreferredShiftsDTOList = getEmployeePreferredShiftsAsQueue(empNo);
                nextPreferredShift = queuePreferredShiftsDTOList.remove(); 
            }

            // *** Getting confused on why I need the while loop. Maybe it can just be a if
            // i have a shift locked, if i can assign the employee to the shift
            // we need to keep the employee and move to the next shift
            
            for (ShiftDTO shiftDTO : shiftDTOList) {
                if (shiftDTO.getCurrStaffed() == shiftDTO.getStaffNeeded()) {
                    shiftDTOList.remove(shiftDTO);
                    continue;
                }
                int i = 1;
                HashMap<Integer, Integer> mapOfEmployeeNumbers = new HashMap<>();
                for (EmployeeDTO employeeDTO : shiftDTO.getEmployeeList()) {
                    
                    mapOfEmployeeNumbers.put(i, employeeDTO.getEmpNo());
                    i++;
                }
               
                if (shiftDTO.getDay() == nextPreferredShift.getDay() 
                && shiftDTO.getTimeslot() == nextPreferredShift.getTimeslot()  
                && currentEmployee.getShiftsGiven() <= 4
                && !mapOfEmployeeNumbers.containsValue(currentEmployee.getEmpNo())) {
                    shiftDTO.addEmployee(currentEmployee);
                    shiftDTO.setCurrStaffed(shiftDTO.getCurrStaffed() + 1);
                    currentEmployee.setShiftsGiven(currentEmployee.getShiftsGiven() + 1);
                    if (queuePreferredShiftsDTOList.isEmpty()) {
                        // assign shifts according to timeslot preferences
                    } 
                    else {
                        nextPreferredShift = queuePreferredShiftsDTOList.remove();

                    }
                }
            } 
        }
    }

    private Queue<PreferredShiftsDTO> getEmployeePreferredShiftsAsQueue(Integer empNo) {
        Queue<PreferredShiftsDTO> queuePreferredShiftsDTOList = new LinkedList<>();

        List<PreferredShifts> preferredShiftsEntityList = preferredShiftRepo.getEmployeesPreferredShiftsInOrder(empNo); 
        for (PreferredShifts preferredShiftEntity : preferredShiftsEntityList) {
            PreferredShiftsDTO preferredShiftDTO = new PreferredShiftsDTO();
            preferredShiftDTO.setDay(preferredShiftEntity.getDay());
            preferredShiftDTO.setTimeslot(preferredShiftEntity.getTimeslot());
            preferredShiftDTO.setPrefLevel(preferredShiftEntity.getPrefLevel());
            queuePreferredShiftsDTOList.add(preferredShiftDTO);
        }
        return queuePreferredShiftsDTOList;
    }
}









        

        



       
}
