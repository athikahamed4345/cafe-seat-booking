package com.restaurant.booking.dto;

public class BookingRequest {
    private Long tableId;
    private String bookingDate;
    private String bookingTime;
    private int partySize;
    private String specialOccasion;
    private String specialNotes;
    private double depositAmount;

    public Long getTableId() { return tableId; }
    public void setTableId(Long v) { this.tableId = v; }
    public String getBookingDate() { return bookingDate; }
    public void setBookingDate(String v) { this.bookingDate = v; }
    public String getBookingTime() { return bookingTime; }
    public void setBookingTime(String v) { this.bookingTime = v; }
    public int getPartySize() { return partySize; }
    public void setPartySize(int v) { this.partySize = v; }
    public String getSpecialOccasion() { return specialOccasion; }
    public void setSpecialOccasion(String v) { this.specialOccasion = v; }
    public String getSpecialNotes() { return specialNotes; }
    public void setSpecialNotes(String v) { this.specialNotes = v; }
    public double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(double v) { this.depositAmount = v; }
}
