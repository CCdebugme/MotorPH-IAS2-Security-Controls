package com.group33.cp2.motorph.model;

// Callback interface used by Admin.manageUsers() to delegate UI frame creation
// back to the presentation layer, keeping the domain model free of Swing imports.
public interface UserManagementCallback {

    // Called when the "create" action is requested; implementor opens the new-employee form.
    void onCreateUser();

    // Called when the "update" action is requested; implementor opens the update form.
    void onUpdateUser(String employeeId);
}
