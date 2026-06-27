package com.group33.cp2.motorph.model;

// Interface for IT-specific operations: system access management.
public interface ITOperations {

    boolean manageSystemAccess(int userId, String accessLevel);
}
