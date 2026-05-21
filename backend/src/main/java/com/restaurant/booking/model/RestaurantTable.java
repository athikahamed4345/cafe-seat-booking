package com.restaurant.booking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {
    public enum TableType { STANDARD, GROUP, PRIVATE, MERGED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String tableNumber;
    private int minCapacity;
    private int maxCapacity;
    private boolean isAvailable = true;
    private boolean isAccessible = false;
    @Enumerated(EnumType.STRING)
    private TableType type = TableType.STANDARD;
    private String specialFeatures;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String v) { this.tableNumber = v; }
    public int getMinCapacity() { return minCapacity; }
    public void setMinCapacity(int v) { this.minCapacity = v; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int v) { this.maxCapacity = v; }
    public boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(boolean v) { this.isAvailable = v; }
    public boolean getIsAccessible() { return isAccessible; }
    public void setIsAccessible(boolean v) { this.isAccessible = v; }
    public TableType getType() { return type; }
    public void setType(TableType v) { this.type = v; }
    public String getSpecialFeatures() { return specialFeatures; }
    public void setSpecialFeatures(String v) { this.specialFeatures = v; }
    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }
}
