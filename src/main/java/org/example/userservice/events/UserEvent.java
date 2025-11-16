package org.example.userservice.events;

import java.util.Objects;

public class UserEvent {
    private String email;
    private OperationType operation;

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEvent that = (UserEvent) o;
        return Objects.equals(email, that.email)
                && Objects.equals(operation, that.operation);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(email);
        result = 31 * result + Objects.hashCode(operation);
        return result;
    }
}
