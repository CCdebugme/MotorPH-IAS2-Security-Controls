package com.group33.cp2.motorph.dao;

import com.group33.cp2.motorph.model.LeaveRequest;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// leaveRequests.csv. No other class opens this file directly.
public class LeaveRequestReader {

    private static final String FILE_PATH = "src/main/resources/data/LeaveRequests.csv";

    private static List<String[]> readAll() throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(FILE_PATH))) {
            String[] row;
            while ((row = reader.readNext()) != null) {
                rows.add(row);
            }
        } catch (CsvValidationException e) {
            throw new IOException("CSV parse error in LeaveRequests.csv: " + e.getMessage(), e);
        }
        return rows;
    }

    private static void writeAll(List<String[]> rows) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(FILE_PATH))) {
            writer.writeAll(rows);
        }
    }

    public static void addLeaveRequest(LeaveRequest leaveRequest) throws IOException {
        List<String[]> data = readAll();
        data.add(new String[]{
            leaveRequest.getLeaveID(),
            leaveRequest.getEmployeeID(),
            leaveRequest.getLeaveType(),
            leaveRequest.getDateRequest().toString(),
            leaveRequest.getStartDate().toString(),
            leaveRequest.getEndDate().toString(),
            leaveRequest.getReason(),
            leaveRequest.getStatus(),
            leaveRequest.getApprover() != null ? leaveRequest.getApprover() : "",
            leaveRequest.getDateResponded() != null ? leaveRequest.getDateResponded().toString() : "",
            leaveRequest.getRemark() != null ? leaveRequest.getRemark() : ""
        });
        writeAll(data);
    }

    public static LeaveRequest getLeaveById(String leaveID) throws IOException {
        List<String[]> dataList = readAll();
        for (int i = 1; i < dataList.size(); i++) {
            String[] data = dataList.get(i);
            if (data.length > 0 && data[0].trim().equals(leaveID.trim())) {
                return parseRow(data);
            }
        }
        return null;
    }

    public static void updateLeaveRequest(LeaveRequest updatedLeave) throws IOException {
        List<String[]> data = readAll();
        for (int i = 1; i < data.size(); i++) {
            if (data.get(i).length > 0 && data.get(i)[0].trim().equals(updatedLeave.getLeaveID().trim())) {
                data.set(i, new String[]{
                    updatedLeave.getLeaveID(),
                    updatedLeave.getEmployeeID(),
                    updatedLeave.getLeaveType(),
                    updatedLeave.getDateRequest().toString(),
                    updatedLeave.getStartDate().toString(),
                    updatedLeave.getEndDate().toString(),
                    updatedLeave.getReason(),
                    updatedLeave.getStatus(),
                    updatedLeave.getApprover() != null ? updatedLeave.getApprover() : "",
                    updatedLeave.getDateResponded() != null ? updatedLeave.getDateResponded().toString() : "",
                    updatedLeave.getRemark() != null ? updatedLeave.getRemark() : ""
                });
                break;
            }
        }
        writeAll(data);
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        List<LeaveRequest> leaveRequests = new ArrayList<>();
        try {
            List<String[]> dataList = readAll();
            for (int i = 1; i < dataList.size(); i++) {
                String[] data = dataList.get(i);
                if (data.length >= 8) {
                    leaveRequests.add(parseRow(data));
                } else {
                    System.err.println("LeaveRequestReader: skipping invalid row (too few columns): "
                            + String.join(",", data));
                }
            }
        } catch (IOException e) {
            System.err.println("LeaveRequestReader.getAllLeaveRequests: " + e.getMessage());
        }
        return leaveRequests;
    }

    private static LeaveRequest parseRow(String[] data) {
        return new LeaveRequest(
            data[0].trim(),
            data[1].trim(),
            data[2].trim(),
            LocalDate.parse(data[3].trim()),
            LocalDate.parse(data[4].trim()),
            LocalDate.parse(data[5].trim()),
            data[6].trim(),
            data[7].trim(),
            data.length > 8 && !data[8].isBlank() ? data[8].trim() : "",
            data.length > 9 && !data[9].isBlank() ? LocalDate.parse(data[9].trim()) : null,
            data.length > 10 ? data[10].trim() : ""
        );
    }
}
