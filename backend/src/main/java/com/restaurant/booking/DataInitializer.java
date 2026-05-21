package com.restaurant.booking;

import com.restaurant.booking.model.*;
import com.restaurant.booking.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepo;
    private final ZoneRepository zoneRepo;
    private final TableRepository tableRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepo, ZoneRepository zoneRepo,
                           TableRepository tableRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.zoneRepo = zoneRepo;
        this.tableRepo = tableRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepo.findByEmail("admin@cafebook.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@cafebook.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(User.Role.ADMIN);
            admin.setEmailVerified(true);
            userRepo.save(admin);
        }

        if (zoneRepo.count() == 0) {
            Zone[] zones = new Zone[6];
            String[][] zd = {
                {"Window Side",   "Scenic views with natural light"},
                {"Cosy Corner",   "Quiet, intimate setting"},
                {"Outdoor Patio", "Fresh air open seating"},
                {"Private Nook",  "Semi-private booth seating"},
                {"Main Floor",    "Central, lively atmosphere"},
                {"Lounge",        "Relaxed low-seating area"}
            };
            for (int i = 0; i < zd.length; i++) {
                Zone z = new Zone();
                z.setName(zd[i][0]);
                z.setDescription(zd[i][1]);
                zones[i] = zoneRepo.save(z);
            }

            Object[][] td = {
                {"W1", 2, 4, "STANDARD", zones[0], false},
                {"W2", 2, 4, "STANDARD", zones[0], false},
                {"C1", 1, 2, "PRIVATE",  zones[1], false},
                {"C2", 2, 4, "STANDARD", zones[1], false},
                {"P1", 2, 6, "GROUP",    zones[2], true},
                {"P2", 4, 8, "GROUP",    zones[2], true},
                {"N1", 2, 4, "PRIVATE",  zones[3], false},
                {"N2", 2, 4, "PRIVATE",  zones[3], false},
                {"M1", 4, 8, "GROUP",    zones[4], true},
                {"M2", 2, 4, "STANDARD", zones[4], false},
                {"L1", 2, 4, "STANDARD", zones[5], false},
                {"L2", 1, 3, "STANDARD", zones[5], false}
            };
            for (Object[] t : td) {
                RestaurantTable table = new RestaurantTable();
                table.setTableNumber((String) t[0]);
                table.setMinCapacity((int) t[1]);
                table.setMaxCapacity((int) t[2]);
                table.setType(RestaurantTable.TableType.valueOf((String) t[3]));
                table.setZone((Zone) t[4]);
                table.setIsAccessible((boolean) t[5]);
                table.setIsAvailable(true);
                tableRepo.save(table);
            }
        }
    }
}
