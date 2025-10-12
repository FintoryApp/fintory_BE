package com.fintory.domain.attendence.dto;

public record CheckInResponse(
        boolean isCheckedIn,
        int continuousDays
) {
    public static CheckInResponse from(boolean isCheckedIn,  int continuousDays) {
        return new CheckInResponse(
                isCheckedIn,
                continuousDays
        );
    }
}
